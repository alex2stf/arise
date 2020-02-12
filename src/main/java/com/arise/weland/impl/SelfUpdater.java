package com.arise.weland.impl;

import com.arise.cargo.management.DependencyManager;
import com.arise.core.tools.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

public class SelfUpdater {

    String root = "https://raw.githubusercontent.com/alex2stf/arise/master";
    String buildIdFile = root + "/build-info.properties";


    private static final Mole log = Mole.getInstance(SelfUpdater.class);

    private File tmpDir(){
        return  FileUtil.getAppDirChild("tmp");
    }

    private File workDir(){
        File w =  new File(tmpDir(), "update");
        if (!w.exists()){
            w.mkdirs();
        }

        return w;

    }

    public void check() {
        checkUpdate();
    }

    private void checkUpdate()  {


        File buildInfoFile = null;
        try {
            buildInfoFile = DependencyManager.download(buildIdFile, tmpDir(), "build-info" + UUID.randomUUID().toString());
            buildInfoFile.deleteOnExit();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!buildInfoFile.exists()){
            return;
        }
        Properties properties = null;
        try {
            properties = FileUtil.loadProps(buildInfoFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(properties);

        Enumeration<String> enums = (Enumeration<String>) properties.propertyNames();

        List<File> downloads = new ArrayList<>();

        while (enums.hasMoreElements()) {
            String key = enums.nextElement();
            if ("BUILD_ID".equals(key)){
                continue;
            }
            if (!key.startsWith("/")){
                key = "/" + key;
            }
            File out = new File(workDir(), key);
            File parentDir = out.getParentFile();
            if (!parentDir.exists()){
                parentDir.mkdirs();
            }
            String uri = root + key;
            try {
                File file = DependencyManager.download(root + key, parentDir, out.getName());
                downloads.add(file);
//                file.deleteOnExit();
            } catch (Exception e) {
                log.error("Failed to fetch " + uri);
            }
        }

        compile(downloads);

        buildInfoFile.delete();

    }

    public static File getJava(){
        String name = "java";
        String javaHome = System.getProperty("java.home");
        File f = new File(javaHome);
        f = new File(f, "bin");
        if (SYSUtils.isWindows()){
            name = name + ".exe";
        }
        f = new File(f, name);

        if (!f.exists()){
            f = new File("C:\\Program Files\\Java\\jre7\\bin\\java.exe");
        }

        return f;
    }

    public static File getJavac(){
        File f = getJavac(System.getProperty("java.home"));

        if (f == null){
            f = getJavac(System.getProperty("jdk.home"));
        }

        if (f == null){
            String sunLib = System.getProperty("sun.boot.library.path");
            if (StringUtil.hasText(sunLib)){

                File slb = new File(sunLib);
//C:\Program Files\Java\jre8\bin
                if (slb.isDirectory() && "bin".equalsIgnoreCase(slb.getName())){
                    slb = slb.getParentFile();
                }

                if (slb.isDirectory() && slb.getName().toLowerCase().startsWith("jre")){
                    slb = slb.getParentFile();
                }

            }
        }
        return f;
    }

    public static File getJavac(String javaHome){
        String name = "javac";
        File f = new File(javaHome);
        f = new File(f, "bin");
        if (SYSUtils.isWindows()){
            name = name + ".exe";
        }
        f = new File(f, name);
        if (!f.exists()){
            f = new File(javaHome);
            f = f.getParentFile();
            f = new File(f, "bin");
            f = new File(f, name);
        }

        if (!f.exists()){
            return null;
        }
        log.info("using javac " + f.getAbsolutePath());
        return f;
    }


    private void compile(List<File> downloads) {

        File javac = getJavac();
        File java = getJava();

        File builder = new File(workDir(), "src/main/java/com/arise/Builder.java");
        if (!builder.exists()){
            closeUpdate("Builder class not found");
            return;
        }

        File buildClz = new File(workDir(), "build/classes");
        if (!buildClz.exists() && !buildClz.mkdirs()){
            closeUpdate("Failed to create output");
            return;
        }

        SYSUtils.exec(javac.getAbsolutePath(), "-d", buildClz.getAbsolutePath(), builder.getAbsolutePath());

        String jdkHome = System.getProperty("jdk.home");
        SYSUtils.exec(new String[]{
                getJava().getAbsolutePath(), "-cp",
                buildClz.getAbsolutePath(),
                "com.arise.Builder",
                "release",
                workDir().getAbsolutePath(),
                new File(jdkHome).getAbsolutePath() //TODO improve
        }, new SYSUtils.ProcessLineReader() {
            @Override
            public void onStdoutLine(int line, String content) {
                System.out.println(content);
            }

            @Override
            public void onErrLine(int line, String content) {
                System.out.println(content);
            }
        }, true, false);
    }

    private void closeUpdate(String reason) {
    }

    public static void main(String[] args) {

        new SelfUpdater().check();
    }
}
