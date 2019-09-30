package com.arise.astox.clib;

public class Runner {














  public static void main(String[] args) {



//    System.out.println( reverseInParentheses("foo(bar)baz(blim(baz))"));
//    System.out.println( reverseInParentheses("foo(bar(baz))blim")); //foobazrabblim
        System.exit(0);
        new CCompiler()
            .src("src/main/cpp/src")
            .testSrc("src/main/cpp/test/src")

            .acceptExt("cpp", "cc")

            .bin("bin")
            .output("build")

            .compiler("/usr/bin/g++")
            .includeDir("src/main/cpp/include")
            .testIncludeDir("src/main/cpp/test/include/")

            .exclude("operators.cpp", "ext-jni.cpp", "socket-linux.cpp")
            .srcMain(null)
//            .testMain("astox-tests.cpp")
            .testMain("server-test.cpp")

            .linkerFlags("-lpthread", "-lm")

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
