package com.arise.weland.impl;

import com.arise.astox.net.models.ServerResponse;
import com.arise.astox.net.models.http.HttpResponse;
import com.arise.canter.CommandRegistry;
import com.arise.core.models.Handler;
import com.arise.core.models.Tuple2;
import com.arise.core.serializers.parser.Groot;
import com.arise.core.tools.*;
import com.arise.weland.dto.ContentInfo;
import com.arise.weland.dto.ContentPage;
import com.arise.weland.dto.Playlist;

import java.io.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static com.arise.weland.dto.Playlist.MUSIC;
import static com.arise.weland.dto.Playlist.VIDEOS;

public class ContentInfoProvider {


    final ContentInfoDecoder decoder;
    BlockingQueue<String> scan_queue = new LinkedBlockingQueue<>();
    private Mole log = Mole.getInstance(ContentInfoProvider.class);
    private volatile boolean scannedOnce = false;
    private volatile boolean _scanning = false;
    private int fcnt = 0;
    private int lsc = 0;
    private static final Map<String, Integer> DURATIONS = new HashMap<>();
    private static final Map<String, String> TITLES = new HashMap<>();



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

    private void integrityCheck(Playlist playlist){
        List<ContentInfo> infos = getPlaylistFromFile(playlist);
        if(CollectionUtil.isEmpty(infos)){
            return;
        }
        boolean shouldResave = false;
        Iterator<ContentInfo> it = infos.iterator();
        while (it.hasNext()){
            ContentInfo ci = it.next();
            File f = ContentInfo.fileFromPath(ci.getPath());
            if (!f.exists()){
                log.warn("File " + f.getAbsolutePath() + " NOT FOUND. Resave required.");
                it.remove();
                shouldResave = true;
            }
        }

        if (shouldResave){
            resavePlaylistAfterIntegrityCheck(infos, playlist);
        }
    }

    private void resavePlaylistAfterIntegrityCheck(List<ContentInfo> infos, Playlist playlist) {
        File f = getPlaylistFile(playlist);
        if (f.exists()){
            f.delete();
        }

        String enc = StringUtil.join(infos, "\n", new StringUtil.JoinIterator<ContentInfo>() {
            @Override
            public String toString(ContentInfo value) {
                return value.toCsv();
            }
        }) + "\n";
        FileUtil.writeStringToFile(f, enc);
        log.warn("Resaved playlist " + playlist);

    }

