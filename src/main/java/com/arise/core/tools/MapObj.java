package com.arise.core.tools;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class MapObj extends LinkedHashMap<String, Object> {
    public String getString(String name) {
        if (containsKey(name)){
            return String.valueOf(get(name));
        }
        return null;
    }

    public Arr getArray(String key) {
        if (containsKey(key)) {
            return (Arr) get(key);
        }
        return null;
    }

    public Set getSet(String key){
        return new HashSet(getArray(key));
    }

    public Integer getInt(String key) {
        if (containsKey(key)){
            try {
                return Integer.valueOf(getString(key));
            } catch (NumberFormatException e){
                return null;
            }
        }
        return null;
    }

    public int getInt(String key, int defaultValue) {
        Integer r = getInt(key);
        if (r == null){
            return defaultValue;
        }
        return r;
    }

    public Map getMap(String key) {
        if (containsKey(key)){
            Object o = get(key);
            if (o instanceof Map){
                return (Map) o;
            }
        }
        return null;
    }
}
