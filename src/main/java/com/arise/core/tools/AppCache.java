package com.arise.core.tools;


import java.io.File;
import java.net.URL;
import java.util.*;

import static com.arise.core.tools.StringUtil.hasContent;

public class AppCache {
    //TODO thread safe
    public static final Map<String, String> stringsCache = new HashMap<>();

    public static final Map<String, URL> urlsMap = new HashMap<>();
    private static final Set<URL> urlsCache = new HashSet<>();
    private static Object worker;


    public static class StoredList {
        private final List<String> items;
        private final int index;

        public StoredList(List<String> items, int index) {
            this.items = items;
            this.index = index;
        }

        public List<String> getItems() {
            return items;
        }

        public int getIndex() {
            return index;
        }

        public boolean isEmpty() {
            return CollectionUtil.isEmpty(items);
        }

        public boolean isIndexExceeded() {
            return !isEmpty() && (index > items.size() - 1);
        }
    }

    static File getStoredListFile(String name){
        File listsDir = new File(FileUtil.findAppDir(), "lists");
        if (!listsDir.exists()){
            listsDir.mkdir();
        }
        return new File(listsDir, name);
    }

    public static StoredList storeList(String name, List<String> items, int index){
        if (CollectionUtil.isEmpty(items)){
            return new StoredList(new ArrayList<String>(), 0);
        }
        File list = getStoredListFile(name);
        StringBuilder sb = new StringBuilder().append(index).append("\n");
        for (String s: items){
            sb.append(s.trim()).append("\n");
        }
        FileUtil.writeStringToFile(list, sb.toString());
        return new StoredList(items, index);
    }

    public static void dropStoredList(String name){
        getStoredListFile(name).delete();
    }

    public static StoredList getStoredList(String name) {
        File list = getStoredListFile(name);
        if (!list.exists()){
            return new StoredList(new ArrayList<String>(), 0);
        }
        final List<String> x = FileUtil.readLinesFromFile(list);
        Integer index;
        try {
            index = Integer.valueOf(x.get(0));
        }catch (Exception e){
            index = 0;
        }
        x.remove(0);

        return new StoredList(x, index);
    }

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

    public static boolean getBoolean(String key, boolean defaultVal){
        return "true".equalsIgnoreCase(getString(key, defaultVal + ""));
    }

    public static int getInt(String key, int defaultVal) {
        return workerGetInt(key, defaultVal);
    }


}
