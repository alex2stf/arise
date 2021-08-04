package com.arise.weland.impl;

import com.arise.core.tools.ThreadUtil;
import com.arise.weland.utils.JPEGOfferResponse;
import com.arise.weland.utils.MJPEGResponse;
import com.github.sarxos.webcam.Webcam;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Date;

public class DesktopCamStream {

    final MJPEGResponse mjpegResponse;
    final JPEGOfferResponse jpegOfferResponse;

    volatile Webcam webcam;
    volatile boolean started = false;

    public DesktopCamStream(MJPEGResponse mjpegResponse, JPEGOfferResponse jpegOfferResponse) {
        this.mjpegResponse = mjpegResponse;
        this.jpegOfferResponse = jpegOfferResponse;
    }

    public synchronized void stop() {
        if (webcam == null){
            return;
        }
        webcam.close();
        started = false;
    }

    public static byte[] toByteArray(BufferedImage bi, String format) throws IOException {
        if (bi == null){
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bi, format, baos);
//        ImageIO.write(bi, "PNG", new File("test2.png"));
        byte[] bytes = baos.toByteArray();
        return bytes;
    }

    public synchronized void start() {
        if (webcam == null){
            webcam = Webcam.getDefault();
        }
        if (!webcam.isOpen()){
            webcam.open();
        }
        started = true;
        ThreadUtil.repeatedTask(new Runnable() {
            @Override
            public void run() {
                System.out.println("take snapshot at " + new Date());
                byte[] bytes = null;
                try {
                    bytes = (toByteArray(webcam.getImage(), "JPEG"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (bytes != null) {
                    mjpegResponse.pushJPEGFrame(bytes);
                    jpegOfferResponse.offerJPEG(bytes);
                    System.out.println("OK BYTES");
                }
                else {
                    System.out.println("NULL BYTES");
                }
            }
        }, 500);
    }
}
