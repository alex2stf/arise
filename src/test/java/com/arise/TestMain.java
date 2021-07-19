package com.arise;

import com.arise.core.tools.Assert;
import com.arise.core.tools.Mole;
import com.arise.weland.utils.URLBeautifier;

import java.io.File;

public class TestMain {

    private static final Mole log = Mole.getInstance(TestMain.class);

    public static void main(String[] args) {
        log.info("start tests");
//        URLBeautifier.beautify("https://www.youtube.com/watch?v=Uuc7Md2adSU");
//        Assert.assertEquals("https://www.youtube.com/watch?v=Uuc7Md2adSU?autoplay=1",
//                URLBeautifier.fix("https://www.youtube.com/watch?v=Uuc7Md2adSU")
//        );

        long maxMem = Runtime.getRuntime().maxMemory() / 100;
        long totalMem = Runtime.getRuntime().totalMemory() / 100;
        long freeMem = Runtime.getRuntime().freeMemory() / 100;

        for (File f: File.listRoots()){
            long totalSpace = f.getTotalSpace() / 1000000;
            long freeSpace = f.getFreeSpace() / 1000000;

            System.out.println(f + " size " + (totalSpace - freeSpace) ) ;


            long sum = 0;
            for (File s : f.listFiles()){
//                long t2 = s.getTotalSpace() / 1000000;
//                long f2 = s.getFreeSpace() / 1000000;

                System.out.println(s.getAbsolutePath() + " size " + s.length()) ;
                sum+=s.length();
            }
            System.out.println("sum = " + ( f.getUsableSpace() - sum) );
//            System.out.println(freeMem - f.length());
        }

//
//
//        System.out.println("diff = " + (maxMem - totalMem) );
    }
}
