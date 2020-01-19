package com.arise.rapdroid.activities;

import android.Manifest;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.arise.core.tools.ContentType;
import com.arise.core.tools.FileUtil;
import com.arise.rapdroid.components.FileMediaRecorder;

import java.io.File;


public abstract class CameraRecorder extends com.arise.rapdroid.RAPDroidActivity {


    private TextureView mPreview;

    private Button captureButton;

    private FileMediaRecorder fileMediaRecorder = new FileMediaRecorder();


    @Override
    protected String[] permissions() {
        return new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA,
        };
    }

    protected abstract TextureView getTextureView();
    protected abstract Button getCaptureButton();
    protected abstract int getLayoutId();

    @Override
    protected void afterPermissionsGranted(@Nullable Bundle savedInstanceState) {
        setContentView(getLayoutId());
        mPreview = getTextureView();
        captureButton = getCaptureButton();

        if(mPreview.getParent() == null) {
            this.addContentView(mPreview, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
        }
        //refreshUI the preview
        fileMediaRecorder.setCaptureRate(2); //2fps for fast forwarding
        fileMediaRecorder.setPreview(mPreview);
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCapture();
            }
        });
        fileMediaRecorder.setEventHandler(new FileMediaRecorder.EventHandler() {
            @Override
            public File getOutputFile() {
                return FileUtil.getNextFile(ContentType.VIDEO_MP4, "FastRecorder");
            }

            @Override
            public void onMediaTaskPostExecute(Boolean result) {
                if (!result) {
                    CameraRecorder.this.finish();
                }
                // inform the user that recording has started
                setCaptureButtonText("Stop");
            }
        });
    }


    private void startCapture(){
        fileMediaRecorder.startCapture();
        setCaptureButtonText("Capture");
    }

    private void setCaptureButtonText(String title) {
        captureButton.setText(title);
    }


    @Override
    protected void onPause() {
        super.onPause();
        fileMediaRecorder.onPause();
    }

}
