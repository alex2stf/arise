package com.arise.weland.impl;

import com.arise.core.tools.ThreadUtil;
import com.arise.weland.utils.JPEGOfferResponse;
import com.arise.weland.utils.MJPEGResponse;
import com.github.sarxos.webcam.Webcam;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
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
        ThreadUtil.closeTimer(timerResult);
        started = false;
        if (webcam == null){
            return;
        }
        webcam.close();

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

   private static ThreadUtil.TimerResult timerResult;



    public boolean isRunning() {
        return webcam != null ? webcam.isOpen() : false;
    }

    public synchronized void start() {
        if (webcam == null){
            webcam = Webcam.getDefault();
        }

        if (!webcam.isOpen()){
            webcam.open();
        }
        started = true;
        ThreadUtil.closeTimer(timerResult);
        timerResult = ThreadUtil.repeatedTask(new Runnable() {
            @Override
            public void run() {
                if(!started){
                    ThreadUtil.closeTimer(timerResult);
                    System.out.println("closing time");
                    return;
                }
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
