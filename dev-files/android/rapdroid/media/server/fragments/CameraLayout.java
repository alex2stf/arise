package com.arise.rapdroid.media.server.fragments;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.arise.core.tools.AppCache;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.Mole;
import com.arise.core.tools.StringUtil;
import com.arise.weland.model.ContentHandler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.hardware.Camera.Parameters.FLASH_MODE_OFF;
import static android.hardware.Camera.Parameters.FLASH_MODE_RED_EYE;
import static android.hardware.Camera.Parameters.FLASH_MODE_TORCH;

public class CameraLayout extends FrameLayout {
    @NonNull
    private final Context context;
    LinearLayout cameraLayout;
    CameraPreview mPreview =  null;
    Camera mCamera;

    LinearLayout buttonsLayout;

    public static final int getActiveCameraIndex(){
        return AppCache.getInt(LAST_CAM_INDEX, 0);
    }


    public static final boolean isActiveCameraRecordingState(){
        return RUNNING.equals(AppCache.getString(LAST_CAM_STATE, STOPPED));
    }

    public static String getActiveFlashMode() {
        return AppCache.getString(LAST_CAM_MODE, FLASH_MODE_OFF);
    }


    private static final String LAST_CAM_INDEX = "lcs234";
    private static final String LAST_CAM_MODE = "flm568";
    private static final String LAST_CAM_STATE = "3elcm";

    private static final String STOPPED = "S";
    private static final String RUNNING = "R";

    private Camera.PictureCallback mPicture;

    private static final Mole log = Mole.getInstance(CameraLayout.class);

