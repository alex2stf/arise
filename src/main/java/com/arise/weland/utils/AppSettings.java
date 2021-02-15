package com.arise.weland.utils;

import com.arise.core.tools.AppCache;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.StreamUtil;
import com.arise.weland.dto.ContentInfo;
import com.arise.weland.dto.Playlist;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class AppSettings {

//    private String preferred

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
