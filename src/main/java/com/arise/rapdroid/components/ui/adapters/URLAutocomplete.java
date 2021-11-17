package com.arise.rapdroid.components.ui.adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import com.arise.core.tools.AppCache;

import java.net.MalformedURLException;
import java.net.URL;

public class URLAutocomplete extends AdapterContainer<String> {
    public URLAutocomplete(@NonNull Context context, int resource) {
        super(context, resource);
        add("https://stackoverflow.com/");
        add("https://www.youtube.com/");
        add("https://www.facebook.com/");
    }

    public URLAutocomplete(@NonNull Context context) {
        super(context);
    }


    @Override
    public AdapterContainer<String> add(String value) {
        try {
            URL url = new URL(value);
            AppCache.putURL(url);
            for (String s: AppCache.urlHints()){
                if (!items.contains(s)){
                    items.add(s);
                }
            }
            arrayAdapter.notifyDataSetChanged();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return this;
    }



    public String fixUrl(String keyword){
        URL url = AppCache.getURL(keyword);
        if (url != null){
            return url.toString();
        }
        try {
            url = new URL(keyword);
            return url.toString();
        } catch (MalformedURLException e) {
            return  "https://duckduckgo.com/?q=" + keyword + "&ia=web";
        }
    }




}
