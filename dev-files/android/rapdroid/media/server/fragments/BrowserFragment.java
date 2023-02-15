package com.arise.rapdroid.media.server.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.arise.core.tools.AppCache;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.Mole;
import com.arise.core.tools.StreamUtil;
import com.arise.core.tools.StringUtil;
import com.arise.rapdroid.SmartWebView;
import com.arise.rapdroid.media.server.Icons;
import com.arise.rapdroid.media.server.MainActivity;
import com.arise.rapdroid.media.server.R;
import com.arise.weland.dto.ContentInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;


public class BrowserFragment extends Fragment {
    private static final Mole log = Mole.getInstance(BrowserFragment.class);
    public static final String CURRENT_BROWSER_URL = "cbrl976";
    private static final String BLOCK_MODE = "blm857";



    SmartWebView smartWebView;
    private volatile boolean created = false;




    private static final String FALLBACK_URL = "http://localhost:8221/app";


//
//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        smartWebView = (SmartWebView) inflater.inflate(R.layout.smart_wv, container, false);
        create_view();
        return smartWebView;
    }


    private void create_view(){
        created = true;

        List<String> hosts = Collections.emptyList();
        try {
            hosts = FileUtil.readLines(FileUtil.findStream("weland/ad_hosts"));
        } catch (IOException e) {
            e.printStackTrace();
            hosts = new ArrayList<>();
        }

        hosts.add("https://m.youtube.com/youtubei/v1/log_event");
        hosts.add("https://m.youtube.com/ptracking");
        hosts.add("https://www.youtube.com/pagead/paralleladview");
        hosts.add("m.youtube.com/api/stats/qoe");
        hosts.add("youtube.com/api/stats/watchtime");
        hosts.add("youtube.com/pagead/conversion");
        hosts.add("youtube.com/pagead/adview");
        hosts.add("googlevideo.com/videogoodput");
        hosts.add("m.youtube.com/api/stats/delayplay");
        hosts.add("m.youtube.com/api/stats/ads");
        hosts.add("m.youtube.com/api/stats/playback?ns=yt&el=adunit"); //TODO chack domain and query param
        hosts.add("https://tpc.googlesyndication.com/sodar");


        List<ContentInfo> urls = new ArrayList<>();
        urls.add(new ContentInfo().setPath("https://www.google.com").setTitle("google"));
        urls.add(new ContentInfo().setPath("https://www.youtube.com").setTitle("youtube"));
        urls.add(new ContentInfo().setPath("m.youtube.com").setTitle("mobile youtube"));
        urls.add(new ContentInfo().setPath("https://consent.youtube.com/m?continue=https%3A%2F%2Fm.youtube.com&gl=RO&m=1&pc=yt&uxe=23983171&hl=ro&src=1")
                .setTitle("youtube consent"));
        String names[] = new String[urls.size()];
        for (int i = 0; i < urls.size(); i++){
            names[i] = urls.get(i).getTitle();
        }


        SmartWebView.Resources res = new SmartWebView.Resources();
        res.menuButtonImage = R.drawable.ic_menu_light;
        res.menuBackgroundColor = Icons.tab2Background;
        res.searchBarColor = Icons.tab2Background;
        res.searchTextColor = Color.GRAY;

        smartWebView = new SmartWebView(getContext(), res)
                .setBlockedDomains(hosts)
                .setJavascriptSnipping(
                        StreamUtil.toString(
                                FileUtil.findStream("weland/webview_snip.js")
                        )
                );

        PopupMenu popupMenu = smartWebView.addSearchBar();
        Menu root = popupMenu.getMenu();
        root.add("Open");
        root.add("Navigate");

        boolean blockMode = AppCache.getBoolean(BLOCK_MODE, true);
        root.add(blockMode ? "Block off" : "Block on");
        smartWebView.setBlockMode(blockMode);


        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getTitle().toString()){
                    case "Open":
                        Activity activity = getActivity();
                        if (activity instanceof MainActivity){
                            ((MainActivity)activity).showSendUrlOptions(smartWebView.getCurrentUri());
                        }
                        break;

                    case "Navigate":
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setItems(names, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                loadUrl(urls.get(i).getPath());
                            }
                        });
                        builder.create().show();
                        break;
                    case "Block on":
                        AppCache.putBoolean(BLOCK_MODE, true);
                        smartWebView.setBlockMode(true);
                        menuItem.setTitle("Block off");
                        break;
                    case "Block off":
                        AppCache.putBoolean(BLOCK_MODE, false);
                        smartWebView.setBlockMode(false);
                        menuItem.setTitle("Block on");
                        break;
                }
                return false;
            }
        });
        smartWebView.init();
        smartWebView.setId(UUID.randomUUID().version());

        smartWebView.getWebView().setVerticalScrollBarEnabled(true);
        smartWebView.getWebView().setHorizontalScrollBarEnabled(true);


        loadUrl(AppCache.getString(CURRENT_BROWSER_URL, FALLBACK_URL));
    }



    public void saveState() {
        if (smartWebView != null && StringUtil.hasText(smartWebView.getCurrentUri())
            && !"about:blank".equals(smartWebView.getCurrentUri())
        ) {
            AppCache.putString(CURRENT_BROWSER_URL, smartWebView.getCurrentUri());
        }
    }

    public void goBack() {
        if (smartWebView != null){
            smartWebView.goToPrevious();
            saveState();
        }
    }

    public synchronized void loadUrl(String path) {
        if (smartWebView != null){
            smartWebView.loadUrl(path);
            saveState();
        }
    }

    @Override
    public void onDestroy() {
        saveState();
        if (smartWebView != null) {
            smartWebView.onDestroy();
            smartWebView = null;
        }

        super.onDestroy();

    }

    @Override
    public void onResume() {
        if (smartWebView != null){
            smartWebView.onResume();
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        saveState();
        smartWebView.onPause();
        super.onPause();

    }

    @Override
    public void onStop() {
        saveState();
        if (smartWebView != null) {
            smartWebView.onStop();
        }

        super.onStop();
    }
}
