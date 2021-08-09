package com.arise.rapdroid.net;

import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;

import com.arise.core.tools.Mole;
import com.arise.core.tools.models.CompleteHandler;
import com.arise.rapdroid.components.CameraWorker;

public class CameraHandlerThread extends HandlerThread {
    Handler mHandler = null;
    Mole log = Mole.getInstance(CameraHandlerThread.class);

    public CameraHandlerThread() {
        super("CameraHandlerThread");
        mHandler = new Handler(getLooper());
    }

    synchronized void notifyCameraOpened() {
        notify();
    }

    void openCamera(int index, CompleteHandler<Camera> onCameraFound) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Camera camera = CameraWorker.getCameraInstance(index);
                notifyCameraOpened();
                onCameraFound.onComplete(camera);
            }
        });
        try {
            wait();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
