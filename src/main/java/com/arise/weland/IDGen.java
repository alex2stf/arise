package com.arise.weland;

import java.io.File;
import java.net.URL;

public class IDGen {


    public static String fromURL(URL uri) {
        return parsePath(uri.getPath() + uri.getPath());
    }

    public static String fromFile(File file) {
        return parsePath(file.getName());
    }

    public static String parsePath(String s) {
        return s.replaceAll("/", "_")
                .replaceAll("\\s+","")
                .replaceAll("\\:", "_")
                ;
    }
}
