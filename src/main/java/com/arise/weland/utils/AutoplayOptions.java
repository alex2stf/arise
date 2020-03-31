package com.arise.weland.utils;

import com.arise.core.tools.AppCache;
import com.arise.weland.dto.Playlist;

public class AutoplayOptions {
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


    public static boolean isAutoplayVideos() {
        String val = AppCache.getString("autoplay", "");
        return Playlist.VIDEOS.equals(Playlist.find(val));
    }

    public static boolean isAutoplayMusic() {
        String val = AppCache.getString("autoplay", "");
        return Playlist.MUSIC.equals(Playlist.find(val));
    }


    public static void setAutoplayGroup(String groupName) {
        AppCache.putString("autoplay", "GR:" + groupName);
    }

    public static void setAutoplayPlaylist(Playlist playlist) {
        AppCache.putString("autoplay", playlist.name());
    }
}
