package com.arise.corona.dto;

import com.arise.core.serializers.parser.Groot;
import com.arise.core.tools.Arr;
import com.arise.core.tools.ContentType;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.MapObj;
import com.arise.core.tools.MapUtil;
import com.arise.core.tools.SYSUtils;

import java.io.File;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.arise.core.tools.CollectionUtil.isEmpty;
import static com.arise.core.tools.StringUtil.*;
import static com.arise.core.tools.StringUtil.hasText;
import static com.arise.core.tools.ThreadUtil.fireAndForget;

public class ContentInfo implements Serializable {
    private byte[] emb;
    private String albumName;
    private String path;
    private ContentType contentType;
    private transient String name;
    private String artist;
    private String composer;
    private String title;
    private int visited = 0;
    private int width = 0;
    private int height = 0;
    private int position = 0;

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
        String prts[] = path.split("//");
        prts = prts[prts.length - 1].split(File.separator);

        return prts[prts.length - 1];
    }



    public String getPath() {
        return path;
    }






    public ContentInfo setEmbeddedPic(byte[] emb) {
        this.emb = emb;
        return this;
    }

    public byte[] embeddedPic(){
        return emb;
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




    private void addVal(StringBuilder sb, String key, Object val){
        if (val != null){
            sb.append('"').append(key).append("\":").append(jsonVal(val)).append(",");
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

    public static ContentInfo fromMap(Map m) {
        ContentInfo i = new ContentInfo();
        i.setAlbumName(MapUtil.getString(m, "A"));


        String src = MapUtil.getString(m, "P").replaceAll("\\\\/", "/").replaceAll("//", "/");

        i.setPath(decodePath(src));
        i.setArtist(MapUtil.getString(m, "F"));
        i.setComposer(MapUtil.getString(m, "X"));
        i.setTitle(MapUtil.getString(m, "T"));
        i.setVisited(MapUtil.getInt(m, "V", 0));
        i.setWidth(MapUtil.getInt(m, "w", 0));
        i.setHeight(MapUtil.getInt(m, "h", 0));
        i.setPosition(MapUtil.getInt(m, "p", 0));


        String cnt = MapUtil.getString(m, "Z");
        ContentType ct = ContentType.search(cnt);
        i.setContentType(ct);
        return i;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().append("{");
        addVal(sb, "A", albumName);
        addVal(sb, "F", artist);
        addVal(sb, "X", composer);
        if (visited > 0){
            addVal(sb, "V", visited);
        }
        if (width > 0){
            addVal(sb, "w", width);
        }
        if (height > 0){
            addVal(sb, "h", height);
        }
        if (position > 0){
            addVal(sb, "p", position);
        }


        addVal(sb, "T", title);
            sb.append("\"P\":").append(jsonVal(encodePath(path)));
        sb.append("}");
        return sb.toString();
    }

    public static String decodePath(String s){
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
