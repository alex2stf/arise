package com.arise.rapdroid.components;

import androidx.fragment.app.Fragment;



public class ContextFragment extends Fragment {
    public void runOnUiThread(Runnable runnable){
        if (getActivity() != null) {
            getActivity().runOnUiThread(runnable);
        }
    }
}
