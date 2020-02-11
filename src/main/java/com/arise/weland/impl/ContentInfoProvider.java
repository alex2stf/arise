package com.arise.weland.impl;

import com.arise.astox.net.models.http.HttpResponse;
import com.arise.core.tools.CollectionUtil;
import com.arise.core.tools.ContentType;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.Mole;
import com.arise.core.tools.StreamUtil;
import com.arise.core.tools.StringUtil;
import com.arise.core.tools.models.CompleteHandler;
import com.arise.weland.dto.AutoplayMode;
import com.arise.weland.dto.ContentInfo;
import com.arise.weland.dto.ContentPage;
import com.arise.weland.dto.Playlist;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.arise.core.tools.ThreadUtil.fireAndForget;

public class ContentInfoProvider {

    List<File> roots = new ArrayList<>();
    private Mole log = Mole.getInstance(ContentInfoDecoder.class);

    private volatile boolean scanned = false;

    final ContentInfoDecoder decoder;

    final List<ContentInfo> music = new ArrayList<>();
    final List<ContentInfo> streams = new ArrayList<>();
    final List<ContentInfo> videos = new ArrayList<>();
    final List<ContentInfo> games = new ArrayList<>();
    final List<ContentInfo> presentations = new ArrayList<>();

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



    private boolean isGame(File f){
        return false;
    }

    private boolean isPresentation(File f){
        return false;
    }


    Map<Playlist, AutoplayMode> autoplayLists = new ConcurrentHashMap<>();


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
                        setAutoplay(Playlist.find(playlistId), autoplayMode);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


