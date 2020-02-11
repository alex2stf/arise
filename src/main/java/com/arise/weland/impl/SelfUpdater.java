package com.arise.weland.impl;

import com.arise.cargo.management.DependencyManager;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.Mole;
import com.arise.core.tools.SYSUtils;
import com.arise.core.tools.ThreadUtil;

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
        ThreadUtil.fireAndForget(new Runnable() {
            @Override
            public void run() {

                    checkUpdate();

            }
        });
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
        return f;
    }


    public static File getJavac(){
        String name = "javac";
        String javaHome = System.getProperty("java.home");
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
        return f;
    }


    private void compile(List<File> downloads) {

        File javac = getJavac();
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

        SYSUtils.exec(new String[]{
                getJava().getAbsolutePath(), "-cp",
                buildClz.getAbsolutePath(),
                "com.arise.Builder",
                "release",
                workDir().getAbsolutePath()
        }, new SYSUtils.ProcessLineReader() {
            @Override
            public void onStdoutLine(int line, String content) {

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

        try {
            DependencyManager.download(
                    "https://duckduckgo.com/?q=logo&iar=image&ia=images&iax=images",
                    FileUtil.findAppDir(),
                    "tmp.html"
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
