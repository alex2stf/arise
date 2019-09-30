package com.arise.core.tools;

import com.arise.core.tools.StreamUtil.LineIterator;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;


public class SYSUtils {

    private static final Mole log = new Mole(SYSUtils.class);

    private SYSUtils(){

    }

    private static final OS os ;
    static {
        os = new OS(System.getProperty("os.name"), System.getProperty("os.version"), System.getProperty("os.arch"));
    }





    public static OS getOS(){
        return os;
    }


    private static boolean linuxBinExists(String bin){
        return new File(bin).exists();
    }


    public static void runInTerminal(File executable){
        if ("linux".equalsIgnoreCase(os.name)){

//x-terminal-emulator e link la default

            if (linuxBinExists("/usr/bin/mate-terminal")) {
                exec(new String[]{"mate-terminal",
                    "--working-directory=.",
//                    "--command=/bin/bash -c ls;bash",
                    "--command=/bin/bash -c "+executable.getAbsolutePath()+";bash",
                    "--window"}
                , new ProcessLineReader() {
                        @Override
                        public void onStdoutLine(int line, String content) {

                        }
                    }, true);

//                try {
//                    Process proc = new ProcessBuilder("x-terminal-emulator", "/home/alex/Dropbox/arise/bin/socket").start();
//                    Writer writer = new OutputStreamWriter(proc.getOutputStream ());;
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    System.out.println("write");
//                    writer.write("ls");
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }

//                exec("/bin/bash", "-c", "ls"
//                );
            }

        }
        System.out.println(os);
    }


    public static Result exec(String ... args){

       final StringBuilder sb = new StringBuilder();
       final StringBuilder eb = new StringBuilder();
        exec(args, new ProcessLineReader() {
            @Override
            public void onStdoutLine(int line, String content) {
                sb.append(content);
            }

            @Override
            public void onErrLine(int line, String content) {
                eb.append(content);
            }
        }, false);
        return new Result(sb.toString(), eb.toString());
    }


    public static void exec(String[] args, ProcessOutputHandler outputHandler){
        exec(args, outputHandler, false);
    }


    public static void exec(String[] args, final ProcessOutputHandler outputHandler, boolean useBuilder){
        System.out.println(StringUtil.join(args, "  "));
        Process proc = null;
        if (!useBuilder){
            Runtime rt = Runtime.getRuntime();
            try {
                proc = rt.exec(args);
            } catch (IOException e) {
               e.printStackTrace();
                proc = null;
            } finally {
                if (proc != null && outputHandler != null){
                    outputHandler.handle(proc.getInputStream());
                    outputHandler.handleErr(proc.getErrorStream());
                }
            }
        } else {
            ProcessBuilder processBuilder = new ProcessBuilder(args);
            processBuilder.redirectErrorStream(false);
            try {
                proc = processBuilder.start();
                final Process finalProc = proc;
                ThreadUtil.startThread(new Runnable() {
                    @Override
                    public void run() {
                        outputHandler.handle(finalProc.getInputStream());
                        outputHandler.handleErr(finalProc.getErrorStream());
                    }
                });

                proc.waitFor();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }


    public static Result which(String executable){
        if (isLinux()){
            return exec(new String[]{"which", executable});

        } else {
            System.out.println("METHOD NOT IMPLEMENTED FOR WINDOWS");
        }
        return null;
    }




    public static boolean isLinux() {
        return "linux".equalsIgnoreCase(System.getProperty("os.name"));
    }


    public static class bins {
        public static final String NODE_JS = which("node").nullable();
        public static final String CHROME;
        public static final String FIREFOX = which("firefox").nullable();
        public static final String ANY_BROWSER;
        static {
            String[] vars = new String[]{"chrome", "chromium-browser"};
            String c = null;
            for (String s: vars){
                Result r = which(s);
                if (r.validExe()){
                   c = r.stdout();
                   break;
                }
            }
            CHROME = c;

            if (CHROME != null){
                ANY_BROWSER = CHROME;
            }
            else if (FIREFOX != null){
                ANY_BROWSER = FIREFOX;
            }
            else {
                ANY_BROWSER = null;
            }
        }
    }


    public static class Result {
        private final String o;
        private final String e;

        public Result(String o, String e){
            this.o = o;
            this.e = e;
        }

        public boolean validExe(){
            File f = new File(o);
            return f.exists() && f.canExecute();
        }

        public String stdout() {
            return o.trim();
        }

        public String error() {
            return e.trim();
        }

        protected String nullable(){
            if (validExe()){
                return o;
            }
            return null;
        }


    }


    public static class OS {
        private final String name;
        private final String version;
        private final String arch;

        private OS(String name, String version, String arch){
            this.name = name;
            this.version = version;
            this.arch = arch;
        }

        public String getName() {
            return name;
        }

        public String getVersion() {
            return version;
        }

        public String qualifiedName(){
            return name + " " + version + " " + arch;
        }
    }

    public interface ProcessOutputHandler {
        void handle(InputStream inputStream);
        void handleErr(InputStream errorStream);
    }


    public abstract static class ProcessLineReader implements ProcessOutputHandler{


        private LineIterator stdoutIterator;
        private LineIterator errIterator;

        public ProcessLineReader(){
            final ProcessLineReader self = this;
            stdoutIterator = new LineIterator() {
                @Override
                public void onLine(int lineNo, String content) {
                    self.onStdoutLine(lineNo, content);
                }
            };

            errIterator = new LineIterator() {
                @Override
                public void onLine(int lineNo, String content) {
                    self.onErrLine(lineNo, content);
                }
            };
        }

        @Override
        public final void handle(InputStream inputStream) {
            StreamUtil.readLineByLine(inputStream, stdoutIterator);
        }

        @Override
        public final void handleErr(InputStream errorStream) {
            StreamUtil.readLineByLine(errorStream, errIterator);
        }


        public abstract void onStdoutLine(int line, String content);
        public void onErrLine(int line, String content){
            if (content != null && content.length() > 1) {
                System.err.println(line + " " + content);
            }
        }
    }
}