        fireAndForget(new Runnable() {
            @Override
            public void run() {
                for (final File root: roots){
                    FileUtil.recursiveScan(root, new FileUtil.FileFoundHandler() {
                        @Override
                        public void foundFile(File file) {

                            if (!file.getName().startsWith(".")){
                                if (isMusic(file)){
                                    music.add(decoder.decode(file, root).setPlaylist(Playlist.MUSIC));
                                }
                                else if (isVideo(file)){
                                    videos.add(decoder.decode(file, root).setPlaylist(Playlist.VIDEOS));
                                }
                                else if (isGame(file)){
                                    games.add(decoder.decode(file, root).setPlaylist(Playlist.GAMES));
                                }
                                else if (isPresentation(file)){
                                    presentations.add(decoder.decode(file, root).setPlaylist(Playlist.PRESENTATIONS));
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

                for (Map.Entry<Playlist, AutoplayMode> entry: autoplayLists.entrySet()){
                    if (AutoplayMode.on.equals(entry.getValue()) || AutoplayMode.on_shuffle.equals(entry.getValue())){
                        ContentInfo info = next(entry.getKey());
                        log.trace("Scan complete play advice " + info.getPath());
                        break;
                    }
                }
                log.trace("RECURSIVE SCAN COMPLETE");
                decoder.onScanComplete();
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


    private List<ContentInfo> getPlaylist(Playlist playlist){
        switch (playlist){
            case MUSIC:
                return music;
            case VIDEOS:
                return videos;
            case GAMES:
                return games;
            case  PRESENTATIONS:
                return presentations;
            case STREAMS:
                return streams;
        }
        return music;
    }


    public ContentPage getPage(Playlist playlist, Integer index) {
        int rIndex = 0;
        int pageSize = 10;
        if (index != null){
            rIndex = index;
        }

        AutoplayMode autoplayMode = autoplayLists.containsKey(playlist) ? autoplayLists.get(playlist): AutoplayMode.off;

        List<ContentInfo> source = getPlaylist(playlist);
        int sourceSize = source.size();

        if (sourceSize == 0){
            if (!scanned){
                return new ContentPage().setData(Collections.EMPTY_LIST).setIndex(rIndex).setAutoplayMode(autoplayMode);
            }
            else {
                return new ContentPage().setData(Collections.EMPTY_LIST).setIndex(null).setAutoplayMode(autoplayMode);
            }
        }

        List<ContentInfo> info = new ArrayList<>();

        int limit = rIndex + pageSize;

        if (limit > sourceSize){
            limit = sourceSize;
        }


        log.info("Fetch from " + rIndex + " to " + limit + " from a size of " + sourceSize + " id " + playlist);
        if (rIndex == limit && scanned){
            return new ContentPage().setData(Collections.EMPTY_LIST).setIndex(null).setAutoplayMode(autoplayMode);
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
        byte[] bytes = decoder.getThumbnail(id);
        ContentType contentType = decoder.getThumbnailContentType(id);
        response.setBytes(bytes)
                .setContentType(contentType);
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

    public void clearState() {
        decoder.clearState();
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




    public void setAutoplay(Playlist playlist, AutoplayMode autoplayMode) {
        for (Playlist s: autoplayLists.keySet()){
            autoplayLists.put(s, AutoplayMode.off);
        }
        log.trace(playlist + " auto " + autoplayMode);
        autoplayLists.put(playlist, autoplayMode);
        File cache = autoplayCache();
        String cnt = playlist.name() + "\n" + autoplayMode.name();
        FileUtil.writeStringToFile(cache, cnt);
    }



    File autoplayCache(){
        File cacheDir = decoder.getStateDirectory();
        return new File(cacheDir, "auto.play");
    }


//    public void onPlayComplete(ContentInfo currentInfo) {
//        currentInfo.incrementVisit();
//
//
//        if (!scanned){
//            return;
//        }
//
//        Playlist playlist = currentInfo.getPlaylist();
//
//        if (playlist == null){
//            return;
//        }
//
//        //increment visit
//        for (ContentInfo info: getPlaylist(playlist)){
//            if (info.getPath().equalsIgnoreCase(currentInfo.getPath())){
//                info.incrementVisit();
//            }
//        }
//
//        //play next if autoplay
//        for (Map.Entry<Playlist, AutoplayMode> entry: autoplayLists.entrySet()){
//            if (entry.getKey().equals(playlist) && !AutoplayMode.off.equals(entry.getValue())){
//                ContentInfo next = next(playlist);
//                if (next != null && playAdviceHandler != null){
//                   playAdviceHandler.onComplete(next);
//                }
//            }
//        }
//    }





    public ContentInfo next(Playlist playlist){
        List<ContentInfo> data = getPlaylist(playlist);

        if (data.isEmpty()){
            return null;
        }
        final ContentInfo[] res = new ContentInfo[1];
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
        log.trace("next " + playlist + " = " + res[0].getPath());
        return res[0];
    }


    List<ContentInfo> imported = new ArrayList<>();

    public ContentInfoProvider importJson(String path) {
        InputStream inputStream = FileUtil.findStream(path);
        if (inputStream == null){
            return this;
        }

        String cnt = StreamUtil.toString(inputStream).replaceAll("\r\n", " ");
        if (!StringUtil.hasContent(cnt)){
            return this;
        }
        List<ContentInfo> infos = ContentInfo.deserializeCollection(cnt);

        for (ContentInfo info: infos){
            //TODO check path not null
            imported.add(info); //add to imported no matter what
            Playlist playlist = info.getPlaylist();
            if (playlist == null){
                log.warn("Content Info" + info.getPath() + " has no playlist defined");
                continue;
            }
            push(playlist, info);
        }

        return this;
    }

    private void push(Playlist playlist, ContentInfo info){
        switch (playlist){
            case STREAMS:
                streams.add(info);
                break;
            case VIDEOS:
                videos.add(info);
                break;
        }
    }

    public ContentInfo findByPath(String path) {
        if (!scanned){
            log.warn("Scan not completed");
            return findByPathInList(imported, path);
        }
        ContentInfo info = findByPathInList(streams, path);
        if (info == null){
            info = findByPathInList(videos, path);
        }
        if (info == null){
            info = findByPathInList(music, path);
        }
        if (info == null){
            info = findByPathInList(games, path);
        }
        if (info == null){
            info = findByPathInList(presentations, path);
        }
        return info;
    }

    private ContentInfo findByPathInList(List<ContentInfo> contentInfos, String path){
        for (ContentInfo info: contentInfos){
            if (info.getPath().equals(path)){
                return info;
            }
        }
        return null;
    }


    public List<ContentInfo> getWebStreams() {
        List<ContentInfo> res = new ArrayList<>();
        int size = streams.size();
        for (int i = 0; i < size; i++){
            ContentInfo info = streams.get(i);
            if (info.isWebPage()){
                res.add(info);
            }
        }
        return res;
    }
}
