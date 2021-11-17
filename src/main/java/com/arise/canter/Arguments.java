package com.arise.canter;

import com.arise.core.tools.TypeUtil;

import java.util.*;

import static com.arise.core.tools.CollectionUtil.isEmpty;
import static com.arise.core.tools.CollectionUtil.merge;
import static com.arise.core.tools.StringUtil.hasText;

public class Arguments {



    List<String> stringArgs = new ArrayList<>();
    private Map<String, Object> mapArgs = new HashMap<>();






    static boolean shouldParse(String s){
        return hasText(s) && s.indexOf("}") > -1 && (!s.endsWith("\\}") && !s.endsWith("/}"));
    }




    public String get(int i) {
        return stringArgs.get(i);
    }


    public Object get(String name){
        return mapArgs.get(name);
    }


    public Object find(String ... names){
        if (isEmpty(names)){
            return null;
        }
        return TypeUtil.search(names, mapArgs, 0);
    }



    public void put(String name, Object arg) {
        mapArgs.put(name, arg);
    }






    public void add(String s) {
        stringArgs.add(s);
    }


    public List<String> list() {
        return stringArgs;
    }

    public String[] array() {
        String [] r = new String[stringArgs.size()];
        stringArgs.toArray(r);
        return r;
    }



    void addResults(Map<String, Object> source) {
        merge(source, mapArgs);
    }

    public Map<String, Object> getMapArgs() {
        return mapArgs;
    }


    void setMapArgs(Map<String, Object> mapargs) {
        this.mapArgs = mapargs;
    }

    public static Arguments fromList(String[] args) {
        Arguments a = new Arguments();
        a.stringArgs = Arrays.asList(args);
        return a;
    }

    public static Arguments fromCollection(Collection<String> collection){
        Arguments a = new Arguments();
        for (String s: collection){
            a.add(s);
        }
        return a;
    }


}
