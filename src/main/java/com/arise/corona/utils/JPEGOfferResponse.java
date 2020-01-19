package com.arise.corona.utils;

import com.arise.astox.net.models.HttpProtocol;
import com.arise.astox.net.models.http.HttpResponse;
import com.arise.core.tools.ContentType;

import java.nio.charset.Charset;
import java.util.Date;

public class JPEGOfferResponse extends HttpResponse {
    public JPEGOfferResponse(){
        setStatusCode(200)
                .setStatusText("OK")
                .setProtocol(HttpProtocol.V1_0)
                .addHeader("Server", "Astox-Srv")
                .addHeader("Date", new Date())
                .setContentType(ContentType.IMAGE_JPEG);
    }

    volatile boolean assigning = false;

    byte[] img = new byte[]{0};

    byte[][] imgBuffer = new byte[][]{
            null, null, null
    };

    @Override
    public  byte[] bytes() {
        byte[] headBytes = (headerLine().getBytes(Charset.forName("UTF-8")));
//        assigning = true;
//        byte[] body = img;
//        assigning = false;
        byte[] body = getFromImgBuffer();
        int bodyLength = body.length;

        byte[] result = new byte[headBytes.length + bodyLength];
        for (int i = 0; i < headBytes.length; i++){
            result[i] = headBytes[i];
        }
        if (body != null){
            for (int i = 0; i < body.length; i++){
                result[i + headBytes.length] = body[i];
            }
        }
        return result;
    }

    int offerCount = -1;
    int getCount = -1;


    public byte[] getFromImgBuffer(){
        synchronized (this) {
            getCount++;
            if (getCount > imgBuffer.length - 1) {
                getCount = 0;
            }
//            System.out.println("IM_GET " + getCount);
        }
        return imgBuffer[getCount];
    }

    public void offerJPEG(byte[] bytes) {
        synchronized (this){
            offerCount++;
            if (offerCount > imgBuffer.length -1){
                offerCount = 0;
            }
//            System.out.println("IM_PUT " + offerCount);
        }
        imgBuffer[offerCount] = bytes;
    }

    public synchronized void offerJPEG2(byte[] bytes) {
        synchronized (this){
            if (assigning){
                return;
            }
            assigning = true;
            img = bytes;
            assigning = false;
        }
    }
}
