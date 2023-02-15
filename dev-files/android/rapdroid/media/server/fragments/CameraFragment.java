package com.arise.rapdroid.media.server.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class CameraFragment extends Fragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    CameraLayout cameraLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (cameraLayout == null) {
            cameraLayout = new CameraLayout(getContext());
        }

        return cameraLayout;
    }


    @Override
    public void onStop() {
        if (cameraLayout != null) {
            cameraLayout.stop();
        }
        super.onStop();
    }

    @Override
    public void onPause() {
        if (cameraLayout != null) {
            cameraLayout.stop();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (cameraLayout != null) {
            cameraLayout.stop();
        }
        super.onDestroy();
    }

    @Override
    public void onResume() {
        if (cameraLayout != null) {
//            cameraLayout.resume();
        }
        super.onResume();
    }

    public void start(){
        if (cameraLayout != null){
            cameraLayout.resume();
        }
    }


    public void checkUpdateState(int camId, String lightMode) {
        cameraLayout.updateIfRequired(camId, lightMode);

    }

    public void takeSnapshot() {
        if(cameraLayout != null){
            cameraLayout.takeSnapshot();
        }
    }
}
