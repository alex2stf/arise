package com.arise.core;

import com.arise.core.tools.FileUtil;
import com.arise.core.tools.Mole;
import com.arise.core.tools.SYSUtils;
import com.arise.core.tools.StringUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
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

        Properties binProps = null;
        try {
            binProps = FileUtil.loadProps(FileUtil.findStream("weland/app-default.properties"));
        } catch (IOException e) {
            log.error("Binary corrupted, default properties not found", e);
            binProps = new Properties();
        }

        if (!expect.exists()){
            FileUtil.saveProps(binProps, expect, "weland first props save");
            applicationProperties = binProps;
            log.info("Init application properties 1st time");
        }
        else {
            Properties tmpProps;
            try {
                log.info("Loading properties from " + expect.getAbsolutePath());
                Properties svdProps = FileUtil.loadProps(expect);
                Enumeration<String> enums = (Enumeration<String>) binProps.propertyNames();
                boolean updated = false;
                while (enums.hasMoreElements()) {
                    String key = enums.nextElement();
                    if (!svdProps.containsKey(key)){
                        String value = binProps.getProperty(key);
                        svdProps.put(key, value);
                        log.info("Build offer new application property " + key + "=" + value);
                        updated = true;
                    }
                }

                if (updated){
                    FileUtil.saveProps(svdProps, expect, "updated at " + new Date());
                    log.info("Saved updated properties file");
                }
                tmpProps = svdProps;
            } catch (IOException e) {
                log.error("Failed to load application properties from file " + expect.getAbsolutePath(), e);
                tmpProps = binProps;
            }
            applicationProperties = tmpProps;

        }
    }


    public static Properties getApplicationProperties(){
        return applicationProperties;
    }


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

    public static List<String> getListWithPrefix(String prefix) {

        List<String> res = new ArrayList<>();
        int index = 0;
        while(true){

            String key = prefix + "." + index;
            if (!applicationProperties.containsKey(key)){
                break;
            }
            String value = applicationProperties.getProperty(key);

            if (null == value){
                break;
            }
            res.add(index, value);
            index++;
        }
        return res;
    }


    public enum Keys {
        PREFERRED_BROWSER("preferred.browser", new String[]{"selenium"}),
        PREFERRED_YOUTUBE_PLAYER("preferred.youtube.player", new String[]{"external, internal"}, "internal"),
        SERVER_PORT("server.port", new String[]{"8221 default"}),
        SINGLE_INSTANCES("single.instances", new String[]{"true (default)", "false"}),
        LATEST_SNAPSHOT_PATH("paths.latest.snapshot", new String[]{"latest/snapshot.jpeg"}),
        KEEP_SCREEN_ON("config.keep.screen.on", new String[]{"true/false"}),
        CRONUS_CONFIG_FILE("cronus.config.file", new String[]{"path to cronus.json"}),
        CRONUS_ENABLED("cronus.enabled", new String[]{"true (default)", "false"}),
        UI_ENABLED("ui.enabled", new String[]{"true", "false(default)"}),
        UI_CLOSE_ON_EXIT("ui.closeOnExit", new String[]{"true", "false(default)"}),

        UI_IMAGE_ICON_PATH("ui.image.icon.path", new String[]{"/valid/path"}),

        //Deprecater
        TAKE_SNAPSHOT_CMD("cmd.take.snapshot", new String[]{"fswebcam out.jpeg"}),
        GET_VOLUME_CMD("cmd.get.volume", new String[]{"fswebcam out.jpeg"}),
        UI_IMAGE_ICON_REFRESH("ui.image.icon.refresh", new String[]{"true", "false(default)"} ),
        LOCAL_COMANDS_FILE("cmds.local.file", new String[]{"/path/to/local/commands.json"} ),
        MEDIA_PLAY_STRATEGY("media.play.strategy", new String[]{"commands", "javafx"})
        , RADIO_ENABLED("radio.enabled", new String[]{"true", "false(default)"})
        , RADIO_SHOWS_PATH("radio.shows.path", new String[]{"/path/to/local/shows.json"})
        , DEPENDENCY_FORCED_PROFILES("dependencies.forced.profiles", new String[]{})
        ;



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
