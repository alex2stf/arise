package com.arise.weland.dto;

import com.arise.core.tools.MapUtil;

import java.util.List;
import java.util.Map;

public class ContentPage {
    private Integer index;
    private List<ContentInfo> data;


    public static ContentPage fromMap(Map m) {
        Integer index = MapUtil.getInteger(m, "i");
        List data = MapUtil.getList(m, "d");
        List<ContentInfo> infos = ContentInfo.deserializeList(data);
        return new ContentPage().setData(infos).setIndex(index);
    }

    public Integer getIndex() {
        return index;
    }

    public ContentPage setIndex(Integer index) {
        this.index = index;
        return this;
    }

    public List<ContentInfo> getData() {
        return data;
    }

    @Override
    public String toString() {
        return "{" +
                "\"i\":" + index +
                ",\"d\":" + ContentInfo.serializeCollection(data) +
                '}';
    }

    public ContentPage setData(List<ContentInfo> data) {
        this.data = data;
        return this;
    }


}
