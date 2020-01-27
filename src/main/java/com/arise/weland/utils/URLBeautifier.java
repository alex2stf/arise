package com.arise.weland.utils;

import com.arise.core.tools.SYSUtils;
import com.arise.core.tools.StringUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class URLBeautifier {
    public static String[] beautify(String[] args) {

        if (SYSUtils.isAndroid()) {
            return args;
        }

        //https://m.youtube.com/channel/UCa6ufakZHYGC62Io9rsWaiA
        String r[] = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            String url = args[i];
            if (url.indexOf("youtube.com") > -1) {
                try {
                    URI uri = new URI(url);
                    Map<String, List<String>> params = new HashMap<>();
                    StringUtil.decodeQuery(uri.getQuery(), params);
                    if (params.containsKey("v") && params.get("v").size() > 0){
                        url = uri.getScheme() + "://www.youtube.com/embed/" + params.get("v").get(0) + "?autoplay=1&feature=emb_rel_err";
                    }
                } catch (URISyntaxException e) {
                    url = args[i];
                }
            }
            r[i] = url;
        }
        return r;
    }
}
