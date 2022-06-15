/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.arise.core.tools;


import com.arise.core.serializers.parser.Groot;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static com.arise.core.tools.ContentType.Location.*;

/**
 *
 * @author alexstf
 */


public enum ContentType  {



    TEXT_PLAIN("text/plain; charset=UTF-8", "text", DOCUMENTS, "txt"),
    TEXT_XYAML("text/x-yaml", DOCUMENTS, "yml"),
    TEXT_YAML("text/yaml", DOCUMENTS, "yml"),
    APPLICATION_XYAML("application/x-yaml", DOCUMENTS, "yml"),
    APPLICATION_JAVASCRIPT("application/javascript", DOCUMENTS, "js"),
    APPLICATION_PDF("application/pdf", DOCUMENTS, "pdf"),
    APPLICATION_JSON("application/json; charset=UTF-8", "json", DOCUMENTS, "json"),
    X_SHOCKWAVE_FLASH("application/x-shockwave-flash", DOCUMENTS, "swf"),

    APPLICATION_BAT("application/bat", DOCUMENTS, "bat"),
    APPLICATION_XBAT("application/x-bat", DOCUMENTS, "bat"),

    IMAGE_JPEG("image/jpeg", PICTURES, "jpeg", "jpg"),
    IMAGE_GIF("image/gif", PICTURES, "gif"),
    IMAGE_PNG("image/png", PICTURES, "png"),
    IMAGE_XPNG("image/x-png", PICTURES, "png"),
    ICON("image/x-icon", PICTURES),

    TEXT_CSS("text/css", DOCUMENTS, "css"),
    TEXT_HTML("text/html", DOCUMENTS, "html"),


    VIDEO_MP4("video/mp4", MOVIES, "mp4"),
    AUDIO_MPEG_3("audio/mpeg3", MUSIC, "mp3"),
    AUDIO_X_MPEG_3("audio/x-mpeg-3", MUSIC, "mp3"),
    VIDEO_MPEG("video/mpeg", MOVIES, "mp3"),
    VIDEO_X_MPEG("video/x-mpeg", MOVIES, "mp3"),

    AUDIO_WAVE("audio/wave", MOVIES, "wav"),


    AUDIO_WAV("audio/wav", MUSIC, "wav"),
    AUDIO_XWAV("audio/x-wav", MUSIC, "wav"),
    AUDIO_X_PNWAV("audio/x-pn-wav", MUSIC, "wav"),
    VIDEO_AVI("video/avi", MOVIES, "avi");


    private static ContentType searchInArr(List arr){
        for (Object o: arr){
            ContentType contentType = ContentType.search((String) o);
            if (contentType != null){
                return contentType;
            }
        }
        return null;
    }

    public static void loadDefinitions(){
        InputStream inputStream = FileUtil.findStream("content-types.json");
        String text = StreamUtil.toString(inputStream).replaceAll("\\s+", " ");
        List data = (List) Groot.decodeBytes(text);

        for (Object o : data){
            Map obj = (Map) o;
            List keys = (List) obj.get("keys");
            for (Object key: keys){
                ContentType contentType = search((String) key);
                if (contentType != null){
                    List processes = (List) obj.get("processes");
                    for (Object process: processes){
                        String path = (String) process;
                        File f = new File(path);
                        if (f.exists()){
                            contentType.registerProcessPath(f);
                        }
                    }
                }
            }

        }
    }

    private static final String[] EMPTY_EXT = new String []{""};
    private final String p_alias;
    private final String displayName;
    private final Location loc;
    private final String[] ext;

    private int resId;


    ContentType(String displayName, Location location, String ... ext) {
        this.displayName = displayName;
        this.p_alias = displayName;
        this.loc = location;
        this.ext = ext;
    }


    ContentType(String displayName, String alias, Location location, String ... ext) {
        this.displayName = displayName;
        this.p_alias = alias;
        this.loc = location;
        this.ext = ext;
    }

