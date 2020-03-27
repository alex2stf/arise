package com.arise.cargo.management;

import com.arise.core.tools.FileUtil;
import com.arise.core.tools.StringUtil;
import com.arise.core.tools.Util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DependencyManager {

    private DependencyManager(){

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
            throw new RuntimeException("wget " + responseCode + " " + url);
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
                    System.out.print("\n");
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
        System.out.print(" OK\n");
        Util.close(stream);
        Util.close(file);

        return out;
    }



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

    static {
        unarchivers.add(new Zip());
    }

    static String getExt(File f){
        String p[] = f.getName().split("\\.");
        return p[p.length -1];
    }

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

    public static Resolution solve(Dependency dependency) throws IOException {
        File out = getRoot();
        Rule systemRule = download(dependency, new File(out, "src"));
        if (systemRule == null){
            return null;
        }
        File currentPath = uncompress(dependency, systemRule, new File(out, "out") );
        return new Resolution(systemRule, currentPath, dependency);
    }

    public static class Resolution{
        private final Rule  _r;
        private final File  _u;
        private final Dependency _s;

        public Resolution(Rule _r, File _u, Dependency _s) {
            this._r = _r;
            this._u = _u;
            this._s = _s;
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
