/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.arise.core.tools;



import java.io.File;

import static com.arise.core.tools.ContentType.Location.DOCUMENTS;
import static com.arise.core.tools.ContentType.Location.MOVIES;
import static com.arise.core.tools.ContentType.Location.MUSIC;
import static com.arise.core.tools.ContentType.Location.PICTURES;

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

    IMAGE_JPEG("image/jpeg", PICTURES, "jpeg", "jpg"),
    IMAGE_GIF("image/gif", PICTURES, "gif"),
    IMAGE_PNG("image/png", PICTURES, "png"),
    IMAGE_XPNG("image/x-png", PICTURES, "png"),
    ICON("image/x-icon", PICTURES),

    TEXT_CSS("text/css", DOCUMENTS, "css"),
    TEXT_HTML("text/html", DOCUMENTS, "html"),


    VIDEO_MP4("video/mp4", MOVIES, "mp4"),
    AUDIO_WAVE("audio/wave", MOVIES, "wav"),


    AUDIO_WAV("audio/wav", MUSIC, "wav"),
    AUDIO_XWAV("audio/x-wav", MUSIC, "wav"),
    AUDIO_X_PNWAV("audio/x-pn-wav", MUSIC, "wav");

    private static final String[] EMPTY_EXT = new String []{""};
    private final String p_alias;
    private final String displayName;
    private final Location loc;
    private final String[] ext;

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

    public Location location() {
        return loc;
    }

    public String [] extensions(){
        return ext != null && ext.length > 0 ? ext : EMPTY_EXT;
    }

    public String mainExtension(){
        return extensions()[0];
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




    //TODO make extension part of enum
    public static ContentType search(String s) {
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
        return ContentType.TEXT_PLAIN;
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
    }


}
