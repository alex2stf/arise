package com.arise.corona;

import java.io.File;
import java.net.URL;

public class IDGen {


    public static String fromURL(URL uri) {
        return parsePath(uri.getPath());
    }

    public static String fromFile(File file) {
        return parsePath(file.getName());
    }

    private static String parsePath(String s) {
        return s.replaceAll("/", "_").replaceAll("\\s+","");
    }
}
