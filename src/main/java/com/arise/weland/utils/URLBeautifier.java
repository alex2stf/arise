package com.arise.weland.utils;

import com.arise.core.tools.SYSUtils;
import com.arise.core.tools.StringUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class URLBeautifier {
    public static String beautify(String url) {

        if (SYSUtils.isAndroid()) {
            return url;
        }

        //https://m.youtube.com/channel/UCa6ufakZHYGC62Io9rsWaiA


        if (url.indexOf("youtube.com") > -1) {
            try {
                URI uri = new URI(url);
                Map<String, List<String>> params = new HashMap<>();
                StringUtil.decodeQuery(uri.getQuery(), params);
                if (params.containsKey("v") && params.get("v").size() > 0){
                    return uri.getScheme() + "://www.youtube.com/embed/" + params.get("v").get(0) + "?autoplay=1&feature=emb_rel_err";
                }
            } catch (URISyntaxException e) {

            }
        }

        return url;
    }
}
