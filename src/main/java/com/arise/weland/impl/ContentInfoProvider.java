package com.arise.weland.impl;

import com.arise.astox.net.models.http.HttpResponse;
import com.arise.core.serializers.parser.Groot;
import com.arise.core.tools.CollectionUtil;
import com.arise.core.tools.ContentType;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.MapObj;
import com.arise.core.tools.Mole;
import com.arise.core.tools.StreamUtil;
import com.arise.core.tools.StringUtil;
import com.arise.weland.dto.ContentInfo;
import com.arise.weland.dto.ContentPage;
import com.arise.weland.dto.Playlist;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.arise.core.tools.ThreadUtil.fireAndForget;

public class ContentInfoProvider {

    List<File> roots = new ArrayList<>();
    private Mole log = Mole.getInstance(ContentInfoProvider.class);

    private volatile boolean scanned = false;
    private volatile boolean scanning = false;

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





    static boolean acceptFilename(String name){
        if (name.indexOf(".") > -1){
            return ContentType.isMusic(name) || ContentType.isVideo(name);

        }
        return true;
    }



    private int fcnt = 0;

    private void asyncScan(){

        if (scanning){
            return;
        }
        scanning = true;
        fireAndForget(new Runnable() {
            @Override
            public void run() {

                for (final File root: roots){
                    log.info("start recursive read root " + root.getAbsolutePath() + " size " + root.length());

                    scanning = true;
                    FileUtil.recursiveScan(root, new FileUtil.FileFoundHandler() {
                        @Override
                        public void foundFile(File file) {
                            if (!file.getName().startsWith(".")) {
                                if (isMusic(file)) {
                                    fcnt++;
                                    music.add(decoder.decode(file, root).setPlaylist(Playlist.MUSIC));
                                } else if (isVideo(file)) {
                                    fcnt++;
                                    videos.add(decoder.decode(file, root).setPlaylist(Playlist.VIDEOS));
                                }
                            }
                        }
                    }, new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String name) {
                            return acceptFilename(name);
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


                log.trace("\n\nRECURSIVE SCAN COMPLETE\n\n");

                decoder.onScanComplete();
                scanning = false;
            }
        });
    }

    public boolean noFilesScanned(){
        return fcnt == 0;
    }

    public ContentInfoProvider get(){
        readStaticContent();
        asyncScan();
        return this;
    }

    private void readStaticContent() {
        File gamedirs[] = getGamesDirectory().listFiles();
        if (gamedirs == null || gamedirs.length == 0){
            return;
        }
        for (File gdir: gamedirs){
            File json = new File(gdir, "package-info.json");
            if (json.exists()){
                try {
                    MapObj obj = (MapObj) Groot.decodeFile(json);
                    ContentInfo info = new ContentInfo()
                            .setTitle(obj.getString("title"))
                            .setThumbnailId(obj.getString("thumbnail"))
                            .setPath("{host}/games/" + gdir.getName());
                    games.add(info);
                    System.out.println("import game " + info);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public ContentInfoProvider addRoot(File root) {
        if (root == null || !root.exists() || !root.isDirectory()){
            return this;
        }
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


        List<ContentInfo> source = getPlaylist(playlist);
        int sourceSize = source.size();

        if (sourceSize == 0){
            if (!scanned){
                return new ContentPage().setData(Collections.EMPTY_LIST).setIndex(rIndex);
            }
            else {
                return new ContentPage().setData(Collections.EMPTY_LIST).setIndex(null);
            }
        }

        List<ContentInfo> info = new ArrayList<>();

        int limit = rIndex + pageSize;

        if (limit > sourceSize){
            limit = sourceSize;
        }


        log.info("Fetch from " + rIndex + " to " + limit + " from a size of " + sourceSize + " id " + playlist);
        if (rIndex == limit && scanned){
            return new ContentPage().setData(Collections.EMPTY_LIST).setIndex(null);
        }
        for (int i = rIndex; i < limit; i++){
            info.add(source.get(i));
        }

        return new ContentPage()
                .setData(Collections.unmodifiableList(info))
                .setIndex(limit);
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














    public ContentInfo nextFile(Playlist playlist){

        List<ContentInfo> data = getPlaylist(playlist);
        List<ContentInfo> r = new ArrayList<>();
        int size = data.size();
        for(int i = 0; i < size; i++){
            ContentInfo c = data.get(i);
            if (!c.isWebPage()){
                r.add(c);
            }
        }
        return next(r);
    }

    public ContentInfo next(Playlist playlist){
        return next(getPlaylist(playlist));
    }

    public ContentInfo next(List<ContentInfo> data){


        if (CollectionUtil.isEmpty(data)){
            return null;
        }
        if (data.size() == 1){
            return data.get(0);
        }
        int size = data.size();
        ContentInfo info = data.get(0);
        for (int i = 1; i < size; i++){
            ContentInfo tmp = data.get(i);
            if (tmp.getVisited() < info.getVisited()){
                info = tmp;
            }
        }
        return info.incrementVisit();

    }


    List<ContentInfo> imported = new ArrayList<>();

    private ContentInfoProvider importJson(String path) {
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

    public ContentInfo decode(String currentPath) {
        return decoder.decodeFile(new File(currentPath));
    }

    public ContentInfo previous(Playlist playlist) {
        return null;
    }


    public File getGame(String id) {
        File root = getGamesDirectory();
        File gameDir = new File(root, id);
        File json = new File(gameDir, "package-info.json");

        MapObj mapObj = null;
        try {
            mapObj = (MapObj) Groot.decodeFile(json);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new File(gameDir, mapObj.getString("main"));
    }


    //TODO support android
    private File getImportDirectory(){
        return new File("webapp");
    }

    private File getGamesDirectory(){
        return new File(getImportDirectory(), "games");
    }
}
