package com.arise.cargo.clib;

import com.arise.core.exceptions.LogicalException;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.FileUtil.FileFoundHandler;
import com.arise.core.tools.SYSUtils;
import com.arise.core.tools.StringUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CCompiler {

    private File compilerBin;
    private File binDir;
    private File outDir;


    private Set<File> sources = new HashSet<>();
    private Set<File> mainSources = new HashSet<>();
    private Set<String> testSources = new HashSet<>();
    private Set<String> mainTestSources = new HashSet<>();
    private Set<File> includedDirectories = new HashSet<>();
    private Set<String> testIncludes = new HashSet<>();
    private Set<String> linkerFlags = new HashSet<>();
    private Set<File> libsPaths = new HashSet<>();
    private Set<File> executables = new HashSet<>();
    private Set<String> extensions = new HashSet<>();
    private String[] excludes = new String[]{};

    public CCompiler compiler(File compiler){
        compilerBin = compiler;
        return this;
    }


    public CCompiler sourceDirectory(File file){
        if (file.isDirectory()){
            FileUtil.recursiveScan(file, new FileFoundHandler() {
                @Override
                public void foundFile(File f) {
                    for (String s: extensions){
                        if (f.getName().endsWith(s)){
                            sources.add(f);
                            break;
                        }
                    }
                }
            });
        }

        else if (file.isFile()){
            sources.add(file);
            return this;
        }
        else {
            //TODO logical codes
            throw new LogicalException("WTF");
        }
        return this;
    }

//    private boolean fileIsCommon(File f){
//        for (String s: mainSources){
//            if (f.getName().equals(s)){
//                return false;
//            }
//        }
//
//
//        for (String s: mainTestSources){
//            if (f.getName().equals(s)){
//                return false;
//            }
//        }
//
//        for (String s: excludes){
//            if (f.getName().equals(s)){
//                return false;
//            }
//        }
//
//        for (String s: extensions){
//            if (f.getName().endsWith(s)){
//                return true;
//            }
//        }
//
//        return false;
//    }



//    private File compileOutput(File file, final Set<String> includes){
//        final Set<File> compiled = new HashSet<>();
//
//
//        for (String src: input){
//            FileUtil.linearScan(new File(src), new FileFoundHandler() {
//                @Override
//                public void foundFile(File file) {
//                    if (fileIsCommon(file)){
//                        String fname = file.getName().split("\\.")[0]+ ".o";
//                        String locOut = outDir.getAbsolutePath() + File.separator + fname;
//
//                        File loutFile = new File(locOut);
//
//
//                        Set<String> compilerFlags = new HashSet<>();
//                        compilerFlags.add("-c");
//                        compileCPP(
//                                "",
//                                file.getAbsolutePath(),
//                                new HashSet<File>(),
//                                includes,
//                                outDir,
//                                fname,
//                                compilerFlags,
//                                compilerBin.getAbsolutePath(),
//                                libsPaths,
//                                new HashSet<String>()
//                        );
//
//
//
//
//
//                        if (loutFile.exists()){
//                            compiled.add(loutFile);
//                        }
//                    }
//                }
//            });
//        }
//
//        return compiled;
//    }

    private boolean isPartOf(File f, Set<File> files){
        for (File x: files){
            if (f.getAbsolutePath().equals(x.getAbsolutePath())){
                return true;
            }
        }
        return false;
    }

    public Set<File> compileObjects() {

        Set<File> res = new HashSet<>();

        for(File src: sources){
            if (!isPartOf(src, mainSources)) {

                File out = new File(outDir, src.getName().split("\\.")[0] + ".o");

                List<String> arguments = new ArrayList<>();
                //gcc
                arguments.add(compilerBin.getAbsolutePath());

                //-I
                for (File i: includedDirectories){
                    arguments.add("-I" + i.getAbsolutePath());

                }


                arguments.add("-c");
                arguments.add(src.getAbsolutePath());





                arguments.add("-o");
                arguments.add(out.getAbsolutePath());

//                System.out.println("[ " + StringUtil.join(arguments, " "));

                String [] args = new String[arguments.size()];
                arguments.toArray(args);
                SYSUtils.exec(args);

                res.add(out);
            }
        }

       return res;


    }

    private void ccompile(Set<String> mainSources) {
        System.out.println(StringUtil.join(mainSources, ","));
    }


    private static File compileCPP(String inputRoot,
                                   String fileName,
                                   Set<File> objects,
                                   Set<String> includes,
                                   File outputRoot,
                                   String outFileName,
                                   Set<String> compilerFlags,
                                   String compilerBin,
                                   Set<File> libraryPaths,
                                   Set<String> linkerFlags){


            File outfile = new File(outputRoot.getAbsolutePath() + File.separator + outFileName);
            File main = new File(inputRoot + File.separator + fileName);
            if (main.exists()){
                List<String> argsList = new ArrayList<>();
                argsList.add(compilerBin);
//                argsList.add("-U__STRICT_ANSI__");
                argsList.add("-std=c++0x");


                for(File o: objects){
                    argsList.add(o.getAbsolutePath());
                }

                argsList.add(main.getAbsolutePath());

                for (String x: includes){
                    argsList.add("-I" + new File(x).getAbsolutePath());
                }

                for (File x: libraryPaths){
                    argsList.add("-L" + x.getAbsolutePath());
                }

                for (String s: compilerFlags){
                    argsList.add(s);
                }

                argsList.add("-o");

                argsList.add(outfile.getAbsolutePath());


                for (String s: linkerFlags){
                    argsList.add(s);
                }


                String[] args = new String[argsList.size()];
                args = argsList.toArray(args);

                System.out.println(StringUtil.join(args, " "));



                SYSUtils.exec(args);
            } else {
                System.out.println(main.getAbsolutePath() + " not found");
            }

            return outfile;
    }


    public CCompiler run(String s) {

        for (File f: executables){
            if (s.equals(f.getName())){
//                SYSUtils.exec(new String[]{f.getAbsolutePath()}, new ProcessLineReader() {
//                    @Override
//                    public void onStdoutLine(int line, String content) {
//                        System.out.println(content);
//                    }
//                }, true);
                return this;
            }
        }
        return this;
    }

//    /**
//     * @deprecated use file
//     * @param bin
//     * @return
//     */
//    @Deprecated
//    public CCompiler bin(String bin) {
//        binDir = FileUtil.getOrCreateDirs(bin);
//        return this;
//    }


    public CCompiler outputDirectory(File out) {
        if (!out.exists()){
            out.mkdirs();
        }
        outDir = out;
        return this;
    }

    public CCompiler includeDirectory(File s) {
        includedDirectories.add(s);
        return this;
    }

    public CCompiler testIncludeDir(String s) {
        testIncludes.add(s);
        return this;
    }

    public CCompiler testSrc(String s) {
        testSources.add(s);
        return this;
    }

    public CCompiler mainSource(File src) {
        mainSources.add(src);
        return this;
    }

    public CCompiler testMain(String s) {
        mainTestSources.add(s);
        return this;
    }

    public CCompiler acceptExt(String ... ext) {
       for (String s: ext){
           extensions.add(s);
       }
        return this;
    }

    public CCompiler exclude(String ... args) {

        this.excludes = args;
        return this;
    }

    public CCompiler linkerFlags(String ... args) {
        for (String s: args){
            linkerFlags.add(s);
        }
        return this;
    }

    public CCompiler runInTerminal(String s) {
        for (File f: executables){
            if (s.equals(f.getName())){
                SYSUtils.runInTerminal(f);
                return this;
            }
        }
        return this;

    }

    public CCompiler extraLibPath(File path) {
        libsPaths.add(path);
        return this;
    }

    public void compileMains(Set<File> objects) {
        for (File f: mainSources){
            List<String> args = new ArrayList<>();
            args.add(compilerBin.getAbsolutePath());
            args.add(f.getAbsolutePath());

            for (File i: includedDirectories){
                args.add("-I" + i.getAbsolutePath());

            }
            for (File x: objects){
                args.add(x.getAbsolutePath());
            }

            args.add("-o");
            args.add("main");

            String[] a = new String[args.size()];
            a = args.toArray(a);

            System.out.println(StringUtil.join(a, " "));
            SYSUtils.exec(a);
        }
    }
}
