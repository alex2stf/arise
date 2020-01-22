package com.arise.corona.impl;

import com.arise.core.tools.ContentType;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.StreamUtil;
import com.arise.corona.dto.ContentInfo;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.images.Artwork;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PCDecoder extends ContentInfoDecoder {
    Map<String, ContentInfo> cache = new HashMap<>();


    @Override
    protected ContentInfo decodeFile(File file) {
        if (cache.containsKey(file.getAbsolutePath())){
            return cache.get(file.getAbsolutePath());
        }
        ContentInfo info = new ContentInfo(file);

        if (!file.getName().endsWith("mp3")){
            return info;
        }
        tryAudioTagger(info, file);

        cache.put(file.getAbsolutePath(), info);

        return info;
    }


    private void tryAudioTagger(ContentInfo info,File file){
        AudioFile f = null;
        try {
            f = AudioFileIO.read(file);
        } catch (Exception e) {
            f = null;
        }
        if (f == null){
            return;
        }

        Tag t = f.getTag();

        try {
            info.setAlbumName(t.getFirst(FieldKey.ALBUM));
        }catch (Exception e){

        }

        try {

        }catch (Exception e){
            info.setArtist(t.getFirst(FieldKey.ARTIST));
        }

        try {
            info.setComposer(t.getFirst(FieldKey.COMPOSER));
        }catch (Exception e){

        }

        try {
            Artwork artwork = t.getFirstArtwork();
            byte [] data = artwork.getBinaryData();
            File out = new File(info.getName() + "_art.jpg");
            FileOutputStream fileOutputStream
                    = new FileOutputStream(file);
            fileOutputStream.write(data);
            fileOutputStream.flush();
            fileOutputStream.close();
            System.out.println("Successfully written " + out.getAbsolutePath());
        } catch (Exception e){

        }

        System.out.println("Audiotagger tryed " + file.getAbsolutePath());

    }

    @Override
    protected File getStateDirectory() {
        return FileUtil.findAppDir();
    }

}