    public static boolean isUrl(String str) {
        try {
            URL url = new URL(str);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }


    public Location location() {
        return loc;
    }

    public String [] extensions(){
        return ext != null && ext.length > 0 ? ext : EMPTY_EXT;
    }

    public String mainExtension(){
        String[] ext = extensions();
        return ext.length > 0 ? ext[0] : "" ;
    }

    public String alias(){
        return p_alias;
    }
    public String toString() {
        return displayName;
    }

    public static ContentType search(File f) {
       return search(getExtension(f));
    }

    public static String getExtension(File f){
        String p[] = f.getName().split("\\.");
        return (p[p.length - 1]);
    }

    public int getResId() {
        return resId;
    }

    public ContentType setResId(int resId) {
        this.resId = resId;
        return this;
    }

    //TODO make extension part of enum
    public static ContentType search(String s) {
        if (!StringUtil.hasText(s)){
            return TEXT_PLAIN;
        }
        try {
            ContentType ct = ContentType.valueOf(s);
            if (ct != null){
                return ct;
            }
        }catch (Exception e){

        }

        if (s.length() > 4){
            for (ContentType contentType : ContentType.values()){
                if (contentType.displayName.equalsIgnoreCase(s)){
                    return contentType;
                }
            }
        }


        if ("gif".equalsIgnoreCase(s)){
            return IMAGE_GIF;
        }
        if ("js".equalsIgnoreCase(s)){
            return APPLICATION_JAVASCRIPT;
        }
        if ("json".equalsIgnoreCase(s)){
            return APPLICATION_JSON;
        }
        if ("pdf".equalsIgnoreCase(s)){
            return APPLICATION_PDF;
        }
        if ("ico".equalsIgnoreCase(s)){
            return ICON;
        }
        if ("jpeg".equalsIgnoreCase(s) || "jpg".equalsIgnoreCase(s)){
            return IMAGE_JPEG;
        }
        if ("png".equalsIgnoreCase(s)){
            return IMAGE_PNG;
        }

        if ("html".equalsIgnoreCase(s)){
            return TEXT_HTML;
        }

        if ("css".equalsIgnoreCase(s)){
            return TEXT_CSS;
        }

        if ("yaml".equalsIgnoreCase(s)){
            return TEXT_YAML;
        }
        if ("wav".equalsIgnoreCase(s)){
            return AUDIO_WAV;
        }
        if ("mp4".equalsIgnoreCase(s)){
            return VIDEO_MP4;
        }
        //TODO return based on location (videos/music)
        if ("mp3".equalsIgnoreCase(s)){
            return AUDIO_MPEG_3;
        }
        if ("avi".equalsIgnoreCase(s)){
            return VIDEO_AVI;
        }

        try {
            Integer index = Integer.valueOf(s);
            ContentType vals[] = ContentType.values();
            if (index > 0 && index < vals.length){
                return vals[index];
            }
        } catch (NumberFormatException e){

        }
        return ContentType.TEXT_PLAIN;
    }


    public static boolean isMedia(File file) {
        return isMusic(file) || isVideo(file);
    }


    public static boolean isVideo(File file) {
        return isVideo(file.getName());
    }

    public static boolean isVideo(String path) {
        return  path.endsWith(".mp4") || path.endsWith(".3gp")
                || path.endsWith(".mkv")
                || path.endsWith(".avi");
    }


    public static boolean isMusic(File file) {
        return isMusic(file.getName());
    }

    public static boolean isMusic(String name) {
        return name.endsWith(".mp3") ||
                name.endsWith(".flac") ||
                name.endsWith(".m4a") ||
                name.endsWith(".aac") ||
                name.endsWith(".wav")||
                name.endsWith(".aacp");
    }

    public static boolean isPicture(File f) {
        return isPicture(f.getAbsolutePath());
    }

    public static boolean isPicture(String s) {
        return s.endsWith(".jpg")
                || s.endsWith(".png")
                || s.endsWith(".jpeg");
    }


    public enum Location {
        PICTURES("IMG"), DOCUMENTS("DOC"), MOVIES("VID"), MUSIC("AUD");
        private final String al;

        Location(String al){
            this.al = al;
        }

        public String alias(){
            return al;
        }

        public static Location find(String input){
            for (Location l: Location.values()){
                if (l.name().equalsIgnoreCase(input)){
                    return l;
                }
            }
            return null;
        }
    }


    private List<String> availableProcesses = new ArrayList<>();

    public List<String> processes(){
        return availableProcesses;
    }

    public ContentType registerProcessPath(File file){
        if (file.exists()){
            availableProcesses.add(file.getAbsolutePath());
        }
        return this;
    }

    //load process mapping



}
