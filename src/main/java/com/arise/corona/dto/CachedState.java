package com.arise.corona.dto;

public class CachedState {
    private String playlistId;
    private ContentInfo currentInfo;
    private boolean autoplay;

    public boolean isAutoplay() {
        return autoplay;
    }

    public CachedState setAutoplay(boolean shouldAutoplay) {
        this.autoplay = shouldAutoplay;
        return this;
    }



    public CachedState setPlaylistId(String playlistId) {
        this.playlistId = playlistId;
        return this;
    }

    public CachedState setCurrentInfo(ContentInfo currentInfo) {
        this.currentInfo = currentInfo;
        return this;
    }

    public String getPlaylistId() {
        return playlistId;
    }

    public ContentInfo getCurrentInfo() {
        return currentInfo;
    }
}
