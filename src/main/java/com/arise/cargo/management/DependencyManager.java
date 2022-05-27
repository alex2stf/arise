package com.arise.cargo.management;

import com.arise.core.exceptions.DependencyException;
import com.arise.core.models.Handler;
import com.arise.core.models.Tuple2;
import com.arise.core.serializers.parser.Groot;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.MapUtil;
import com.arise.core.tools.Mole;
import com.arise.core.tools.ReflectUtil;
import com.arise.core.tools.StreamUtil;
import com.arise.core.tools.StringUtil;
import com.arise.core.tools.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.arise.cargo.management.Locations.dest;
import static com.arise.cargo.management.Locations.downloads;
import static com.arise.cargo.management.Locations.out;
import static com.arise.cargo.management.Locations.src;

public class DependencyManager {

    private DependencyManager(){

    }


    private static final Map<String, Dependency> dependencyMap = new HashMap<>();


    private static final Mole log = Mole.getInstance(DependencyManager.class);

    public static void withJarDependencyLoader(String name, final Handler<URLClassLoader> handler){
        withJarDependency(name, new Handler<Resolution>() {
            @Override
            public void handle(Resolution resolution) {
                File jar = resolution.file();
                try {
                    URLClassLoader classLoader  = new URLClassLoader(
                            new URL[] {jar.toURI().toURL()},
                            DependencyManager.class.getClassLoader()
                    );
                    handler.handle(classLoader);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    throw new DependencyException("URLClassLoader failed for " + jar.getAbsolutePath(), e);
                }
            }
        });
    }

    public static void withJarDependency(String name, Handler<Resolution> handler){
        Resolution r = solveDependency(name);
        if (r._u != null && r._u.exists()) {
            handler.handle(r);
        } else {
            log.warn("Could not solve dependency " + name);
        }
    }

    public static Resolution solveDependency(String name){
        Dependency dependency = getDependency(name);
        for (Map.Entry<String, Dependency.Version> e: dependency.getVersions().entrySet()){
            Resolution r = trySolveVersion(e.getValue(), dependency);
            if (r != null){
                return r;
            }
        }
        return null;
    }

    private static Resolution trySolveVersion(Dependency.Version v, Dependency d){
        for (String url: v.urls){
            Resolution r = trySolveUrlString(url, d, v);
            if (r != null){
                return r;
            }
        }
        return null;
    }

    private static Resolution trySolveUrlString(String url, Dependency dependency, Dependency.Version v){
        if (url.startsWith("$maven-local:")){
            url = url.substring("$maven-local:".length());
            File m2 = FileUtil.getUserDirectory(".m2");
            File jarFile = new File(new File(m2, "repository"), url);
            if (m2.exists() && jarFile.exists()){
                return new Resolution(jarFile, dependency, v);
            }
        }

        try {
            File fetched = download(url, Locations.libs(), v.id);
            return new Resolution(fetched, dependency, v);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }



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




    public static final List<Unarchiver> unarchivers = new ArrayList<>();

    private static Zip zipper = new Zip();
    static {
        unarchivers.add(zipper);
    }

    static String getExt(File f){
        String p[] = f.getName().split("\\.");
        return p[p.length -1];
    }











    public static Resolution solveSilent(Dependency dependency){
        return solveDependency(dependency.getName());
    }



    public static Resolution solveWithPlatform(Dependency dependency,
                                               Dependency.Version version,
                                               File outputDir,
                                               Mole logger) throws IOException {
       for(String url: version.urls) {
           if (url.startsWith("internal://")) {
               String id = url.substring("internal://".length());
               InputStream inputStream = FileUtil.findStream("_cargo_/" + id);

               String outName = (dependency.getName() + "_" + version.platformMatch).toLowerCase();
               File libsDir = new File(outputDir, "libs");
               if (!libsDir.exists()) {
                   libsDir.mkdirs();
               }
               File outZip = new File(libsDir, outName + ".zip");

               File outUnzipped = new File(libsDir, outName);

               if(!outUnzipped.exists()) {
                   FileOutputStream fileOutputStream = new FileOutputStream(outZip);
                   logger.info("Transfer " + id + " into " + outZip.getAbsolutePath());

                   StreamUtil.transfer(inputStream, fileOutputStream, 1024);
                   zipper.extract(outZip, outUnzipped);
               }


               return new Resolution(outUnzipped, dependency, version);
           }
       }

        return null;
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






    private static boolean requiresUncompressed(File downloaded){
        String name = downloaded.getAbsolutePath();
        return name.endsWith(".zip");
    }


    public static Tuple2<List<Resolution>, URLClassLoader> withDependencies(String [] names) throws Exception {
        List<Resolution> resolutions = new ArrayList<>();
        List<File> jars = new ArrayList<>();
        for (String name: names) {
            Dependency dependency = dependencyMap.get(name);
            Dependency.Version version = dependency.getVersion("WIN64");
            File downloaded = fetchOneOfUrls(version.urls, downloads(), version.id );
            File outDir = dest(dependency);
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
        private final File  _u;
        private final Dependency _s;
        private Dependency.Version _cp;



        public Resolution(File _u, Dependency _s, Dependency.Version _cp) {
            this._cp = _cp;
            this._u = _u;
            this._s = _s;
        }

        public Dependency.Version selectedVersion(){
            return _cp;
        }


        @Deprecated
        public File uncompressed() {
            return _u;
        }

        public File file() {
            return _u;
        }

        public Dependency source() {
            return _s;
        }
    }


    @SuppressWarnings("unused")
    public static final void addUnarchiver(Unarchiver unarchiver) {

        unarchivers.add(unarchiver);
    }
}
