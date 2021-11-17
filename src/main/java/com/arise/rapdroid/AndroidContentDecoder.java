package com.arise.rapdroid;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import com.arise.core.tools.ContentType;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.StringEncoder;
import com.arise.weland.dto.ContentInfo;
import com.arise.weland.impl.ContentInfoDecoder;
import com.arise.weland.impl.SuggestionService;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AndroidContentDecoder extends ContentInfoDecoder

{



    Map<String, Bitmap> bitmapCache = new ConcurrentHashMap<>();


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




    public ContentInfo fromFile(File file){

        if (contentCache.containsKey(file.getAbsolutePath())){
            return contentCache.get(file.getAbsolutePath());
        }

        ContentInfo mediaInfo = new ContentInfo(file);
        String thumbnailId = StringEncoder.encodeShiftSHA(file.getName());

        int width = 0;
        int height = 0;
        byte[] thumbnailBytes = null;
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();


        try {
            mmr.setDataSource(file.getPath());
        } catch (Throwable e){
            mmr = null;
        }

        if (mmr != null){

            //getThumbnail for music
            try {
                thumbnailBytes = mmr.getEmbeddedPicture();
            } catch (Throwable t){
                thumbnailBytes = null;
            }

            if (ContentType.isVideo(file)) {
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


                if (thumbnailBytes == null){
                    Bitmap thumbnailBitmap = null;
                    Bitmap bmp = null;

                    try {
                        thumbnailBitmap = mmr.getFrameAtTime();
                    } catch (Exception e) {
                        thumbnailBitmap = null;
                    }

                    if (thumbnailBitmap == null) {
                        try {
                            thumbnailBitmap = ThumbnailUtils.createVideoThumbnail(file.getPath(),
                                    MediaStore.Images.Thumbnails.FULL_SCREEN_KIND);
                        }catch (Exception e){
                            thumbnailBitmap = null;
                        }
                    }

                    if (thumbnailBitmap != null){
                        //daca nu a luat dimensiunile din video le ia din thumbnail, ca e facut FULL_SCREEN
                        if (width == 0 || height == 0) {
                            width = thumbnailBitmap.getWidth();
                            height = thumbnailBitmap.getHeight();
                        }
                        if (thumbnailBytes == null){
                            bmp = getMinifiedVersion(thumbnailId, thumbnailBitmap);
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                            thumbnailBytes = stream.toByteArray();
                        }
                    }
                } //exit logica de video cu bitmap daca nu a mers getEmbeddedPicture
            }//exit thumbnail logic for videos

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
                    .setDuration(duration);


            mmr.release();
        } //exit mmr

        if (thumbnailBytes != null){

            dataCache.put(thumbnailId, new SuggestionService.Data(
                    thumbnailId,
                    thumbnailBytes,
                    ContentType.IMAGE_JPEG)
            );
            mediaInfo.setThumbnailId(thumbnailId);
        }
        else {
            fixThumbnails(mediaInfo);
        }

        mediaInfo.setWidth(width)
                .setHeight(height);
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









//    public byte[] get(String id, URL url, File cacheDir){
//        if (bytesCache.containsKey(id)){
//            return bytesCache.get(id);
//        }
//        byte[] bytes = readLocalIfExists(cacheDir, id);
//        if (bytes != null){
//            bytesCache.put(id, bytes);
//            return bytes;
//        }
//
//
//        InputStream inputStream = null;
//        try {
//            inputStream = url.openStream();
//            bytes = StreamUtil.toBytes(inputStream);
//        } catch (IOException e) {
//            System.out.println("Failed to fetch " + url);
//        }
//        finally {
//            Util.close(inputStream);
//        }
//
//        if (bytes != null){
//            Bitmap minified = getMinifiedBitmap(id, bytes);
//            File f = new File(cacheDir + File.separator + id);
//            FileOutputStream fileOutputStream = null;
//            try {
//                fileOutputStream = new FileOutputStream(f);
//                //TODO:::AICI e logica de compress
//                minified.compress(getCompressFormat(f.getName()), 100, fileOutputStream);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            Util.close(fileOutputStream);
//        }
//
//        if (id != null && bytes != null){
//            bytesCache.put(id, bytes);
//        }
//        return bytes;
//    }

//    Bitmap.CompressFormat getCompressFormat(String in){
//        if (in.endsWith(".png")){
//            return Bitmap.CompressFormat.PNG;
//        }
//        return Bitmap.CompressFormat.JPEG;
//    }








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


//    public Bitmap getBitmapById(String thumbnailId){
//        if (bitmapCache.containsKey(thumbnailId)) {
//            return bitmapCache.get(thumbnailId);
//        }
//        byte [] bytes = readLocalIfExists(getImgDir(), thumbnailId);
//        if (bytes != null){
//            bytesCache.put(thumbnailId, bytes);
//
//            return getMinifiedBitmap(thumbnailId, bytes);
//
//        }
//        return null;
//    }


}
