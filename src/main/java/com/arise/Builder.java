package com.arise;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public class Builder {

    public static boolean isWindows(){
        return String.valueOf(System.getProperty("os.name")).toLowerCase().startsWith("windows");
    }

    public static File getJavac(){
        String name = "javac";
        String javaHome =
                //new File("Jv7Win32SDK\\Java\\jdk1_7_0_79").getAbsolutePath();
                System.getProperty("java.home");
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

    public static void showProcess(InputStream inputStream){
        if (inputStream == null){
            return;
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        boolean shouldExit = false;
        try {
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println("        " + line);
                if (!shouldExit && line.toLowerCase().indexOf("error") > -1){
                    shouldExit = true;
                }
            }
        } catch (Exception e){
                e.printStackTrace();
        }
        if (shouldExit){
            System.exit(-1);
        }
//        System.exit(-1);
    }

    static String join(String [] sss){
        String r = "";
        for (String s: sss){
            s = s.replaceAll("\\\\", "\\\\");
            if (s.indexOf("\\") > -1){
                String p[] = s.split("\\\\");
                s = p[p.length -1];
            }
            r+= " " + s;
        }
        return r;
    }

    public synchronized static void compile(String[] xxx) throws IOException, InterruptedException {
        System.out.println("exec cmd\n    " + join(xxx));
        ProcessBuilder processBuilder = new ProcessBuilder(xxx);
        processBuilder.redirectErrorStream(false);
        final Process proc = processBuilder.start();
        showProcess(proc.getErrorStream());
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
            ,new Lib(ROOT + "core", ROOT + "canter", ROOT + "astox/net", ROOT + "weland", ROOT + "cargo/management")
                    .jarLib("libs/bluecove-2.1.0.jar", "libs/jaudiotagger-2.2.3.jar")
                    .jarLib("libs/jna-3.5.2.jar")
                    .jarLib("libs/platform-3.5.2.jar")
                    .jarLib("libs/vlcj-3.0.1.jar" )
                    .jarLib("libs/commons-compress-1.19.jar" )
                    .jarLib("libs/xz-1.8.jar" )
                    .resourcesDirs(RES_ROOT + "templates", RES_ROOT + "weland")
                    .resourceFiles(RES_ROOT + "content-types.json")
                    .mainClass("com.arise.weland.Main")
                    .named("weland")
                    .version("1.0")
                    .pack()


            /**
            * steiner base lib
            */
//            ,new Lib(ROOT + "core", ROOT + "canter", ROOT + "cargo",
//                           ROOT + "astox/net/models", ROOT + "astox/net/clients")
//                    .resourcesDirs(RES_ROOT + "templates", RES_ROOT + "weland")
//                    .named("jumper")
//                    .version("1.0")




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

            if (lib.shouldPack){
                File batFile = new File(lib.name + ".bat");
                String batContent =  "java -cp \"out/"+lib.name + "-" + lib.vrs +".jar;libs\\*\" " + lib.mainClazz;

                try (PrintStream out = new PrintStream(new FileOutputStream(batFile))) {
                    out.print(batContent);
                }

            }

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
        String vrs;
        String name;
        String[] resDirs;
        String[] srcDirs;
        String mainClazz;
        List<String> jarlibs;
        String[] srcFiles;
        String[] resourceFiles;
        boolean shouldPack = false;

        Lib(String ... dirs){
            srcDirs = dirs;
        }

        Lib pack() {
            shouldPack = true;
            return this;
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
            if (jarlibs != null && !jarlibs.isEmpty()){
                extra = 5;
            }

            String[] r = new String[files.size() + extra];

            if (jarlibs == null || jarlibs.isEmpty()){
                r[0] = javac.getAbsolutePath();
                r[1] = "-d";
                r[2] = output.getAbsolutePath();
            } else {
                r[0] = javac.getAbsolutePath();
                r[1] = "-cp";
                r[2] = concat(jarlibs, ";");
                r[3] = "-d";
                r[4] = output.getAbsolutePath();
            }

            for (int i = 0; i < files.size(); i++){
                r[i+extra] = files.get(i).getAbsolutePath();
            }
            return r;
        }

        String concat(List<String> values, String delimiter){
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < values.size(); i++){
                if (i > 0){
                    sb.append(delimiter);
                }
                sb.append(values.get(i));
            }
            return sb.toString();
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

        public Lib jarLib(String ... x) {
            if (jarlibs == null){
                jarlibs = new ArrayList<>();
            }
            for (String s: x){
                jarlibs.add(s);
            }
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
