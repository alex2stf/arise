package com.arise.rapdroid.components;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.view.TextureView;

import com.arise.core.tools.Mole;

public abstract class CameraWorker {

    protected static final Mole log = Mole.getInstance(CameraWorker.class);

    protected static volatile Camera mainCamera;

    protected TextureView mPreview;
    protected SurfaceTexture surfaceTexture;

    protected volatile boolean recording = false;
    protected int cameraIndex;

    public CameraWorker setPreview(TextureView mPreview) {
        this.mPreview = mPreview;
        return this;
    }

    public CameraWorker setPreview(SurfaceTexture surfaceTexture) {
        this.surfaceTexture = surfaceTexture;
        return this;
    }

    public void releaseCamera(){
        if (mainCamera != null){
            mainCamera.setPreviewCallback(null);
            mainCamera.stopPreview();
            mainCamera.release();
            mainCamera = null;
            recording = false;
        }
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

    protected abstract boolean prepare();


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

    public void setCameraIndex(int cameraIndex) {
        this.cameraIndex = cameraIndex;
    }
}
