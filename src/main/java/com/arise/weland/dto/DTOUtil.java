package com.arise.weland.dto;

public class DTOUtil {

    public static final String WALL_RESERVED_ID = "LAYNEE_WALL";
    public static String sanitize(String s){
        if (WALL_RESERVED_ID.equals(s)){
            return s;
        }
        return  ("" + s).replaceAll("\\s+","")
                .replaceAll("http:", "L")
                .replaceAll("https:", "U")
                .replaceAll(":", "Q")
                .replaceAll("=", "v")
                .replaceAll("\\?", "z")
                .replaceAll("\\.", "d")
                .replaceAll("\\+", "5")
                .replaceAll("/", "")
                .replaceAll("-", "9")
                .replaceAll("inux", "Xx")
                .replaceAll("samsung", "sG")
                .replaceAll("aarch", "yH")
                .replaceAll("\\\\", "")
                .replaceAll("//", "g")
                .replaceAll("storage", "y")
                .replaceAll("ovies", "W")
                .replaceAll("usic", "R")
                .replaceAll("/", "7")
                .replaceAll("mate", "M3")
                .replaceAll("generic", "89")
                .replaceAll("alex", "SAP")
                ;
    }

}
