package com.arise.core.tools;

import com.arise.core.serializers.parser.Groot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.nio.channels.FileChannel;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

import static com.arise.core.tools.CollectionUtil.*;

public class FileUtil {

    private static final Mole log = Mole.getInstance(FileUtil.class);
    private static final String ANDROID_OS_ENVIRONMENT = "android.os.Environment";
    private static final String ANDROID_OS_CONTEXT_WRAPPER = "android.content.ContextWrapper";
    private static final String ANDROID_CONTEXT = "android.content.Context";


    public static void writeStringToFile(File out, String s) {
        try {
            FileUtil.writeStringToFile(out.getAbsolutePath(), s);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    public static File findSomeTempDir(String sub){
        String property = "java.io.tmpdir";
        String tempDir = System.getProperty(property);
        File tmp = new File(tempDir + File.separator + sub);
        if (!tmp.exists()){
            tmp.mkdirs();
        }
        return tmp;
    }

    public static File findSomeTempFile(String fileName){
        return new File(findSomeTempDir("arise") + File.separator + fileName);
    }


    public static File getUploadDir(){
        File docs = new File(findDocumentsDir(), "arise-app");
        if (!docs.exists()){
            docs.mkdirs();
        }
        return docs;
    }


    public static File findDir(ContentType.Location location){

        //new File(System.getenv("EXTERNAL_STORAGE") )

        //System. getenv("EXTERNAL_SDCARD_STORAGE")
        switch (location){
            case MUSIC: return findMusicDir();
            case MOVIES: return findMoviesDir();
            case PICTURES: return findPicturesDir();
            case DOCUMENTS: return findDocumentsDir();

        }
        return findDocumentsDir();
    }

    public static File findPicturesDir(){
        File picturesDir = ReflectUtil.getStaticMethod(ANDROID_OS_ENVIRONMENT,
                "getExternalStoragePublicDirectory", String.class)
                .callFor(File.class, "Pictures");

        if (picturesDir == null){
            return new File("/home/alex/Pictures");
        }

        return picturesDir;
    }

    public static File findMusicDir(){
        File musicDir = ReflectUtil.getStaticMethod(ANDROID_OS_ENVIRONMENT,
                "getExternalStoragePublicDirectory", String.class)
                .callFor(File.class, "Music");
        if (musicDir == null || !musicDir.exists()){
            musicDir = getUserDirectory("Music");
        }
        return musicDir;
    }

    public static File findDownloadDir(){
        File musicDir = ReflectUtil.getStaticMethod(ANDROID_OS_ENVIRONMENT,
                "getExternalStoragePublicDirectory", String.class)
                .callFor(File.class, "Download");
        return musicDir;
    }

    public static File findMoviesDir(){
        File moviesDir = ReflectUtil.getStaticMethod(ANDROID_OS_ENVIRONMENT,
                "getExternalStoragePublicDirectory", String.class)
                .callFor(File.class, "Movies");
        if (moviesDir == null || !moviesDir.exists()){
            moviesDir = getUserDirectory("Videos");
        }
        if (moviesDir == null || !moviesDir.exists()){
            moviesDir = getUserDirectory("Movies");
        }
        return moviesDir;
    }



    public static File findDocumentsDir(){
        File documentsDir = ReflectUtil.getStaticMethod(ANDROID_OS_ENVIRONMENT,
                "getExternalStoragePublicDirectory", String.class)
                .callFor(File.class, "Documents");

        if (documentsDir == null || !documentsDir.exists()){
           documentsDir = getUserDirectory("Documents");
        }
        return documentsDir;
    }

    private static final String[] locations = new String[]{
            System.getenv("EXTERNAL_SDCARD_STORAGE"),   System.getenv("EXTERNAL_STORAGE"),  System.getProperty("user.home")
    };
    public static File getUserDirectory(String name){
        File externalStorage  = ReflectUtil.getStaticMethod(ANDROID_OS_ENVIRONMENT,
                "getExternalStorageDirectory")
                .callFor(File.class);

        if (externalStorage != null && externalStorage.exists()){
            File next = new File(externalStorage, "Music");
            if (!next.exists()){
                next.mkdirs();
            }
            return next;
        }
        File rootDir = null;
        for (String s: locations){
            if (StringUtil.hasText(s)) {
                rootDir = new File(s);
                if (rootDir != null && rootDir.exists()) {
                    break;
                }
            }
        }

        if (rootDir == null){
            rootDir = new File("arise-app" + File.separator + name);
            if (!rootDir.exists()){
                rootDir.mkdirs();
            }
        }
        return new File(rootDir, name);
    }

    public static File findOrCreateUserDirectory(String name){
        String usrHome = System.getProperty("user.home");
        if (!StringUtil.hasText(usrHome)){
            usrHome = "usr_dir";
        }
        if (!usrHome.endsWith(File.separator)){
            usrHome += File.separator;
        }


        File f = new File(usrHome, name);
        if (!f.exists()){
            f.mkdirs();
        }
        return f;
    }

    public static File findAppDir(){
        Object context = Util.getContext(ANDROID_OS_CONTEXT_WRAPPER);
        File result = null;
        if (context != null){
            result = ReflectUtil.getMethod(context, "getFilesDir").callFor(File.class);
        }
        if (result == null || !result.exists()){
            result = ReflectUtil.getStaticMethod(ANDROID_OS_ENVIRONMENT,
                    "getDataDirectory")
                    .callFor(File.class);

        }


        if (result == null || !result.exists()){
            result = findOrCreateUserDirectory("arise-app");
        }




        return result;
    }

     public static File getAppDirChild(String name){
            File f = new File(findAppDir(), name);
            if (!f.exists()){
                f.mkdirs();
            }
            return f;
     }

    public static InputStream findStream(String path) {
        InputStream stream = null;
        if (path.indexOf("#") > -1){
            String[] parts = path.split("#");
            String sysRoot = parts[0];
            String classPathRoot = parts[1];
            stream = findInFS(sysRoot + File.separator + classPathRoot);

            if (stream == null){
                stream = findInResOrAssets(classPathRoot);
            }
            return stream;

        }
        stream = findInFS(path);
        if (stream == null){
            stream = findInResOrAssets(path);
        }
        return stream;
    }

    private static InputStream findInFS(String path){
        File f = new File(path);
        InputStream stream = null;
        if (f.exists()){
            try {
                stream = new FileInputStream(f);
            } catch (Exception e){
                stream = null;
            }
            if (stream != null){
                return stream;
            }
        }
        return stream;
    }

    private static InputStream findInResOrAssets(String path){
        InputStream stream = StreamUtil.readResource(path);
        if (stream != null){
            return stream;
        }
        Object context = Util.getContext(ANDROID_CONTEXT);
        if (context != null){
            stream = readFromAndroidAssets(context, path);
        }

        return stream;

    }

    private static InputStream readFromAndroidAssets(Object context, String classPathRoot){
        Object assetManager = ReflectUtil.getMethod(context, "getAssets").call();
        if (assetManager == null){
            return null;
        }
        InputStream inputStream = ReflectUtil.getMethod(assetManager, "open", String.class)
                .callFor(InputStream.class, classPathRoot);
        return inputStream;
    }

    public static void writeWithBuffer(File file, String content) throws IOException {
        try (BufferedWriter out = new BufferedWriter(new FileWriter(file))) {
            out.write(content);
        }
    }

    public static void writeStringToFile(String fileName, String content) throws FileNotFoundException {
        try (PrintStream out = new PrintStream(new FileOutputStream(fileName))) {
            out.print(content);
        }
    }





    public static void copyDirectory(File sourceLocation , File targetLocation)
            throws IOException {

        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdir();
            }

            String[] children = sourceLocation.list();
            for (int i=0; i < children.length; i++) {
                copyDirectory(new File(sourceLocation, children[i]),  new File(targetLocation, children[i]));
            }
        } else {
            copyFile(sourceLocation, targetLocation);
        }
    }

