package com.arise.weland.dto;

import com.arise.core.tools.MapUtil;

import java.util.List;
import java.util.Map;

import static com.arise.core.tools.StringUtil.jsonVal;
import static com.arise.core.tools.StringUtil.map;

public class ContentPage {
    private Integer index;
    private List<ContentInfo> data;
    private AutoplayMode autoplayMode;

    public static ContentPage fromMap(Map m) {
        Integer index = MapUtil.getInteger(m, "i");
        List data = MapUtil.getList(m, "d");
        List<ContentInfo> infos = ContentInfo.deserializeList(data);

        String mode = MapUtil.getString(m, "p");
        AutoplayMode autoplayMode;
        try {
            autoplayMode = AutoplayMode.valueOf(mode);
        }catch (Exception e){
            autoplayMode = AutoplayMode.off;
        }
        return new ContentPage().setData(infos).setIndex(index).setAutoplayMode(autoplayMode);
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
                ",\"p\":\"" + autoplayMode.name() +
                "\",\"d\":" + ContentInfo.serializeCollection(data) +
                '}';
    }

    public ContentPage setData(List<ContentInfo> data) {
        this.data = data;
        return this;
    }

    public AutoplayMode getAutoplayMode() {
        return autoplayMode;
    }

    public ContentPage setAutoplayMode(AutoplayMode autoplayMode) {
        this.autoplayMode = autoplayMode;
        return this;
    }
}
