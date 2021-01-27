package com.arise.rapdroid;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.text.InputType;
import android.text.Layout;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.arise.core.tools.FileUtil;
import com.arise.core.tools.Mole;
import com.arise.core.tools.StreamUtil;
import com.arise.rapdroid.components.ui.Layouts;
import com.arise.rapdroid.components.ui.adapters.URLAutocomplete;
import com.arise.rapdroid.components.ui.views.SmartLayout;
import com.arise.rapdroid.media.server.R;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static com.arise.core.tools.CollectionUtil.isEmpty;

public class SmartWebView extends LinearLayout {
    public static final Resources DEFAULT = new Resources();
    private final WebView webView;
    WebView soundThread;
    private final Context ctx;
    private Resources resources = DEFAULT;

    private AutoCompleteTextView searchBar;
    private SmartLayout top;
    private List<Button> actionButtons = new ArrayList<>();
    private Mole log = Mole.getInstance(SmartWebView.class);
    private String uri;
    private static URLAutocomplete urls;



    public SmartWebView(Context context, Resources resources) {
        super(context);
        webView = new WebView(context);
        this.ctx = context;
        if (resources != null){
            this.resources = resources;
        }
        decorateWebViewMinimal();


    }

    public SmartWebView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        webView = new WebView(context);
        this.ctx = context;
        decorateWebViewMinimal();
    }

    public SmartWebView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        webView = new WebView(context);
        this.ctx = context;
        decorateWebViewMinimal();
    }



    private void decorateWebViewMinimal(){
        setOrientation(VERTICAL);

        webView.setWebViewClient(new WebViewClient() {

//            @Override
//            public void onLoadResource(WebView view, String url) {
//                System.out.println("webview onLoadResource " + url);
//                super.onLoadResource(view, url);
//            }


//            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
//                System.out.println("webview shouldInterceptRequest " + url);
//                return super.shouldInterceptRequest(view, url);
//            }

            //https://www.hidroh.com/2016/05/19/hacking-up-ad-blocker-android/
            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                    System.out.println("Intercept request " + request.getUrl());
                }
//                System.out.println("Intercept request " + request.toString());
                return super.shouldInterceptRequest(view, request);
            }

//            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                log.i("Processing webview url click... " + url);
//                uri = url;
//                if (urls != null){
//                    urls.add(url);
//                }
//                if (searchBar != null){
//                    searchBar.setText(url);
//                }
//
//
//
//                return super.shouldOverrideUrlLoading(view, url);
//            }

//            @Override
//            public void onPageStarted(WebView view, String url, Bitmap favicon) {
//                uri = url;
//                super.onPageStarted(view, url, favicon);
//            }

            public void onPageFinished(WebView view, String url) {
                log.i("Finished loading URL: " + url);
                uri = url;
                if (searchBar != null){
                    searchBar.setText(webView.getUrl());
                }
                InputStream inputStream = FileUtil.findStream("scripts/webview_postfix.js");
                String script = StreamUtil.toString(inputStream);
                webView.loadUrl("javascript:(function() { " + script + " \n postfix('"+url+"') })()");
                super.onPageFinished(view, url);
            }



            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                log.e("Error: " + description);
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                log.info("WEBVIEW_MSG: " + consoleMessage.messageLevel() + "] (" + consoleMessage.lineNumber() + ") " + consoleMessage.message());
                return super.onConsoleMessage(consoleMessage);
            }



        });

        webView.setPadding(0, 0, 0, 0);

        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        }
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);

        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setScrollbarFadingEnabled(false);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setLoadsImagesAutomatically(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        if (Build.VERSION.SDK_INT >= 19) {
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }


        webView.getSettings().setUserAgentString("Mozilla/5.0 (Linux; Android 9; SM-A530F) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.106 Mobile Safari/537.36");
        webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        webView.getSettings().setDomStorageEnabled(true);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }


        youtubeSetup();
        hideZoomControls();
    }

    private void hideZoomControls() {
        webView.getSettings().setBuiltInZoomControls(false);
        webView.getSettings().setDisplayZoomControls(false);
    }

    public String getCurrentUri(){
        return webView.getUrl();
    }

    private SmartLayout getTopView(){
        if (top == null){
            top = new SmartLayout(ctx);
        }
        top.setBackgroundColor(resources.topColor);
        return top;
    }

    public PopupMenu addSearchBar() {
        ImageButton button = new ImageButton(ctx);
        if (resources.menuButtonImage != null) {
            button.setImageResource(resources.menuButtonImage);
        }
        if (resources.menuBackgroundColor != null){
            button.setBackgroundColor(resources.menuBackgroundColor);
        }
        return addSearchBar(button);
    }

    public PopupMenu addSearchBar(View menuBtn){
        getTopView().setOrientation(HORIZONTAL);
        searchBar = new AutoCompleteTextView(ctx);
        searchBar.setInputType(InputType.TYPE_CLASS_TEXT);
        searchBar.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchBar.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,200));
        searchBar.setThreshold(1); ////will start working from first character
        searchBar.setBackgroundColor(resources.searchBarColor);
        searchBar.setTextColor(resources.searchTextColor);


        urls = new URLAutocomplete(ctx, android.R.layout.simple_list_item_1);
        searchBar.setAdapter(urls.adapter());



        searchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // do something, e.g. set your TextView here via .setText()
                    InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    Toast.makeText(ctx, urls.fixUrl(v.getText().toString()), Toast.LENGTH_SHORT).show();
                    loadUrl(urls.fixUrl(v.getText().toString()));

                    return true;
                }
                return false;
            }
        });

        PopupMenu popupMenu = new PopupMenu(ctx, getTopView());

        menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                popupMenu.show();
            }
        });

//        menuBtn.setPadding(2, 2, 2, 2);
        getTopView().addView(searchBar, Layouts.matchParentMatchParent02f());
        getTopView().addView(menuBtn, Layouts.matchParentMatchParent09f());
        return popupMenu;
    }

    private void youtubeSetup(){
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.setInitialScale(1);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        }

    }

    public void loadUrl(String uri) {
        log.info("load uri " + uri);
        this.uri = uri;
        webView.loadUrl(uri);

        if (searchBar != null){
            searchBar.setText(uri);
        }
    }

    public String url(){
        return webView.getUrl();
    }

    public SmartWebView init() {
        if (top != null){
            addView(top, Layouts.matchParentWrapContent());
        }
        addView(webView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.3F));

        if (!isEmpty(actionButtons)){
            LinearLayout bottomButtons = new LinearLayout(ctx);
            bottomButtons.setOrientation(HORIZONTAL);
            for (Button btn: actionButtons){
                bottomButtons.addView(btn, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.5F ));
            }
            addView(bottomButtons, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }



        return this;
    }

    public void goToPrevious() {
        if (webView.canGoBack()) {
            webView.goBack();
        }
    }

//    public void hideWebview() {
//        webView.setVisibility(INVISIBLE);
//    }
//
//    public void showWebview() {
//        webView.setVisibility(VISIBLE);
//    }

//    public void pause() {
//        webView.onPause();
//    }
//
//    public void resume(){
//        webView.onResume();
//    }



    public interface OnClickListener {
        void onClick(SmartWebView webView, View view);
    }


    public static class Resources {
        public Integer menuButtonImage;
        public Integer menuBackgroundColor = Color.BLUE;
        public int topColor = Color.BLUE;
        public int searchBarColor = Color.BLUE;
        public int searchTextColor = Color.WHITE;
    }

}
