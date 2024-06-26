package com.arise.core.tools;

import com.arise.core.exceptions.LogicalException;
import com.arise.core.models.Handler;

import java.io.*;
import java.util.*;

import static com.arise.core.tools.FileUtil.fileExists;
import static com.arise.core.tools.StringUtil.*;


public class SYSUtils {

    private static final Mole log = new Mole(SYSUtils.class);

    private SYSUtils(){

    }

    private static final OS os ;
    static {
        os = new OS(System.getProperty("os.name"), System.getProperty("os.version"), System.getProperty("os.arch"));
    }


    public static String getDeviceId(){
        return (os.qualifiedName() + getDeviceName()).replaceAll("\\s+","");
    }

    static  String _deviceName = null;


    //TODO non duplicate keys

    private static void append(StringBuilder sb, String part){
        String ext = sb.toString();
        String parts[] = part.split(" ");
        for (String s :parts){

            if(ext.indexOf(s) < 0){
                sb.append(s).append(" ");
            }
        }
    }

    public static String getDeviceName() {
        return StringUtil.sanitizeAppId(getDeviceDetailsString());
    }

    public static String getDeviceDetailsString() {

        if(_deviceName != null){
            return _deviceName;
        }
        Map<String, String> env = System.getenv();
        StringBuilder sb = new StringBuilder();

        if (env.containsKey("COMPUTERNAME")) {
            sb.append(env.get("COMPUTERNAME")).append(" ");
        }
        if (env.containsKey("HOSTNAME")) {
            sb.append(env.get("HOSTNAME")).append(" ");
        }

        if (env.containsKey("USER")){
            sb.append(env.get("USER")).append(" ");
        }

        if (env.containsKey("DESKTOP_SESSION")){
            sb.append(env.get("DESKTOP_SESSION")).append(" ");
        }

        if (env.containsKey("XDG_SESSION_TYPE")){
            sb.append(env.get("XDG_SESSION_TYPE")).append(" ");
        }

        if (env.containsKey("XDG_SEAT")){
            sb.append(env.get("XDG_SEAT")).append(" ");
        }

        if (ReflectUtil.classExists("android.os.Build")){

            Set<String> strings = new HashSet<>();

            Object model = ReflectUtil.readStaticMember(ReflectUtil.getClassByName("android.os.Build"), "MODEL");
            Object manuf = ReflectUtil.readStaticMember(ReflectUtil.getClassByName("android.os.Build"), "MANUFACTURER");
            Object brand = ReflectUtil.readStaticMember(ReflectUtil.getClassByName("android.os.Build"), "BRAND");

            strings.add((model != null ? String.valueOf(model) : ""));
            strings.add((manuf != null ? String.valueOf(manuf) : ""));
            strings.add((brand != null ? String.valueOf(brand) : ""));
            sb.append(StringUtil.join(strings, ""));
        }

        Properties p = SYSUtils.getLinuxDetails();
        if(p!= null){
            if(p.containsKey("ID")){
                append(sb, p.getProperty("ID"));
            }

            if(p.containsKey("VERSION_CODENAME")){
                append(sb, p.getProperty("VERSION_CODENAME"));
            }

            if(p.containsKey("VERSION")){
                append(sb, p.getProperty("VERSION"));
            }
        }

        return sb.toString();
    }



    public static OS getOS(){
        return os;
    }


    private static boolean linuxBinExists(String bin){
        return new File(bin).exists();
    }


    public static void runInTerminal(File executable){
        if ("linux".equalsIgnoreCase(os.name)) {

        }
//        System.out.println(os);
    }

    public static Result exec(List<String> list){
        String args[] = new String[list.size()];
        list.toArray(args);
        return exec(args);
    }

