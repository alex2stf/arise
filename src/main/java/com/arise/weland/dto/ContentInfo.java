package com.arise.weland.dto;


import com.arise.core.serializers.parser.Groot;
import com.arise.core.tools.Arr;
import com.arise.core.tools.ContentType;
import com.arise.core.tools.MapUtil;

import java.io.File;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.arise.core.tools.CollectionUtil.isEmpty;
import static com.arise.core.tools.StringUtil.*;
import static com.arise.core.tools.StringUtil.hasText;
import static com.arise.core.tools.ThreadUtil.fireAndForget;

public class ContentInfo implements Serializable {
    //    private transient byte[] emb;
    private transient String name;
    private String albumName;
    private String path;
    private ContentType contentType;
    private String artist;
    private String composer;
    private String title;
    private int visited = 0;
    private int width = 0;
    private int height = 0;
    private int position = 0;
    private int duration = 0;
    private String thumbnailId;
    private String playlistId;

    public String getPlaylistId() {
        return playlistId;
    }

    public ContentInfo setPlaylistId(String playlistId) {
        this.playlistId = playlistId;
        return this;
    }

    public String getThumbnailId() {
        return thumbnailId;
    }

    public ContentInfo setThumbnailId(String thumbnailId) {
        this.thumbnailId = thumbnailId;
        return this;
    }

    public int getPosition() {
        return position;
    }

    public ContentInfo setPosition(int position) {
        this.position = position;
        return this;
    }

    public boolean shouldPlay() {
        return position > 0;
    }



    public int getWidth() {
        return width;
    }

    public ContentInfo setWidth(int width) {
        this.width = width;
        return this;
    }

    public int getHeight() {
        return height;
    }

    public ContentInfo setHeight(int height) {
        this.height = height;
        return this;
    }



    public ContentInfo(){

    }

    public ContentInfo(File file){
        contentType = ContentType.search(file);
        name = file.getName();
        path = file.getAbsolutePath().replaceAll("\\\\", "/");
        if (!hasContent(name)){
            name = path;
        }
    }

    public int getVisited() {
        return visited;
    }

    public ContentInfo incrementVisit(){
        visited++;
        return this;
    }


    public ContentInfo setVisited(int visited) {
        this.visited = visited;
        return this;
    }





    public String getComposer() {
        return composer;
    }

    public ContentInfo setComposer(String composer) {
        this.composer = composer;
        return this;
    }

    public String getArtist() {
        return artist;
    }

    public ContentInfo setArtist(String artist) {
        this.artist = artist;
        return this;
    }

    public String getName() {
        try {
            String prts[] = path.split("/");
            prts = prts[prts.length - 1].split("\\\\");

            String x = prts[prts.length - 1];
            if (x.indexOf(".") > -1){
                x = x.substring(0, x.lastIndexOf("."));
            }
            return x;
        } catch (Exception e){
            e.printStackTrace();
            return path;
        }
    }




    public String getExt(){
        if (path.indexOf(".") > -1){
            String parts[] = path.split("\\.");

            String x = parts[parts.length - 1];
            return x;
        }
        return "cinf";
    }


    public String getPath() {
        return path;
    }

    public String getAlbumName() {
        return albumName;
    }

    public int getDuration() {
        return duration;
    }

    public ContentInfo setDuration(int duration) {
        this.duration = duration;
        return this;
    }

    public ContentInfo setAlbumName(String albumName) {
        this.albumName = albumName;
        return this;
    }

    public String getAlbum() {
        return albumName;
    }

    public String getTitle() {
        if (hasText(title)){
            return title;
        }
        String filename = getName();
        if (hasText(filename) && filename.indexOf(".") > -1){
            String parts[] = filename.split("\\.");
            filename = parts[0];
        }
        return filename;
    }

    public ContentInfo setTitle(String title) {
        this.title = title;
        return this;
    }




    private void addVal(StringBuilder sb, String key, String val){
        if (val != null){
            sb.append('"').append(key).append("\":").append(jsonVal(encodePath(val))).append(",");
        }
    }

    private void addVal(StringBuilder sb, String key, int val){
        if (val > 0){
            sb.append('"').append(key).append("\":").append(val).append(",");
        }
    }


    public String getId(){
        return Message.sanitize(getPath());
    }



    public ContentInfo setPath(String path) {
        this.path = path;
        return this;
    }

    public ContentInfo setContentType(ContentType contentType) {
        this.contentType = contentType;
        return this;
    }

    public static String serializeCollection(Collection<ContentInfo> infos){
        if (infos == null){
            return "[]";
        }
        return new StringBuilder().append("[").append(join(infos, ",")).append("]").toString();
    }

    public static List<ContentInfo> deserializeCollection(String content){
        List<ContentInfo> res = new ArrayList<>();
        Arr arr = (Arr) Groot.decodeBytes(content.getBytes());
        return deserializeList(arr);
    }

    public static List<ContentInfo> deserializeList(List list){
        List<ContentInfo> res = new ArrayList<>();
        for (Object o: list){
            if (o instanceof Map){
                ContentInfo info = fromMap((Map) o);
                res.add(info);
            }
        }
        return res;
    }


    public ContentType getContentType() {
        return contentType;
    }


    public static String getMediaPath(Map m){
        String src = MapUtil.getString(m, "P").replaceAll("\\\\/", "/").replaceAll("//", "/");
        return decodePath(src);
    }

    public static ContentInfo fromMap(Map m) {
        ContentInfo i = new ContentInfo();
        i.setAlbumName(MapUtil.getString(m, "A"));




        i.setPath(getMediaPath(m));
        i.setArtist(decodeString(m, "F"));
        i.setComposer(decodeString(m, "X"));
        i.setThumbnailId(decodeString(m, "Q"));
        i.setTitle(decodeString(m, "T"));
        i.setVisited(MapUtil.getInt(m, "V", 0));
        i.setWidth(MapUtil.getInt(m, "w", 0));
        i.setHeight(MapUtil.getInt(m, "h", 0));
        i.setPosition(MapUtil.getInt(m, "p", 0));
        i.setDuration(MapUtil.getInt(m, "g", 0));
        i.setPlaylistId(decodeString(m, "F"));


        String cnt = MapUtil.getString(m, "Z");
        ContentType ct = ContentType.search(cnt);
        i.setContentType(ct);
        return i;
    }

    private static String decodeString(Map m, String key){
        return decodePath(MapUtil.getString(m, key));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().append("{");
        addVal(sb, "A", albumName);
        addVal(sb, "F", artist);
        addVal(sb, "X",  composer);
        addVal(sb, "T",  title);
        addVal(sb, "V", visited);
        addVal(sb, "Q", thumbnailId);
        addVal(sb, "F", playlistId);
        addVal(sb, "w", width);
        addVal(sb, "h", height);
        addVal(sb, "p", position);
        addVal(sb, "g", duration);



        sb.append("\"P\":").append(jsonVal(encodePath(path)));
        sb.append("}");
        return sb.toString();
    }

    public static String decodePath(String s){
        if (s == null){
            return null;
        }
        try {
            return URLDecoder.decode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return s;
        }
    }

    public static  String encodePath(String s){

        String val;
        try {
            val = URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            val = s;
        }
        return val;
    }


}