package com.arise.weland.utils;

import com.arise.core.tools.AppCache;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.Mole;
import com.arise.core.tools.SYSUtils;
import com.arise.core.tools.StreamUtil;
import com.arise.core.tools.StringUtil;
import com.arise.weland.dto.ContentInfo;
import com.arise.weland.dto.Playlist;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class AppSettings {


    private static final Mole log = Mole.getInstance(AppSettings.class);

    private static final Properties applicationProperties;

    static {
        File expect;

        if (SYSUtils.isAndroid()){
            expect = new File(FileUtil.findDocumentsDir(), "weland.properties");
        }
        else {
            expect = new File("application.properties");
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

        }
        return r;
    }




    public static void setVideosAutoplay(boolean h){
        if (h){
            AppCache.putString("autoplay", Playlist.VIDEOS.name());
        }else {
            AppCache.putString("autoplay", "---");
        }
    }


    public static void setMusicAutoplay(boolean h){
        if (h){
            AppCache.putString("autoplay", Playlist.MUSIC.name());
        }else {
            AppCache.putString("autoplay", "---");
        }
    }

    public static Playlist getAutoPlaylist(){
        String val = AppCache.getString("autoplay", "MUSIC");
        Playlist playlist = Playlist.find(val);
        return playlist;
    }



    public static void setAutoplayGroup(String groupName) {
        AppCache.putString("autoplay", "GR:" + groupName);
    }

    public static void setAutoplayPlaylist(Playlist playlist) {
        AppCache.putString("autoplay", playlist.name());
    }


    public static boolean isAutoplayVideos() {
        return Playlist.VIDEOS.equals(getAutoPlaylist());
    }

    public static boolean isAutoplayMusic() {
        return Playlist.MUSIC.equals(getAutoPlaylist());
    }

    private static final HashMap<String, String> savedConnections = new HashMap<>();

    public static Map<String, String> storeHost(String name, String host) {
        savedConnections.put(name, host);
        FileUtil.serializableSave(savedConnections, new File("connections.txt"));
        return savedConnections;
    }

    public static Map<String, String> getSavedConnections(){
        return savedConnections;
    }
}