    @Deprecated
    /**
     * use PROCESS_EXEC instead
     * @param args
     * @return
     */
    public static Result exec(String ... args){
       System.out.println("---!" + StringUtil.join(args, " "));

       final StringBuilder sb = new StringBuilder();
       final StringBuilder eb = new StringBuilder();
        exec(args, new ProcessLineReader() {
            @Override
            public void onStdoutLine(int line, String content) {
                System.out.println("   !" + content);
                sb.append(content);
            }

            @Override
            public void onErrLine(int line, String content) {
                System.out.println("   !" + content);
                eb.append(content);
            }
        }, true, false);
        return new Result(args, sb.toString(), eb.toString());
    }



    /**
     * use DefaultCommands#PROCESS_EXEC
     * @param args
     * @param outputHandler
     * @param useBuilder
     * @param async
     */
    @Deprecated
    public static Process exec(String[] args, final ProcessOutputHandler outputHandler,
                            boolean useBuilder,
                            boolean async){
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
            processBuilder.redirectErrorStream(true);
//            processBuilder.directory("work-dir")
//            String path = processBuilder.environment().get("PATH");
//            path+=";C:\\Users\\alexandru2.stefan\\arise-app\\dpmngmt\\out\\mingw-portable_win32\\MinGW-master\\MinGW\\bin";
//            path+=";C:\\Users\\alexandru2.stefan\\arise-app\\dpmngmt\\out\\mingw-portable_win32\\MinGW-master\\MinGW\\msys\\1.0\\bin";
//            path+=";C:\\Users\\alexandru2.stefan\\arise-app\\dpmngmt\\out\\mingw-portable_win32\\MinGW-master\\MinGW\\dll";
//            processBuilder.environment().put("PATH", path);
//            processBuilder.environment().put("Path", path);
            try {
                proc = processBuilder.start();
                if (outputHandler != null){
                    outputHandler.handleErr(proc.getErrorStream());
                    outputHandler.handle(proc.getInputStream());
                }

                if (!async) {
                    proc.waitFor();
                }
            } catch (Exception e) {
                throw new LogicalException("Cannot execute " + StringUtil.join(args, " "), e);
            }

        }

        if (outputHandler != null){
            outputHandler.handle(proc);
        }

        if(!async){
            proc.destroy();
        }

