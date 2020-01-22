package com.arise.corona.impl;

import com.arise.core.serializers.parser.Groot;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.Mole;
import com.arise.core.tools.StreamUtil;
import com.arise.core.tools.StringUtil;
import com.arise.corona.dto.ContentInfo;
import com.arise.corona.utils.CoronaServerHandler;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.arise.core.tools.StringUtil.hasContent;

public abstract class ContentInfoDecoder {

    protected Map<String, ContentInfo> contentCache = new HashMap<>();

    ContentInfo currentInfo;

    public ContentInfoDecoder(){
        File cache = getCache();
        if (cache.exists()){
            try {
                byte bytes[] = StreamUtil.fullyReadFileToBytes(getCache());
                Map m = (Map) Groot.decodeBytes(bytes);
                ContentInfo info = find(m);
                currentInfo = info;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public final ContentInfo decode(File file){
        if (contentCache.containsKey(file.getAbsolutePath())){
            ContentInfo info = contentCache.get(file.getAbsolutePath());
            if (info != null){
                return info;
            }
        }
        return decodeFile(file);
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


}
