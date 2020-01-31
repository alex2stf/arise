package com.arise.cargo.management;

import com.arise.core.tools.FileUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

public class DependencyManager {



    static File download(String uri, File outDir, String name) throws IOException {
        URL url = new URL(uri);

        File out = new File(outDir, name);
        if (out.exists()){
            return out;
        }
//        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("sibelius.orange.intra", 8080));

//        HttpURLConnection connection = (HttpURLConnection) url.openConnection(proxy);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int contentLength = connection.getContentLength();
        int responseCode = connection.getResponseCode();
        if (responseCode != 200){
            throw new RuntimeException("wget " + responseCode + " " + uri);
        }

        RandomAccessFile file = new RandomAccessFile(out.getAbsolutePath(), "rw");
        InputStream stream = connection.getInputStream();

        System.out.println("wget " + uri);
        int size = contentLength; // size of download in bytes
        int downloaded = 0; // number of bytes downloaded
        int MAX_BUFFER_SIZE = 1024;
        int dotCount = 0;

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
                System.out.print(".");
                if (dotCount % 10000 == 0) {


                    System.out.print( (lX == x ? lX + 1 : x) + "%\n");
                    dotCount = 0;
                    lX = x;
                }
            }
        }
        System.out.print(" OK\n");
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

    public static File uncompress( Dependency dependency, Rule rule, File rootDir){
        File unarchived = new File(rootDir, dependency.getName() + "_" + rule.getName());
        if (!unarchived.exists()){
            new ZipUnarchiver().extract(rule.getZipped(), unarchived);
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

    public static File solve(Dependency dependency) throws IOException {
        File out = getRoot();
        Rule systemRule = download(dependency, new File(out, "src"));
        File currentPath = uncompress(dependency, systemRule, new File(out, "out") );
        return currentPath;
    }

    public static void main(String[] args) throws IOException {



       solve(Dependencies.VLC_2_1_0);
       solve(Dependencies.NWJS_0_12_0);


    }
}
