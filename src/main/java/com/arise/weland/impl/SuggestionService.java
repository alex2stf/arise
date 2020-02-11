package com.arise.weland.impl;

import com.arise.core.serializers.parser.Groot;
import com.arise.core.tools.Arr;
import com.arise.core.tools.B64;
import com.arise.core.tools.ContentType;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.MapObj;
import com.arise.core.tools.MapUtil;
import com.arise.core.tools.StreamUtil;
import com.arise.core.tools.StringEncoder;
import com.arise.core.tools.StringUtil;
import com.arise.weland.IDGen;

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



    public SuggestionService searchIcons(String filename, Manager manager) {


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
        String ext[] = filename.split("\\.");
        if (ext.length > 1){
            String last = ext[ext.length -1];
            combs.add("_" + last);
        }
        //search for icons:
        for (String s: combs){
            Map x = getSuggestion(s);
            if (x != null){
                List<String> icons = MapUtil.getList(x, "icons");
                if (!isEmpty(icons)){
                    for (String ic: icons){
                        if (validUrl(ic, manager, filename)){
                            return this;
                        }
                    }
                }
            }
        }



        return this;
    }

    private boolean validUrl(String url, Manager manager, String path){
        String id = StringEncoder.encodeShiftSHA(url);

        if (url.startsWith("data:")){
            int sepIndex = url.indexOf(",");
            String start = url.substring(0, sepIndex);
            System.out.println(start);

            String ctype = start.substring(start.indexOf(":") + 1, start.indexOf(";"));
            ContentType contentType = ContentType.search(ctype);
            String content = url.substring(sepIndex + 1);

            try {
                byte bytes[] = B64.decodeToByteArray(content);
                return manager.manageBytes(id, bytes, contentType);
            } catch (Exception e) {
               return false;
            }
        }

        URL uri;
        try {
            uri = new URL(url);
            id = IDGen.fromURL(uri);
        } catch (Exception e) {
            return false;
        }

        return manager.manage(id, path, uri);
    }


    public interface Manager {
        boolean manage(String id, String path, URL url);
        boolean manageBytes(String id, byte[] bytes, ContentType contentType);
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





}
