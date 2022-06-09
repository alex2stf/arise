package com.arise.cargo.management;

import java.io.File;

import static com.arise.core.tools.FileUtil.findAppDir;


@Deprecated
public class Locations {
    private static File rt(){
        File x = findAppDir();
        return new File(x, "dpmngmt");
    }

    private static File gd(String n){
        File f = new File(rt(), n);
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

        if ("bin".equalsIgnoreCase(s) || "binary".equalsIgnoreCase(s)){
            return bin();
        }

        if ("downloads".equalsIgnoreCase(s) || "down".equalsIgnoreCase(s)
        || "download".equalsIgnoreCase(s)) {
            return down();
        }
        return new File(s);
    }

    public static File down(){
        return gd("down");
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
}
