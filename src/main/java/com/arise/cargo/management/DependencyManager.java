package com.arise.cargo.management;

import com.arise.canter.Command;
import com.arise.canter.CommandRegistry;
import com.arise.core.exceptions.LogicalException;
import com.arise.core.models.Handler;
import com.arise.core.models.Unarchiver;
import com.arise.core.serializers.parser.Groot;
import com.arise.core.tools.CollectionUtil;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.Mole;
import com.arise.core.tools.SYSUtils;
import com.arise.core.tools.StreamUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.arise.core.AppSettings.Keys.DEPENDENCY_FORCED_PROFILES;
import static com.arise.core.AppSettings.getProperty;
import static com.arise.core.tools.CollectionUtil.safeGetItem;
import static com.arise.core.tools.SYSUtils.getOS;
import static com.arise.core.tools.SYSUtils.is32Bits;
import static com.arise.core.tools.SYSUtils.is64Bits;
import static com.arise.core.tools.SYSUtils.isAndroid;
import static com.arise.core.tools.SYSUtils.isLinux;
import static com.arise.core.tools.SYSUtils.isMac;
import static com.arise.core.tools.SYSUtils.isRaspberryPI;
import static com.arise.core.tools.SYSUtils.isSolaris;
import static com.arise.core.tools.SYSUtils.isUnix;
import static com.arise.core.tools.SYSUtils.isWindows;
import static com.arise.core.tools.StringUtil.hasText;
import static com.arise.core.tools.StringUtil.join;
import static com.arise.core.tools.Util.close;
import static java.lang.String.valueOf;

public class DependencyManager {

    private static final CommandRegistry cmdReg = new CommandRegistry();
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
        cmdReg.addCommand(DOWNLOAD_COMMAND);
        cmdReg.addCommand(new Command<String>("file-exist") {
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

        cmdReg.addCommand(new Command<String>("maven-repo") {
            @Override
            public String execute(List<String> args) {
                File repo = new File(FileUtil.getUserDirectory(".m2"), "repository");
                return new File(repo, args.get(0)).getAbsolutePath();
            }
        });



        cmdReg.addCommand(new Command<String>("unzip") {
            @Override
            public String execute(List<String> x) {
                File s = new File(x.get(0));
                File out = Locations.forName(x.get(1));
                log.info("$unzip " + s.getAbsolutePath() + " to " + out.getAbsolutePath());
                Unarchiver.ZIP.extract(s, out);
                return out.getAbsolutePath();
            }
        });

        cmdReg.addCommand(new Command<String>("sub-location") {
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

    }

    private DependencyManager(){

    }

    public static CommandRegistry getCommandRegistry(){
        return cmdReg;
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



    public static void importDependencyRules(String in) throws IOException {
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

    static Map<String, Object> solve(String n) {
        Dependency d = dependencyMap.get(n);
        if (d == null){
            throw new LogicalException("Dependency not found");
        }
        for (Dependency.Version v: d.getVersions().values()){
            log.info("try solve dependency " + d.getName() + " version " + v.name());
            Map<String, Object> res = parseParams(v);
            if (res != null){
                return res;
            }
        }
        return null;
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
            Object ret = cmdReg.executeCmdLine(val);
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


    static List<String> rls = new ArrayList<>();

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
            p.add(String.valueOf(v));
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
        p.add(getOS().getArch());
        p.add(getOS().getName().toLowerCase());



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
        if (args.length < 2){
            System.out.println("usage");
            return;
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
//       execProgram(args)
        execProgram(new String[]{"--solve", "NWJS"});


    }


}
