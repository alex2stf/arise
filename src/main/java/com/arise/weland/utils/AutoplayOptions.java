package com.arise.weland.utils;

import com.arise.core.tools.AppCache;
import com.arise.weland.dto.ContentInfo;
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

    public static Playlist getAutoPlaylist(){
        String val = AppCache.getString("autoplay", "MUSIC");
        Playlist playlist = Playlist.find(val);
        return playlist;
    }

//    public static boolean isAutoplayVideos() {
//        String val = AppCache.getString("autoplay", "");
//        return Playlist.VIDEOS.equals(Playlist.find(val));
//    }
//
//    //setat pe default autoplay MUSIC
//    public static boolean isAutoplayMusic() {
//
//        return Playlist.MUSIC.equals(Playlist.find(val));
//    }


    public static void setAutoplayGroup(String groupName) {
        AppCache.putString("autoplay", "GR:" + groupName);
    }

    public static void setAutoplayPlaylist(Playlist playlist) {
        AppCache.putString("autoplay", playlist.name());
    }

    public static ContentInfo nextMusic() {
        return null;
    }

    public static boolean isAutoplayVideos() {
        return Playlist.VIDEOS.equals(getAutoPlaylist());
    }

    public static boolean isAutoplayMusic() {

        return Playlist.MUSIC.equals(getAutoPlaylist());
    }
}
