package com.arise.weland.dto;

public enum Playlist {
    MUSIC("music", "M", "Music"),
    STREAMS("streams", "S", "Streams"),
    GAMES("games", "G", "Games"),
    PRESENTATIONS("presentations", "P", "Presentations"),
    VIDEOS("videos", "V", "Videos");

    private final String apiId;
    private final String shortcut;
    private final String displayName;

    Playlist(String apiId, String shortcut, String displayName){

        this.apiId = apiId;
        this.shortcut = shortcut;
        this.displayName = displayName;
    }

    public static Playlist find(String what) {
        for (Playlist p: values()){
            if (p.apiId.equals(what)
                || p.shortcut.equals(what)
                || p.displayName.equals(what)
                || p.name().equals(what)){
                return p;
            }
        }
        return null;
    }

    public static String[] displayableNames() {
        String r[] = new String[values().length];
        for (int i = 0; i < values().length; i++){
            r[i] = values()[i].displayName;
        }
        return r;
    }

    @Override
    public String toString() {
        return "Playlist{" +
                "apiId='" + apiId + '\'' +
                ", shortcut='" + shortcut + '\'' +
                ", displayName='" + displayName + '\'' +
                '}';
    }

    public String shortId() {

        return shortcut;
    }
}
