package com.arise.core.tools;

import java.util.List;

public class ListUtil {
    public static boolean listContainsIgnorecase(String search, String[] items){
        for (String s: items){
            if (s.equalsIgnoreCase(search)){
                return true;
            }
        }
        return false;
    }





    public static  String[] toArray(List<String> s) {
        String[]c = new String[s.size()];
        return s.toArray(c);
    }


    public static <T> boolean hasData(T[] dimpl) {
        return dimpl != null && dimpl.length > 0;
    }
}
