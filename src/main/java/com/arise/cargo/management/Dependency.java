package com.arise.cargo.management;

import com.arise.core.tools.MapUtil;
import com.arise.core.tools.SYSUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Dependency {
    List<Rule> ruleList = new ArrayList<>();
    private String name;

    String type;

    Map<String, Version> versions = new HashMap<>();


    public Map<String, Version> getVersions(){
        return versions;
    }


    public static void decorate(Dependency dependency, Map args) {
        dependency.type = MapUtil.getString(args, "type");
        List<Map<String, Object>> versions = MapUtil.getList(args, "versions");



        for (Map m: versions){
            Version version = new Version();
            version.id = MapUtil.getString(m, "id");
            version.platformMatch = MapUtil.getString(m, "platform-match");
            version.urls = MapUtil.getList(m, "urls");
            version.libPaths = MapUtil.getList(m, "lib-paths");
            version.dynamicLibs = MapUtil.getList(m, "dynamic-libs");
            version.staticLibs = MapUtil.getList(m, "static-libs");
            version.includes = MapUtil.getList(m, "includes");
            version.executable = MapUtil.getString(m, "executable");
            dependency.versions.put(version.id, version);
        }
    }


    public Dependency addRule(Rule rule) {
        ruleList.add(rule);
        return this;
    }


    public Dependency setWindowsSource(final String path){
        return addRule(new Rule() {
            @Override
            public boolean acceptConditions() {
                return SYSUtils.isWindows();
            }

            @Override
            public String getPath() {
                return path;
            }

            @Override
            public String getName() {
                return "win32";
            }
        });
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




    public Version getVersion(String in) {
        if (null == this.versions){
            throw new RuntimeException("Invalid dependency " + this);
        }
        for (Version v: this.versions.values()){
            if (null == v.platformMatch || null == in){
                throw new RuntimeException("Invalid dependency " + this);
            }
            if (in.equals(v.platformMatch) || in.equals(v.id) || "*".equals(in)){
                return v;
            }
        }

        return getLatestVersion();
    }


    public static class Version {
        List<String> urls;
        List<String> libPaths;
        List<String> includes;
        List<String> dynamicLibs;
        List<String> staticLibs;

        //TODO foloseste nume, match-ul tb sa fie regex
        @Deprecated
        String platformMatch;
        String id;
        String executable;


        public String getExecutable() {
            if (SYSUtils.isWindows() && !executable.endsWith(".exe")){
                return executable + ".exe";
            }
            return executable;
        }
        public String name(){
            return platformMatch;
        }
    }
}
