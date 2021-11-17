package com.arise.core.tools;

import java.io.File;
import java.util.HashMap;

public class StandardCacheWorker {
    HashMap<String, String> map ;

    public StandardCacheWorker(){
        map = FileUtil.serializableRead(new File("local-cache"));
        if (map == null){
            map = new HashMap<>();
        }
    }
    public Editor edit(){
        return new Editor(map);
    }


    public String getString(String key, String defaultValue){
        if (map.containsKey(key)){
            return map.get(key);
        }
        return defaultValue;
    }

    public int getInt(String key, int defaultValue){
        if (map.containsKey(key)){
            try {
                return Integer.valueOf(map.get(key));
            }catch (Exception e){
                return defaultValue;
            }
        }
        return defaultValue;
    }


    public static class Editor {


        private final HashMap<String, String> hashMap;

        public Editor (HashMap<String, String> hashMap){
            this.hashMap = hashMap;
        }
        public Editor  putString(String var1, String var2){
            hashMap.put(var1, var2);
            return this;
        }

        public Editor putInt(String var1, int var2){
            hashMap.put(var1, String.valueOf(var2));
            return this;
        }

        public void commit(){
            FileUtil.serializableSave(hashMap, new File("local-cache"));
        }
    }

}
