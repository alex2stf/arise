package com.arise.weland.impl;

import com.arise.core.serializers.parser.Groot;
import com.arise.core.tools.AppCache;
import com.arise.core.tools.ContentType;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.StreamUtil;
import com.arise.core.tools.StringUtil;
import com.arise.core.tools.models.FoundHandler;
import com.arise.weland.dto.ContentInfo;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import static com.arise.core.tools.StringUtil.hasContent;

public abstract class ContentInfoDecoder {


    protected Map<String, ContentInfo> contentCache = new HashMap<>();
    protected Map<String, SuggestionService.Data> dataCache = new HashMap<>();

    protected ContentInfoProvider provider;

    public final SuggestionService suggestionService = new SuggestionService()
            .load("weland/config/commons/suggestions.json");

    private static final byte[] EMPTY_BYTE = new byte[0];


    public ContentInfoDecoder(){
        suggestionService.setCacheStrategy(new SuggestionService.CacheStrategy(){
            @Override
            public boolean contains(String id) {
                if (dataCache.containsKey(id)){
                    return true;
                }
                File f = new File(getStateDirectory(), id);
                return f.exists();
            }

            @Override
            public SuggestionService.Data get(String id) {
                if (dataCache.containsKey(id)){
                    return dataCache.get(id);
                }
                File f = new File(getStateDirectory(), id);
                if (f.exists()){
                    return new SuggestionService.Data(id, null, ContentType.search(f));
                }
                return null;
            }

            @Override
            public void put(String id, SuggestionService.Data data) {
                File out = new File(getStateDirectory(), id);
                if (!out.exists()){
                   writeSuggestionThumbnailToFile(data.getBytes(), out);
                }
                dataCache.put(id, data);
            }
        });
    }


    protected void writeSuggestionThumbnailToFile(byte[] bytes, File file){
        FileUtil.writeBytesToFile(bytes, file);
    }

    public byte[] getThumbnail(String id) {
        if (!StringUtil.hasText(id)){
            return EMPTY_BYTE;
        }

        if (dataCache.containsKey(id)){
            return dataCache.get(id).getBytes();
        }

        File f = new File(getStateDirectory(), id);

        if (!f.exists()){
            return EMPTY_BYTE;
        }
        try {
           return StreamUtil.fullyReadFileToBytes(f);
        } catch (Exception e) {
            e.printStackTrace();//TODO log this error
            return EMPTY_BYTE;
        }
    }

    public ContentType getThumbnailContentType(String id) {
        if (!StringUtil.hasText(id)){
            return ContentType.IMAGE_JPEG;
        }
        if (dataCache.containsKey(id)){
            return dataCache.get(id).getContentType();
        }
        File f = new File(getStateDirectory(), id);
        if (f.exists()){
            return ContentType.search(f);
        }
        return ContentType.IMAGE_JPEG;
    }

    public ContentInfo getSavedState(){
        File cache = getCache();
        if (cache.exists()){
            try {
                byte bytes[] = StreamUtil.fullyReadFileToBytes(getCache());
                Map m = (Map) Groot.decodeBytes(bytes);
                if (!m.isEmpty()) {
                    return ContentInfo.fromMap(m);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public abstract ContentInfo decode(File file);


    protected abstract File getStateDirectory();

    @Deprecated
    public final ContentInfo find(Map obj){
        String path = ContentInfo.getMediaPath(obj);
        if (hasContent(path) && contentCache.containsKey(path) && contentCache.get(path) != null){
            return contentCache.get(path);
        }
        return ContentInfo.fromMap(obj);
    };


    private File getCache(){
        return new File(getStateDirectory() + File.separator + "current_state.json");
    }

    public final void saveState(ContentInfo currentInfo){
        FileUtil.writeStringToFile(getCache(), currentInfo.toString());
    };




    public void setProvider(ContentInfoProvider contentInfoProvider) {
        this.provider = contentInfoProvider;
    }

    public void onScanComplete() {


    }

    protected byte[] readLocalIfExists( File cacheDir, String id){
        byte[] bytes = null;
        File f = new File(cacheDir + File.separator + id);
        if (f.exists()){
            try {
                bytes = StreamUtil.fullyReadFileToBytes(f);
            } catch (IOException e) {
                bytes = null;
            }
        }
        return bytes;
    }

    public void clearState() {
        FileUtil.writeStringToFile(getCache(), "{}");
    }

    public void fixThumbnails(ContentInfo i) {
        SuggestionService.Data data = suggestionService.solveThumbnail(i);
        if (data != null){
            i.setThumbnailId(data.getId());
        } else {
            i.setThumbnailId(null);
        }

    }


    public SuggestionService.Data fixThumbnail(String thumbnail) {
       return suggestionService.solveUrlOrBase64(thumbnail);
    }
}
