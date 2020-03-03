package com.arise.astox.clib;

import com.arise.cargo.management.Dependencies;
import com.arise.cargo.management.DependencyManager;
import com.arise.core.tools.SYSUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;

public class Runner {












  public static void main(String[] args) throws IOException {

      File root = DependencyManager.solve(Dependencies.MINGW_PORTABLE).uncompressed();

      //"/usr/bin/g++"
      File gpp = new File(root, "MinGW/bin/c++.exe");
      File libs = new File(root, "MinGW/lib");

      System.out.println(root);




        new CCompiler()
            .src("src/main/cpp")
            .acceptExt("cpp", "cc")

            .bin("bin")
            .output("build")

            .compiler(gpp.getAbsolutePath())
//            .includeDir("src/main/cpp/include")
//            .testIncludeDir("src/main/cpp/test/include/")

//            .exclude("operators.cpp", "ext-jni.cpp", "socket-linux.cpp")
            .srcMain("main.cpp")
//            .testMain("astox-tests.cpp")
//                .includeDir("C:\\Users\\alexandru2.stefan\\Desktop\\Dev-Cpp\\MinGW32\\lib\\gcc\\mingw32\\4.6.1\\include\\c++\\bits")
//                .includeDir("C:\\Users\\alexandru2.stefan\\Desktop\\Dev-Cpp\\MinGW32\\lib\\gcc\\mingw32\\4.6.1\\include\\c++\\mingw32\\bits")
//                .includeDir("C:\\Users\\alexandru2.stefan\\Desktop\\Dev-Cpp\\MinGW32\\lib\\gcc\\mingw32\\4.6.1\\include")
//            .testMain("server-test.cpp")

            .linkerFlags("-lpthread", "-lm")
            .extraLibPath( new File(root, "bin"))

            .compile()
            .runInTerminal("server-test");

//        String output;
//
//
//
//        System.out.println("Compile and run astox project from directory " + rootDir);
//
//
//        SYSUtils.exec(new String[]{"/usr/bin/make", "-C", rootDir, "-f", "Makefile"}, new ProcessLineReader() {
//            @Override
//            public void onStdoutLine(int line, String content) {
//                System.out.println(line + " " + content);
//            }
//        }, true);






    }
}
