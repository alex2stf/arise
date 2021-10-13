package com.arise.core.tools;


import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class StreamUtil {




    public static byte[] fullyReadFileToBytes(File f) throws IOException {
        int size = (int) f.length();
        byte bytes[] = new byte[size];
        byte tmpBuff[] = new byte[size];
        FileInputStream fis= new FileInputStream(f);;
        try {

            int read = fis.read(bytes, 0, size);
            if (read < size) {
                int remain = size - read;
                while (remain > 0) {
                    read = fis.read(tmpBuff, 0, remain);
                    System.arraycopy(tmpBuff, 0, bytes, size - remain, read);
                    remain -= read;
                }
            }
        }  catch (IOException e){
            throw e;
        } finally {
            fis.close();
        }

        return bytes;
    }


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
        transfer(bis, buf);
        try {
            bis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buf.toString();
    }


    public static void transfer(InputStream in, OutputStream out, int bufferSize) throws IOException {
        byte[] buffer = new byte[bufferSize];
        int len = in.read(buffer);
        while (len != -1) {
            out.write(buffer, 0, len);
            len = in.read(buffer);
        }
    }

    public static void transfer(InputStream bis, OutputStream buf){
        int result = 0;
        try {
            result = bis.read();
            while(result != -1) {
//            while(result > 0) {
                buf.write((byte) result);
                result = bis.read();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
