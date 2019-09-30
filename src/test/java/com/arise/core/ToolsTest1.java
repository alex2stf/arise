package com.arise.core;

import com.arise.core.tools.StringUtil;
import com.arise.core.tools.Util;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ToolsTest1 {

    @Test
    public void testCoerce(){
        Dummy dummy = new Dummy();
        Optional<Dummy> optionalDummy = Optional.of(dummy);
        Assert.assertTrue(Util.coerce(dummy) == Util.coerce(optionalDummy));
    }

    @Test
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
