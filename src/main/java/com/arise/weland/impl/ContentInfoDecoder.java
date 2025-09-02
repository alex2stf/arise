package com.arise.weland.impl;

import com.arise.astox.net.models.ServerResponse;
import com.arise.astox.net.models.http.HttpResponse;
import com.arise.core.serializers.parser.Groot;
import com.arise.core.tools.ContentType;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.StreamUtil;
import com.arise.weland.dto.ContentInfo;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static com.arise.core.tools.StringUtil.hasContent;
import static com.arise.core.tools.StringUtil.hasText;

public abstract class ContentInfoDecoder {


    protected Map<String, ContentInfo> contentCache = new HashMap<>();
//    protected Map<String, SGData> dataCache = new HashMap<>();

    protected ContentInfoProvider provider;

    public final SGService sugServ = SGService.getInstance()
            .load("weland/config/commons/suggestions.json");

    public static final byte[] EMPTY_BYTE = new byte[0];






    public ServerResponse getThumbnail(String id) {
        if (!hasText(id)){
            return new HttpResponse().setBytes(EMPTY_BYTE).setContentType(ContentType.IMAGE_JPEG);
        }


        File f = new File(getStateDirectory(), id);
        byte[] bytes = EMPTY_BYTE;
        if(f.exists()) {
            try {
                bytes = StreamUtil.fullyReadFileToBytes(f);
            } catch (Exception e) {
                e.printStackTrace();//TODO log this error
                bytes = EMPTY_BYTE;
            }
        }
        else {
            Object res = SGService.getInstance().find(id);
            if(res instanceof ServerResponse){
                return (ServerResponse) res;
            }
        }

        return new HttpResponse().setBytes(bytes).setContentType(ContentType.IMAGE_JPEG);
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


    public void clearState() {
        FileUtil.writeStringToFile(getCache(), "{}");
    }

    public void searchThumbnail(ContentInfo i) {
        //TODO cauta thumbnail conform OS
//        i.setThumbnailId("cale catre fisier");

    }


//    public SGData fixThumbnail(String thumbnail) {
//       return sugServ.solveUrlOrBase64(thumbnail);
//    }
}
