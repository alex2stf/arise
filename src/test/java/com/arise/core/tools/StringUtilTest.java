package com.arise.core.tools;

import org.junit.Assert;
import org.junit.Test;

public class StringUtilTest {

    @Test
    public void testQuote(){
        String r =StringUtil.quote("\"");

        Assert.assertEquals(r, "\"\\\"\"");
        r = StringUtil.quote("");
        Assert.assertEquals(r, "\"\"");


    }

    @Test
    public void testToNumber(){
        Number n;

        n = StringUtil.toNumber("23");
        Assert.assertEquals(n, 23);
        Assert.assertTrue(n instanceof Integer);


        n = StringUtil.toNumber("23.45");
        Assert.assertEquals(n, 23.45);
        Assert.assertTrue(n instanceof Double);

        n = StringUtil.toNumber("23.45.");
        Assert.assertNull(n);

        n = StringUtil.toNumber("");
        Assert.assertNull(n);

        n = StringUtil.toNumber("-");
        Assert.assertNull(n);


    }
}