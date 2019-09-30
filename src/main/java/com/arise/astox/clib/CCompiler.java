package com.arise.astox.clib;

import com.arise.core.tools.FileUtil;
import com.arise.core.tools.FileUtil.FileFoundHandler;
import com.arise.core.tools.SYSUtils;
import com.arise.core.tools.SYSUtils.ProcessLineReader;
import com.arise.core.tools.StringUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class CCompiler {

    private String compilerBin;
    private File binDir;
    private File buildDir;


    private Set<String> sources = new HashSet<>();
    private Set<String> mainSources = new HashSet<>();
    private Set<String> testSources = new HashSet<>();
    private Set<String> mainTestSources = new HashSet<>();
    private Set<String> includes = new HashSet<>();
    private Set<String> testIncludes = new HashSet<>();
    private Set<String> linkerFlags = new HashSet<>();
    private Set<File> executables;
    private String[] extensions;
    private String[] excludes;

    public CCompiler compiler(String s){
        compilerBin = s;
        return this;
    }


    public CCompiler src(String s){
        sources.add(s);
        return this;
    }

    private boolean fileIsCommon(File f){
        for (String s: mainSources){
            if (f.getName().equals(s)){
                return false;
            }
        }


        for (String s: mainTestSources){
            if (f.getName().equals(s)){
                return false;
            }
        }

        for (String s: excludes){
            if (f.getName().equals(s)){
                return false;
            }
        }

        for (String s: extensions){
            if (f.getName().endsWith(s)){
                return true;
            }
        }

        return false;
    }



    private Set<File> compile(Set<String> input, final Set<String> includes){
        final Set<File> compiled = new HashSet<>();


        for (String src: input){
            FileUtil.linearScan(new File(src), new FileFoundHandler() {
                @Override
                public void foundFile(File file) {
                    if (fileIsCommon(file)){
                        String fname = file.getName().split("\\.")[0]+ ".o";
                        String locOut = buildDir.getAbsolutePath() + File.separator + fname;

                        File loutFile = new File(locOut);


                        if (FileUtil.changesDetected(file, loutFile)){

                            Set<String> compilerFlags = new HashSet<>();
                            compilerFlags.add("-c");
                            compileCPP(
                                "",
                                file.getAbsolutePath(),
                                new HashSet<File>(),
                                includes,
                                buildDir,
                                fname,
                                compilerFlags,
                                compilerBin,
                                new HashSet<String>()
                            );
                        } else {
//                            System.out.println("No changes detected for ");
                        }






                        if (loutFile.exists()){
                            compiled.add(loutFile);
                        }
                    }
                }
            });
        }

        return compiled;
    }

    public CCompiler compile() {

        executables = new TreeSet<>();
        Set<File> srcouts = compile(sources, includes);
//
        for (String s: includes){
            testIncludes.add(s);
        }
//
        Set<File> testouts = compile(testSources, testIncludes);

        Set<File> outs = new HashSet<>();
        outs.addAll(srcouts);
        for (File f: testouts){
            outs.add(f);
        }

        for (String ts: testSources){
            for (String mainTest: mainTestSources){
                String outfile = mainTest.split("\\.")[0];
                File f = compileCPP(ts, mainTest, outs, testIncludes, binDir, outfile, new HashSet<String>(), compilerBin, linkerFlags);
                executables.add(f);
            }
        }

        return this;
    }


    private static File compileCPP(String inputRoot, String fileName,
        Set<File> objects, Set<String> includes, File outputRoot, String outFileName,
        Set<String> compilerFlags,
        String compilerBin, Set<String> linkerFlags){


            File outfile = new File(outputRoot.getAbsolutePath() + File.separator + outFileName);
            File main = new File(inputRoot + File.separator + fileName);
            if (main.exists()){
                List<String> argsList = new ArrayList<>();
                argsList.add(compilerBin);
                for(File o: objects){
                    argsList.add(o.getAbsolutePath());
                }

                argsList.add(main.getAbsolutePath());

                for (String x: includes){
                    argsList.add("-I" + new File(x).getAbsolutePath());
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

                SYSUtils.exec(args, new ProcessLineReader() {
                    @Override
                    public void onStdoutLine(int line, String content) {
                        System.out.println(content);
                    }
                }, true);
            } else {
                System.out.println(main.getAbsolutePath() + " not found");
            }

            return outfile;
    }


    public CCompiler run(String s) {

        for (File f: executables){
            if (s.equals(f.getName())){
                SYSUtils.exec(new String[]{f.getAbsolutePath()}, new ProcessLineReader() {
                    @Override
                    public void onStdoutLine(int line, String content) {
                        System.out.println(content);
                    }
                }, true);
                return this;
            }
        }
        return this;
    }

    public CCompiler bin(String bin) {
        binDir = FileUtil.getOrCreateDirs(bin);
        return this;
    }

    public CCompiler output(String build) {
        buildDir = FileUtil.getOrCreateDirs(build);
        return this;
    }

    public CCompiler includeDir(String s) {
        includes.add(s);
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

    public CCompiler srcMain(String o) {
        mainSources.add(o);
        return this;
    }

    public CCompiler testMain(String s) {
        mainTestSources.add(s);
        return this;
    }

    public CCompiler acceptExt(String ... extensions) {
        this.extensions = extensions;
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
}
