package com.arise.astox.net.models;

public enum HttpProtocol {
    V1_1("HTTP/1.1"), V1_0("HTTP/1.0");

    private final String v;

    HttpProtocol(String value) {
        this.v = value;
    }

    public String value(){
        return v;
    }

    public static HttpProtocol findByValue(String s){
        for (HttpProtocol p: HttpProtocol.values()){
            if (p.v.equalsIgnoreCase(s)){
                return p;
            }
        }
        return null;
    }
}
