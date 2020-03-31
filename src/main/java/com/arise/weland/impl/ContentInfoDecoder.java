package com.arise.weland.impl;

import com.arise.core.serializers.parser.Groot;
import com.arise.core.tools.AppCache;
import com.arise.core.tools.ContentType;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.StreamUtil;
import com.arise.core.tools.StringUtil;
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
    protected Map<String, byte[]> bytesCache = new ConcurrentHashMap<>();

    ContentInfo currentInfo;
    protected ContentInfoProvider provider;

    public ContentInfoDecoder(){
        File cache = getCache();
        if (cache.exists()){
            try {
                byte bytes[] = StreamUtil.fullyReadFileToBytes(getCache());
                Map m = (Map) Groot.decodeBytes(bytes);
                if (!m.isEmpty()) {
                    ContentInfo info = find(m);
                    currentInfo = info;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public final ContentInfo decode(File file, File root){
        if (contentCache.containsKey(file.getAbsolutePath())){
            ContentInfo info = contentCache.get(file.getAbsolutePath());
            if (info != null){
                return info;
            }
        }
        ContentInfo contentInfo = decodeFile(file);
        if (file.getParentFile() != null){
            contentInfo.setGroupId(file.getParentFile().getName());
        }
        return contentInfo;
    }

    protected abstract ContentInfo decodeFile(File file);

    protected abstract File getStateDirectory();

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
        this.currentInfo = currentInfo;
        FileUtil.writeStringToFile(getCache(), currentInfo.toString());
    };



    public abstract byte[] getThumbnail(String id);

    public abstract ContentType getThumbnailContentType(String id);

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
}
