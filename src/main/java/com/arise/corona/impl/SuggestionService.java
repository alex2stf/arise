package com.arise.corona.impl;

import com.arise.core.serializers.parser.Groot;
import com.arise.core.tools.Arr;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.MapObj;
import com.arise.core.tools.MapUtil;
import com.arise.core.tools.StreamUtil;
import com.arise.core.tools.StringUtil;
import com.arise.core.tools.ThreadUtil;
import com.arise.core.tools.Util;
import com.arise.core.tools.models.CompleteHandler;
import com.arise.corona.dto.BinResult;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static com.arise.core.tools.CollectionUtil.isEmpty;
import static com.arise.core.tools.TypeUtil.isNull;

public class SuggestionService {
    MapObj root = new MapObj();
    CacheManager cacheManager;

    public CacheManager getCacheManager() {
        return cacheManager;
    }

    public SuggestionService setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
        return this;
    }

    public SuggestionService load(String path){
        InputStream inputStream = FileUtil.findStream(path);
        if (inputStream == null){
            return this;
        }

        String cnt = StreamUtil.toString(inputStream).replaceAll("\r\n", " ");

        Map<Object, Object> local = (Map<Object, Object> ) Groot.decodeBytes(cnt);
        if (local == null){
            return this;
        }
        for (Map.Entry<Object, Object> e: local.entrySet()){
            root.put(String.valueOf(e.getKey()), e.getValue());
        }
        return this;
    }

    private Map getSuggestion(String  val){
        Arr suggestions = root.getArray("suggestions");
        if (suggestions == null){
            return null;
        }
        for (Object o: suggestions){
            if (o instanceof Map){
                Map m = (Map) o;
                if (m.containsKey("key")){
                    if (String.valueOf(m.get("key")).equalsIgnoreCase(val)){
                        return m;
                    }
                }
            }
        }
        return null;
    }

    public SuggestionService searchIcons(String filename, CompleteHandler<BinResult> completeHandler) {
        ThreadUtil.fireAndForget(new Runnable() {
            @Override
            public void run() {
                searchIconsSync(filename, completeHandler);
            }
        });
        return this;
    }

    private SuggestionService searchIconsSync(String filename, CompleteHandler<BinResult> completeHandler) {


        if (!StringUtil.hasContent(filename)){
            return this;
        }
        String parts[] = filename.split("\\\\");
        String next = parts[parts.length - 1];
        parts = next.split("/");
        next = parts[parts.length - 1];
        parts = next.split("\\.");
        next = parts[0];

        parts = next.split("\\s+");
        List<String> vals = new ArrayList<>();
        for (int i = 0; i < parts.length; i++){
            String k = parts[i].trim();
            if (isValidWord(k)){
                vals.add(k);
            }
        }

        List<String> combs =  getLinearCombinations(vals);
        //search for icons:
        for (String s: combs){
            Map x = getSuggestion(s);
            if (x != null){
                List<String> icons = MapUtil.getList(x, "icons");
                if (!isEmpty(icons)){
                    for (String ic: icons){
                        if (download(ic, completeHandler)){
                            return this;
                        }
                    }
                }
            }
        }

        completeHandler.onComplete(null);

        return this;
    }

    private boolean download(String url, CompleteHandler<BinResult> completeHandler){
        URL uri;
        String id;
        try {
            uri = new URL(url);
            id = uri.getPath().replaceAll("/", "_").replaceAll("\\s+","");
        } catch (Exception e) {
            return false;
        }

        if (cacheManager.getBytes(id) != null){
            completeHandler.onComplete(cacheManager.getBytes(id));
            return true;
        }


        try {
            InputStream inputStream = uri.openStream();
            cacheManager.put(id, inputStream);
            Util.close(inputStream);
            completeHandler.onComplete(cacheManager.getBytes(id));
        } catch (IOException e) {
            return false;
        }
        return true;
    }




    char disallowed[] = new char[]{'~', '-'};
    private boolean isValidWord(String s){
        if (isNull(s)){
            return false;
        }
        if (s.length() == 1) {
            for (char c: disallowed){
                if (c == s.charAt(0)){
                    return false;
                }
            }
        }
        return true;
    }



    private List<String> getLinearCombinations(List<String> vals) {
        List<String> r = new ArrayList<>();
        for (int j = 0; j < vals.size(); j++){
            String c = vals.get(j);
            for (int i = j + 1; i < vals.size(); i++){
                r.add(c + " " + vals.get(i));
                c+=" " + vals.get(i);
            }
        }
        Collections.sort(r, new Comparator<String>() {
            @Override
            public int compare(String s, String t1) {
                return Integer.compare(s.length(), t1.length());
            }
        });
        Collections.reverse(r);
        return r;
    }

    public interface CacheManager {

        void put(String filename, InputStream inputStream);

        BinResult getBytes(String filename);

    }


}