    private void take_from_queue(){
        if (_scanning){
            _scanning = true;
            return;
        }
        integrityCheck(MUSIC);
        integrityCheck(VIDEOS);
        fcnt = 0;


        while (scan_queue.size() != 0){
            try {
                String input = scan_queue.take();
                if(input.startsWith("dir:")) {
                    scanRootDirectory(input.substring("dir:".length()));
                }
                else if (input.startsWith("config:")) {
                    scanConfigFile(input.substring("config:".length()));
                }
                else if (input.startsWith("stream:")) {
                    addDirectStream(input.substring("stream:".length()));
                }
//                System.out.println("TAKE: " + scan_queue.take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        _scanning = false;
        scannedOnce = true;
        log.info("SCAN COMPLETE: " + fcnt + " files found, _scanning = " + _scanning);
    }

    private void addDirectStream(String url) {
        mergeContent(new ContentInfo().setPath(url).setPlaylist(Playlist.STREAMS).setTitle(url), Playlist.STREAMS);

    }

    private void scanConfigFile(String path) {
        log.trace("scan config " + path);
        String content = StreamUtil.toString(FileUtil.findStream(path)).replaceAll("\r\n", " ");
        List<Map<Object, Object>> contentInfos = (List<Map<Object, Object>>) Groot.decodeBytes(content);

        for (Map<Object, Object> map: contentInfos){
            ContentInfo contentInfo = fromMap(map);
            mergeContent(contentInfo, contentInfo.getPlaylist());
        }
    }


    public ContentInfo fromMap(Map map) {
        ContentInfo cI = new ContentInfo();
        cI.setTitle(MapUtil.getString(map, "title"));
        cI.setPath(MapUtil.getString(map, "path"));

        String durationVal = MapUtil.getString(map, "duration");
        if(StringUtil.hasText(durationVal)){
            int duration = Integer.parseInt(durationVal);
            DURATIONS.put(cI.getPath(), duration * 60);

            cI.setDuration(duration * 60);
        }

        if(StringUtil.hasText(cI.getTitle())){
            TITLES.put(cI.getPath(), cI.getTitle());
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

    private void scanRootDirectory(String sroot){

        File root = new File(sroot);
//        log.trace("scan root " + root.getAbsolutePath());
        File [] innerFiles = root.listFiles();
        if (innerFiles == null || innerFiles.length == 0){
            return;
        }

        Set<File> dirs = new HashSet<>();
        for (File f: innerFiles){
            if (f.getName().startsWith(".")){
                continue;
            }
            if (f.isDirectory()){
                dirs.add(f);
            } else {
                decodeAndMergeFile(f);
            }
        }

        if (!CollectionUtil.isEmpty(dirs)){
            if (fcnt > lsc){
//                log.info(fcnt + " scanned files...");
                lsc = fcnt;
            }
            for (File f: dirs){
                scanRootDirectory(f.getAbsolutePath());
            }
        }
    }


    private void decodeAndMergeFile(File f){
        if (ContentType.isMusic(f)){
            mergeContent(decoder.decode(f).setPlaylist(MUSIC), MUSIC);
            fcnt++;
        }
        if (ContentType.isVideo(f)){
            mergeContent(decoder.decode(f).setPlaylist(VIDEOS), VIDEOS);
            fcnt++;
        }
    }


    //TODO persista doar o data
    public void mergeContent(ContentInfo contentInfo, Playlist playlist){

        File playlistFile = null;
        try {
            playlistFile = getPlaylistFile(playlist);
        }catch (Exception e){
            e.printStackTrace();
            System.exit(-1);
        }
        if(!playlistFile.exists()){
            try {
                playlistFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String line = contentInfo.toCsv();

        String existingContent = getPlaylistFileContent(playlist);
        String encodedPath = ContentInfo.csvEncode(contentInfo.getPath());
        if (existingContent.indexOf(encodedPath) > -1){
//            log.trace("Skip already registered " + contentInfo.getPath() );
            return;
        }
        try {
            //TODO aici append pe baza de set
            FileUtil.appendNewLineToFile(line, playlistFile);
//            log.trace("Register " + contentInfo.getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void asyncScan(){
        ThreadUtil.fireAndForget(new Runnable() {
            @Override
            public void run() {
                take_from_queue();
            }
        }, "ContentInfoProvider#asyncScan-" + UUID.randomUUID().toString());
    }






    /**
     * used by android for same check thread mechanism
     * @return
     */
    @SuppressWarnings("unused")
    public boolean noFilesScanned(){
        return fcnt == 0;
    }



    public ContentInfoProvider get(){
        if(_scanning){
            return this;
        }
        asyncScan();
        return this;
    }

    public ContentInfoProvider addRoot(File root) {
        if (root == null || !root.exists() || !root.isDirectory()){
            return this;
        }
        scan_queue.add("dir:" + root.getAbsolutePath());
        return this;
    }



    @Deprecated
    /**
     * asta nu e bine
     */
    public ContentPage getPage(Playlist playlist, Integer index) {
        int rIndex = 0;
        int pageSize = 10;
        if (index != null){
            rIndex = index;
        }

        List<ContentInfo> source = getPlaylistFromFile(playlist);
        int sourceSize = source.size();

        if (sourceSize == 0){
            if (!scannedOnce){
                return new ContentPage().setData(Collections.EMPTY_LIST).setIndex(rIndex);
            }
            else {
                return new ContentPage().setData(Collections.EMPTY_LIST).setIndex(null);
            }
        }

        List<ContentInfo> info = new ArrayList<>();

        int limit = rIndex + pageSize;

        if (limit >= sourceSize){
            limit = sourceSize;
        }


//        log.info("Fetch from " + rIndex + " to " + limit + " from a size of " + sourceSize + " id " + playlist);
        if (rIndex == limit && scannedOnce){
//            log.info("Fetch complete " + playlist.name() + "  at " + Util.now());
            return new ContentPage().setData(Collections.EMPTY_LIST).setIndex(null);

        }
        for (int i = rIndex; i < limit; i++){
            info.add(source.get(i));
        }

        return new ContentPage()
                .setData(Collections.unmodifiableList(info))
                .setIndex(limit);
    }

    public ServerResponse getMediaPreview(String id) {
        return decoder.getThumbnail(id);
    }

    public ContentInfoDecoder getDecoder() {
        return decoder;
    }

    @Deprecated
    private File getQueueFile(){
        return new File(decoder.getStateDirectory(), "media-queue.json");
    }



    @Deprecated
    public final void saveState(ContentInfo currentInfo) {
        decoder.saveState(currentInfo);
    }

    public void clearState() {
        decoder.clearState();
    }



    File getPlaylistFile(Playlist playlist){
        if(null == playlist){
            System.out.println("WTFFFFFFFFFFFFFFFFFFFFFFF");
            System.exit(-1);
        }
        return new File(FileUtil.findAppDir() + File.separator + playlist.name() + ".v2plst");
    }



    public String getPlaylistFileContent(Playlist playlist){
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(getPlaylistFile(playlist));
        } catch (FileNotFoundException e) {
            return null;
        }
        if (inputStream == null){
            return null;
        }
        return StreamUtil.toString(inputStream);
    }


    List<ContentInfo> persistPlaylist(Playlist playlist, List<ContentInfo> infos){
        File out = getPlaylistFile(playlist);
        FileUtil.writeStringToFile(out, ContentInfo.serializeCollection(infos));
        return infos;
    }


    public synchronized List<ContentInfo> getPlaylistFromFile(Playlist playlist){
        File f = getPlaylistFile(playlist);
        if (!f.exists()){
            return Collections.emptyList();
        }
        final List<ContentInfo> contentInfos = new ArrayList<>();
        try {
            FileUtil.readLineByLine(getPlaylistFile(playlist), new Handler<String>() {
                @Override
                public void handle(String data) {
                    if (StringUtil.hasText(data)) {
                        contentInfos.add(ContentInfo.fromCsv(data));
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
        return contentInfos;
    }


    public ContentInfo findByPath(String path) {
        if (!scannedOnce){
            log.warn("Scan not completed");

            //TODO fix this
            return decoder.decode(new File(path));
        }
        ContentInfo info = findByPathInList(getPlaylistFromFile(MUSIC), path);
        if (info == null){
            info = findByPathInList(getPlaylistFromFile(Playlist.VIDEOS), path);
        }
        if (info == null){
            info = findByPathInList(getPlaylistFromFile(Playlist.STREAMS), path);
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



    /**
     * used in android
     * @return
     */
    @SuppressWarnings("unused")
    public boolean finishedToScanAtLeastOnce() {
        return scannedOnce;
    }

    /**
     * used by speech recognition in android
     * @param data
     * @return
     */
    @SuppressWarnings("unused")
    public ContentInfo searchByKeyWord(ArrayList<String> data) {
        if (CollectionUtil.isEmpty(data)){
            return null;
        }

        List<String> alls = new ArrayList<>();
        for (String s: data){
            String parts[] = s.toLowerCase().split("\\s+");

            for (String x: parts){
                alls.add(x.toLowerCase());
            }
        }
        Collections.sort(alls, new Comparator<String>() {
            @Override
            public int compare(String s, String t1) {
                return Integer.compare(s.length(), t1.length()) * -1;
            }
        });

        List<ContentInfo> found = new ArrayList<>();
        for (String s: alls){
            for (ContentInfo info: getPlaylistFromFile(MUSIC)){
                if (info.getPath().toLowerCase().indexOf(s) > -1){
                    found.add(info);
                }
            }
        }

        if (CollectionUtil.isEmpty(found)){
            return null;
        }

        Collections.sort(found, new Comparator<ContentInfo>() {
            @Override
            public int compare(ContentInfo contentInfo, ContentInfo t1) {
                return Integer.compare(contentInfo.getVisited(), t1.getVisited());
            }
        });

        ContentInfo selected = found.get(0);
        incrementAndPersistVisitFor(selected.getPath(), MUSIC);

        return selected;
    }

    private void incrementAndPersistVisitFor(String path, Playlist playlist) {
        List<ContentInfo> infos  = getPlaylistFromFile(playlist);
        for (int i = 0; i < infos.size(); i++){
            ContentInfo x = infos.get(i);
            if (x.getPath().equals(path)){
                infos.get(i).incrementVisit();
                if (!_scanning) {
                    persistPlaylist(playlist, infos);
                }
                return;
            }
        }
    }

    /**
     * adauga fiser json de import
     * @param path
     * @return
     */
    public ContentInfoProvider addFromLocalResource(String path) {
        scan_queue.add("config:" + path);
        return this;
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
        return path;
    }
}
