package com.arise.weland.impl;

import com.arise.astox.net.models.http.HttpResponse;
import com.arise.core.tools.CollectionUtil;
import com.arise.core.tools.ContentType;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.Mole;
import com.arise.core.tools.StringUtil;
import com.arise.core.tools.models.CompleteHandler;
import com.arise.weland.dto.AutoplayMode;
import com.arise.weland.dto.ContentInfo;
import com.arise.weland.dto.ContentPage;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.arise.core.tools.ThreadUtil.fireAndForget;

public class ContentInfoProvider {

    List<File> roots = new ArrayList<>();
    private Mole log = Mole.getInstance(ContentInfoDecoder.class);

    private volatile boolean scanned = false;

    final ContentInfoDecoder decoder;

    List<ContentInfo> music = new ArrayList<>();
    List<ContentInfo> videos = new ArrayList<>();
    List<ContentInfo> pictures = new ArrayList<>();
    List<ContentInfo> games = new ArrayList<>();
    List<ContentInfo> presentations = new ArrayList<>();

    public ContentInfoProvider(ContentInfoDecoder decoder){
        this.decoder = decoder;
        decoder.setProvider(this);
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


    Map<String, AutoplayMode> autoplayLists = new ConcurrentHashMap<>();


    private void asyncScan(){
        File cache = autoplayCache();
        String cnt = null;

            if (cache.exists()){
                try {
                    cnt = FileUtil.read(cache);
                    String parts[] = cnt.split("\n");
                    if (parts.length > 1){
                        String playlistId = parts[0].trim();
                        String mode = parts[1];
                        AutoplayMode autoplayMode = AutoplayMode.valueOf(mode.trim());
                        setAutoplay(playlistId, autoplayMode);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


        fireAndForget(new Runnable() {
            @Override
            public void run() {
                for (File root: roots){
                    FileUtil.recursiveScan(root, new FileUtil.FileFoundHandler() {
                        @Override
                        public void foundFile(File file) {

                            if (!file.getName().startsWith(".")){
                                if (isMusic(file)){
                                    music.add(decoder.decode(file).setPlaylistId("M"));
                                }

                                else if (isVideo(file)){
                                    videos.add(decoder.decode(file).setPlaylistId("V"));
                                }
                                else if (isPicture(file)){
                                    pictures.add(decoder.decode(file).setPlaylistId("I"));
                                }
                                else if (isGame(file)){
                                    games.add(decoder.decode(file).setPlaylistId("G"));
                                }
                                else if (isPresentation(file)){
                                    presentations.add(decoder.decode(file).setPlaylistId("P"));
                                }
                            }
                        }
                    });
                }
                scanned = true;
                if (shuffleMusic) {
                    Collections.shuffle(music);
                    log.trace("Scan complete shuffle music");
                }

                if (shuffleVideos) {
                    Collections.shuffle(videos);
                    log.trace("Scan complete shuffle videos");
                }

                for (Map.Entry<String, AutoplayMode> entry: autoplayLists.entrySet()){
                    if (AutoplayMode.on.equals(entry.getValue()) || AutoplayMode.on_shuffle.equals(entry.getValue())){
                        ContentInfo info = next(entry.getKey());
                        playAdviceHandler.onComplete(info);
                        log.trace("Scan complete play advice " + info.getPath());
                        break;
                    }
                }
                log.trace("RECURSIVE SCAN COMPLETE");

            }
        });
    }

    public ContentInfoProvider get(){
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

        AutoplayMode autoplayMode = autoplayLists.containsKey(location) ? autoplayLists.get(location): AutoplayMode.off;

        List<ContentInfo> source = getPlaylist(location);
        int sourceSize = source.size();

        if (sourceSize == 0){
            if (!scanned){
                return new ContentPage().setData(Collections.emptyList()).setIndex(rIndex).setAutoplayMode(autoplayMode);
            }
            else {
                return new ContentPage().setData(Collections.emptyList()).setIndex(null).setAutoplayMode(autoplayMode);
            }
        }

        List<ContentInfo> info = new ArrayList<>();

        int limit = rIndex + pageSize;

        if (limit > sourceSize){
            limit = sourceSize;
        }


        log.info("Fetch from " + rIndex + " to " + limit + " from a size of " + sourceSize + " id " + location);
        if (rIndex == limit && scanned){
            return new ContentPage().setData(Collections.emptyList()).setIndex(null).setAutoplayMode(autoplayMode);
        }
        for (int i = rIndex; i < limit; i++){
            info.add(source.get(i));
        }

        return new ContentPage()
                .setData(Collections.unmodifiableList(info))
                .setIndex(limit)
                .setAutoplayMode(autoplayMode);
    }




    public  ContentInfo getCurrentInfo() {
        return decoder.currentInfo;
    }



    public HttpResponse getMediaPreview(String id) {
        HttpResponse response = new HttpResponse();
//        InputStream inputStream = FileUtil.findStream("src/main/resources#weland/icons/download.jpg");
//        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
//        StreamUtil.transfer(inputStream, buffer);
//
        byte[] bytes = decoder.getThumbnail(id);
        ContentType contentType = decoder.getThumbnailContentType(id);
        response.setBytes(bytes)
                .setContentType(contentType);
//        Util.close(inputStream);
//        Util.close(buffer);

        return response;
    }



    public ContentInfoDecoder getDecoder() {
        return decoder;
    }

    public void addToQueue(ContentInfo info) {

    }


    public final void saveState(ContentInfo currentInfo) {
        decoder.saveState(currentInfo);
    }

    private boolean shuffleMusic = false;
    private boolean shuffleVideos = false;

    public void shuffle(String playlistId) {
        if (scanned){
            switch (playlistId){
                case "music":
                    Collections.shuffle(music);
                    log.trace("Instant shuffle music");
                    break;
                case "videos":
                    Collections.shuffle(videos);
                    log.trace("Instant shuffle videos");
                    break;
            }
        }
        else {
            switch (playlistId){
                case "music":
                    shuffleMusic = true;
                    break;
                case "videos":
                    shuffleVideos = true;
                    break;
            }
        }
    }




    public void setAutoplay(String playlistId, AutoplayMode autoplayMode) {
        for (String s: autoplayLists.keySet()){
            autoplayLists.put(s, AutoplayMode.off);
        }
        log.trace(playlistId + " auto " + autoplayMode);
        autoplayLists.put(playlistId, autoplayMode);
        File cache = autoplayCache();
        String cnt = playlistId + "\n" + autoplayMode.name();
        FileUtil.writeStringToFile(cache, cnt);
    }



    File autoplayCache(){
        File cacheDir = decoder.getStateDirectory();
        return new File(cacheDir, "auto.play");
    }


    public void onPlayComplete(ContentInfo currentInfo) {
        currentInfo.incrementVisit();


        if (!scanned){
            return;
        }

        String currentPlaylistId = getPlaylistId(currentInfo);

        if (!StringUtil.hasText(currentPlaylistId)){
            return;
        }

        //increment visit
        for (ContentInfo info: getPlaylist(currentPlaylistId)){
            if (info.getPath().equalsIgnoreCase(currentInfo.getPath())){
                info.incrementVisit();
            }
        }

        //play next if autoplay
        for (Map.Entry<String, AutoplayMode> entry: autoplayLists.entrySet()){
            if (entry.getKey().equalsIgnoreCase(currentPlaylistId) && !AutoplayMode.off.equals(entry.getValue())){
                ContentInfo next = next(currentPlaylistId);
                if (next != null && playAdviceHandler != null){
                   playAdviceHandler.onComplete(next);
                }
            }
        }
    }

    private String getPlaylistId(ContentInfo currentInfo) {
        switch (currentInfo.getPlaylistId()){
            case "M":
                return "music";
            case "I":
                return "images";
            case "V":
                return "videos";
            case "G":
                return "games";
            case "P":
                return "presentations";
        }
        return null;
    }

    private CompleteHandler<ContentInfo> playAdviceHandler;

    public ContentInfoProvider onPlayAdvice(CompleteHandler<ContentInfo> playAdviceHandler) {
        this.playAdviceHandler = playAdviceHandler;
        return this;
    }

    public ContentInfo next(String playlistId){
        List<ContentInfo> data = getPlaylist(playlistId);

        if (data.isEmpty()){
            return null;
        }
        ContentInfo[] res = new ContentInfo[1];
        CollectionUtil.smartIterate(data, new CollectionUtil.SmartHandler<ContentInfo>() {
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
        log.trace("next " + playlistId + " = " + res[0].getPath());
        return res[0];
    }


}
