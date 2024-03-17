package com.arise.droid.fragments;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.arise.core.models.Handler;
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

    public static void stopWebViewOnMainThread(Handler handler) {
        if (self != null){
            self.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    stop(webView);
                    handler.handle(webView);
                }
            });
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (!created){
            webView = new WebView(getContext());
            webView.setWebChromeClient(new WebChromeClient(){
                @Override
                public boolean onConsoleMessage(ConsoleMessage consoleMessage) {

                    return super.onConsoleMessage(consoleMessage);
                }
            });

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
            }

            webView.getSettings().setPluginState(WebSettings.PluginState.ON);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

            webView.setWebViewClient(new WebViewClient()
//                                     {
//                @Override
//                public void onPageFinished(WebView view, String url) {
//                    super.onPageFinished(view, url);
//                    // mimic onClick() event on the center of the WebView
//                    long delta = 100;
//                    long downTime = SystemClock.uptimeMillis();
//                    float x = view.getLeft() + (view.getWidth()/2);
//                    float y = view.getTop() + (view.getHeight()/2);
//
//                    MotionEvent tapDownEvent = MotionEvent.obtain(downTime, downTime + delta, MotionEvent.ACTION_DOWN, x, y, 0);
//                    tapDownEvent.setSource(InputDevice.SOURCE_CLASS_POINTER);
//                    MotionEvent tapUpEvent = MotionEvent.obtain(downTime, downTime + delta + 2, MotionEvent.ACTION_UP, x, y, 0);
//                    tapUpEvent.setSource(InputDevice.SOURCE_CLASS_POINTER);
//
//                    view.dispatchTouchEvent(tapDownEvent);
//                    view.dispatchTouchEvent(tapUpEvent);
//                }
//            }
            );

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
            log.warn("\n\n\t\t\t loadUrl " + path + "\n\n");
            path = path.trim();
            if (path.startsWith("html-content:")){

                path = path.substring("html-content:".length());
                webView.loadData(path, "text/html; charset=UTF-8", null);

                return;
            }
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
            log.info("Stopping webview.....");

            wV.stopLoading();
            wV.pauseTimers();

//            wV.destroy();
            wV.loadUrl("about:blank");

            wV.resumeTimers();
        }
    }


}
