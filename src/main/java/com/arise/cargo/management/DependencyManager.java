package com.arise.cargo.management;

import com.arise.core.models.Tuple2;
import com.arise.core.serializers.parser.Groot;
import com.arise.core.tools.*;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DependencyManager {

    private DependencyManager(){

    }


    private static final Map<String, Dependency> dependencyMap = new HashMap<>();

    public static void importDependencyRules(String in) throws IOException {
        InputStream inps = FileUtil.findStream(in);
        if(inps == null){
                System.out.println(in + " NOT FOUND ");
        }
        String x = StreamUtil.toString(inps);
        Map<Object, Object> data = (Map) Groot.decodeBytes(x);


        for (Map.Entry<Object, Object> e: data.entrySet()){
            String name = (String) e.getKey();

            Map args = (Map) e.getValue();
            String type = MapUtil.getString(args, "type");
            if ("wrapper".equals(type) || "indev".equals(type)){
                System.out.println("in dev dependency " + name);
                continue;
            }

            System.out.println("import dependency " + name);
            Dependency dependency = new Dependency();
            dependency.setName(name);
            Dependency.decorate(dependency, args);
            dependencyMap.put(name, dependency);
        }
    }


    public static HttpURLConnection getConnection(URL url) throws IOException {
        Proxy proxy = null;

        if (StringUtil.hasContent(System.getProperty("proxy.host"))){
            Proxy.Type proxyType = Proxy.Type.HTTP;

            Integer port = 8080;
            if (StringUtil.hasContent(System.getProperty("proxy.port"))){
                try {
                    port = Integer.valueOf(System.getProperty("proxy.port"));
                }catch (Exception e){
                    port = 8080;
                }
            }

            InetSocketAddress inetSocketAddress = new InetSocketAddress(System.getProperty("proxy.host"), port);
            proxy  = new Proxy(proxyType, inetSocketAddress);
        }

        HttpURLConnection connection;
        if (proxy != null){
            return  (HttpURLConnection) url.openConnection(proxy);
        } else {
            return  (HttpURLConnection) url.openConnection();
        }
    }


    public static File download(String uri, File outDir, String name) throws IOException {
        return download(new URL(uri), outDir, name);
    }


    public static File download(URL url, File outDir, String name) throws IOException {


        File out = new File(outDir, name);
        if (out.exists()){
            return out;
        }
        HttpURLConnection connection = getConnection(url);

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
        Util.close(stream);
        Util.close(file);

        return out;
    }



    @Deprecated
    public static Rule download(Dependency dependency, File outputDir) throws IOException {

        if (!outputDir.exists()){
            outputDir.mkdirs();
        }



        for (Rule rule: dependency.ruleList){
            if (rule.acceptConditions()){
                File zipped = download(rule.getPath(), outputDir, rule.getSourceName());
                return rule.setZipped(zipped);
            }
        }

        return null;
    }

    public static final List<Unarchiver> unarchivers = new ArrayList<>();

    private static Zip zipper = new Zip();
    static {
        unarchivers.add(zipper);
    }

    static String getExt(File f){
        String p[] = f.getName().split("\\.");
        return p[p.length -1];
    }


    @Deprecated
    public static File uncompress(Dependency dependency, Rule rule, File rootDir){
        File unarchived = new File(rootDir, dependency.getName() + "_" + rule.getName());
        if (!FileUtil.hasFiles(unarchived)){
            File zipped = rule.getZipped();
            for (Unarchiver unarchiver: unarchivers){
               if (unarchiver.extract(zipped, unarchived)){
                   break;
               }
            }
        }
        File list[] = unarchived.listFiles();
        while (list != null && list.length == 1 && list[0].isDirectory()){
            unarchived = list[0];
            list = unarchived.listFiles();
        }

        return unarchived;
    }


    public static File getRoot(){
        File out = FileUtil.findAppDir();
        return new File(out, "dpmngmt");
    }

    public static File getSrc(){
        File src = new File(getRoot(), "src");
        if (!src.exists()){
            src.mkdirs();
        }
        return src;
    }

    public static File getLibs(){
        File libs = new File(getRoot(), "libs");
        if (!libs.exists()){
            libs.mkdirs();
        }
        return libs;
    }

    public static Resolution solveSilent(Dependency dependency){
        try {
            return solve(dependency);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Resolution solve(Dependency dependency, File out) throws IOException {
        Rule systemRule = download(dependency, new File(out, "src"));
        if (systemRule == null){
            return null;
        }
        File currentPath = uncompress(dependency, systemRule, new File(out, "out") );
        return new Resolution(systemRule, currentPath, dependency);
    }

    public static Resolution solveWithPlatform(Dependency dependency,
                                               Dependency.Version version,
                                               File outputDir,
                                               Mole logger) throws IOException {
       for(String url: version.urls) {
           if (url.startsWith("internal://")) {
               String id = url.substring("internal://".length());
               InputStream inputStream = FileUtil.findStream("_cargo_/" + id);

               String outName = (dependency.getName() + "_" + version.getPlatformMatch()).toLowerCase();
               File libsDir = new File(outputDir, "libs");
               if (!libsDir.exists()) {
                   libsDir.mkdirs();
               }
               File outZip = new File(libsDir, outName + ".zip");
               FileOutputStream fileOutputStream = new FileOutputStream(outZip);
               logger.info("Transfer " + id + " into " + outZip.getAbsolutePath());

               StreamUtil.transfer(inputStream, fileOutputStream, 1024);
               File outUnzipped = new File(libsDir, outName);
               zipper.extract(outZip, outUnzipped);

               return new Resolution(outUnzipped, dependency, version);
           }
       }

        return null;
    }



    public static Resolution solve(Dependency dependency) throws IOException {
        File out = getRoot();
        Rule systemRule = download(dependency, new File(out, "src"));
        if (systemRule == null){
            return null;
        }
        File currentPath = uncompress(dependency, systemRule, new File(out, "out") );
        return new Resolution(systemRule, currentPath, dependency);
    }



    public static Dependency getDependency(String name) {
        return dependencyMap.get(name);
    }




    public static File fetchOneOfUrls(List<String> urls, File out, String name){
        for (String url: urls){
            File fetched = null;
            try {
                String actualName = name;
                if (url.endsWith(".jar")){
                    actualName += ".jar";
                }
                else if (url.endsWith(".zip")){
                    actualName += ".zip";
                }
                fetched = download(url, out, actualName);
                if (fetched.exists()){
                    return fetched;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }



    public static String getDestination(Dependency dependency){
        if ("binary".equalsIgnoreCase(dependency.type)){
            return "bin";
        }
        return "libs";
    }


    private static boolean requiresUncompressed(File downloaded){
        String name = downloaded.getAbsolutePath();
        return name.endsWith(".zip");
    }


    public static Tuple2<List<Resolution>, URLClassLoader> withDependencies(String [] names) throws Exception {
        List<Resolution> resolutions = new ArrayList<>();
        File root = getRoot();
        File downloadLocation = new File(root, "downloads");
        if (!downloadLocation.exists()){
            downloadLocation.mkdirs();
        }
        List<File> jars = new ArrayList<>();
        for (String name: names) {
            Dependency dependency = dependencyMap.get(name);
            Dependency.Version version = dependency.getVersion("LINUX");
            File downloaded = fetchOneOfUrls(version.urls, downloadLocation, version.id );
            File outDir = new File(getRoot(), getDestination(dependency));
            if (!outDir.exists()){
                outDir.mkdirs();
            }
            Resolution resolution;

            if (requiresUncompressed(downloaded)){

                File unzipped = new File(outDir, version.id);

                if (!unzipped.exists()) {
                    System.out.println("Transfer " + downloaded.getAbsolutePath() + " into " + unzipped.getAbsolutePath());
                    zipper.extract(downloaded, unzipped);
                }
                resolution = new Resolution(unzipped, dependency, version);
            } else {
                File moved = new File(outDir, downloaded.getName());
                if (!moved.exists()) {
                    Files.copy(downloaded.toPath(), new File(outDir, downloaded.getName()).toPath());
                }
                resolution = new Resolution(moved, dependency, version);
            }



            if ("jar".equalsIgnoreCase(dependency.type)) {
                try {
                    jars.add(downloaded);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            resolutions.add(resolution);
        }
        return new Tuple2<>(resolutions, ReflectUtil.loadJars(jars));
    }

    public static Resolution findResolution(List<Resolution> resolutions, String name) {
            for (Resolution s: resolutions){
                if (name.equalsIgnoreCase(s.source().getName())){
                    return s;
                }
            }
            return null;
    }


    public static class Resolution{
        private final Rule  _r;
        private final File  _u;
        private final Dependency _s;
        private Dependency.Version _cp;

        public Resolution(Rule _r, File _u, Dependency _s) {
            this._r = _r;
            this._u = _u;
            this._s = _s;
        }

        public Resolution(File _u, Dependency _s, Dependency.Version _cp) {
            this._cp = _cp;
            this._r = null;
            this._u = _u;
            this._s = _s;
        }

        public Dependency.Version selectedVersion(){
            return _cp;
        }

        public Rule rule() {
            return _r;
        }

        public File uncompressed() {
            return _u;
        }

        public Dependency source() {
            return _s;
        }
    }


    public static final void addUnarchiver(Unarchiver unarchiver) {

        unarchivers.add(unarchiver);
    }
}
