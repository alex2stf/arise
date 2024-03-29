package com.arise.cargo.management;

import com.arise.canter.Command;
import com.arise.canter.CommandRegistry;
import com.arise.core.exceptions.LogicalException;
import com.arise.core.models.Archiver;
import com.arise.core.models.Handler;
import com.arise.core.models.Tuple2;
import com.arise.core.serializers.parser.Groot;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.Mole;
import com.arise.core.tools.SYSUtils;
import com.arise.core.tools.StreamUtil;
import com.arise.weland.impl.OSProxies;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.arise.core.AppSettings.Keys.DEPENDENCY_FORCED_PROFILES;
import static com.arise.core.AppSettings.getProperty;
import static com.arise.core.tools.CollectionUtil.isEmpty;
import static com.arise.core.tools.CollectionUtil.safeGetItem;
import static com.arise.core.tools.SYSUtils.*;
import static com.arise.core.tools.StringUtil.hasText;
import static com.arise.core.tools.StringUtil.join;
import static com.arise.core.tools.Util.close;
import static java.lang.String.valueOf;

public class DependencyManager {

    private static final Map<String, Dependency> dependencyMap = new HashMap<>();
    private static final Mole log = Mole.getInstance(DependencyManager.class);

    public static final Command<String> DOWNLOAD_COMMAND = new Command<String>("download") {
        @Override
        public String execute(List<String> args) {
            String urlStr = args.get(0);
            File loc = Locations.forName(safeGetItem(args, 1, "downloads"));
            String name = safeGetItem(args, 2, UUID.randomUUID().toString()).trim();

            try {
                URL url = new URL(urlStr);
                return download(url, loc, name, null, null).getAbsolutePath();
            } catch (Exception e) {
                dispatchError(e);
            }
            return null;
        }
    };




