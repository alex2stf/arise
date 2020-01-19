package com.arise.rapdroid;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;

import com.arise.core.tools.ContentType;
import com.arise.core.tools.ReflectUtil;
import com.arise.core.tools.StringUtil;
import com.arise.corona.dto.ContentInfo;

import java.io.File;

import static com.arise.core.tools.StringUtil.hasContent;

public class RUtil {

    private static String getProperty(Object mmr, String key){
        int prop = ReflectUtil.getStaticIntProperty(mmr, key);
        return ReflectUtil.getMethod( mmr, "extractMetadata").callForString(prop);
    }

    public static ContentInfo fromFile(File file){
        ContentInfo mediaInfo = new ContentInfo(file);
        if (!ContentType.isMedia(file)){
            return mediaInfo;
        }
        int width = 0;
        int height = 0;
        MediaMetadataRetriever mmr = (MediaMetadataRetriever) ReflectUtil.newInstance("android.media.MediaMetadataRetriever");

        try {
            ReflectUtil.getMethod(mmr, "setDataSource").call(file.getPath());
        } catch (Throwable e){
            mmr = null;
        }

        if (mmr != null){

            if (ContentType.isVideo(file)){


                String widthStr = getProperty(mmr, "METADATA_KEY_VIDEO_WIDTH");
                String heightStr = getProperty(mmr, "METADATA_KEY_VIDEO_HEIGHT");
                if (hasContent(widthStr)){
                    try {
                        width = Integer.valueOf(widthStr);
                    }catch (Exception e){

                    }
                }

                if (hasContent(heightStr)){
                    try {
                        height = Integer.valueOf(heightStr);
                    }catch (Exception e){

                    }
                }
//                        mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
//
//                height = Integer.valueOf(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));

                if (width == 0 || height == 0) {
                    Bitmap bmp = mmr.getFrameAtTime();
                    if (bmp != null) {
                        width = bmp.getWidth();
                        height = bmp.getHeight();
                    } else {
                       bmp = ThumbnailUtils.createVideoThumbnail(file.getPath(),
                                MediaStore.Images.Thumbnails.FULL_SCREEN_KIND);
                       if (bmp != null){
                           width = bmp.getWidth();
                           height = bmp.getHeight();
                       }
                    }
                }


            }

            mediaInfo
                    .setWidth(width).setHeight(height)
                    .setTitle(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE))
                    .setAlbumName(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM))
                    .setArtist(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST))
                    .setComposer(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_COMPOSER));
            mmr.release();
            try {
                mediaInfo.setEmbeddedPic(mmr.getEmbeddedPicture());
            } catch (Throwable t){

            }
        }


        return mediaInfo;
    }


}
