package com.arise.quixot;


//import com.arise.astox.net.models.ServiceProvider;
//import com.arise.astox.net.servers.io.IOServer;

import com.arise.core.tools.FileUtil;
import com.arise.core.tools.Mole;
import com.arise.core.tools.StreamUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Properties;

//import axl.stf.arise.tools.SYSUtils;
//import axl.stf.arise.tools.Util;

/**
 * Created by alex on 19/10/2017.
 */
public class Quixot {

    private static final Mole log = new Mole(Quixot.class);

    public static final String libs[] = new String[]{
            "000-engine-fix.js",
            "001-lib-header.js",
            "global.js",
            "003-fingerprint-getters.js",
            "event.js",
            "004-time-utils.js",
            "util.js",
            "006-canvas-gl.js",
            "007-require.js",
            "008-url-utils.js",
            "009-logger.js",
            "cookie.js",
            "011-environment.js",
            "012-caching.js",
            "013-browser.js",
            "014-unit-testing.js",
            "tween.js",
            "016-html4notification.js",
            "016-html5notification.js",
            "016-node-notification.js",
            "017-media.js",
            "018-rtc.js",
            "019-injectors.js",
            "020-http.js",
            "jqfuck.js",
            "dulcineea.js",
            "gog.js",
            "MinguiApi.js",
            "StringUtils.js",
            "Services.js",
            "gamepad.js",
            "game_engine.js",
            "quizz_engine.js",
            "mustache.js",
            "xxx-lib-end.js"
    };





    private static String fix(String s, Properties props){
        Enumeration e = props.propertyNames();

        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            try {
                s = s.replaceAll(key, props.getProperty(key));
            }catch (Exception ex){
                System.out.println("at keyword " + key);
                ex.printStackTrace();

            }

        }