    AdapterView.OnItemSelectedListener spinnerListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            Integer index = (Integer) parent.getItemAtPosition(position);
            if(STOPPED.equals(AppCache.getString(LAST_CAM_STATE, STOPPED))){
                return;
            }
//            setCameraIndex(index);
            updateIfRequired(
                    index,
                    AppCache.getString(LAST_CAM_MODE, FLASH_MODE_OFF)
            );
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };


    AdapterView.OnItemSelectedListener selectLigthMode = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            String mode = (String) parent.getItemAtPosition(position);
            if(STOPPED.equals(AppCache.getString(LAST_CAM_STATE, STOPPED))){
                return;
            }

            updateIfRequired(
                    AppCache.getInt(LAST_CAM_INDEX, 0),
                    mode
            );
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };


    OnClickListener captureListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            mCamera.takePicture(null, null, mPicture);
        }
    };


    Spinner lightModeSpinner;
    ArrayAdapter<String> lightModeAdapter;

    Spinner camIndexSpinner;
    ArrayAdapter<Integer> camIndexAdapter;


    public CameraLayout(@NonNull Context context) {


        super(context);

        this.context = context;
        cameraLayout = new LinearLayout(context);
        addView(cameraLayout);


        buttonsLayout = new LinearLayout(context);
        addView(buttonsLayout);


        camIndexSpinner = new Spinner(context);
        int numberOfCameras = Camera.getNumberOfCameras();
        Integer[] items = new Integer[numberOfCameras];
        for (int i = 0; i < numberOfCameras; i++){
            items[i] = i;
        }



        camIndexAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, items);
        camIndexSpinner.setAdapter(camIndexAdapter);
        buttonsLayout.addView(camIndexSpinner);


        camIndexSpinner.setOnItemSelectedListener(spinnerListener);



        lightModeSpinner = new Spinner(getContext());
        lightModeAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, new String[]{
                FLASH_MODE_OFF,
                Camera.Parameters.FLASH_MODE_TORCH,
                Camera.Parameters.FLASH_MODE_RED_EYE
        }
        );
        lightModeSpinner.setAdapter(lightModeAdapter);

        buttonsLayout.addView(lightModeSpinner);
        lightModeSpinner.setOnItemSelectedListener(selectLigthMode);



        ImageButton captureButton = new ImageButton(context);
        captureButton.setImageResource(android.R.drawable.ic_menu_view);
        captureButton.setOnClickListener(captureListener);
        buttonsLayout.addView(captureButton);


    }



    private Camera.PictureCallback getPictureCallback() {
        Camera.PictureCallback picture = new Camera.PictureCallback() {

            @Override
            public void onPictureTaken(byte[] data, Camera camera) {


                byte[] jpeg = CameraPreview.captureFrameToJpeg(data, camera);
                //make a new picture file
                File pictureFile = new File(FileUtil.findAppDir(), "snapshot.jpeg");

                if (pictureFile == null) {
                    return;
                }
                try {
                    //write the file
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    fos.write(jpeg);
                    fos.close();
//                    Toast toast = Toast.makeText(myContext, "Picture saved: " + pictureFile.getName(), Toast.LENGTH_LONG);
//                    toast.show();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //refresh camera to continue preview
                mPreview.refreshCamera(mCamera);
            }
        };
        return picture;
    }



    private void releaseAndSetCamera(int cameraIndex, String mode){


        cameraIndex = cameraIndex < 0 ? 0 : cameraIndex;

        release_stop_free_camera();
        try {
            mCamera = Camera.open(cameraIndex);
            AppCache.putInt(LAST_CAM_INDEX, cameraIndex);
        }catch (Exception e){
            log.error("Failed to open camera index " + cameraIndex, e);
           try {
               mCamera = Camera.open();
               AppCache.putInt(LAST_CAM_INDEX, 0);
           }catch (Exception e2){
               e2.printStackTrace();

               //TODO Toast
               stop();
               return;
           }
        }

        if (isValidMode(mode)){
            Camera.Parameters p;
            try {
                p = mCamera.getParameters();
            } catch (Exception e){
                log.error("Failed to get camera  [" + cameraIndex + "] parameters", e);
                p = null;
            }
            if(p != null){
                try {
                    p.setFlashMode(mode);
//                p.setPreviewFpsRange(10000, 30000); -- use default
                    mCamera.setParameters(p);
                    AppCache.putString(LAST_CAM_MODE, mode);
                } catch (Exception e){
                    log.error("Failed to set flash mode " + mode + " for camera " + cameraIndex, e);
                }
            }
        }



        mPicture = getPictureCallback();
        mPreview.refreshCamera(mCamera);
        AppCache.putString(LAST_CAM_STATE, RUNNING);
    }


    public void releaseAndSetCameraFromCache(){
        if (!hasCamera(context)) {
            Toast toast = Toast.makeText(context, "Sorry, your phone does not have a camera!", Toast.LENGTH_LONG);
            toast.show();
            return;
        }

        if(!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)){
            Toast toast = Toast.makeText(context, "Sorry, your phone does not have flash features!", Toast.LENGTH_LONG);
            toast.show();
        }


        releaseAndSetCamera(
                AppCache.getInt(LAST_CAM_INDEX, 0),
                AppCache.getString(LAST_CAM_MODE, FLASH_MODE_OFF)
        );
    }

    private boolean hasCamera(Context context) {
        //check if the device has camera
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }






    public void release_stop_free_camera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
            AppCache.putString(LAST_CAM_STATE, STOPPED);
        }
    }


    public void release_stop_mpreview(){
        if (mPreview != null){
            mPreview.removeCallback();
            mPreview.destroyDrawingCache();
            mPreview.setCamera(null);
            mPreview = null;
            AppCache.putString(LAST_CAM_STATE, STOPPED);
        }
    }


    public void stop() {
        AppCache.putString(LAST_CAM_STATE, STOPPED);
        release_stop_free_camera();
        release_stop_mpreview();

    }

    public void resume() {
        boolean isStoppedState = STOPPED.equals(AppCache.getString(LAST_CAM_STATE, STOPPED));
        if (!isStoppedState && !instancesAreNull()){
            return;
        }
        release_stop_free_camera();
        release_stop_mpreview();

        cameraLayout.removeAllViews();
        mPreview = new CameraPreview(context);
        cameraLayout.addView(mPreview);
        releaseAndSetCameraFromCache();
        if (lightModeSpinner != null){
            lightModeSpinner.setSelection(lightModeAdapter.getPosition(AppCache.getString(LAST_CAM_MODE, FLASH_MODE_OFF)));
        }
        if (camIndexSpinner != null){
            camIndexSpinner.setSelection(camIndexAdapter.getPosition(AppCache.getInt(LAST_CAM_INDEX, 0)));
        }
        AppCache.putString(LAST_CAM_STATE, RUNNING);
    }


    private boolean isValidMode(String lightMode) {
        if(!StringUtil.hasText(lightMode)){
            return false;
        }
        return lightMode.equals(FLASH_MODE_OFF) ||
                lightMode.equals(FLASH_MODE_RED_EYE) ||
                lightMode.equals(FLASH_MODE_TORCH);
    }

    private boolean instancesAreNull(){
        return (mPreview == null && mCamera == null);
    }

    public void updateIfRequired(int camId, String lightMode) {
        boolean shouldUpdate = false;
        if (camId != AppCache.getInt(LAST_CAM_INDEX, 0) && camId > -1){
            shouldUpdate = true;
        }

        if (isValidMode(lightMode) && !lightMode.equals(AppCache.getString(LAST_CAM_MODE, FLASH_MODE_OFF))){
            shouldUpdate = true;
        }

        if (instancesAreNull() || shouldUpdate){
            AppCache.putInt(LAST_CAM_INDEX, camId > -1 ? camId : 0);
            AppCache.putString(LAST_CAM_MODE, lightMode);
            AppCache.putString(LAST_CAM_STATE, STOPPED);
            resume();
        }

    }


    public void takeSnapshot() {
        if (mPreview != null){
            mPreview.takeSnapshot();
        }

    }
}
