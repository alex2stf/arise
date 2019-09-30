package com.arise.core.tools;


import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class StreamUtil {


    public static byte[] read(File file) throws IOException{

        ByteArrayOutputStream ous = null;
        InputStream ios = null;
        try {
            byte[] buffer = new byte[4096];
            ous = new ByteArrayOutputStream();
            ios = new FileInputStream(file);
            int read = 0;
            while ((read = ios.read(buffer)) != -1) {
                ous.write(buffer, 0, read);
            }
        }finally {
            try {
                if (ous != null)
                    ous.close();
            } catch (IOException e) {
            }

            try {
                if (ios != null)
                    ios.close();
            } catch (IOException e) {
            }
        }
        return ous.toByteArray();
    }


    public static void readLineByLine(InputStream inputStream, LineIterator lineIterator){
        String[] lines =  toString(inputStream).split("\n");
        for (int i = 0; i < lines.length; i++){
            lineIterator.onLine(i, lines[i]);
        }
    }

    public static String toString(InputStream inputStream){
        BufferedInputStream bis = new BufferedInputStream(inputStream);
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int result = 0;
        try {
            result = bis.read();
            while(result != -1) {
                buf.write((byte) result);
                result = bis.read();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buf.toString();
    }

    public static InputStream readResource(String resource){
        InputStream response = StreamUtil.class.getResourceAsStream(resource);
        if (response == null){
            response = StreamUtil.class.getClassLoader().getResourceAsStream(resource);
        }
        if (response == null){
            response = StreamUtil.class.getClass().getResourceAsStream(resource);
        }
        if(response == null){
            response = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
        }
        return response;
    }

    public static byte[] toBytes(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        return buffer.toByteArray();
    }

    public interface LineIterator {
        void onLine(int lineNo, String content);
    }
}
