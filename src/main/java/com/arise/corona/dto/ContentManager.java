package com.arise.corona.dto;

import com.arise.core.serializers.parser.Groot;
import com.arise.core.tools.AppCache;
import com.arise.core.tools.CollectionUtil;
import com.arise.core.tools.MapObj;
import com.arise.core.tools.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.arise.core.tools.CollectionUtil.smartIterate;
import static com.arise.core.tools.StringUtil.jsonVal;


@Deprecated
public class ContentManager {
    Map<String, Playlist > map = new ConcurrentHashMap<>();
    Playlist currentPlaylist;

    public Playlist getPlaylist(String key, List<ContentInfo> data){
        return getPlaylist(key, data, false);
    }

    public synchronized Playlist getPlaylist(String key, List<ContentInfo> data, boolean shuffle) {
        if (data == null){
            data = new ArrayList<>();
        }
        String cc = key + "PL";
        if (map.containsKey(cc)){
            return map.get(cc);
        }

        String cache = AppCache.getString(cc, null);
        Playlist playlist;
        if (StringUtil.hasContent(cache)){
            playlist = deserialize(cache);
        } else {
            playlist = new Playlist(cc, data, shuffle);
            AppCache.putString(cc, serialize(playlist));
        }
        map.put(cc, playlist);
        return playlist;
    }

    public  static String serialize(Playlist playlist) {
        StringBuilder sb = new StringBuilder();
        sb
                .append("{\"s\":").append(playlist.shuffle).append(",\"d\":")
                .append(ContentInfo.serializeCollection(playlist.getData()))
                .append(",\"x\":").append(jsonVal(playlist.getId()))
                .append(",\"i\":").append(jsonVal(playlist.getCurrentId()))
                .append("}");
        return sb.toString();
    }


    public static Playlist deserialize(String content){
        MapObj obj = (MapObj) Groot.decodeBytes(content.getBytes());
        boolean shuffle = "true".equalsIgnoreCase(obj.getString("s"));
        String id = (obj.getString("x"));
        String currentId = obj.getString("i");
        List<ContentInfo> infos = ContentInfo.deserializeList(obj.getArray("d"));
        Playlist p = new Playlist(id, infos, shuffle);
        p.currentId = currentId;
        return p;
    }



    public void setCurrentPlaylist(Playlist playlist) {
        if (playlist == null){
            return;
        }
        currentPlaylist = playlist;
        AppCache.putString("current-playlist", playlist.getId());
        AppCache.putString(playlist.getId(), serialize(playlist));
    }

    public Playlist getCurrentPlaylist() {
       if (currentPlaylist != null){
           return currentPlaylist;
       }
       String cachedId = AppCache.getString("current-playlist", null);
       if (StringUtil.hasContent(cachedId)){
           String cachedData = AppCache.getString(cachedId, null);
           if (StringUtil.hasContent(cachedData)){
               Playlist playlist;
               try {
                   playlist = deserialize(cachedData);
               } catch (Exception e){
                   playlist = null;
               }
               if (playlist != null){
                   currentPlaylist = playlist;
                   return currentPlaylist;
               }
           }
           for (Map.Entry<String, Playlist> e: map.entrySet()){
               Playlist tmp = e.getValue();
               if (tmp != null){
                   currentPlaylist = tmp;
                   return currentPlaylist;
               }
           }
       }
       return null;
    }


    public static class Playlist {

        private final List<ContentInfo> data;
        private final String id;
        private boolean shuffle;
        private String currentId;


        public Playlist(String id, List<ContentInfo> data, boolean shuffle) {
            this.id = id;
            if (shuffle){
                Collections.shuffle(data);
            }
            this.data = data;
            this.shuffle = shuffle;


        }

        public String getCurrentId() {
            return currentId;
        }


        public String getId() {
            return id;
        }

        public Playlist setShuffle(boolean shuffle) {
            this.shuffle = shuffle;
            return this;
        }
        //for shuffle mode
        final ContentInfo[] res = new ContentInfo[1];

        boolean cprovided = false;

        public ContentInfo next(){
            if (data.isEmpty()){
                return null;
            }
            if (currentId != null && !cprovided){
                cprovided = true;
                for (ContentInfo i: data){
                    if (currentId.equals(i.getId())){
                        return i;
                    }
                }
            }
            smartIterate(data, new CollectionUtil.SmartHandler<ContentInfo>() {
                @Override
                public void handle(ContentInfo t1, ContentInfo t2) {
                    ContentInfo tmp;
                    if (t2 != null){
                        if (t1.getVisited() < t2.getVisited()){
                            tmp = t1;
                        } else {
                            tmp = t2;
                        }
                    }
                    else {
                        tmp = t1;
                    }
                    if (res[0] == null){
                        res[0] = tmp;
                    } else {
                        if (tmp.getVisited() < res[0].getVisited()){
                            res[0] = tmp;
                        }
                    }
                }
            });
            res[0].incrementVisit();
            currentId = res[0].getId();
            return res[0];
        }

        public List<ContentInfo> getData() {
            return data;
        }

        public boolean isShuffle() {
            return shuffle;
        }

        public void add(ContentInfo mediaInfo) {
            data.add(mediaInfo);
        }

        public void setCurrent(ContentInfo info) {
            if (info == null){
                currentId = null;
                return;
            }
            if (data.isEmpty()){
                data.add(info);
                currentId = info.getId();
                return;
            }

            for (ContentInfo x: data){
                if (x.getPath().equalsIgnoreCase(info.getPath())){
                    x.setPosition(info.getPosition());
                }
            }
            currentId = info.getId();
        }
    }
}
