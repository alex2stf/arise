package com.arise.cargo.management;

import com.arise.core.tools.CollectionUtil;
import com.arise.core.tools.MapUtil;
import com.arise.core.tools.Mole;
import com.arise.core.tools.SYSUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.arise.core.tools.CollectionUtil.isEmpty;
import static com.arise.core.tools.Mole.logWarn;

public class Dependency {


    private String name;

    String type;

    Map<String, Version> versions = new HashMap<>();


    public Map<String, Version> getVersions(){
        return versions;
    }


    public static void decorate(Dependency dependency, Map args) {
        dependency.type = MapUtil.getString(args, "type");
        List<Map<String, Object>> versions = MapUtil.getList(args, "versions");

        if (isEmpty(versions)){
            logWarn( "Empty versions definition, skip dependency " + dependency.getName());
            return;
        }

        for (Map m: versions){

            boolean disabled = MapUtil.getBool(m, "disabled");
            if (!disabled) {

                Version version = new Version();
                version._n = MapUtil.getString(m, "name");
                version._c = MapUtil.getString(m, "condition");
                version._p = MapUtil.getMap(m, "params");
                dependency.versions.put(version._n, version);
            }
        }
    }


   public Dependency setName(String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    public Version getLatestVersion(){
       for (Version v: versions.values()){
           if (v != null){
               return v;
           }
       }
       return null;
    }







    public static class Version {
       String _n;
       String _c;
       Map<String, String> _p;


        public Map<String, String> params(){
            return _p;
        }
        public String name(){
            return _n;
        }
        public String condition(){
            return _c;
        }
    }
}
