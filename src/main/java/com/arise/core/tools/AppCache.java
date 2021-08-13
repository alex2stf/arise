package com.arise.core.tools;



import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.arise.core.tools.StringUtil.hasContent;
import static com.arise.core.tools.StringUtil.urlDecode;

public class AppCache {
    //TODO thread safe
    public static final Map<String, String> stringsCache = new HashMap<>();

    public static final Map<String, URL> urlsMap = new HashMap<>();
    private static final Set<URL> urlsCache = new HashSet<>();
    private static Object worker;


    public static void putString(String key, String value){
        stringsCache.put(key, value);
        workerPutString(key, value);
    }

    public static void putBoolean(String key, boolean value){
        stringsCache.put(key, String.valueOf(value));
        workerPutString(key, String.valueOf(value));
    }

    public static String getString(String key, String defaultValue){
        if (!stringsCache.containsKey(key)){
            return workerGetString(key, defaultValue);
        }
        return stringsCache.get(key);
    }

    public static boolean contains(String key){
        if (!stringsCache.containsKey(key)){
            return workerGetString(key, null) != null;
        }
        return true;
    }


    public static URL getURL(String key){
        return urlsMap.get(key);
    }

    public static void putURL(URL url) {
        urlsCache.add(url);
        String path = url.getPath();

        if (hasContent(path)){
            if (path.startsWith("/")){
                path = path.substring(1);
            }
            urlsMap.put(path, url);
        }

        if (hasContent(url.getQuery())){
            String query = url.getQuery();
            if (query.startsWith("/")){
                query = query.substring(1);
            }
            urlsMap.put(query, url);
        }

        if (hasContent(url.getHost())){
            String host = url.getHost();
            if (host.startsWith("www.")){
                host = host.substring("www.".length());
            }
            urlsMap.put(host, url);
        }

    }

    public static Set<String> urlHints(){
        return urlsMap.keySet();
    }

    private static synchronized void saveCache(){
        System.out.println("TODO SAVE CACHE");
    }

    public static void load(){
        System.out.println("TODO LOAD CACHE");
    }


    private static void workerPutString(String key, String value){
        Object res = ReflectUtil.getMethod(worker, "edit").call();
        if (res != null){
            ReflectUtil.getMethod(res, "putString", String.class, String.class).call(key, value);
            ReflectUtil.getMethod(res, "commit").call();
        }
    }

    public static void putInt(String key, int value){
        workerPutInt(key, value);
    }

    private static void workerPutInt(String key, int value){
        Object res = ReflectUtil.getMethod(worker, "edit").call();
        if (res != null){
            ReflectUtil.getMethod(res, "putInt", String.class, int.class).call(key, value);
            ReflectUtil.getMethod(res, "commit").call();
        }
    }

    private static String workerGetString(String key, String defaultValue){
        return (String) ReflectUtil.getMethod(worker, "getString", String.class, String.class).call(key, defaultValue);
    }

    private static int workerGetInt(String key, int defaultValue){
        return (int) ReflectUtil.getMethod(worker, "getInt", String.class, int.class).call(key, defaultValue);
    }

    public static void setWorker(Object worker) {
        AppCache.worker = worker;
    }

    public static boolean getBoolean(String key) {
        return "true".equalsIgnoreCase(getString(key, "false"));
    }

    public static int getInt(String key, int defaultVal) {
        return workerGetInt(key, defaultVal);
    }


}
