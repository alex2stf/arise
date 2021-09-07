package com.arise.weland.dto;

import com.arise.core.tools.CollectionUtil;
import com.arise.core.tools.MapUtil;
import com.arise.core.tools.StringUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static com.arise.core.tools.StringUtil.jsonVal;

@Deprecated
public class Detail {
    private final String id;
    private final String name;

    public static final Detail NULL = new Detail(null, null);



    public static Detail of(int x, boolean y) {
        return new Detail(x + "", y + "");
    }

    public Detail(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }


    public String getName() {
        return name;
    }


    @Override
    public String toString() {
        return "{\"i\":" + jsonVal(id) + "," + "\"n\":" + jsonVal(name) + "}";
    }


    public static String toJson(Collection<Detail> details){
        if (CollectionUtil.isEmpty(details)){
            return "[]";
        }
        return "[" + StringUtil.join(details, ",", new StringUtil.JoinIterator<Detail>() {
            @Override
            public String toString(Detail value) {
                return value.toString();
            }
        }) + "]";
    }
}