        return proc;
    }





    public static boolean isAndroid(){
        return ReflectUtil.classExists( "android.util.Log");
    }

    private static String osName(){
        return String.valueOf(System.getProperty("os.name"));
    }


    public static boolean isWindows() {
        return (osName().toLowerCase().indexOf("win") >= 0);
    }

    public static boolean isMac() {
        return (osName().toLowerCase().indexOf("mac") >= 0);

    }

    public static boolean isUnix() {
        String osl = osName().toLowerCase();
        return (osl.indexOf("nix") >= 0 || osl.indexOf("nux") >= 0 || osl.indexOf("aix") > 0 );

    }

    public static boolean isSolaris() {
        return (osName().toLowerCase().indexOf("sunos") >= 0);
    }


    public static boolean isRaspberryPI(){
        Properties p = getLinuxDetails();
        if(p == null){
            return false;
        }
        Enumeration<String> enums = (Enumeration<String>) p.propertyNames();
        while (enums.hasMoreElements()) {
            String key = enums.nextElement();
            String value = p.getProperty(key);
            if(value.indexOf("raspbian") > -1){
                return true;
            }
        }
        return false;
    }








    public static boolean isLinux() {
        return "linux".equalsIgnoreCase(System.getProperty("os.name"));
    }


    static Properties ldet = null;

    public static Properties getLinuxDetails(){
        if (ldet != null){
            return ldet;
        }
        File f = new File("/etc/os-release");
        Properties props = new Properties();
        if(f.exists()) {
            try {
                props = FileUtil.loadProps(f);
            } catch (Exception e) {
                ;;
            }
        }

        final Properties fpr = props;
        File rel = new File("/usr/bin/lsb_release");
        if (rel.exists() ){
            try {
                SYSUtils.exec(new String[]{rel.getAbsolutePath(), "-a"}, new ProcessLineReader() {
                    @Override
                    public void onStdoutLine(int x, String cnt) {
                        if (cnt.indexOf(":") > -1) {
                            int dx = cnt.indexOf(":");
                            String key = fix(cnt.substring(0, dx), "_");
                            String val = fix(cnt.substring(dx + 1), "");
                            fpr.put("lsb_" + key, val);
                            System.out.println(key + " = " + val);
                        }
                    }
                }, true, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        ldet = fpr;

        return fpr;
    }

    public static String fix(String s, String w){
        String c = s.trim().toLowerCase().replaceAll("\\s+", w);
        if (c.startsWith("\"") && c.endsWith("\"")){
            return c.substring(1, c.length() -1);
        }
        return c;
    }



    public static boolean open(String path) {
        File f = new File(path);
        ContentType contentType = ContentType.search(f);
        for (String s: contentType.processes()){
            File proc = new File(s);
            if (proc.exists() && proc.canExecute()){
                exec(new String[]{proc.getAbsolutePath(), path}, null, true, true);
                return true;
            }
        }
        return false;
    }

    private static volatile Boolean _is_32_bits = null;


    public static boolean is64Bits(){
        String sadm = System.getProperty("sun.arch.data.model");
        if (hasText(sadm)){
            try {
                Integer i = Integer.valueOf(sadm.trim());
                return 64 == i;
            }catch (Exception e){
                return false;
            }
        }
        return false;
    }

    public static boolean is32Bits() {

        if(_is_32_bits != null){
            return _is_32_bits;
        }

        if(is64Bits()){
            _is_32_bits = false;
            return _is_32_bits;
        }


        if(fileExists("/lib/systemd/systemd") && isUnix()){
            Result r = exec("file", "/lib/systemd/systemd");
            if (hasText(r.stdout())){
                boolean is32 = r.stdout().indexOf("32-bit") > -1;
                if(is32) {
                    _is_32_bits = is32;
                }
            }
        }
        return _is_32_bits;
    }


    public static class Result {
        private final String args[];
        private final String o;
        private final String e;

        public Result(String args[], String o, String e){
            this.o = o;
            this.e = e;
            this.args = args;
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


        public String toJson(){

            StringBuilder sb = new StringBuilder();
            sb.append("{").append("\"validExe\":").append(validExe()).append(",")
                    .append("\"stdout\":").append("\"" + jsonEscape(o) + "\"").append(",")
                    .append("\"err\":").append("\"" + jsonEscape(e) + "\"").append(",")
                    .append("\"args\":[").append(StringUtil.join(args, ",", new StringUtil.JoinIterator<String>() {
                        @Override
                        public String toString(String value) {
                            return "\"" + jsonEscape(value) + "\"";
                        }
                    })).append("]}");
            return sb.toString();
        }

    }


    public static class OS {
        private final String name;
        private final String version;
        private final String arch;

        public OS(String name, String version, String arch){
            this.name = name;
            this.version = version;
            this.arch = arch;
        }

        public String getArch() {
            return arch;
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

        @Override
        public String toString() {
            return "{\"name\":" + jsonVal(name) +
                    ",\"version\":" + jsonVal(version) +
                    ",\"arch\":" + jsonVal(arch) + "}";
        }
    }

    public static abstract class ProcessOutputHandler implements Handler<Process> {
        public abstract void handle(InputStream inputStream);
        public abstract void handleErr(InputStream errorStream);

        public void handle(Process process){

        };
    }


    public abstract static class ProcessLineReader extends ProcessOutputHandler {



        public ProcessLineReader(){
        }

        @Override
        public final void handle(InputStream inputStream) {
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    onStdoutLine(0, line);
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }



        public void onStdoutLine(int line, String content) {
//            System.out.println(content);
        }


        public void onErrLine(int line, String content) {
//            System.out.println(content);
        }

        @Override
        public final void handleErr(InputStream errorStream) {
            BufferedReader in = new BufferedReader(new InputStreamReader(errorStream));
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    onErrLine(0, line);
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }



    }
}
