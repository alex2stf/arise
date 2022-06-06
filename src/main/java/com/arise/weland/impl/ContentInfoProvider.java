package com.arise.weland.impl;

import com.arise.astox.net.models.http.HttpResponse;
import com.arise.core.models.Handler;
import com.arise.core.serializers.parser.Groot;
import com.arise.core.tools.CollectionUtil;
import com.arise.core.tools.ContentType;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.MapUtil;
import com.arise.core.tools.Mole;
import com.arise.core.tools.StreamUtil;
import com.arise.core.tools.StringUtil;
import com.arise.core.tools.ThreadUtil;
import com.arise.weland.dto.ContentInfo;
import com.arise.weland.dto.ContentPage;
import com.arise.weland.dto.Playlist;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.arise.weland.dto.Playlist.MUSIC;
import static com.arise.weland.dto.Playlist.VIDEOS;

public class ContentInfoProvider {

    final ContentInfoDecoder decoder;
    List<File> roots = new ArrayList<>();
    private Mole log = Mole.getInstance(ContentInfoProvider.class);
    private volatile boolean scannedOnce = false;
    private volatile boolean scanning = false;
    private int fcnt = 0;
    private List<String> paths = new ArrayList<>();

    public ContentInfoProvider(ContentInfoDecoder decoder){
        this.decoder = decoder;
        decoder.setProvider(this);
    }

    static boolean acceptFilename(String name){
        if (name.indexOf(".") > -1){
            return ContentType.isMusic(name) || ContentType.isVideo(name) || "package-info.json".equals(name);
        }
        return true;
    }

    public static Map packageInfoProps(File f) throws IOException {
        String content = FileUtil.read(f);
        return (Map) Groot.decodeBytes(content.replaceAll("\\s+", " ").getBytes());
    }

    public static Object[] decodePackageInfo(File f){
        Object[] r = new Object[2];
        try {
            String content = FileUtil.read(f);
            Map props = (Map) Groot.decodeBytes(content.replaceAll("\\s+", " ").getBytes());
            ContentInfo contentInfo = new ContentInfo();
            String main = MapUtil.getString(props, "main");
            String pname = MapUtil.getString(props, "playlist");

            if (!StringUtil.hasText(main) || !StringUtil.hasText(pname)){
                return null;
            }
            Playlist plobj = Playlist.find(pname);
            if (plobj == null){
                return null;
            }
            File mainFile = new File(f.getParentFile(), main);
            if (!mainFile.exists()){
                return null;
            }
            contentInfo.setPath(f.getAbsolutePath());
            String title = MapUtil.getString(props, "title");
            contentInfo.setTitle(title);
            contentInfo.setPlaylist(plobj);
//            contentInfo.setGroupId()

            r[0] = contentInfo;
            r[1] = plobj;
            return r;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
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

    private void syncScan(){
        if (scanning){
            scanning = true;
            return;
        }
        integrityCheck(MUSIC);
        integrityCheck(VIDEOS);
        fcnt = 0;

        try {
            for (String path: paths){
                log.info("Loading static content from path " + path );
                String content = StreamUtil.toString(FileUtil.findStream(path)).replaceAll("\r\n", " ");
                List<Map<Object, Object>> contentInfos = (List<Map<Object, Object>>) Groot.decodeBytes(content);

                for (Map<Object, Object> map: contentInfos){
                    ContentInfo contentInfo = new ContentInfo();
                    String thumbnailId = MapUtil.getString(map, "thumbnail");
                    if (thumbnailId != null){
                        //this is intensive blocking
                        try {
                            SuggestionService.Data d = decoder.fixThumbnail(thumbnailId + "");
                            if (d != null){
                                contentInfo.setThumbnailId(d.getId());
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                    contentInfo.setPath(MapUtil.getString(map, "path"));
                    contentInfo.setPlaylist(Playlist.find(MapUtil.getString(map, "playlist")));
                    contentInfo.setTitle(MapUtil.getString(map, "title"));
                    mergeContent(contentInfo, contentInfo.getPlaylist());
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        for (final File root: roots){
            scanRoot(root);
        }
        scanning = false;
        scannedOnce = true;
        log.info("SCAN COMPLETE: " + fcnt + " files found, scanning = " + scanning);
    }

    private int lsc = 0;

    private void scanRoot(File root){
        log.trace("scan root " + root.getAbsolutePath());
        File [] innerFiles = root.listFiles();
        if (innerFiles == null || innerFiles.length == 0){
            return;
        }

        List<File> dirs = new ArrayList<>();
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

//        System.out.println(root.getAbsolutePath() + " has " + innerFiles.length + " files fh " + dirs.size());
        if (!CollectionUtil.isEmpty(dirs)){
            if (fcnt > lsc){
                log.info(fcnt + " scanned files...");
                lsc = fcnt;
            }
            for (File f: dirs){
                scanRoot(f);
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


    private void mergeContent(ContentInfo contentInfo, Playlist playlist){

        File playlistFile = getPlaylistFile(playlist);
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
                syncScan();
            }
        }, "ContentInfoProvider#asyncScan-" + UUID.randomUUID().toString());
    }

    private boolean isPackageInfo(File file) {
        return "package-info.json".equalsIgnoreCase(file.getName());
    }

    int getMatchIndex(List<ContentInfo> sources, ContentInfo criteria){
        for (int i = 0; i < sources.size(); i++){
            if (sources.get(i).getPath().equals(criteria.getPath())){
                return i;
            }
        }
        return -1;
    }



    /**
     * used by android for same check thread mechanism
     * @return
     */
    @SuppressWarnings("unused")
    public boolean noFilesScanned(){
        return fcnt == 0;
    }

    boolean infoAlreadyExists(File file, List<ContentInfo> playlist){
        for (ContentInfo c: playlist){
            if (c.getPath().equals(file.getAbsolutePath())){
                return true;
            }
        }
        return false;
    }

    public ContentInfoProvider get(){
        asyncScan();
        return this;
    }

    public ContentInfoProvider addRoot(File root) {
        if (root == null || !root.exists() || !root.isDirectory()){
            return this;
        }
        roots.add(root);
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


        log.info("Fetch from " + rIndex + " to " + limit + " from a size of " + sourceSize + " id " + playlist);
        if (rIndex == limit && scannedOnce){
            log.info("Fetch complete " + playlist.name() + "  at " + new Date());
            return new ContentPage().setData(Collections.EMPTY_LIST).setIndex(null);

        }
        for (int i = rIndex; i < limit; i++){
            info.add(source.get(i));
        }

        return new ContentPage()
                .setData(Collections.unmodifiableList(info))
                .setIndex(limit);
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
                if (!scanning) {
                    persistPlaylist(playlist, infos);
                }
                return;
            }
        }
    }

    public ContentInfoProvider addFromLocalResource(String path) {
        paths.add(path);
        return this;
    }



}
