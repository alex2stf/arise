package com.arise.droid.tools;

import com.arise.core.models.Convertor;
import com.arise.core.tools.StringUtil;
import com.arise.weland.impl.ContentInfoProvider;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AndroidUrlSolver {


    private static final Map<String, Convertor<String, String>> urlRewriteDomains = new HashMap<>();

    static {
        urlRewriteDomains.put("youtube", new Convertor<String, String>() {
            @Override
            public String convert(String data) {
                try {
                    //https://youtu.be/ZlY0d4mao0E
                    URL url = new URL(data);
                    Map<String, List<String>> params = StringUtil.decodeQueryParams(url.getQuery());
                    if (params.containsKey("v")){
                        String youtubeId = params.get("v").get(0);
                        if (StringUtil.hasText(youtubeId)){

                            String returnval = "https://youtu.be/"+youtubeId+"?t=1";

                            if (params.containsKey("list")){
                                returnval += "&list=" + params.get("list").get(0);
                            }

                            if (params.containsKey("index")){
                                returnval += "&index=" + params.get("index").get(0);
                            }

                            return returnval;
                        }
                    }
                } catch (MalformedURLException e) {
                    return null;
                }
                return data.indexOf("youtube") > -1 ? data : null;
            }
        });
    }

    private static final Map<String, String> cache= new HashMap<>();

    public static boolean canRunInExternalBrowser(String path, ContentInfoProvider contentInfoProvider) {
        if (contentInfoProvider.findByPath(path) != null){
            return false;
        }
        for (String s: urlRewriteDomains.keySet()){

            if (path.indexOf(s) > -1){
                String rewrite = urlRewriteDomains.get(s).convert(path);
                if (rewrite != null){
                    cache.put(path, rewrite);
                    return false;
                }
            }
        }

        return true;
    }

    public static String urlRewrite(String path) {
        if (cache.containsKey(path)){
            return cache.get(path);
        }
        for (String s: urlRewriteDomains.keySet()){
            if (path.indexOf(s) > -1){
                String rewrite = urlRewriteDomains.get(s).convert(path);
                if (rewrite != null){
                    cache.put(path, rewrite);
                    return rewrite;
                }
            }
        }
        return path;
    }



}
