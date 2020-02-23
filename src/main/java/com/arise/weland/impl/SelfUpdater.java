package com.arise.weland.impl;

import com.arise.cargo.management.DependencyManager;
import com.arise.core.tools.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Pattern;

public class SelfUpdater {

    String root = "https://raw.githubusercontent.com/alex2stf/arise/master";
    String buildIdFile = root + "/build-info.properties";

    File jdkHome;
    File jdkExe;
    File jvmExe;

    String getExe(String name){
        if (SYSUtils.isWindows()){
            return name + ".exe";
        }
        return name;
    }

    public SelfUpdater(){

        String [] propertiesNames = new String[]{
                "jdk.home",
                "java.home",
                "java.library.path",
                "java.class.path",
                "java.ext.dirs",
                "sun.boot.class.path"
        };

        for (String s: propertiesNames){
            if (!isValid()){
                searchIsSystemProperty(s);
            }
        }


    }


    private void searchIsSystemProperty(String name){
        String libPath = System.getProperty(name);
        if (!StringUtil.hasText(libPath)){
            log.info("system property " + name + " not found");
            return;
        }
        log.info("check system property " + name);
        String paths[] = libPath.split(Pattern.quote(File.pathSeparator));

        for (String s: paths){
            if (s.indexOf("jdk") > -1 && !isValid()){
                searchInPath(s);
            }
        }
    }

    private void searchInPath(String s){
        log.info("search using base " + s);
        File tmp = new File(s);
        if (!tmp.isDirectory()){
            tmp = tmp.getParentFile();
        }

        while (tmp != null && tmp.exists() && !isValid()){


            jdkExe = new File(tmp, getExe("javac"));
            jvmExe = new File(tmp, getExe("java"));
            jdkHome = tmp;

            if (!isValid()){
                File binDir = new File(tmp, "bin");
                if (binDir.exists()){
                    jdkExe = new File(binDir, getExe("javac"));
                    jvmExe = new File(binDir, getExe("java"));
                    jdkHome = tmp;
                }
            }
            if (isValid()){
                break;
            }
            tmp = tmp.getParentFile();
        }

    }


    private boolean isValid(){
        return jdkExe != null && jdkExe.exists() &&
                jvmExe != null && jvmExe.exists() &&
                jdkHome != null && jdkHome.exists();
    }


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
        if (!isValid()){
            closeUpdate("JDK not found");
            return;
        }
        log.info( "JDK_HOME=" + jdkHome);
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
                file.deleteOnExit();
            } catch (Exception e) {
                log.error("Failed to fetch " + uri);
            }
        }

        compile(downloads);

        buildInfoFile.delete();

    }








    private void compile(List<File> downloads) {




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


        SYSUtils.exec(jdkExe.getAbsolutePath(), "-d", buildClz.getAbsolutePath(), builder.getAbsolutePath());

        SYSUtils.exec(new String[]{
                jvmExe.getAbsolutePath(), "-cp",
                buildClz.getAbsolutePath(),
                "com.arise.Builder",
                "release",
                workDir().getAbsolutePath(),
                jdkHome.getAbsolutePath() //TODO improve
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
