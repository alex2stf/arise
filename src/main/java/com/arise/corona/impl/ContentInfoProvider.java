package com.arise.corona.impl;

import com.arise.core.tools.ContentType;
import com.arise.core.tools.FileUtil;
import com.arise.corona.dto.CachedState;
import com.arise.corona.dto.ContentInfo;
import com.arise.corona.dto.ContentPage;

import java.io.File;
import java.util.*;

import static com.arise.core.tools.ThreadUtil.fireAndForget;

public class ContentInfoProvider {

    List<File> roots = new ArrayList<>();


    private volatile boolean scanned = false;

    final ContentInfoDecoder decoder;

    List<ContentInfo> music = new ArrayList<>();
    List<ContentInfo> videos = new ArrayList<>();
    List<ContentInfo> pictures = new ArrayList<>();
    List<ContentInfo> games = new ArrayList<>();
    List<ContentInfo> presentations = new ArrayList<>();

    public ContentInfoProvider(ContentInfoDecoder decoder){
        this.decoder = decoder;
    }



    private boolean isMusic(File f){
        return ContentType.isMusic(f);
    }

    private boolean isVideo(File f){
        return ContentType.isVideo(f);
    }

    private boolean isPicture(File f){
        return ContentType.isPicture(f);
    }

    private boolean isGame(File f){
        return false;
    }

    private boolean isPresentation(File f){
        return false;
    }

    private void saveCache(List<ContentInfo> infos){
        File dir = FileUtil.findAppDir();
        File path = new File(dir + File.separator + "cache.json");
        FileUtil.writeStringToFile(path, ContentInfo.serializeCollection(infos));
    }

    private List<ContentInfo> getCache(){
        File dir = FileUtil.findAppDir();
        File path = new File(dir + File.separator + "cache.json");
        try {
            String data = FileUtil.read(path);
            return ContentInfo.deserializeCollection(data);
        } catch (Exception e) {
            return null;
        }
    }

    private void asyncScan(){
        fireAndForget(new Runnable() {
            @Override
            public void run() {
                for (File root: roots){
                    FileUtil.recursiveScan(root, new FileUtil.FileFoundHandler() {
                        @Override
                        public void foundFile(File file) {
                            if (isMusic(file)){
                                music.add(decoder.decode(file));
                            }

                            else if (isVideo(file)){
                                videos.add(decoder.decode(file));
                            }
                            else if (isPicture(file)){
                                pictures.add(decoder.decode(file));
                            }
                            else if (isGame(file)){
                                games.add(decoder.decode(file));
                            }
                            else if (isPresentation(file)){
                                presentations.add(decoder.decode(file));
                            }

//                            System.out.println(file.getAbsolutePath());
//                            if (ContentType.isMusic(file)){
//                                ContentInfo info = decoder.decode(file);
//                                add(ContentType.Location.MUSIC, info);
//                                cache.add(info);
//                            }
//                            else if (ContentType.isVideo(file)){
//                                ContentInfo info = decoder.decode(file);
//                                add(ContentType.Location.MOVIES, info);
//                                cache.add(info);
//                            }
                        }
                    });
                }
                scanned = true;
                System.out.println("SCAN COMPLETE");
//                dispatch();
//                saveCache(cache);
            }
        });
    }

    public ContentInfoProvider get(){
//        if (!CollectionUtil.isEmpty(cache)){
//            for (ContentInfo info: cache){
//                if (ContentType.isVideo(info.getPath())){
//                    add(ContentType.Location.MOVIES, info);
//                }
//                else if (ContentType.isMusic(info.getPath())){
//                    add(ContentType.Location.MUSIC, info);
//                }
//            }
//            scanned = true;
//            dispatch();
//            return this;
//        }

        asyncScan();
        return this;
    }






    public ContentInfoProvider addRoot(File root) {
        roots.add(root);
        return this;
    }


    private List<ContentInfo> getPlaylist(String name){
        switch (name){
            case "music":
                return music;
            case "videos":
                return videos;
            case "games":
                return games;
            case "presentations":
                return presentations;
            case "images":
                return pictures;
        }
        return pictures;
    }


    public ContentPage getPage(String location, Integer index) {
        int rIndex = 0;
        int pageSize = 10;
        if (index != null){
            rIndex = index;
        }

        List<ContentInfo> source = getPlaylist(location);
        int sourceSize = source.size();

        if (sourceSize == 0){
            if (!scanned){
                return new ContentPage().setData(Collections.emptyList()).setIndex(rIndex);
            }
            else {
                return new ContentPage().setData(Collections.emptyList()).setIndex(null);
            }
        }

        List<ContentInfo> info = new ArrayList<>();

        if (pageSize > sourceSize){
            pageSize = sourceSize;
        }
        for (int i = rIndex; i < rIndex + pageSize; i++){
            info.add(source.get(i));
        }

        ContentPage contentPage = new ContentPage();
        contentPage.setData(Collections.unmodifiableList(info));
        contentPage.setIndex(rIndex + pageSize);
        return contentPage;
    }

    public final CachedState currentState = new CachedState();

    public void saveState(String path, Integer position, String playlistId) {
        String cnt =ContentInfo.encodePath(path) + "\n" + position + "\n" + playlistId;
        File out = new File(FileUtil.findAppDir() + File.separator + "cache_state.aris");
        FileUtil.writeStringToFile(out, cnt);
    }

    public  CachedState getCurrentState() {


        throw new RuntimeException("TODO get currentState");
//        ContentInfo contentInfo = decoder.decode(new File(path)).setPosition(position);
//        currentState.setPlaylistId(playlistId).setCurrentInfo(contentInfo);
//        persist(currentState);
//        return currentState;
    }


    public ContentInfo next() {
        throw new RuntimeException("TODO get next should be visible from everywhere");
    }
}
