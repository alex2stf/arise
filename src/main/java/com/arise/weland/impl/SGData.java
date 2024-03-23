package com.arise.weland.impl;

import com.arise.core.tools.ContentType;

/**
 * Created by alex2 on 20/03/2024.
 */
public class SGData {

    public final String id;
    public final byte[] bytes;
    public final ContentType contentType;

    public SGData(String id, byte[] bytes, ContentType contentType) {
        this.id = id;
        this.bytes = bytes;
        this.contentType = contentType;
    }


    public String getId() {
        return id;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public ContentType getContentType() {
        return contentType;
    }
}
