package com.arise;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

public class Builder {

    public static boolean isWindows(){
        return String.valueOf(System.getProperty("os.name")).toLowerCase().startsWith("windows");
    }

    public static File getJavac(){
        String name = "javac";
        String javaHome = System.getProperty("java.home");
        File f = new File(javaHome);
        f = new File(f, "bin");
        if (isWindows()){
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

    private static void showProcess(InputStream inputStream){
        if (inputStream == null){
            return;
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        try {
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println("        " + line);
            }
        } catch (Exception e){

        }
    }

    static String join(String [] sss){
        String r = "";
        for (String s: sss){
            if (s.indexOf("\\") > -1){
                String p[] = s.split("\\\\");
                s = p[p.length -1];
            }
            r+= " " + s;
        }
        return r;
    }

    private synchronized static void compile(String[] xxx) throws IOException, InterruptedException {
        System.out.println("    " + join(xxx));
        ProcessBuilder processBuilder = new ProcessBuilder(xxx);
        processBuilder.redirectErrorStream(false);
        final Process proc = processBuilder.start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                showProcess(proc.getInputStream());
                showProcess(proc.getErrorStream());
            }
        }).start();
        proc.waitFor();
    }

    static final String ROOT = "src/main/java/com/arise/";
    static final String RES_ROOT = "src/main/resources/";

    static Lib[] libs = new Lib[]{

            new Lib(ROOT + "core")
                    .named("core")
                    .version("1.0")

            ,new Lib(ROOT + "canter", ROOT + "core")
                    .resourcesDirs(RES_ROOT + "templates")
                    .named("canter")
                    .version("1.0")

            ,new Lib(ROOT + "cargo", ROOT + "core")
                    .resourcesDirs(RES_ROOT + "templates")
                    .named("cargo")
                    .version("1.0")

            /**
             * CORONA	COntROl aNy mAchine
            */
            ,new Lib(ROOT + "core", ROOT + "canter", ROOT + "astox/net", ROOT + "corona")
                    .jarLib("libs/bluecove-2.1.0.jar" )
                    .resourcesDirs(RES_ROOT + "templates", RES_ROOT + "corona")
                    .resourceFiles(RES_ROOT + "content-types.json")
                    .mainClass("com.arise.weland.Main")
                    .named("corona")
                    .version("1.0")


            /**
            * steiner base lib
            */
            ,new Lib(ROOT + "core", ROOT + "canter", ROOT + "cargo",
                           ROOT + "astox/net/models", ROOT + "astox/net/clients")
                    .resourcesDirs(RES_ROOT + "templates", RES_ROOT + "corona")
                    .named("jumper")
                    .version("1.0")




    };

    public static void main(String[] args) throws IOException, InterruptedException {


        File javac = getJavac();


        File outJars = new File("out");
        if (!outJars.exists()){
            outJars.mkdirs();
        }




        for (Lib lib: libs){
            System.out.println(lib.name);
            File buildDir = new File("build");

            File outClasses = new File(buildDir, "classes");
            if (!outClasses.exists()){
                outClasses.mkdirs();
            }

            String[] params = lib.buildArgs(javac, outClasses);
            compile(params);

            File inputClasses = new File(outClasses, "com");
            if (!inputClasses.exists()){
                inputClasses.mkdirs();
            }

            lib.buildJar("build/classes/com", outJars.getAbsolutePath(), "build/classes/");
            inputClasses.delete();
            outClasses.delete();
            buildDir.delete();

        }





    }

    static void recursiveScan(String dir, List<File> files, boolean checkJava){
        File f = new File(dir);
        File[] childs = f.listFiles();
        if (childs == null){
            return;
        }

        for (File child: childs){
            if (child.isDirectory()){
                recursiveScan(child.getAbsolutePath(), files, checkJava);
            } else {
                if (checkJava){
                    if(child.getName().endsWith("java")){
                        files.add(child);
                    }
                } else {
                    files.add(child);
                }
            }
        }
    }

    static class Lib {
        List<File> files = new ArrayList<>();
        List<File> resources = new ArrayList<>();
        private String vrs;
        private String name;
        private String[] resDirs;
        private String[] srcDirs;
        private String mainClazz;
        private String[] jarlibs;
        private String[] srcFiles;
        private String[] resourceFiles;


        Lib(String ... dirs){
            srcDirs = dirs;
        }



        Lib version(String vrs){
            this.vrs = vrs;
            return this;
        }

        String[] buildArgs(File javac, File output){

            for (String s: srcDirs){
                recursiveScan(s, files, true);
            }
            if (srcFiles != null){
                for (String f: srcFiles){
                    files.add(new File(f));
                }
            }

            int extra = 3;
            if (jarlibs != null){
                extra = 5;
            }

            String[] r = new String[files.size() + extra];

            if (jarlibs == null){
                r[0] = javac.getAbsolutePath();
                r[1] = "-d";
                r[2] = output.getAbsolutePath();
            } else {
                r[0] = javac.getAbsolutePath();
                r[1] = "-cp";
                r[2] = jarlibs[0];//TODO join
                r[3] = "-d";
                r[4] = output.getAbsolutePath();
            }

            for (int i = 0; i < files.size(); i++){
                r[i+extra] = files.get(i).getAbsolutePath();
            }
            return r;
        }

        void buildJar(String inputDir, String output, String root) throws IOException {
            Manifest manifest = new Manifest();
            manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, vrs);
            if (mainClazz != null){
                manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, mainClazz);
            }
            if (!output.endsWith(File.separator)){
                output += File.separator;
            }
            String outJar = output + name + "-" + vrs + ".jar";
            JarOutputStream target = new JarOutputStream(new FileOutputStream(outJar), manifest);
            File[] files = new File(inputDir).listFiles();
            for (File f: files){
                add(f, target, root);
            }

            if (resDirs != null){
                for (String resDir: resDirs){
                    recursiveScan(resDir, resources, false);
                }

                for (File source: resources){
                    add(source, target, "src/main/resources/");
                }
            }
            if (resourceFiles != null){
                for (String s: resourceFiles){
                    add(new File(s), target, "src/main/resources/");
                }
            }





            target.close();
        }

        private void add(File source, JarOutputStream target, String except) throws IOException{

            String name = source.getPath().replaceAll("\\\\", "/");
            except = except.replaceAll("\\\\", "/");
            if (except != null){
                except = except.trim();
                if ( !"".equals(except.trim()) ){
                    String parts[] = name.split(except);
                    name = parts[1];
                }
            }

            System.out.println("    jar entry " + name);
            BufferedInputStream in = null;
            try
            {
                if (source.isDirectory()) {
                    if (!name.isEmpty()) {
                        if (!name.endsWith("/"))
                            name += "/";
                        JarEntry entry = new JarEntry(name);
                        entry.setTime(source.lastModified());
                        target.putNextEntry(entry);
                        target.closeEntry();
                    }
                    for (File nestedFile: source.listFiles()) {
                        add(nestedFile, target, except);
                    }
                    return;
                }

                JarEntry entry = new JarEntry(name);
                entry.setTime(source.lastModified());
                target.putNextEntry(entry);
                in = new BufferedInputStream(new FileInputStream(source));

                byte[] buffer = new byte[1024];
                while (true) {
                    int count = in.read(buffer);
                    if (count == -1) {
                        break;
                    }
                    target.write(buffer, 0, count);
                }
                target.closeEntry();
            }
            finally {
                if (in != null)
                    in.close();
            }

        }

        Lib named(String name){
            this.name = name;
            return this;
        }

        public Lib resourcesDirs(String ... resDirs) {
            this.resDirs = resDirs;
            return this;
        }

        public Lib mainClass(String s) {
            mainClazz = s;
            return this;
        }

        public Lib jarLib(String ... jarlibs) {
            this.jarlibs = jarlibs;
            return this;
        }

        public Lib sourceFiles(String ... srcFiles) {
            this.srcFiles = srcFiles;
            return this;
        }

        public Lib resourceFiles(String ... resourceFiles) {
            this.resourceFiles = resourceFiles;
            return this;
        }
    }
}
