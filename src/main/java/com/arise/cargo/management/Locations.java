package com.arise.cargo.management;

import java.io.File;

import static com.arise.core.tools.FileUtil.findAppDir;

public class Locations {
    private static File rt(){
        File x = findAppDir();
        return new File(x, "dpmngmt");
    }

    private static File gd(String name){
        File f = new File(rt(), name);
        if (!f.exists()){
            f.mkdirs();
        }
        return f;
    }

    public static File forName(String s){
        s = s.trim();
        if ("libs".equalsIgnoreCase(s)){
            return libs();
        }
        if ("src".equalsIgnoreCase(s)){
            return src();
        }
        if ("out".equalsIgnoreCase(s)){
            return out();
        }

        if ("bin".equalsIgnoreCase(s)){
            return bin();
        }
        return new File(s);
    }

    public static File downloads(){
        return gd("downloads");
    }

    public static File libs(){
        return gd("libs");
    }

    public static File src(){
       return gd("src");
    }

    public static File out() {
       return gd("out");
    }

    public static File bin() {
        return gd("bin");
    }

    public static File dest(Dependency dependency){
        if ("binary".equalsIgnoreCase(dependency.type)){
            return gd("bin");
        }
        return libs();
    }
}
