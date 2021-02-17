package com.arise.rapdroid;

import android.app.Activity;
import android.content.Context;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;


public class QuixotInterface {

    private final Context context;
    private final WebView parent;

    public QuixotInterface(Context context, WebView parent){
        this.context = context;

        this.parent = parent;
    }

    @JavascriptInterface
    public void showToast(String toast) {
        Toast.makeText(context, toast, Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public void loadUrl(String url){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                parent.loadUrl(url);
            }
        });
    }

    private void runOnUiThread(Runnable r){
        if (context instanceof Activity){
            ((Activity)context).runOnUiThread(r);
        }
    }
}
