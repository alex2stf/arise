package com.arise.weland.utils;

import com.arise.core.tools.SYSUtils;
import com.arise.core.tools.StringUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class URLBeautifier {






    public static String fix(String path) {

        String yIdKey = "v";
        String yDomainMatch = "youtube";

        try {
            URI uri = new URI(path);

            if (uri.getHost().indexOf(yDomainMatch) > -1){

                String schema = uri.getScheme();
                String host = uri.getHost();
                String query = uri.getRawQuery();

                Map<String, List<String>> queryparams = new HashMap<>();
                StringUtil.decodeQuery(query, queryparams);
                if (queryparams.containsKey(yIdKey)){

                }

                System.out.println(queryparams);

            }
        } catch (URISyntaxException e) {
            return path;
        }
        return path;
    }
}
