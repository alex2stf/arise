package com.arise.droid.fragments;

import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.arise.core.tools.Mole;
import com.arise.droid.tools.ContextFragment;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


public class BrowserFragment extends ContextFragment {
    private static final Mole log = Mole.getInstance(BrowserFragment.class);

    private volatile static boolean created = false;


    public static WebView webView;

    private static BrowserFragment self;


    static final BlockingQueue<String> urlQueue = new ArrayBlockingQueue<>(4);
    public static void openUrlOnMainThread(String path) {
        if (self != null){
            self.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                   self.loadUrl(path);
                }
            });
        } else {
            urlQueue.add(path);
        }
    }

    public static void stopWebViewOnMainThread() {
        if (self != null){
            self.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    stop(webView);
                }
            });
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (!created){
            webView = new WebView(getContext());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
            }
            self = this;

            if (!urlQueue.isEmpty()){
                try {
                    loadUrl(urlQueue.take());
                } catch (Exception e) {
                    log.e(e);
                }
            }
            created = true;
        }
        return webView;
    }




    public void saveState() {

        /*
         if (smartWebView != null && StringUtil.hasText(smartWebView.getCurrentUri())
            && !"about:blank".equals(smartWebView.getCurrentUri())
        ) {
            AppCache.putString(CURRENT_BROWSER_URL, smartWebView.getCurrentUri());
        }
         */
    }

    public void goBack() {

    }

    public synchronized void loadUrl(String path) {
        if (webView != null){
            webView.loadUrl(path);
            saveState();
        }
    }

    @Override
    public void onDestroy() {
        saveState();
        if (webView != null) {
            webView.clearHistory();
            webView.clearCache(true);
            webView.clearView();
            webView.destroy();
            webView =  null;
        }
        super.onDestroy();
    }

    @Override
    public void onResume() {

        super.onResume();
    }

    @Override
    public void onPause() {
        saveState();
        super.onPause();
    }

    @Override
    public void onStop() {
        saveState();
        stop(webView);

        super.onStop();
    }

    public static void stop(WebView wV){
        if (wV != null){
            wV.stopLoading();
            wV.loadUrl("about:blank");
        }
    }


}
