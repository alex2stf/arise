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
                .replaceAll("\\(", "_")
                .replaceAll("\\)", "_")
                .replaceAll("=", "v")
                .replaceAll("\\?", "z")
                .replaceAll("\\.", "d")
                .replaceAll("\\+", "5")
                .replaceAll("/", "")
                .replaceAll("inux", "Xx")
                .replaceAll("samsung", "sG")
                .replaceAll("aarch", "yH")
                .replaceAll("\"", "")
                .replaceAll("\\\\", "")
                .replaceAll("%2", "s")
                .replaceAll("%28", "7")
                .replaceAll("#", "_")
                .replaceAll("%", "x")
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
