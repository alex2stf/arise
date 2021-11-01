package com.arise.rapdroid.net;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.SurfaceHolder;

import com.arise.core.tools.Provider;
import com.arise.core.tools.models.CompleteHandler;
import com.arise.rapdroid.media.server.MainActivity;
import com.arise.weland.utils.JPEGOfferResponse;
import com.arise.weland.utils.MJPEGResponse;
import com.arise.rapdroid.components.CameraWorker;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CamStreamResponse extends CameraWorker {


    private final MJPEGResponse mjpegResponse;
    private final JPEGOfferResponse jpegOfferResponse;



    private volatile String lightMode = Camera.Parameters.FLASH_MODE_OFF;
    private volatile String pendingLightMode = Camera.Parameters.FLASH_MODE_OFF;
    private volatile boolean allowFrameSend = true;
    private int frameWidth = 0;
    private int frameHeight = 0;
    private int frameFormat = ImageFormat.UNKNOWN;
    private Rect rectangle;
    private YuvImage image;
    Map<Integer, Camera> cams = new ConcurrentHashMap<>();


    private Handler mainHandler;

//    private Provider<SurfaceHolder> surfaceProvider;

    private SurfaceSolver surfaceSolver;

    public CamStreamResponse(final MJPEGResponse mjpegResponse, final JPEGOfferResponse jpegOfferResponse) {
        this.mjpegResponse = mjpegResponse;
        this.jpegOfferResponse = jpegOfferResponse;
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                mainHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        if (msg.obj instanceof byte[]) {
                            pushCurrentData((byte[]) msg.obj);
                        }
                    }
                };
                Looper.loop();
            }
        }).start();
    }


    private void pushCurrentData(byte[] data) {
        allowFrameSend = false;
        try {
            if (frameFormat == ImageFormat.JPEG) {
                mjpegResponse.pushJPEGFrame(data);
                jpegOfferResponse.offerJPEG(data);
            } else {

                image = new YuvImage(data, frameFormat, frameWidth, frameHeight, null);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();

                image.compressToJpeg(rectangle, 20, stream);
                byte[] imgBytes = stream.toByteArray();
                mjpegResponse.pushJPEGFrame(imgBytes);
                jpegOfferResponse.offerJPEG(imgBytes);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        allowFrameSend = true;
    }

    public synchronized CamStreamResponse startStream() {
        if (recording) {
            return this;
        }
        recording = true;

        for (Map.Entry<Integer, Camera> e : cams.entrySet()) {
            if (!e.getKey().equals(cameraIndex)) {
                Camera c = e.getValue();
                c.stopPreview();
                c.setPreviewCallback(null);
                c.release(); //da nu da realease nu face switch
                cams.remove(e.getKey());
            }

        }

        if (!cams.containsKey(cameraIndex)) {
            Camera newCamera = CameraWorker.getCameraInstance(cameraIndex);
            Camera.Parameters parameters = newCamera.getParameters();
            parameters.setPreviewFpsRange(10000, 30000);


            newCamera.setParameters(parameters);
//

            cams.put(cameraIndex, newCamera);
            log.info("CAMERA " + cameraIndex + " CREATED AT " + new Date());


            newCamera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] bytes, Camera cam) {
                    if (!recording || !allowFrameSend) {
                        return;
                    }

                    try {
                        Camera.Parameters localParams = cam.getParameters();



                        frameFormat = localParams.getPreviewFormat();
                        Camera.Size size = localParams.getPreviewSize();
                        frameWidth = size.width;
                        frameHeight = size.height;
                        rectangle = new Rect(0, 0, frameWidth, frameHeight);
                        if (bytes != null) {
                            sendBytes(bytes);
                        } else {
                            System.out.println("null data");
                        }

                        if (!lightMode.equals(pendingLightMode)) {
                            localParams.setFlashMode(pendingLightMode);
                            lightMode = pendingLightMode;
                            try {
                                cam.setParameters(localParams);
                            }catch (Exception e){
                                log.info("Failed to set flash mode " + lightMode);
                            }

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });


            this.surfaceSolver.completeHandler = new CompleteHandler<SurfaceHolder>() {
                @Override
                public void onComplete(SurfaceHolder data) {
                    try {
                        newCamera.setPreviewDisplay(data);
                        startPreview(newCamera);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            this.surfaceSolver.solve();




        } else {
            startPreview(cams.get(cameraIndex));
        }
        return this;
    }


    private void startPreview(Camera camera) {

        try {
            camera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public synchronized void stopPreviews() {
        if (!recording) {
            return;
        }
        for (Camera camera : cams.values()) {
            camera.stopPreview();
//            camera.setPreviewCallback(null);
        }
        recording = false;
    }


//    protected boolean prepare(){
//
//
//
//        try {
//            Camera.Parameters parameters = mainCamera.getParameters();
//
//            if (lightOn) {
//                parameters.setPreviewFpsRange(10000, 30000);
//                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
//            }
//
//            mainCamera.setParameters(parameters);
//        }catch (Exception ex){
//            ex.printStackTrace();
//            log.error("setParameters failed with", ex);
//            return false;
//        }
//
//
//        if (mPreview != null){
//            try {
//                mainCamera.setPreviewTexture(mPreview.getSurfaceTexture());
//            } catch (Exception ex){
//                log.error("setPreviewTexture failed ", ex);
//                return false;
//            }
//        }
//
//        if (surfaceTexture != null){
//            try {
//                mainCamera.setPreviewTexture(surfaceTexture);
//            } catch (IOException e) {
//                e.printStackTrace();
//                log.error("setPreviewTexture failed ", e);
//                return false;
//            }
//        }
//
//
//
//
//        mainCamera.setPreviewCallback(new Camera.PreviewCallback() {
//            @Override
//            public void onPreviewFrame(byte[] data, Camera camera) {
//                if(!recording || !allowFrameSend){
//                    return;
//                }
//
//                try {
//                    Camera.Parameters parameters = camera.getParameters();
//                    frameFormat = parameters.getPreviewFormat();
//                    Camera.Size size = parameters.getPreviewSize();
//                    frameWidth = size.width;
//                    frameHeight = size.height;
//                    rectangle = new Rect(0, 0, frameWidth, frameHeight);
//                    rbytes = data;
//                    sendBytes(rbytes);
//                } catch (Exception e){
//                    e.printStackTrace();
//                }
//            }
//        });
//
//        try {
//            mainCamera.startPreview();
//        } catch (Exception e) {
//            log.e("Surface texture is unavailable or unsuitable" + e.getMessage());
//        }
//
//        recording = true;
//
//        return true;
//    }


    private void sendBytes(byte[] bytes) {
        allowFrameSend = false;
        Message message = new Message();
        message.obj = bytes;
        mainHandler.sendMessage(message);
    }


    public synchronized boolean isRecording() {
        return recording;
    }

    public synchronized boolean setLightMode(String newMode) {
        boolean upd = !pendingLightMode.equals(newMode);
        if (upd){
            pendingLightMode = newMode;
        }
        return upd;
    }

    public synchronized void restart() {
        recording = false;


        startStream();
    }

    public void release() {
        for (Camera c : cams.values()) {
            releaseCamera(c);
        }
    }




    public CamStreamResponse setSurfaceSolver(SurfaceSolver surfaceSolver) {
        this.surfaceSolver = surfaceSolver;

        return this;
    }

    public abstract static class SurfaceSolver {

        public abstract void solve();

        CompleteHandler<SurfaceHolder> completeHandler;
        public final void onFound(SurfaceHolder surfaceHolder){
            completeHandler.onComplete(surfaceHolder);
        }
    }
}