        return s;
    }


    private static Properties getProps(String location, String file) throws IOException {
        Properties map = new Properties();
        InputStream stream = FileUtil.findStream(location + "#" + "quixot/lib/" + file);
        map.load(stream);
        return map;
    }




    public static final String getContent(){
        return minify(devMode(null));
    }

    public static final String minify(String text){
        text = text
                .replaceAll("\\n", " ")
                .replaceAll("\\s+", " ");

        String chrs[] = new String[]{"\\[", "\\]", "\\{", "\\}", "\\(", "\\)",
                "=",
                "\\;", "\\:", "\\!",
                "\\+", "\\-", "\\*", "\\&", "\\|"};

        for (String c: chrs){
            text = text.replaceAll( " " + c, c).replaceAll(c + " ", c).replaceAll(" " + c + " ", c);
        }
        text = text.replaceAll("\\/\\*[^/*]*(?:(?!\\/\\*|\\*\\/)[/*][^/*]*)*\\*\\/", "");
        return text + "\n";
    }

    public static final String devMode(String location){
        return devMode(location, null);
    }





    public static final String devMode(String location, BuildEvent buildEvent){
        String content = "";
        try {
            content = getFullContent(location, buildEvent);
            Properties map = null;
            map = getProps(location, "mapping.properties");
            content = fix(content, map);
            map = getProps(location, "mapping_priv.properties");
            content = fix(content, map);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return (content);
    }

    public static final String devModeStat(String location){
        String content = devMode(location);
        File f = FileUtil.findSomeTempFile("quixot-stat");
        Integer last = null;
        if (f.exists()){
            try {
                last = Integer.valueOf( FileUtil.read(f).trim());
            } catch (Exception e) {
                last = null;
            }
        }
        if (last != null){
            if (last > content.length()){
                log.info(last + "] DECREASED WITH [" + (last - content.length()) + "]");
            }
            else if (last == content.length()){
                log.info(last + "] BUT WHY???");
            }
            else {
                log.warn(last + "] INCREASED WITH [" + (content.length() - last) + "] YOU FUCKED IT AGAIN!!!");
            }
        }

        try {
            FileUtil.writeWithBuffer(f, String.valueOf(content.length()));
        } catch (IOException e) {
            log.error(e);
        }

        return content;
    }


    private static String readInputStreamQuixot(InputStream inputStream, String module) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder out = new StringBuilder();
        String line;
        int lineIndex = 0;
        while ((line = reader.readLine()) != null) {
            out.append(putLineAndModule(line, module, lineIndex) + "\n");
            lineIndex++;
        }
        reader.close();

        return out.toString();
    }


    private static String readFileQuixot(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader (new FileInputStream(file)));
        String         line = null;
        StringBuilder  stringBuilder = new StringBuilder();
        String         ls = System.getProperty("line.separator");
        int lI = 0;
        try {
            while((line = reader.readLine()) != null) {
                stringBuilder.append(putLineAndModule(line, file.getName(), lI));
                stringBuilder.append(ls);
                lI++;
            }
            if(!stringBuilder.toString().endsWith(ls)){
                stringBuilder.append(ls);
            }

            return stringBuilder.toString();
        } finally {
            reader.close();
        }
    }


    private static final String putLineAndModule(String input, String file, int line){
        return input.replaceAll("__LINE__", String.valueOf(line + 1))
                .replaceAll("__MODULE__", file.replaceAll("\\.js", ""));
    }


    public static String getFullContent(String inputDir, BuildEvent buildEvent) throws IOException {
        StringBuilder s = new StringBuilder();
        s.append("var quixot = (function(){ ");
        for (int i = 0; i < libs.length; i++){
            if (i == libs.length - 1 && buildEvent != null){
                s.append(buildEvent.beforeClose());
            }

            if (inputDir != null){
                File f = new File(inputDir + File.separator + libs[i]);
                s.append(readFileQuixot(f)).append(" ");
            } else {
                InputStream in = FileUtil.findStream("quixot/lib/" + libs[i]);
                try {
                    s.append(readInputStreamQuixot(in, libs[i]));
                }catch (Exception e){
//                    throw new RuntimeException("failed to read " + libs[i]);
                }

            }


        }

        s.append("})(); if(typeof module != 'undefined') { module.exports = quixot;} \n");
        return s.toString();
    }

    public static String devWithTests(String location, String testLocation, String outLocation) {
        String content = devModeStat(location);

        if(outLocation == null){
            log.error("no out location defined, ignoring tests");
            return content;
        }
        File out = new File(outLocation);
        if(!out.isDirectory()){
            log.error(out.getAbsolutePath() + " should exist and should be a directory");
            return content;
        }

        File dist = new File(out.getAbsolutePath() + File.separator + "dist");
        if (!dist.exists()){
            dist.mkdirs();
        }
        File tests = new File(out.getAbsolutePath() + File.separator + "tests");

        try {
            FileUtil.copyDirectory(new File(testLocation), tests);
            log.info("copy [" + testLocation + "] into [" + tests.getAbsolutePath() + "]");
        } catch (IOException e) {
           log.error("failed to copy required resources from [" + testLocation + "]");
           return content;
        }

        String outDist = dist.getAbsolutePath() + File.separator + "quixot.js";
        try {
            FileUtil.writeStringToFile(outDist, content);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


//        File testFile = new File(tests.getAbsolutePath() + File.separator + "quixot-tests.js");

//        SYSUtils.Result result = SYSUtils.exec(SYSUtils.bins.nodeJs(), testFile.getAbsolutePath());
//        if (result.error() != null && !result.error().isEmpty()){
//            log.error("FAILED AT NODEJS TESTS", new RuntimeException(result.error()));
//        }else {
//            log.info("NodeJs tests passed succesfully");
//        }


//        Method jsExec = null;
//        try {
//           jsExec =  Class.forName("axl.stf.eminence.tools.JSExec").getDeclaredMethod("exec", String[].class);
//        }catch (Exception ex){
//           jsExec = null;
//           log.warn("failed to load nashorn engine");
//        } finally {
//            if (jsExec != null){
//                try {
//                    jsExec.invoke(null, new Object[] { new String[]{ content, FileUtil.read(testFile)} });
//                    log.info("Nashorn vanilla tests passed succesfully");
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }

        Method clojureCompile = null;
        try {
            clojureCompile =  Class.forName("axl.stf.eminence.tools.ClojureCompiler").getDeclaredMethod("compile", String.class);
        } catch (Exception e) {
            clojureCompile = null;
            log.warn("failed to load nashorn engine");
        } finally {
            if (clojureCompile != null){
                try {
                    clojureCompile.invoke(null, content);
                    log.info("Clojure compilation success");
                } catch (Exception e) {
                    log.warn("Clojure compilation invocation failed");
                }
            }
        }

        return content;
    }

    public static String getTemplate(ID id){
        if (id.equals(ID.SERVICE_TEMPLATE_MUSTACHE)){
            return StreamUtil.toString(
                    Quixot.class.getClassLoader().getResourceAsStream("quixot/lib/_service_template.txt")
            );
        }
        return null;
    }


    private static void startServer(String root, String location, String testLocation, String outLocation, String port){
        File into = new File(root);
        if (!into.exists() || !into.isDirectory()){
            log.error(into.getAbsolutePath() + " directory not found");
            System.exit(-1);
        }

        try {
            System.setProperty("quick.http.enable.auto.ext", "true");
            System.setProperty("quick.http.enable.props", "true");
            System.setProperty("quick.http.enable.quixot", "true");
            if (location != null){
                log.info("setting dev input location to [" + location);
                log.info("setting dev tests location to [" + testLocation);
                log.info("setting out dev directory to  [" + outLocation);
                System.setProperty("quick.http.quixot.dev", location);
                System.setProperty("quick.http.quixot.dev.tests", testLocation);
                System.setProperty("quick.http.quixot.dev.out.dir", outLocation);
            }

//            IOServer ioHttp = new IOServer();
//            ioHttp.setLibName("quixot-dev-svr");
//            ServiceProvider serviceProvider = new ServiceProvider(ioHttp.getLibName());
//            serviceProvider.add(new QuixotService(), "/quixot");
//            ioHttp.setServiceProvider(serviceProvider);
//            ioHttp.setPort(Integer.valueOf(port));
//            ioHttp.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public static void main(String[] args) {
        if ("dev-stat".equals(args[0]) && new File(args[1]).exists()){
            String content = devModeStat(args[1]);
            try {
                FileUtil.writeStringToFile(args[2], content);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        else if("dev-server".equals(args[0])){
            String portNr = null;
            if (args.length > 4){
                if (args.length > 5){
                    portNr = args[5];
                }
                startServer(args[1], args[2], args[3], args[4], portNr);
            } else {
                log.info("dev-server: usage <work-dir> <input-libs> <test-dir> <port>(optional)");
                System.exit(-1);
            }
        }
        else if("compile".equals(args[0])){
            String content = devMode(args[1]);
            try {
                FileUtil.writeStringToFile(new File(args[2]).getAbsolutePath(), content);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

    }


    public interface BuildEvent {
        String beforeClose();
    }

    public enum ID {
        SERVICE_TEMPLATE_MUSTACHE
    }
}
