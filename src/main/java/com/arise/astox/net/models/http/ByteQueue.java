package com.arise.astox.net.models.http;

import java.io.UnsupportedEncodingException;

/**
 * Created by Alexandru on 8/29/2025.
 */
public class ByteQueue {

    byte[] bytes;
    int index = 0;

    public ByteQueue(int capacity){
        bytes = new byte[capacity];
    }

    public void add(byte b){
        if (index > bytes.length -1){
            removeFirst();
            index = bytes.length - 1;
        }

        bytes[index] = b;
        index++;
    }

    public void write (byte b){
        add(b);
    }


    boolean endsWith(String s){
        try {
            byte strBytes[] = s.getBytes("UTF-8");
            if (strBytes.length > bytes.length){
                return false;
            }

            for (int i = bytes.length -1, j = strBytes.length - 1; i > -1 && j > -1; i--, j-- ){
                if (bytes[i] != strBytes[j]){
                    return false;
                }
            }



        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void removeFirst() {
        for(int i = 0; i < bytes.length - 1; i++){
            bytes[i] = bytes[i + 1];
        }
    }

    public byte[] getBytes() {
        return bytes;
    }
}
