package com.arise.rapdroid.components;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.TextureView;

import com.arise.core.tools.AppCache;
import com.arise.core.tools.Mole;

public abstract class CameraWorker {

    protected static final Mole log = Mole.getInstance(CameraWorker.class);

//    protected static volatile Camera mainCamera;

    protected TextureView mPreview;
    protected SurfaceTexture surfaceTexture;

    protected volatile boolean recording = false;
    protected volatile int cameraIndex;

    public int getCameraIndex() {
        return cameraIndex;
    }

    public static Camera getDefaultCameraInstance() {
        int numberOfCameras = Camera.getNumberOfCameras();
        Camera camera = null;


        for (int i = 0; i < numberOfCameras; i++) {
            try {
                camera = Camera.open(i);
            } catch (Exception e) {
                log.error("FAILED TO OPEN CAMERA", i);
                e.printStackTrace();
            } finally {
                if (camera != null) {
                    return camera;
                }
            }
        }
        return null;
    }


//    private static class CameraHandlerThread extends HandlerThread {
//        Handler mHandler = null;
//
//        CameraHandlerThread() {
//            super("CameraHandlerThread");
//            start();
//            mHandler = new Handler(getLooper());
//        }
//
//        synchronized void notifyCameraOpened() {
//            notify();
//        }
//
//        void openCamera(int index) {
//            mHandler.post(new Runnable() {
//                @Override
//                public void run() {
//                    getCameraInstance(index)
//                    notifyCameraOpened();
//                }
//            });
//
//            try {
//                wait();
//            }
//            catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    private CameraHandlerThread mThread = null;
//    private void newOpenCamera(int index) {
//        if (mThread == null) {
//            mThread = new CameraHandlerThread();
//        }
//
//        synchronized (mThread) {
//            mThread.openCamera(index);
//        }
//    }


    public static Camera getCameraInstance(int index) {
        int numberOfCameras = Camera.getNumberOfCameras();
        Camera camera = null;

        try {
            camera = Camera.open(index);
        }
        catch (Exception e){
            log.error("FAILED TO OPEN CAMERA at index", index);
        }
        if (camera != null){
            return camera;
        }

        for (int i = 0; i < numberOfCameras; i++) {
            try {
                camera = Camera.open(i);
            } catch (Exception e) {
                log.error("FAILED TO OPEN CAMERA", i);
                e.printStackTrace();
            } finally {
                if (camera != null) {
                    return camera;
                }
            }
        }
        return null;
    }

    public CameraWorker setPreview(TextureView mPreview) {
        this.mPreview = mPreview;
        return this;
    }

    public CameraWorker setPreview(SurfaceTexture surfaceTexture) {
        this.surfaceTexture = surfaceTexture;
        return this;
    }

    public void releaseCamera(Camera camera){
        recording = false;
        if (camera != null){
            camera.stopPreview();
            camera.setPreviewCallback(null);
            camera.release();
        }
    }



    public synchronized boolean updateCameraIndex(int newIndex) {
        boolean upd = newIndex != cameraIndex;
        if (upd){
            this.cameraIndex = newIndex;
        }
        return upd;
    }
}
