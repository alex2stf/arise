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



    public static void decorate(Dependency dependency, Map args) {
        dependency.type = MapUtil.getString(args, "type");
        List<Map<String, Object>> versions = MapUtil.getList(args, "versions");



        for (Map m: versions){
            Version version = new Version();
            version.id = MapUtil.getString(m, "id");
            version.name = MapUtil.getString(m, "platform-match");
            version.urls = MapUtil.getList(m, "urls");
            version.libPaths = MapUtil.getList(m, "lib-paths");
            version.dynamicLibs = MapUtil.getList(m, "dynamic-libs");
            version.staticLibs = MapUtil.getList(m, "static-libs");
            version.includes = MapUtil.getList(m, "includes");
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

    public Version getVersion(String in) {
        for (Version v: this.versions.values()){
            if (v.getName().equals(in) || v.id.equals(in) || "*".equals(in)){
                return v;
            }
        }

        return null;
    }


    public static class Version {
        List<String> urls;
        List<String> libPaths;
        List<String> includes;
        List<String> dynamicLibs;
        List<String> staticLibs;
        String name;
        String id;



        public String getName() {
            return name;
        }

        public Version setName(String name) {
            this.name = name;
            return this;
        }
    }
}
