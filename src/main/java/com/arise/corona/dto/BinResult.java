package com.arise.corona.dto;

public class BinResult {
    private final String id;
    private final byte[] bytes;

    public BinResult(String id, byte[] bytes) {
        this.id = id;
        this.bytes = bytes;
    }

    public String getId() {
        return id;
    }

    public byte[] getBytes() {
        return bytes;
    }
}
