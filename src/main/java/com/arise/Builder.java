package com.arise;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.regex.Pattern;

public class Builder {

    public static boolean isWindows(){
        return String.valueOf(System.getProperty("os.name")).toLowerCase().startsWith("windows");
    }

    public static File getJavac(){
        System.out.println("JDK_HOME = " +JDK_HOME);
        String name = "javac";
        File f = new File(JDK_HOME);
        f = new File(f, "bin");
        if (isWindows()){
            name = name + ".exe";
        }
        f = new File(f, name);
        if (!f.exists()){
            f = new File(JDK_HOME);
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



    public synchronized static void compile(String[] xxx) throws IOException, InterruptedException {

        for (String s: xxx){
            if(s.indexOf(" ") > -1){
                s = "\"" + s + "\"";
            }
            System.out.print(s + " ");
        }

//        System.out.println("");

        ProcessBuilder processBuilder = new ProcessBuilder(xxx);
        processBuilder.redirectErrorStream(false);
        final Process proc = processBuilder.start();
        showProcess(proc.getErrorStream());
        proc.waitFor();
    }

    static final String ROOT = "src/main/java/com/arise/";
    static final String RES_ROOT = "src/main/resources/";

    static Lib[] libs = new Lib[]{

//            new Lib(ROOT + "core")
//                    .named("core")
//                    .version("1.0")
//
//            ,new Lib(ROOT + "canter", ROOT + "core")
//                    .resourcesDirs(RES_ROOT + "templates")
//                    .named("canter")
//                    .version("1.0")
//
//            ,new Lib(ROOT + "cargo", ROOT + "core")
//                    .resourcesDirs(RES_ROOT + "templates")
//                    .named("cargo")
//                    .version("1.0")

            /**
             * 	Weland
            */
            new Lib(ROOT + "core", ROOT + "canter", ROOT + "astox/net", ROOT + "weland", ROOT + "cargo/management")
                    .jarLib("libs/bluecove-2.1.0.jar", "libs/jaudiotagger-2.2.3.jar")
                    .jarLib("libs/jna-3.5.2.jar")
                    .jarLib("libs/platform-3.5.2.jar")
                    .jarLib("libs/vlcj-3.0.1.jar" )
                    .jarLib("libs/commons-compress-1.19.jar" )
                    .jarLib("libs/xz-1.8.jar" )
                    .jarLib("libs/slf4j-api-1.7.2.jar" )
                    .jarLib("libs/slf4j-simple-1.7.2.jar" )
                    .jarLib("libs/webcam-capture-0.3.12.jar" )
                    .jarLib("libs/bridj-0.7.0.jar" )
                    .resourcesDirs(RES_ROOT + "templates", RES_ROOT + "weland")
                    .resourceFiles(RES_ROOT + "content-types.json")
                    .mainClass("com.arise.weland.Main")
                    .named("weland")
                    .version("1.0")
                    .pack()




            /**
            * steiner base lib
            */
            ,new Lib( "src/main/java/com/munca/in/pandemie")
                    .named("poze_concediu")
                    .mainClass("com.munca.in.pandemie.MirceaFateCaLucrezi")
                    .version("1.0")




    };


    static String BUILD_ID = "1";
    static File ROOT_DIRECTORY = new File(".");
    static String JDK_HOME = System.getProperty("java.home");

    public static void main(String[] args) throws IOException, InterruptedException {




        boolean release = args.length > 0 && "release".equals(args[0]);

       if (args.length > 1){
           ROOT_DIRECTORY = new File(args[1]);
       }

        if (args.length > 2){
            JDK_HOME = args[2];
        }
        
        System.out.println("arise builder start at " + new Date());
        System.out.println("root directory: " + ROOT_DIRECTORY.getAbsolutePath());
        System.out.println("jdk home: " + JDK_HOME);

        File buildInfoFile = new File("build-info.properties");
        Properties properties;
        try {
            properties = loadProps(buildInfoFile);
        } catch (Exception e){
            properties = new Properties();
        }
        Integer buildId  = 1;
        try {
            buildId = Integer.valueOf(properties.getProperty("BUILD_ID"));
        }catch (Exception e){
            buildId = 1;
        }
        buildId += 1;
        buildInfoFile.delete();
        properties.clear();



        BUILD_ID = String.valueOf(buildId);
        properties.put("BUILD_ID", BUILD_ID);
        properties.put("/src/main/java/com/arise/Builder.java", BUILD_ID );

        File javac = getJavac();


        File outJars = new File(ROOT_DIRECTORY.getAbsolutePath() + File.separator + "out");
        if (!outJars.exists()){
            outJars.mkdirs();
        }





        List<File> classesDirs = new ArrayList<>();

        for (Lib lib: libs){
            System.out.println(lib.name);
            File buildDir = new File(ROOT_DIRECTORY, "build");

            File outClasses = new File(buildDir, "classes");
            if (!outClasses.exists()){
                outClasses.mkdirs();
            }

            String[] params = lib.buildArgs(javac, outClasses, properties);
            compile(params);



            File inputClasses = new File(outClasses, "com");
            if (!inputClasses.exists()){
                inputClasses.mkdirs();
            }
            classesDirs.add(inputClasses);

            lib.buildJar(new File(ROOT_DIRECTORY.getAbsolutePath() + File.separator + "build/classes/com"), outJars,
                    "build/classes/", release, properties);
            inputClasses.delete();
            outClasses.delete();
            buildDir.delete();





        }
        saveProps(properties, buildInfoFile, null);


        for (File f: classesDirs){
            rmdir(f);
        }


    }


    static void rmdir(File root){
        if (root.isDirectory()){
            File inners[] = root.listFiles();
            if (inners != null && inners.length > 0){
                for (File x: inners){
                    rmdir(x);
                }
            }
         }
        root.delete();
    }


    public static Properties loadProps(File file) throws IOException {
        Properties prop = new Properties();
        InputStream in = new FileInputStream(file);
        prop.load(in);
        in.close();
        return prop;
    }

    public static void saveProps(Properties props, File f, String comment) {

        try {
            OutputStream out = new FileOutputStream( f );
            if (comment != null){
                props.store(out, comment);
            } else {
                props.store(out, "generated at "+new Date());
            }

        }
        catch (Exception e ) {
            e.printStackTrace();
        }
    }

    public static String read(java.io.File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader((file)));
        int r = reader.read();
        StringBuilder sb = new StringBuilder();
        while (r > -1){
            sb.append((char)r);
            r = reader.read();
        }
        reader.close();
        return sb.toString();
    }

    static void writeStringToFile(File f, String c) throws FileNotFoundException {
        try (PrintStream o = new PrintStream(new FileOutputStream(f))) {
            o.print(c);
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

    static String snapshotId(){
        return new SimpleDateFormat("yyyyMMdd").format(new Date());
    }



    static String getPath(File f){
//        String rootParts[] = ROOT_DIRECTORY.getAbsolutePath().split(Pattern.quote(File.separator));
//        String fileParts[] = f.getAbsolutePath().split(Pattern.quote(File.separator));
//        String res = "";
//
//
//        for (int i = rootParts.length - 1; i < fileParts.length; i++){
//                res+="/" + fileParts[i];
//        }
//
//       if (res.startsWith("/.")){
//           res = res.substring(2);
//       }



//        return  res;
        String fp = f.getAbsolutePath().replaceAll(Pattern.quote(File.separator), "/");
        String rp = ROOT_DIRECTORY.getAbsolutePath().replaceAll(Pattern.quote(File.separator), "/");

        int index = fp.indexOf(rp);
        if (index > -1){
            return fp.replace(rp, "");
        }
        return f.getAbsolutePath().replaceAll(Pattern.quote(ROOT_DIRECTORY.getAbsolutePath()), "")
                .replaceAll("\\\\", "/");
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

        String[] buildArgs(File javac, File output, Properties properties){

            for (String s: srcDirs){
                recursiveScan(s, files, true);
            }
            if (srcFiles != null){
                for (String f: srcFiles){
                    files.add(new File(ROOT_DIRECTORY, f));
                }
            }

            for (File f: files){
                String rel = getPath(f);
                properties.put(rel, BUILD_ID);
            }

            int extra = 3;
            if (jarlibs != null && !jarlibs.isEmpty()){
                extra = 5;

                for (String s: jarlibs){
                    properties.put(s, BUILD_ID);
                }
            }

            String[] r = new String[files.size() + extra];

            if (jarlibs == null || jarlibs.isEmpty()){
                r[0] = javac.getAbsolutePath();
                r[1] = "-d";
                r[2] = output.getAbsolutePath();
            } else {
                r[0] = javac.getAbsolutePath();
                r[1] = "-cp";
                r[2] = concatJars(jarlibs, File.pathSeparator);
                r[3] = "-d";
                r[4] = output.getAbsolutePath();
            }

            for (int i = 0; i < files.size(); i++){
                r[i+extra] = files.get(i).getAbsolutePath();
            }
            return r;
        }

        String concatJars(List<String> values, String delimiter){
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < values.size(); i++){
                if (i > 0){
                    sb.append(delimiter);
                }
                //test under linux for paths like roor/.:
                String cwd = ROOT_DIRECTORY.getAbsolutePath();
                if (cwd.endsWith(".")){
                    cwd = cwd.substring(0, cwd.length() - 1);
                }
                if (!cwd.endsWith(File.separator)){
                    cwd+=File.separator;
                }

                File f = new File(cwd,  values.get(i));
                sb.append(f.getAbsolutePath());
            }
            return sb.toString();
        }

        void buildJar(File inputDir, File outputDirectory, String exclude, boolean release, Properties properties) throws IOException {
            Manifest manifest = new Manifest();
            manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, vrs);
            if (mainClazz != null){
                manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, mainClazz);
            }

            String outJar = outputDirectory.getAbsolutePath() + File.separator + name + "-" + vrs  + ".jar";
            FileOutputStream jarFileOutputStream = new FileOutputStream(outJar);
            JarOutputStream target = new JarOutputStream(jarFileOutputStream, manifest);
            File[] files = inputDir.listFiles();

            if (files != null){
                for (File f: files){
                    add(f, target, exclude);
                }
            }


            if (resDirs != null){
                for (String resDir: resDirs){
                    File rd = new File(ROOT_DIRECTORY.getAbsolutePath() + File.separator + resDir);
                    recursiveScan(rd.getAbsolutePath(), resources, false);
                }

                for (File source: resources){
                    add(source, target, "src/main/resources/");
                    properties.put(getPath(source), BUILD_ID);
                }
            }
            if (resourceFiles != null){
                for (String s: resourceFiles){
                    String rw = ROOT_DIRECTORY.getAbsolutePath();
                    if (rw.endsWith(".")){
                        rw = rw.substring(0, rw.length() - 1);
                    }
                    if (!rw.endsWith(File.separator)){
                        rw += File.separator;
                    }


                    File rs = new File(rw + s);
                    if (rs.exists()) {
                        add(rs, target, "src/main/resources/");
                        properties.put(getPath(rs), BUILD_ID);
                    }
                }
            }





            target.close();
            if (jarFileOutputStream != null){
                jarFileOutputStream.close();
            }
        }

        private void add(File source, JarOutputStream target, String except) throws IOException {



    
            String name = source.getPath()
                    .replaceAll(Pattern.quote(ROOT_DIRECTORY.getAbsolutePath()), " ")
                    .replaceAll("\\\\", "/");

            if (except != null) {
                except = except.replaceAll("\\\\", "/");
                except = except.trim();
                if (!"".equals(except.trim())) {
                    String parts[] = name.split(except);
                    name = parts[1];
                }
            }

            System.out.println("\tjar entry: " +  name);
            BufferedInputStream in = null;
            FileInputStream fileInputStream = null;
            try {
                if (source.isDirectory()) {
                    if (!name.isEmpty()) {
                        if (!name.endsWith("/")) {
                            name += "/";
                        }
                        JarEntry entry = new JarEntry(name);
                        entry.setTime(source.lastModified());
                        target.putNextEntry(entry);
                        target.closeEntry();
                    }
                    for (File nestedFile : source.listFiles()) {
                        add(nestedFile, target, except);
                    }
                    return;
                }

                JarEntry entry = new JarEntry(name);
                entry.setTime(source.lastModified());
                target.putNextEntry(entry);

                fileInputStream = new FileInputStream(source);
                in = new BufferedInputStream(fileInputStream);

                byte[] buffer = new byte[1024];
                while (true) {
                    int count = in.read(buffer);
                    if (count == -1) {
                        break;
                    }
                    target.write(buffer, 0, count);
                }
                target.closeEntry();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (in != null) {
                    in.close();
                }
                if (fileInputStream != null){
                    fileInputStream.close();
                }
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
