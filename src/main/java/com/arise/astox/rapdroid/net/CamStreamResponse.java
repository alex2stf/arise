package com.arise.astox.rapdroid.net;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.TextureView;

import com.arise.astox.net.serviceHelpers.JPEGOfferResponse;
import com.arise.astox.net.serviceHelpers.MJPEGResponse;
import com.arise.astox.rapdroid.components.CameraWorker;
import com.arise.core.tools.ThreadUtil;
import com.arise.core.tools.Util;


import java.io.ByteArrayOutputStream;

public class CamStreamResponse extends CameraWorker {



    private final MJPEGResponse mjpegResponse;
    private final JPEGOfferResponse jpegOfferResponse;


    private volatile boolean lightOn = false;
    private Thread worker;
    private android.os.Handler handler;
    private Thread frameThread;
    private volatile boolean allowFrameSend = true;
    private int frameWidth = 0;
    private int frameHeight = 0;
    private int frameFormat = ImageFormat.UNKNOWN;
    private Rect rectangle;
    private YuvImage image;
    private byte[] rbytes;


    public CamStreamResponse(final MJPEGResponse mjpegResponse, final JPEGOfferResponse jpegOfferResponse) {

        this.mjpegResponse = mjpegResponse;
        this.jpegOfferResponse = jpegOfferResponse;


        frameThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                handler = new Handler(){
                    @Override
                    public void handleMessage(Message msg) {
                        byte[] data = (byte[]) msg.obj;

                        allowFrameSend = false;
                        try {
                            if(frameFormat == ImageFormat.JPEG){
                                mjpegResponse.pushJPEGFrame(data);
                                jpegOfferResponse.offerJPEG(data);
                            }
                            else {

                                image = new YuvImage(data, frameFormat, frameWidth, frameHeight, null);
                                ByteArrayOutputStream stream = new ByteArrayOutputStream();

                                image.compressToJpeg(rectangle, 20, stream);
                                byte[] imgBytes = stream.toByteArray();
                                mjpegResponse.pushJPEGFrame(imgBytes);
                                jpegOfferResponse.offerJPEG(imgBytes);
                            }
                        } catch (Exception ex){
                            ex.printStackTrace();
                        }

                        allowFrameSend = true;

                    }
                };
                Looper.loop();
            }
        });
        frameThread.setName("Frame-Thread");
        frameThread.start();
    }



    /**
     * The capture button controls all user interaction. When recording, the button click
     * stops recording, releases {@link android.media.MediaRecorder} and {@link android.hardware.Camera}. When not recording,
     * it prepares the {@link android.media.MediaRecorder} and starts recording.
     *
     *
     */
    public CamStreamResponse startStream() {
        stop();
        worker = ThreadUtil.startThread(new Runnable() {
            @Override
            public void run() {
                if (prepare()) {
                    isRecording = true;
                } else {
                    stop();
                }
            }
        }, "CamStartStream");
        return this;
    }





    public void stop() {
        releaseCamera();
        Util.close(worker);
    }







    protected boolean prepare(){

        mainCamera = getDefaultCameraInstance();

        try {
            Camera.Parameters parameters = mainCamera.getParameters();

            if (lightOn) {
                parameters.setPreviewFpsRange(10000, 30000);
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            }

            mainCamera.setParameters(parameters);
        }catch (Exception ex){
            ex.printStackTrace();
            log.warn(ex);
        }


        try {
            if (mPreview instanceof TextureView){
                mainCamera.setPreviewTexture(((TextureView)mPreview).getSurfaceTexture());
            }
        } catch (Exception ex){
                ex.printStackTrace();
        }

//        mCamera.unlock();

        mainCamera.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                if(!isRecording || !allowFrameSend){
                    return;
                }

                try {
                    Camera.Parameters parameters = camera.getParameters();
                    frameFormat = parameters.getPreviewFormat();
                    Camera.Size size = parameters.getPreviewSize();
                    frameWidth = size.width;
                    frameHeight = size.height;
                    rectangle = new Rect(0, 0, frameWidth, frameHeight);
                    rbytes = data;
                    sendBytes(rbytes);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        try {
            mainCamera.startPreview();
        } catch (Exception e) {
            log.e("Surface texture is unavailable or unsuitable" + e.getMessage());
        }

        isRecording = true;

        return true;
    }




    private void sendBytes(byte[] bytes){
        allowFrameSend = false;
        Message message = new Message();
        message.obj = bytes;
        handler.sendMessage(message);
    }

    public void turnFlashLightOff() {
        lightOn = false;
        stop();
        startStream();
    }

    public void turnFlashLightOn() {
       lightOn = true;
       stop();
       startStream();
    }


    public void resume() {
        startStream();
    }


}
