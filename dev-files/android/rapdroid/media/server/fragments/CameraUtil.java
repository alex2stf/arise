package com.arise.rapdroid.media.server.fragments;

import android.hardware.Camera;

import java.util.ArrayList;
import java.util.List;

public class CameraUtil {

    public static List<Camera.CameraInfo> getCamerasInfos(){
        List<Camera.CameraInfo> infos = new ArrayList<>();
        int numberOfCameras = Camera.getNumberOfCameras();
        //for every camera check
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            infos.add(info);
        }
        return infos;
    }
}
