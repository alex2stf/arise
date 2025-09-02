package com.arise.core;

import com.arise.core.tools.FileUtil;
import com.arise.core.tools.Mole;
import com.arise.core.tools.SYSUtils;
import com.arise.core.tools.StringUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class AppSettings {


    private static final Mole log = Mole.getInstance(AppSettings.class);

    private static final Properties applicationProperties;






    static {
        File expect;

        if (SYSUtils.isAndroid()){
            File dir = new File(FileUtil.findDocumentsDir(), "laynee-config");
            if (!dir.exists()){
                dir.mkdir();
            }
            expect = new File(dir, "application.properties");
        }
        else {
            String path = System.getProperty("config.location");
            if(!StringUtil.hasText(path)){
                path = "application.properties";
            }
            expect = new File(path);
        }
        Properties tmpProps = null;
        if (!expect.exists()){
            tmpProps = new Properties();
            tmpProps.put("server.port", "8221");

            if (SYSUtils.isAndroid()) {
                tmpProps.put("scan.locations", "Music,Videos,Documents,webapp");
            } else {
                tmpProps.put("scan.locations", "Music,Videos,webapp");

            }
            tmpProps.put("radio.enabled", "true");
            tmpProps.put("radio.shows.path", "#radio_shows.json");
            FileUtil.saveProps(tmpProps, expect, "weland first props save");
            log.info("Init application properties 1st time");
        }
        else {
            try {
                log.info("Loading properties from " + expect.getAbsolutePath());
                tmpProps = FileUtil.loadProps(expect);
            } catch (Exception e) {
                log.error("Failed to load application properties from file " + expect.getAbsolutePath(), e);
            }
        }
        applicationProperties = tmpProps;
    }


    public static Properties getApplicationProperties(){
        return applicationProperties;
    }


    @Deprecated
    public static List<File> getScannableLocations(){
        String val = applicationProperties.getProperty("scan.locations");
        String keys[] = val.split(",");
        List<File> r = new ArrayList<>();
        for (String s: keys){
            if ("music".equalsIgnoreCase(s)){
                r.add(FileUtil.findMusicDir());
            }
            else if ("videos".equalsIgnoreCase(s) || "movie".equalsIgnoreCase(s)){
                r.add(FileUtil.findMoviesDir());
            }
            else if ("downloads".equalsIgnoreCase(s) || "download".equalsIgnoreCase(s)){
                r.add(FileUtil.findDownloadDir());
            }
            else if(s.endsWith(":")){
                File d = getDrive(s);
                if (d != null && d.exists()){
                    r.add(d);
                }
                else {
                    File f = new File(s);
                    if (f.exists()){
                        r.add(f);
                    }
                }

            }
            else {
                File f = new File(s);
                if (f.exists()){
                    r.add(f);
                }
            }

        }
        return r;
    }


    private static File getDrive(String s){
        File drives[] = File.listRoots();
        for (File d: drives){
            if (d.getAbsolutePath().startsWith(s)){
                return d;
            }
        }
        return null;
    }


    public static String getProperty(Keys key){
        if (System.getProperty(key.s) != null){
            return System.getProperty(key.s);
        }
        if (isDefined(key)) {
            return applicationProperties.getProperty(key.s);
        }
        if (StringUtil.hasText(key.defaultValue)){
            return key.defaultValue;
        }
        return null;
    }

    public static String getProperty(Keys key, String defaultvalue){
        String x = getProperty(key);
        if (null == x){
            return defaultvalue;
        }
        return x;
    }

    public static boolean isTrue(Keys key){
        return "true".equalsIgnoreCase(getProperty(key));
    }

    public static boolean isFalse(Keys key){
        return "false".equalsIgnoreCase(getProperty(key));
    }

    public static int getInt(Keys key, int i) {
        try {
            return Integer.valueOf(getProperty(key));
        } catch (Exception e) {
            return i;
        }
    }

    public static boolean isDefined(Keys key) {
        return applicationProperties.containsKey(key.s);
    }

    /**
     * TODO use keys
     * @param prefix
     * @return
     */
    public static List<String> getListWithPrefix(String prefix) {

        List<String> res = new ArrayList<>();
        int index = 0;
        while(true){

            String fileKey = prefix + "." + index;
            String dirKey = prefix + ".srcDir." + index;
            if (applicationProperties.containsKey(fileKey)){
                String value = applicationProperties.getProperty(fileKey);
                if (null != value) {
                    res.add(index, value);
                }
            }

            if (applicationProperties.containsKey(dirKey)){
                String dirPath = applicationProperties.getProperty(dirKey);
                File dir = new File(dirPath);
                if (dir.exists()) {
                   File [] inner = dir.listFiles();
                   if (null != inner) {
                       for(File f: inner){
                           if (f.exists() && !f.isDirectory() && !f.isHidden())    {
                               res.add(index, f.getAbsolutePath());
                           }
                       }
                   }
                }
                
            }
            if (!applicationProperties.containsKey(fileKey) && !applicationProperties.containsKey(dirKey)){
                break;
            }
            index++;
        }

        return res;
    }

    public static String map(String path) {
        if (path.indexOf("{") > -1){
            return StringUtil.map(path.trim(), applicationProperties);
        }
        return path;
    }


    public static void throwOrExit(String s) {
        Mole.getLogger(AppSettings.class).error(s);
        if (isTrue(Keys.APP_ON_ERROR_EXIT)) {
            System.exit(-1);
        }
        throw new RuntimeException(s);
    }


    public enum Keys {
        PREFERRED_BROWSER("preferred.browser", new String[]{"selenium"}),
        PREFERRED_YOUTUBE_PLAYER("preferred.youtube.player", new String[]{"external, internal"}, "internal"),
        SERVER_PORT("server.port", new String[]{"8221 default"}),
        SINGLE_INSTANCES("single.instances", new String[]{"true (default)", "false"}),
        KEEP_SCREEN_ON("config.keep.screen.on", new String[]{"true/false"}),
        CRONUS_CONFIG_FILE("cronus.config.file", new String[]{"path to cronus.json"}),
        CRONUS_ENABLED("cronus.enabled", new String[]{"true (default)", "false"}),
        UI_CLOCK_ENABLED("ui.clock.enabled", new String[]{"true", "false(default)"}),


        //Deprecater
        GET_VOLUME_CMD("cmd.get.volume", new String[]{"fswebcam out.jpeg"}),
        UI_IMAGE_ICON_REFRESH("ui.image.icon.refresh", new String[]{"true", "false(default)"} ),
        LOCAL_COMANDS_FILE("cmds.local.file", new String[]{"/path/to/local/commands_win_edge.json"} )
        , RADIO_ENABLED("radio.enabled", new String[]{"true", "false(default)"})
        , RADIO_PING_STREAM("radio.ping.stream", new String[]{"true", "false(default)"})
        , RADIO_SHOWS_PATH("radio.shows.path", new String[]{"/path/to/local/shows.json"})
        , DEPENDENCY_FORCED_PROFILES("dependencies.forced.profiles", new String[]{})
        , APP_ON_ERROR_EXIT("app.onerror.exit", new String[]{"true", "false(default)"})
        , UI_INCLUDE_SNAPSHOTS("ui.include.snapshots", new String[]{"true", "false"})
        , FORCE_CLOSE_ON_STARTUP("radio.force.close.on.startup", new String[]{"true", "false"}, "false")
        , FORCED_NOW("arise.forced.now", new String[]{"2024-03-12 21:35:32"} );



        final String s;
        private final String[] variants;
        private final  String defaultValue;

        Keys(String s, String[] variants) {
            this.s = s;
            this.variants = variants;
            this.defaultValue = null;
        }

        Keys(String s, String[] variants, String defaultValue) {
            this.s = s;
            this.variants = variants;
            this.defaultValue = defaultValue;
        }
    }




}
