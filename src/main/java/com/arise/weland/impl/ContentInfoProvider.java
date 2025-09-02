package com.arise.weland.impl;

import com.arise.astox.net.models.ServerResponse;
import com.arise.core.models.Handler;
import com.arise.core.serializers.parser.Groot;
import com.arise.core.tools.*;
import com.arise.weland.dto.ContentInfo;
import com.arise.weland.dto.ContentPage;
import com.arise.weland.dto.Playlist;

import java.io.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import static com.arise.weland.dto.Playlist.MUSIC;
import static com.arise.weland.dto.Playlist.VIDEOS;

public class ContentInfoProvider {


    final ContentInfoDecoder decoder;
    private Mole log = Mole.getInstance(ContentInfoProvider.class);
    private static final Map<String, Integer> DURATIONS = new ConcurrentHashMap<>();
    private static final Map<String, String> TITLES = new ConcurrentHashMap<>();



    public ContentInfoProvider(ContentInfoDecoder decoder){
        this.decoder = decoder;
        decoder.setProvider(this);
    }

    public static Map packageInfoProps(File f) throws IOException {
        String content = FileUtil.read(f);
        return (Map) Groot.decodeBytes(content.replaceAll("\\s+", " ").getBytes());
    }

    public static Map<String, String> getTitles() {
        return Collections.unmodifiableMap(TITLES);
    }








    public synchronized ContentInfo fromMap(Map map) {
        ContentInfo cI = new ContentInfo();
        cI.setTitle(MapUtil.getString(map, "title"));
        cI.setPath(MapUtil.getString(map, "path"));

        String durationVal = MapUtil.getString(map, "duration");
        if(StringUtil.hasText(durationVal)){
            int duration = 0;
            try{
                duration = Integer.parseInt(durationVal);
            }catch (Exception e){
                e.printStackTrace();
                System.out.println(map);
                System.exit(-1);
            }
            DURATIONS.put(cI.getPath(), duration * 60);
            cI.setDuration(duration * 60);
        }



        if(StringUtil.hasText(cI.getTitle())){
            if(TITLES.containsKey(cI.getPath())){
                System.out.println("> >>>>>>>>>>>>>>> duplicate url for "  + cI.getPath() + " for title [[[" + cI.getTitle() + "]]] already used " + TITLES.get(cI.getPath()));
            } else {
                TITLES.put(cI.getPath(), cI.getTitle());
            }
            String artist = extrageArtist(cI.getTitle());
            ARTISTS.put(cI.getPath(), artist);
        }



        String thumbnail = null;
        if(map.containsKey("thumbnail")) {
            Object th = map.get("thumbnail");
            if(th instanceof String){
                thumbnail = (String) th;
            }
            else if (th instanceof Collection){
                thumbnail = CollectionUtil.randomPickElement((Collection<String>) th);
            }
        }

        if (thumbnail != null){
            String thumbnailId = SGService.getInstance().createThumbnailId(cI, thumbnail);
            cI.setThumbnailId(thumbnailId);
        }

        if (map.containsKey("playlist")){
            cI.setPlaylist(Playlist.find(MapUtil.getString(map, "playlist")));
        } else {
            cI.setPlaylist(Playlist.STREAMS);
        }

        return cI;
    }











    public ServerResponse getMediaPreview(String id) {
        return decoder.getThumbnail(id);
    }

    public ContentInfoDecoder getDecoder() {
        return decoder;
    }






















    public static int getDuration(String pdir) {
        if(DURATIONS.containsKey(pdir)){
            return DURATIONS.get(pdir);
        }
        return -1;
    }

    public static String findTitle(String path) {
        if(TITLES.containsKey(path)){
            return TITLES.get(path);
        }
        return null;
    }

    static String extrageArtist(String title){
        return String.valueOf(title + "").split("-")[0].trim().toLowerCase();
    }

//    public static synchronized String findArtist(String path){
//        if(ARTISTS.containsKey(path)) {
//            return ARTISTS.get(path);
//        }
//
//        String title = findTitle(path);
//        if(title.equalsIgnoreCase(path)) {
//            return null;
//        }
//        try {
//            String art = extrageArtist(title);
//            ARTISTS.put(path, art);
//            return art;
//        } catch (Exception e){
//            e.printStackTrace();
//            return null;
//        }
//    }

    private static final Map<String, String> ARTISTS = new ConcurrentHashMap<>();

    public static synchronized int artistsCount() {
        return ARTISTS.size();
    }

    public static synchronized void printReport(){
        Map<String, Integer> aristsCount = new HashMap<>();


        for (Map.Entry<String, String> e: ARTISTS.entrySet()){
            String a = e.getValue().trim().toLowerCase();
            if(!aristsCount.containsKey(a)){
                aristsCount.put(a, 1);
            } else {
                int i =aristsCount.get(a);
                aristsCount.put(a, i + 1);
            }
        }

        List<Map.Entry<String, Integer>> ints = new ArrayList<>( aristsCount.entrySet());

        Collections.sort(ints, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });

        for (int i = 0; i < ints.size(); i++){
            Map.Entry<String, Integer> e = ints.get(i);
            System.out.println("\t\t\t\t" + i + ") " + e.getValue() + " " + e.getKey() );
        }

//        for (Map.Entry<String, Integer> e: aristsCount.entrySet()){
//            System.out.println("\t\t\t\t" + e.getValue() + " " + e.getKey() );
//        }

    }
}
