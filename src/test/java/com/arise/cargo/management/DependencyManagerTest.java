package com.arise.cargo.management;

import com.arise.canter.Defaults;
import com.arise.core.models.Handler;
import com.arise.core.models.Unarchiver;
import com.arise.core.tools.Assert;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipNativeInitializationException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchive;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
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
//        DependencyManagerTest.test1();
        Map<String, Object> res;
//        res = DependencyManager.solve("MEDIA_INFO_CLI_WIN32");
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
        }

        System.out.println("7-Zip-JBinding library was initialized");
        Unarchiver.forName("zip").extract(new File("C:\\Users\\Tarya\\Downloads\\FirefoxPortable_101.0_English.paf.exe"), new File("firefox-portable"));
//        System.out.println(res);
    }

}