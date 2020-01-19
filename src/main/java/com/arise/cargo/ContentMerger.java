package com.arise.cargo;

import com.arise.core.tools.FileUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;

public class ContentMerger {

    private final Writer outputStream;

    public ContentMerger(Writer outputStream){
        this.outputStream = outputStream;
    }

    public ContentMerger add(InputStream inputStream){
        int result = 0;
        try {
            result = inputStream.read();
            while(result != -1) {
                outputStream.write(result);
                result = inputStream.read();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }


    public ContentMerger addSmartStream(String path){
        InputStream inputStream = FileUtil.findStream(path);
        if (inputStream != null){
            add(inputStream);
        }
        return this;
    }


    public ContentMerger write(String ... strings) {
        for (String s: strings){
            try {
                outputStream.write(s);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return this;
    }

    public void close(){
        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
