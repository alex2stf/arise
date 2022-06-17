package com.arise.cargo.management;

import com.arise.core.tools.MapUtil;

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
                version._f = MapUtil.getList(m, "profiles");
                version._p = MapUtil.getList(m, "params");
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









    public static class Version {
       String _n;
       List<String> _f;
       List _p;


       public List<String> profiles(){
           return _f;
       }
       public List params(){
            return _p;
        }
       public String name(){
            return _n;
        }
    }
}
