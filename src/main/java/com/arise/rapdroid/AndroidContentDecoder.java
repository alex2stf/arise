package com.arise.rapdroid;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;

import com.arise.core.tools.ContentType;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.StreamUtil;
import com.arise.core.tools.StringUtil;
import com.arise.core.tools.ThreadUtil;
import com.arise.core.tools.Util;
import com.arise.core.tools.models.CompleteHandler;
import com.arise.weland.IDGen;
import com.arise.weland.dto.ContentInfo;
import com.arise.weland.impl.ContentInfoDecoder;
import com.arise.weland.impl.SuggestionService;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AndroidContentDecoder extends ContentInfoDecoder

{

    public final SuggestionService suggestionService = new SuggestionService()
            .load("weland/config/commons/suggestions.json");


    Map<String, Bitmap> bitmapCache = new ConcurrentHashMap<>();



    public AndroidContentDecoder(){

    }

    @Override
    public ContentInfo decode(File file) {
        ContentInfo info = fromFile(file);
        if (file.getParentFile() != null){
            info.setGroupId(file.getParentFile().getName());
        }

        return info;
    }

    @Override
    protected File getStateDirectory() {
        File f = new File(getAppDir() + File.separator + "chc");
        if (!f.exists()){
            f.mkdirs();
        }
        return f;
    }

    @Override
    public byte[] getThumbnail(String id) {
        if (bytesCache.containsKey(id)){
            return bytesCache.get(id);
        }
        if (id.startsWith("data:")){
            try {
                return SuggestionService.decodeBase64Image(id).first();
            } catch (Exception e) {
                e.printStackTrace();
                return new byte[]{0,0};
            }
        }
        return new byte[]{0,0};
    }

    @Override
    public ContentType getThumbnailContentType(String id) {
        return ContentType.IMAGE_JPEG;
    }


    public ContentInfo fromFile(File file){

        if (contentCache.containsKey(file.getAbsolutePath())){
            return contentCache.get(file.getAbsolutePath());
        }

        ContentInfo mediaInfo = new ContentInfo(file);
        int width = 0;
        int height = 0;
        byte[] imageBytes = null;
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();


        try {
            mmr.setDataSource(file.getPath());
        } catch (Throwable e){
            mmr = null;
        }

        if (mmr != null){

            try {
                imageBytes = mmr.getEmbeddedPicture();
            } catch (Throwable t){
                imageBytes = null;
            }

            try {
                width = Integer.valueOf(
                        mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                );
            }catch (Exception e){
                width = 0;
            }


            try {
                height = Integer.valueOf(
                        mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                );
            }catch (Exception e){
                height = 0;
            }

            Bitmap thumbnail = null;
            String binaryId = IDGen.fromFile(file);
            Bitmap bmp = null;
            if (ContentType.isVideo(file)) {
                try {
                    thumbnail = mmr.getFrameAtTime();
                } catch (Exception e) {
                    thumbnail = null;
                }
                if (thumbnail == null) {
                    try {
                        thumbnail = ThumbnailUtils.createVideoThumbnail(file.getPath(),
                                MediaStore.Images.Thumbnails.FULL_SCREEN_KIND);
                    }catch (Exception e){
                        thumbnail = null;
                    }
                }
            }



            if (thumbnail != null){
                if (width == 0 || height == 0) {
                    width = thumbnail.getWidth();
                    height = thumbnail.getHeight();
                }
                if (imageBytes == null){
                    bmp = getMinifiedVersion(binaryId, thumbnail);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    imageBytes = stream.toByteArray();
                }

            }

            if (imageBytes == null){
                searchInSuggestions(mediaInfo);
            }
            else {
                bytesCache.put(binaryId, imageBytes);
                if (bmp != null){
                    bitmapCache.put(binaryId, bmp);
                }
                else {
                   getMinifiedBitmap(binaryId, imageBytes);
                }
                mediaInfo.setThumbnailId(binaryId);
            }

            int duration = 0;
            try {
                duration = Integer.valueOf(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
            }
            catch (Exception e){

            }


            mediaInfo.setTitle(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE))
                    .setAlbumName(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM))
                    .setArtist(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST))
                    .setComposer(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_COMPOSER))
                    .setDuration(duration)
            ;


            mmr.release();
        }

        mediaInfo.setWidth(width).setHeight(height);
        contentCache.put(file.getAbsolutePath(), mediaInfo);

        return mediaInfo;
    }


    public File getAppDir(){
        return FileUtil.findAppDir();
    }

    public File getImgDir(){
        File f = new File(getAppDir() + File.separator + "img");
        if (!f.exists()){
            f.mkdirs();
        }
        return f;
    }


    private void searchInSuggestions(ContentInfo mediaInfo) {
        suggestionService.searchIcons(mediaInfo.getPath(), new SuggestionService.Manager() {
            @Override
            public boolean manage(String id, String path, URL url) {
                try {
                    byte[] bytes = get(id, url, getImgDir());
                    mediaInfo.setThumbnailId(id);
                    bytesCache.put(id, bytes);
                    return true;
                }catch (Exception e){
                    return false;
                }

            }

            @Override
            public boolean manageBytes(String id, byte[] bytes, ContentType contentType) {
                if (bytes != null){
                    mediaInfo.setThumbnailId(id);
                    bytesCache.put(id, bytes);
                    return true;
                }
                return false;
            }


        });
    }






    public byte[] get(String id, URL url, File cacheDir){
        if (bytesCache.containsKey(id)){
            return bytesCache.get(id);
        }
        byte[] bytes = readLocalIfExists(cacheDir, id);
        if (bytes != null){
            bytesCache.put(id, bytes);
            return bytes;
        }


        InputStream inputStream = null;
        try {
            inputStream = url.openStream();
            bytes = StreamUtil.toBytes(inputStream);
        } catch (IOException e) {
            System.out.println("Failed to fetch " + url);
        }
        finally {
            Util.close(inputStream);
        }

        if (bytes != null){
            Bitmap minified = getMinifiedBitmap(id, bytes);
            File f = new File(cacheDir + File.separator + id);
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(f);
                minified.compress(getCompressFormat(f.getName()), 100, fileOutputStream);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Util.close(fileOutputStream);
        }

        if (id != null && bytes != null){
            bytesCache.put(id, bytes);
        }
        return bytes;
    }

    Bitmap.CompressFormat getCompressFormat(String in){
        if (in.endsWith(".png")){
            return Bitmap.CompressFormat.PNG;
        }
        return Bitmap.CompressFormat.JPEG;
    }




    public void getPreview(ContentInfo contentInfo, CompleteHandler<Bitmap> completeHandler){
        String binaryId = contentInfo.getThumbnailId();
        if (!StringUtil.hasContent(binaryId)){
            //TODO default behaviour
            return;
        }


        if (bitmapCache.containsKey(binaryId)){
            completeHandler.onComplete(bitmapCache.get(binaryId));
            return;
        }

        byte[] bytes = null;
        if (bytesCache.containsKey(binaryId)){
            bytes = bytesCache.get(binaryId);
        }



        if (bytes != null && bytes.length > 1){
            completeHandler.onComplete(getMinifiedBitmap(binaryId, bytes));
            return;
        }

        if (binaryId.startsWith("http")) {
            URL uri;
            try {
                uri = new URL(binaryId);
            } catch (MalformedURLException e) {
                uri = null;
                return;
            }
            if (uri == null){
                completeHandler.onComplete(null);
                return;
            }

            String actualBinaryId = IDGen.parsePath(binaryId);
            URL finalUri = uri;
            ThreadUtil.fireAndForget(new Runnable() {
                @Override
                public void run() {
                    byte localBytes[] = get(actualBinaryId, finalUri, getStateDirectory());
                    if (localBytes != null){
                        completeHandler.onComplete(getMinifiedBitmap(actualBinaryId, localBytes));
                    }
                    else {
                        completeHandler.onComplete(null);
                    }
                }
            });


        }
        else {
            completeHandler.onComplete(null);
        }


    }



    public Bitmap getMinifiedBitmap(String binaryId, byte bytes[]){
        if (bitmapCache.containsKey(binaryId)){
            return bitmapCache.get(binaryId);
        }
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        if (bitmap != null) {
            return getMinifiedVersion(binaryId, bitmap);
        }
        return null;
    }

    Bitmap getMinifiedVersion(String binaryId, Bitmap bitmap){
        if (bitmapCache.containsKey(binaryId)){
            return bitmapCache.get(binaryId);
        }
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        int newWidth = (width * 350) / height;
        Bitmap min = Bitmap.createScaledBitmap(bitmap, newWidth, 350, false);
        bitmapCache.put(binaryId, min);
        return min;
    }


    public Bitmap getBitmapById(String thumbnailId){
        if (bitmapCache.containsKey(thumbnailId)) {
            return bitmapCache.get(thumbnailId);
        }
        byte [] bytes = readLocalIfExists(getImgDir(), thumbnailId);
        if (bytes != null){
            bytesCache.put(thumbnailId, bytes);

            return getMinifiedBitmap(thumbnailId, bytes);

        }
        return null;
    }


}