    static {
        CommandRegistry.getInstance().addCommand(DOWNLOAD_COMMAND);
        CommandRegistry.getInstance().addCommand(new Command<String>("file-exist") {
            @Override
            public String execute(List<String> x) {
                String p = x.get(0);
                if (!hasText(p)){
                    log.warn("$file-exist() called with empty param");
                }
                File f = new File(p);
                log.info("$file-exist(" + f.getAbsolutePath() + ")");
                if (f.exists() && f.isFile()){
                    return f.getAbsolutePath();
                }
                return null;
            }
        });

        CommandRegistry.getInstance().addCommand(new Command<String>("maven-repo") {
            @Override
            public String execute(List<String> args) {
                File repo = new File(FileUtil.getUserDirectory(".m2"), "repository");
                return new File(repo, args.get(0)).getAbsolutePath();
            }
        });



        CommandRegistry.getInstance().addCommand(new Command<String>("unzip") {
            @Override
            public String execute(List<String> x) {
                File s = new File(x.get(0));
                File out = Locations.forName(x.get(1));
                log.info("$unzip " + s.getAbsolutePath() + " to " + out.getAbsolutePath());
                Archiver.ZIP.extract(s, out);
                return out.getAbsolutePath();
            }
        });

        CommandRegistry.getInstance().addCommand(new Command<String>("sub-location") {
            @Override
            public String execute(List<String> a) {
                File p = Locations.forName(a.get(0));
                File f = new File(p, a.get(1));
                String m = safeGetItem(a, 2, "xxx");
                log.info("$sub-location(" + f.getAbsolutePath() + ") mode '" + m + "'");
                if ("wd".equalsIgnoreCase(m) || "rwd".equalsIgnoreCase(m)) {
                    if (!f.exists()) {
                        f.mkdirs();
                    }
                }
                if (f.exists()){
                    return f.getAbsolutePath();
                }
                return null;
            }
        });


        CommandRegistry.getInstance().addCommand(new Command<Process>("shell-exec") {
            @Override
            public Process execute(List<String> args) {
                String wdir = args.get(0);
                String path = args.get(1);
                String pa[] = new String[args.size() - 2];
                for (int i = 2; i < args.size(); i++){
                    pa[i - 2] = args.get(i);
                }
                ProcessBuilder pb = new ProcessBuilder(pa);
                if (!wdir.equals("null")){
                    pb.directory(new File(wdir));
                }
                if (!path.equalsIgnoreCase("null")){
                    pb.environment().put("Path", path);
                }
                try {
                    Process proc = pb.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        });

    }

    private DependencyManager(){

    }


    public static void withBinary(String n, final Handler<File> h) {
        withStrictDependency(n, new Handler<Object>() {
            @Override
            public void handle(Object o) {
                File p = new File(valueOf(o));
                if (p.exists()){
                    h.handle(p);
                }
            }
        }, "binary");
    }

    public static void withSdkTool(String n, final String t, final Handler<File> h) {
        withDependency(n, new Handler<Map<String, Object>>() {
            @Override
            public void handle(Map<String, Object> res) {
                if (res.containsKey(t)){
                    File f = new File(res.get(t) + "");
                    h.handle(f);
                }
            }
        });
    }

    public static void withJar(final String n, final Handler<URLClassLoader> handler){
        withStrictDependency(n, new Handler<Object>() {
            @Override
            public void handle(Object o) {
                String path = valueOf(o);
                File jar = new File(path);
                try {
                    URLClassLoader classLoader  = new URLClassLoader(
                            new URL[] {jar.toURI().toURL()},
                            DependencyManager.class.getClassLoader()
                    );
                    handler.handle(classLoader);
                } catch (MalformedURLException e) {
                    throw new LogicalException("jar dependency " + n + " path malformed", e);
                }
            }
        }, "jar");
    }

    public static void withDependency(String n, Handler<Map<String, Object>> h){
        Map<String, Object> res = solve(n);
        if (res == null){
            throw new LogicalException("Unable to solve dependency " + n);
        }
        h.handle(res);
    }

    public static void withStrictDependency(final String n, final Handler<Object> h, final String t){
        withDependency(n, new Handler<Map<String, Object>>() {
            @Override
            public void handle(Map<String, Object> m) {
                String k = t + "-location";
                if (m.containsKey(k)){
                    h.handle(m.get(k));
                } else {
                    throw new LogicalException("Dependency " + n + " has no strict type " + t + ", param [" + t + "-location] required");
                }
            }
        });
    }



    public static void importDependencyRules(String in) {
        InputStream inps = FileUtil.findStream(in);
        if(inps == null){
            log.w("Could not find stream " + in);
        }
        String x = StreamUtil.toString(inps);
        Map<Object, Object> data = (Map) Groot.decodeBytes(x);

        for (Map.Entry<Object, Object> e: data.entrySet()){
            String n = (String) e.getKey();
            Map args = (Map) e.getValue();
            log.i("*** " + n + " ***");
            Dependency dependency = new Dependency();
            dependency.setName(n);
            Dependency.decorate(dependency, args);
            dependencyMap.put(n, dependency);
        }
    }


    public static HttpURLConnection getConnection(URL url, String pH, String pP) throws IOException {
        Proxy proxy = null;
        String apH = hasText(pH) ? pH : System.getProperty("proxy.host");
        String apP = hasText(pP) ? pP : System.getProperty("proxy.port");

        if (hasText(apH)){
            Proxy.Type proxyType = Proxy.Type.HTTP;

            Integer port = 8080;
            if (hasText(apP)){
                try {
                    port = Integer.valueOf(apP);
                }catch (Exception e){
                    log.error("Failed to parse port number " + apP + " using defaul 8080");
                    port = 8080;
                }
            }

            InetSocketAddress inetSocketAddress = new InetSocketAddress(System.getProperty("proxy.host"), port);
            proxy  = new Proxy(proxyType, inetSocketAddress);
        }

        if (proxy != null){
            return  (HttpURLConnection) url.openConnection(proxy);
        } else {
            return  (HttpURLConnection) url.openConnection();
        }
    }

    public static File download(URL url,
                                File outDir,
                                String name,
                                String proxyHost,
                                String proxyPort) throws IOException {


        File out = new File(outDir, name);
        if (out.exists()){
            return out;
        }
        HttpURLConnection connection = getConnection(url, proxyHost, proxyPort);

        connection.setRequestMethod("GET");

        connection.setConnectTimeout(60 * 1000);
        connection.setReadTimeout(60 * 1000);
        int contentLength = connection.getContentLength();
        int responseCode = connection.getResponseCode();
        if (responseCode != 200){
            throw new RuntimeException("wget " + responseCode + " " + url + " in " + out.getAbsolutePath());
        }

        RandomAccessFile file = new RandomAccessFile(out.getAbsolutePath(), "rw");
        InputStream stream = connection.getInputStream();


        int size = contentLength; // size of download in bytes
        if (size < 1){
            size = Integer.MAX_VALUE;
        }
        System.out.print("wget " + url + " ~" + size );
        int downloaded = 0; // number of bytes downloaded
        int MAX_BUFFER_SIZE = 1024;
        int dotCount = 0;
        boolean nl = false;
        int lX = 0;
        while (true) {
    /* Size buffer according to how much of the
       file is left to download. */
            byte buffer[];
            int bufferSize;
            if (size - downloaded > MAX_BUFFER_SIZE) {
                bufferSize = MAX_BUFFER_SIZE;
            } else {
                bufferSize = size - downloaded;
            }




            buffer = new byte[bufferSize];
            // Read from server into buffer.
            int read = stream.read(buffer);
            if (read == -1)
                break;

            // Write buffer to file.
            file.write(buffer, 0, read);
            downloaded += read;




            int x = (100 / (size  / downloaded)) - 9;
            dotCount++;
            if (dotCount % 100 == 0){
                if (!nl){
//                    System.out.print("\n");
                    nl = true;
                }
                System.out.print(".");
                if (dotCount % 10000 == 0) {


                    System.out.print( (lX == x ? lX + 1 : x) + "%\n");
                    dotCount = 0;
                    lX = x;
                }
            }
        }
        System.out.print(" OK "+ out.getAbsolutePath() +"\n");
        close(stream);
        close(file);

        return out;
    }


    static Map<String, Map<String, Object>> cslv = new ConcurrentHashMap<>();

    public static Map<String, Object> solve(String n) {
        if (cslv.containsKey(n)){
            return cslv.get(n);
        }
        Dependency d = dependencyMap.get(n);
        if (d == null){
            throw new LogicalException("Dependency " + n + "not found");
        }
        for (Dependency.Version v: d.getVersions().values()){
            log.info("try solve dependency " + d.getName() + " version " + v.name());
            Map<String, Object> res = null;
            if (!isEmpty(v._f)){
                if (contains(getProfiles(), v._f)) {
                    res = parseParams(v);
                }
            } else {
                res = parseParams(v);
            }


            if (res != null){
                cslv.put(n, res);
                return res;
            }
        }

        //TODO throw error based on configuration
        log.warn("No version compatible found for "+ n);
        return null;
    }

    private static boolean contains(Set<String> x, List<String> y){
        for (String s: y){
            if ("*".equals(s)){
                return true;
            }
            for (String z: x){
                if (s.equalsIgnoreCase(z)){
                    return true;
                }
            }
        }
        return false;
    }

    private static Map<String, Object> parseParams(Dependency.Version v){
        Map<String, Object> res = new HashMap<>();
        for (int i = 0; i < v.params().size(); i++){
            Map<String, String> p = (Map<String, String>) v.params().get(i);
            String key = p.get("key");
            String val = p.get("val");
            if (!hasText(val)){
                val = p.get("value");
            }
            Object ret = CommandRegistry.getInstance().executeCommandLine(val);
            if (ret != null){
                res.put(key, ret);
                log.info("@dpmgmt[" + key + "]=(" + ret + ")");
            }
        }
        if (res.values().size() == v.params().size()){
            return res;
        }
        return null;
    }



    public static Set<String> getProfiles(){
        Set<String> p = new HashSet<>();
        String m;
        if (isWindows()){
            p.add("win");
            p.add("windows");
        }
        if (isUnix()){
            p.add("unix");
        }
        if (isLinux()){
            p.add("linux");
        }
        if (isMac()){
            p.add("mac");
        }

        if (isRaspberryPI()){
            p.add("pi");
            p.add("raspberryPi");
        }

        if (isAndroid()){
            p.add("android");
        }

        if (isSolaris()){
            p.add("sunos");
            p.add("solaris");
        }

        for (Object v: SYSUtils.getLinuxDetails().values()){
            String x = SYSUtils.fix(v + "", "");
            if (!x.startsWith("http")){
                p.add(x);
            }
        }


        List<String> t = new ArrayList<>();
        if (is32Bits()){
            for (String s: p){
                t.add(s + "32");
            }
        }

        if (is64Bits()){
            for (String s: p){
                t.add(s + "64");
            }
        }

        p.addAll(t);
        p.add(SYSUtils.fix(getOS().getArch(), ""));
        p.add(SYSUtils.fix(getOS().getName(), ""));



        if (hasText(getProperty(DEPENDENCY_FORCED_PROFILES))){
            String z[] = getProperty(DEPENDENCY_FORCED_PROFILES).split(",");
            for (String x: z){
                p.add(x);
            }
        }
        return p;
    }


    static void execProgram(String[] args) throws IOException {
        log.i("sysProfiles [" + join(getProfiles(), ",") + "]");
        DependencyManager.importDependencyRules("_cargo_/dependencies.json");


        if ("--test-os".equalsIgnoreCase(args[0])){
            log.info("OSProxies.tryFsWebcam");
            OSProxies.findWebcamIds(new Handler<List<Tuple2<String, String>>>() {
                @Override
                public void handle(List<Tuple2<String, String>> tuple2s) {
                    for (Tuple2 t: tuple2s){
                        log.info(t.first() + " --- " + t.second());
                    }
                }
            });

            OSProxies.takeSnapshot(null);
        }

        if ("--solve".equalsIgnoreCase(args[0]) || "-s".equalsIgnoreCase(args[0])){
            for (int i = 1; i < args.length; i++){
                String n = args[i];
                Map<String, Object> r = DependencyManager.solve(n);
                for (Map.Entry<String, Object> e: r.entrySet()){
                    log.i(n + "[" + e.getKey()  + "] = " + e.getValue());
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
       execProgram(args);
    }



}
