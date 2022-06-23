package com.arise.cargo.management;

import com.arise.core.models.Handler;

import java.io.File;
import java.io.IOException;
import java.net.URLClassLoader;
import java.util.Map;

import static com.arise.cargo.management.DependencyManager.importDependencyRules;
import static com.arise.cargo.management.DependencyManager.withBinary;
import static com.arise.cargo.management.DependencyManager.withJar;
import static com.arise.core.tools.Assert.assertEquals;
import static com.arise.core.tools.Assert.assertFailed;
import static com.arise.core.tools.Assert.assertNotNull;
import static com.arise.core.tools.Assert.assertTrue;


public class DependencyManagerTest {


    public static void test1() {

        Map<String, Object>  res = DependencyManager.solve("JAVAZOOM_JLAYER_101");
        assertTrue((res.get("jar-location") + "").endsWith("jlayer-1.0.1.jar"));
        withJar("JAVAZOOM_JLAYER_101", new Handler<URLClassLoader>() {
            @Override
            public void handle(URLClassLoader classLoader) {
                try {
                    Class c = Class.forName("javazoom.jl.player.Player", true, classLoader);
                    assertNotNull(c);
                } catch (ClassNotFoundException e) {
                    assertFailed(e);
                }
            }
        });


        withBinary("MEDIA_INFO_CLI_WIN32", new Handler<File>() {
            @Override
            public void handle(File file) {
                assertEquals("MediaInfo.exe", file.getName());
            }
        });



    }

    public static void main(String[] args) throws IOException {
//

        importDependencyRules("_cargo_/dependencies.json");
System.setProperty("dependencies.forced.profiles", "ubuntu");
        DependencyManager.solve("FS_WEBCAM");
//        System.out.println(
//                StringUtil.join(
//                        DependencyManager.getProfiles(), "\n"
//                )
//        );
//
//        DependencyManager.withSdkTool("MIN_CCOMPILER", "make", new Handler<File>() {
//            @Override
//            public void handle(File file) {
//                System.out.println(file);
//            }
//        });
////        DependencyManagerTest.test1();
//        Map<String, Object> res;
//        res = DependencyManager.solve("MIN_CCOMPILER");
//        res = DependencyManager.solve("MNT_CLI_WIN_32");
//        res = DependencyManager.solve("MP3_PLAYER");
//        res = DependencyManager.solve("NWJS");
//
//        DependencyManager.withBinary("NWJS", new Handler<File>() {
//            @Override
//            public void handle(File file) {
//                System.out.println(file);
//                Defaults.PROCESS_EXEC.execute(file.getAbsolutePath(), "https://live.rockfm.ro:8443/rockfm.aacp");
//            }
//        });

//        res = DependencyManager.solve("MINGW");

/*
        try {
            SevenZip.initSevenZipFromPlatformJAR();
        } catch (SevenZipNativeInitializationException e) {
            e.printStackTrace();
        }

        IInArchive inArchive = SevenZip.openInArchive(null, // Choose format automatically
                new RandomAccessFileInStream(
                        new RandomAccessFile("C:\\Users\\Tarya\\Downloads\\FirefoxPortable_101.0_English.paf.exe", "r")
                )
        );
        // Getting simple interface of the archive inArchive
        ISimpleInArchive simpleInArchive = inArchive.getSimpleInterface();

        System.out.println("   Size   | Compr.Sz. | Filename");
        System.out.println("----------+-----------+---------");

        for (ISimpleInArchiveItem item : simpleInArchive.getArchiveItems()) {
//            item.extractSlow()
            System.out.println(String.format("%9s | %9s | %s", //
                    item.getSize(),
                    item.getPackedSize(),
                    item.getPath()));
            final File out = new File("firefox-portable",  item.getPath());
            if (item.isFolder()){
                out.mkdirs();
                continue;
            }

            File parent = out.getParentFile();
            if (parent != null && !parent.exists()){
                parent.mkdirs();
            }


            if (!out.exists() && out.length() != item.getSize() && !item.isFolder()) {
                final int[] hash = new int[] { 0 };
                ExtractOperationResult result;
                final long[] sizeArray = new long[1];
                result = item.extractSlow(new ISequentialOutStream() {
                    @Override
                    public int write(byte[] bytes) throws SevenZipException {
                        FileUtil.writeBytesToFile(bytes, out);
                        hash[0] ^= Arrays.hashCode(bytes); // Consume data
                        sizeArray[0] += bytes.length;
                        return bytes.length; // Return amount of consumed data
                    }
                });
                if (result == ExtractOperationResult.OK) {
                    System.out.println(String.format("%9X | %10s | %s",
                            hash[0], sizeArray[0], item.getPath()));
                } else {
                    System.err.println("Error extracting item: " + result);
                }
            }
        }

//        System.out.println("7-Zip-JBinding library was initialized");
        Archiver.forName("7zip").extract(new File("C:\\Users\\Tarya\\Downloads\\FirefoxPortable_101.0_English.paf.exe"), new File("firefox-portable"));
//        System.out.println(res);

//        res = DependencyManager.solve("7-ZIP");
//        DependencyManager.withBinary("7-ZIP", new Handler<File>() {
//            @Override
//            public void handle(File file) {
//                System.out.println(file);
//
//
//            }
//        });
//        System.out.println(res);

 */
    }

}