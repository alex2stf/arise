package com.arise.astox.clib;

import com.arise.core.tools.models.CompleteHandler;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public class Runner {



  public static void main(String[] args) throws IOException {

      File root = new File("C:\\Applications\\codeblocks-17.12mingw-nosetup");

              //DependencyManager.solve(Dependencies.MINGW_PORTABLE).uncompressed();

      //"/usr/bin/g++"
      File gpp = new File(root, "MinGW/bin/c++.exe");
      File gcc = new File(root, "MinGW/bin/gcc.exe");
      File libs = new File(root, "MinGW/lib");


      System.out.println(gcc.getAbsolutePath());

      CCompiler compiler =
      new CCompiler()
              .acceptExt("cpp", "cc", "c")
              .sourceDirectory(new File("astox/src"))
              .includeDirectory(new File("astox/include"))
              .outputDirectory(new File("astox/out"))
              .compiler(gcc)
//              .bin("astox/bin")
              .mainSource(new File("astox/src/main.c"));

      Set<File> outs = compiler.compileObjects();
      compiler.compileMains(outs);




//        new CCompiler()
//            .src("src/main/cpp")
//            .acceptExt("cpp", "cc")
//
//            .bin("bin")
//            .output("build")
//
//            .compiler(gpp.getAbsolutePath())
////            .includeDir("src/main/cpp/include")
////            .testIncludeDir("src/main/cpp/test/include/")
//
////            .exclude("operators.cpp", "ext-jni.cpp", "socket-linux.cpp")
//            .srcMain("main.cpp")
////            .testMain("astox-tests.cpp")
////                .includeDir("C:\\Users\\alexandru2.stefan\\Desktop\\Dev-Cpp\\MinGW32\\lib\\gcc\\mingw32\\4.6.1\\include\\c++\\bits")
////                .includeDir("C:\\Users\\alexandru2.stefan\\Desktop\\Dev-Cpp\\MinGW32\\lib\\gcc\\mingw32\\4.6.1\\include\\c++\\mingw32\\bits")
////                .includeDir("C:\\Users\\alexandru2.stefan\\Desktop\\Dev-Cpp\\MinGW32\\lib\\gcc\\mingw32\\4.6.1\\include")
////            .testMain("server-test.cpp")
//
//            .linkerFlags("-lpthread", "-lm")
//            .extraLibPath( new File(root, "bin"))
//
//            .compile()
//            .runInTerminal("server-test");

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
