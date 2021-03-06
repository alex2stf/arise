package com.arise.core.tools;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MapUtil {
    public static Set getSet(Map map, String key){
        if (map.containsKey(key) && map.get(key) instanceof Set){
            return (Set) map.get(key);
        }
        if (getList(map, key) != null){
            return new HashSet(getList(map, key));
        }
        return null;
    }

    public static List getList(Map map, String key){
        if (map.containsKey(key) && map.get(key) instanceof List){
            return (List) map.get(key);
        }
        return null;
    }

    public static Integer getInteger(Map map, String key) {
        if (map.containsKey(key)){
            try {
                return Integer.valueOf(String.valueOf(map.get(key)));
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    public static int getInt(Map map, String key, int i) {
        if (getInteger(map, key) != null){
            return getInteger(map, key);
        }
        return i;
    }

    public static String getString(Map map, String key) {
        if (map.containsKey(key)){
            return String.valueOf(map.get(key));
        }
        return null;
    }

    public static boolean getBool(Map map, String key){
        if (map.containsKey(key)){
            if (TypeUtil.isBoolean(map.get(key))){
                return (boolean) map.get(key);
            }
            return "true".equalsIgnoreCase(map.get(key) + "");
        }
        return false;
    }

    public static Map getMap(Map map, String key) {
        if (map.containsKey(key) && map.get(key) instanceof Map){
            return (Map) map.get(key);
        }
        return null;
    }


    public static <K, V>  MapBuilder<K, V> newMap(K key, V value) {
        return new MapBuilder<K, V>(new HashMap<K, V>()).put(key, value);
    }


    public static class MapBuilder<K, V> {
        final Map<K, V> x;

        public MapBuilder(Map<K, V> x) {
            this.x = x;
        }

        public Map<K, V> get(){
            return x;
        }

        public MapBuilder<K, V> put(K ky, V vl){
            x.put(ky, vl);
            return this;
        }
    }

}