    private static void copyFile(File sourceFile, File destFile)
            throws IOException {
        if (!sourceFile.exists()) {
            return;
        }
        if (!destFile.exists()) {
            destFile.createNewFile();
        }
        FileChannel source = null;
        FileChannel destination = null;
        source = new FileInputStream(sourceFile).getChannel();
        destination = new FileOutputStream(destFile).getChannel();
        if (destination != null && source != null) {
            destination.transferFrom(source, 0, source.size());
        }
        if (source != null) {
            source.close();
        }
        if (destination != null) {
            destination.close();
        }

    }


    public static String read(java.io.File file) throws IOException {
//        BufferedReader reader = new BufferedReader(new InputStreamReader (new FileInputStream(file)));

        //modified with @Adri
        BufferedReader reader = new BufferedReader(new FileReader((file)));
        String         line = null;
        StringBuilder  stringBuilder = new StringBuilder();
        String         ls = System.getProperty("line.separator");

        try {
            while((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }
            if(!stringBuilder.toString().endsWith(ls)){
                stringBuilder.append(ls);
            }

            return stringBuilder.toString();
        } finally {
            reader.close();
        }
    }



    public static void linearScan(File directory, final FileFoundHandler fileFoundHandler){
        if (!fileExists(directory)){
            return;
        }

        recursiveScan(directory, new FileFoundHandler() {
            @Override
            public void foundFile(File file) {
                fileFoundHandler.foundFile(file);
            }

            @Override
            public boolean acceptDir(File file) {
                return false;
            }
        });
    }


    private static boolean fileExists(File f){
        return (f != null && f.exists());
    }


   

    static File [] listFiles(File directory, FilenameFilter filenameFilter){
        if(!fileExists(directory)){
            return new File[]{};
        }

        File[] files;
        if (filenameFilter != null){
            files = directory.listFiles(filenameFilter);
        } else {
            files = directory.listFiles();
        }
        if (files == null){
//            log.debug("no files found inside " + directory.getAbsolutePath() );
            return new File[]{};
        }

        return files;
    }





    public static void recursiveScan(File directory, FileFoundHandler fileFoundHandler){
        recursiveScan(directory, fileFoundHandler, null);
    }
    public static void recursiveScan(File directory, FileFoundHandler fileFoundHandler, FilenameFilter filenameFilter){
        if (!fileExists(directory)){
            return;
        }



        File[] files = listFiles(directory, filenameFilter);

        if (files == null || files.length == 0){
            return;
        }

        for (int i = 0; i < files.length; i++) {
            File c = files[i];
            if (c.isDirectory()
                 && c.canRead()
                 && fileFoundHandler != null
                 && fileFoundHandler.acceptDir(c)
            ){

//                try {
//                    try {
//                        System.out.println("\n\n\n\n\nsleep " + files.length + "\n\n\n\n\n");
//                        Thread.sleep(files.length);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    walkTree(c, fileFoundHandler);
//                } catch (IOException e) {
//                    log.warn("walkTree failed for " + c.getAbsolutePath());
//                    recursiveScan(c, fileFoundHandler);
//                }
                recursiveScan(c, fileFoundHandler, filenameFilter);
            } else if (c.isFile() && fileFoundHandler != null){
//                System.out.println("found " + c.getAbsolutePath());
                fileFoundHandler.foundFile(c);
            }
        }
    }

    public static void walkTree( File directory,  final FileFoundHandler fileFoundHandler) throws IOException {
            Files.walkFileTree(Paths.get(directory.getAbsolutePath()), new SimpleFileVisitor<Path>(){
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (attrs.isDirectory() && !attrs.isSymbolicLink()){
                        recursiveScan(file.toFile(), fileFoundHandler);
                    } else {
                        fileFoundHandler.foundFile(file.toFile());
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
    }


    public static void saveProps(Properties props, File f, String comment) {

        try {
            OutputStream out = new FileOutputStream( f );
            if (comment != null){
                props.store(out, comment);
            } else {
                props.store(out, "generated at "+new Date() + " by " + FileUtil.class.getCanonicalName());
            }

        }
        catch (Exception e ) {
            e.printStackTrace();
        }
    }




    public static Properties loadProps(File file) throws IOException {
        Properties prop = new Properties();
        InputStream in = new FileInputStream(file);
        prop.load(in);
        in.close();
        return prop;
    }


    public static Properties loadPropsIfExists(File file){
        if (file.exists()){
            try {
                return loadProps(file);
            } catch (IOException e) {
                return new Properties();
            }
        }
        return new Properties();
    }

    public static <T extends Serializable> T serializableRead(File input){
        if (!input.exists()){
            return null;
        }
        FileInputStream fileIn = null;
        ObjectInputStream objectIn = null;
        T r = null;
        try {
            fileIn = new FileInputStream(input);
            objectIn = new ObjectInputStream(fileIn);
            r = (T) objectIn.readObject();
        } catch (Exception ex) {
           ;;
        }
        Util.close(objectIn);
        Util.close(fileIn);
        return r;
    }

    public static  void serializableSave(Serializable serializable, File out){
        if (null == serializable){
            return;
        }
        FileOutputStream fileOut = null;
        ObjectOutputStream objectOut = null;
        try {
            fileOut = new FileOutputStream(out);
            objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(serializable);
            objectOut.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        Util.close(fileOut);
        Util.close(objectOut);
    }


    public static File getOrCreateDirs(String bin) {
        File f = new File(bin);
        if (!f.exists()){
            f.mkdirs();
        }
        return f;
    }

    /**
     * changes detction using cache
     * @param file
     * @param loutFile
     * @return
     */
    public static boolean changesDetected(File file, File loutFile) {
        if (!loutFile.exists()){
            return true;
        }

        File propsFile = findSomeTempFile("file.cache");
        if (!propsFile.exists()){
            Properties properties = new Properties();
            saveProps(properties, propsFile, null);
            return true;
        }

        Properties cache;
        try {
            cache = loadProps(propsFile);
        } catch (IOException e) {
            return true;
        }

        if (cache == null){
            return true;
        }

        String key = file.getAbsolutePath() + ":" + "LM3102";

        if (cache.getProperty(key) != null){
            long fileLastModified = (file.lastModified());
            long cachedValue = 0;
            try {
                cachedValue = Long.valueOf(cache.getProperty(key));
            } catch (Exception e){
                return true;
            }
            if (fileLastModified == cachedValue){
                return false;
            } else {
                cache.put(key, String.valueOf(file.lastModified()));
                saveProps(cache, propsFile, null);
                return true;
            }
        } else {
            cache.put(key, String.valueOf(file.lastModified()));
            saveProps(cache, propsFile, null);
            return true;
        }


    }

    static String buildPath(String start, String ... names){


        StringBuilder sb = new StringBuilder();
        sb.append(start);

        if (!start.endsWith(File.separator)){
            sb.append(File.separator);
        }

        if (names == null || names.length == 0){
            return sb.toString();
        }

        for (int i = 0; i < names.length; i++){
            String s = String.valueOf(names[i]);
            sb.append(s);
            if (!s.endsWith(File.separator) && i < names.length - 1){
                sb.append(File.separator);
            }
        }
        return sb.toString();
    }

    public static File extendPack(File out, String ... names) {
        File r = new File(buildPath(out.getAbsolutePath(), names));
        if (!r.exists()){
            r.mkdirs();
        }
        return r;
    }

    public static File getNextFile(ContentType contentType){
        return getNextFile(contentType, null);
    }

    public static File getNextFile(ContentType contentType, String child) {
        ContentType.Location location = contentType.location();
        File storage = findDir(location);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());

        File resp;
        String path = storage.getPath() + File.separator;
        if (StringUtil.hasText(child)){
            path += child;
            resp = new File(path);
            if (!resp.exists()){
                resp.mkdir();
            }
        }


        return new File(path + File.separator + location.alias() + "_"+ timeStamp + "." + contentType.mainExtension());
    }

    public static boolean hasFiles(File d) {
        if (d == null){
            return false;
        }
        File c[] = d.listFiles();
        return c != null && c.length > 0;
    }

    public static void writeBytesToFile(byte[] bytes, File f) {
        FileOutputStream fos = null;
        try  {
            fos = new FileOutputStream(f);
            fos.write(bytes);
            //fos.close(); There is no more need for this line since you had created the instance of "fos" inside the try. And this will automatically close the OutputStream
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if (fos != null){
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public abstract static class FileFoundHandler {
        public abstract void foundFile(File file);
        public boolean acceptDir(File file){
            return !file.getName().startsWith(".");
        };
    }












}
