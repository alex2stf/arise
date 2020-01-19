package com.arise.rapdroid;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;

import com.arise.core.tools.ContentType;
import com.arise.corona.dto.ContentInfo;
import com.arise.corona.impl.ContentInfoDecoder;

import java.io.File;

public class AndroidContentDecoder extends ContentInfoDecoder {

    @Override
    public ContentInfo decode(File file) {
        return fromFile(file);
    }

    public static ContentInfo fromFile(File file){
        ContentInfo mediaInfo = new ContentInfo(file);
        if (!ContentType.isMedia(file)){
            return mediaInfo;
        }
        int width = 0;
        int height = 0;
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();

        try {
            mmr.setDataSource(file.getPath());
        } catch (Throwable e){
            mmr = null;
        }

        if (mmr != null){

            if (ContentType.isVideo(file)){
                try {
                    width = Integer.valueOf(
                            mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                    );
                }catch (Exception e){

                }


                try {
                    height = Integer.valueOf(
                            mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                    );
                }catch (Exception e){

                }

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
