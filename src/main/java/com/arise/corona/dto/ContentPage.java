package com.arise.corona.dto;

import com.arise.core.tools.MapUtil;

import java.util.List;
import java.util.Map;

import static com.arise.core.tools.StringUtil.jsonVal;

public class ContentPage {
    private Integer index;
    private List<ContentInfo> data;

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
        return ContentInfo.serializeCollection(data);

//                "{" +
//                "\"i\":" + index +
//                ", \"d\":" + ContentInfo.serializeCollection(data) +
//                '}';
    }

    public ContentPage setData(List<ContentInfo> data) {
        this.data = data;
        return this;
    }


}
