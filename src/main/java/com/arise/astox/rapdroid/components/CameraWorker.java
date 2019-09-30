package com.arise.astox.rapdroid.components;

import android.hardware.Camera;
import android.view.TextureView;

import com.arise.core.tools.Mole;

public abstract class CameraWorker {

    protected static final Mole log = Mole.getInstance(CameraWorker.class);

    protected static volatile Camera mainCamera;

    protected TextureView mPreview;

    protected volatile boolean isRecording = false;

    public CameraWorker setPreview(TextureView mPreview) {
        this.mPreview = mPreview;
        return this;
    }

    public void releaseCamera(){
        if (mainCamera != null){
            mainCamera.setPreviewCallback(null);
            mainCamera.stopPreview();
            mainCamera.release();
            mainCamera = null;
            isRecording = false;
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


}
