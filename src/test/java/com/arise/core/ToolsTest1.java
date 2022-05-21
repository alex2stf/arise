package com.arise.core;

import com.arise.core.tools.Assert;
import com.arise.core.tools.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class ToolsTest1 {

    public void testStringUtilToCSV(){
        List<String> xxx = new ArrayList<>();
        xxx.add("1");
        xxx.add("3333");
        xxx.add("434343");

        Assert.assertEquals(StringUtil.toCSV(xxx), "1,3333,434343");
        Assert.assertEquals(
                StringUtil.toCSV(new String[]{"1", "3333", "434343"}),
                StringUtil.toCSV(xxx)
        );
    }


    private class Dummy {

    }
}
