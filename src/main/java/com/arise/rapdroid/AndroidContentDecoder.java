package com.arise.rapdroid;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.provider.MediaStore;

import com.arise.core.tools.StreamUtil;
import com.arise.core.tools.StringUtil;
import com.arise.core.tools.Util;
import com.arise.core.tools.models.CompleteHandler;
import com.arise.corona.IDGen;
import com.arise.corona.dto.ContentInfo;
import com.arise.corona.impl.ContentInfoDecoder;
import com.arise.corona.impl.SuggestionService;
import com.arise.rapdroid.media.server.AppUtil;
import com.arise.rapdroid.media.server.CoronaClient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class AndroidContentDecoder extends ContentInfoDecoder

{

    public final SuggestionService suggestionService = new SuggestionService()
            .load("corona/config/commons/suggestions.json");


    Map<String, BitmapDrawable> drawableCache = new HashMap<>();
    Map<String, Bitmap> bitmapCache = new HashMap<>();
    Map<String, byte[]> bytesCache = new HashMap<>();


    public AndroidContentDecoder(){

    }

    @Override
    public ContentInfo decodeFile(File file) {
        return fromFile(file);
    }

    @Override
    protected File getStateDirectory() {
        File f = new File(getAppDir() + File.separator + "chc");
        if (!f.exists()){
            f.mkdirs();
        }
        return f;
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
            try {
                thumbnail = mmr.getFrameAtTime();
            }catch (Exception e){
                thumbnail = null;
            }
            if (thumbnail == null) {
                thumbnail = ThumbnailUtils.createVideoThumbnail(file.getPath(),
                        MediaStore.Images.Thumbnails.FULL_SCREEN_KIND);
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
        File root = new File( Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "CoronaApp");
        if (!root.exists()){
            root.mkdirs();
        }
        return root;
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
                byte[] bytes = get(id, url, getImgDir());
                mediaInfo.setThumbnailId(id);
                bytesCache.put(id, bytes);
                return false;
            }
        });
    }




    public byte[] get(String id, URL url, File cacheDir){
        if (bytesCache.containsKey(id)){
            return bytesCache.get(id);
        }
        byte[] bytes = null;
        File f = new File(cacheDir + File.separator + id);
        if (f.exists()){
            try {
                bytes = StreamUtil.fullyReadFileToBytes(f);
            } catch (IOException e) {
                bytes = null;
            }
        }
        if (bytes != null){
            bytesCache.put(id, bytes);
            return bytes;
        }

        InputStream inputStream = null;
        try {
            inputStream = url.openStream();
            bytes = StreamUtil.toBytes(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            Util.close(inputStream);
        }

        if (bytes != null){
            Bitmap minified = getMinifiedBitmap(id, bytes);

            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(f);
                minified.compress(getCompressFormat(f.getName()), 100, fileOutputStream);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Util.close(fileOutputStream);
        }

        bytesCache.put(id, bytes);
        return bytes;
    }

    Bitmap.CompressFormat getCompressFormat(String in){
        if (in.endsWith(".png")){
            return Bitmap.CompressFormat.PNG;
        }
        return Bitmap.CompressFormat.JPEG;
    }




    public void getPreview(Object worker, ContentInfo contentInfo, CompleteHandler<BitmapDrawable> completeHandler){
        String binaryId = contentInfo.getThumbnailId();
        if (!StringUtil.hasContent(binaryId)){
            //TODO default behaviour
            return;
        }


        if (drawableCache.containsKey(binaryId)){
            completeHandler.onComplete(drawableCache.get(binaryId));
            return;
        }

        byte[] bytes = null;
        if (bytesCache.containsKey(binaryId)){
            bytes = bytesCache.get(binaryId);
        }


        final BitmapDrawable[] bitmapDrawable = {null};
        if (bytes != null && bytes.length > 1){
            bitmapDrawable[0] = new BitmapDrawable(getMinifiedBitmap(binaryId, bytes));
            drawableCache.put(binaryId, bitmapDrawable[0]);
            completeHandler.onComplete(bitmapDrawable[0]);
            return;
        }

        System.out.println("CONTINUE");

        if (worker instanceof URI){
            URI url = (URI) worker;
            if (url.getHost().equals("localhost")){
                return;
            }
        }

        if (AppUtil.workerIsLocalhost(worker)){
            return;
        }

        //TODO check worker is not localhost
        CoronaClient.findThumbnail(worker, binaryId, new CompleteHandler<byte[]>() {
            @Override
            public void onComplete(byte[] data) {
                if (data != null){
                    bitmapDrawable[0] = new BitmapDrawable(getMinifiedBitmap(binaryId, data));
                    drawableCache.put(binaryId, bitmapDrawable[0]);
                    completeHandler.onComplete(bitmapDrawable[0]);
                }
            }
        });
    }


    Bitmap getMinifiedBitmap(String binaryId, byte bytes[]){
        if (bitmapCache.containsKey(binaryId)){
            return bitmapCache.get(binaryId);
        }
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        return getMinifiedVersion(binaryId, bitmap);
    }

    Bitmap getMinifiedVersion(String binaryId, Bitmap bitmap){
        if (bitmapCache.containsKey(binaryId)){
            return bitmapCache.get(binaryId);
        }
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        int newWidth = (width * 420) / height;
        Bitmap min = Bitmap.createScaledBitmap(bitmap, newWidth, 420, false);
        bitmapCache.put(binaryId, min);
        return min;
    }


}
