package com.arise.weland.impl;

import com.arise.core.tools.FileUtil;
import com.arise.core.tools.Mole;
import com.arise.weland.dto.ContentInfo;

import java.io.File;


public class PCDecoder extends ContentInfoDecoder {

    private static final Mole log = Mole.getInstance(PCDecoder.class);


    public PCDecoder(){
        //adauga convertoare de PC:

    }

    public static File thumbnailsDirectory(){
        File f = new File(FileUtil.findAppDir(), "wlndicns");
        if (!f.exists()){
            f.mkdirs();
        }
        return f;
    }





    @Override
    public ContentInfo decode(File file) {
        if (contentCache.containsKey(file.getAbsolutePath())){
            return contentCache.get(file.getAbsolutePath());
        }
        final ContentInfo info = new ContentInfo(file);
        if (file.getParentFile() != null){
            info.setGroupId(file.getParentFile().getName());
        }

        this.searchThumbnail(info);


        contentCache.put(file.getAbsolutePath(), info);

        return info;
    }



    @Override
    protected File getStateDirectory() {
       return thumbnailsDirectory();
    }



}