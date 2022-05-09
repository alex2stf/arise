package com.arise.core.tools;



import java.util.HashMap;
import java.util.Map;

import static com.arise.core.tools.Assert.assertEquals;


public class StringUtilTest {

    public void testQuote(){
        String r =StringUtil.quote("\"");

        assertEquals(r, "\"\\\"\"");
        r = StringUtil.quote("");
        assertEquals(r, "\"\"");


    }

    public void testToNumber(){
        Number n;

        n = StringUtil.toNumber("23");
        assertEquals(n, 23);
        Assert.assertTrue(n instanceof Integer);


        n = StringUtil.toNumber("23.45");
        assertEquals(n, 23.45);
        Assert.assertTrue(n instanceof Double);

        n = StringUtil.toNumber("23.45.");
        Assert.assertNull(n);

        n = StringUtil.toNumber("");
        Assert.assertNull(n);

        n = StringUtil.toNumber("-");
        Assert.assertNull(n);

    }

    public void testParsing(){
        Map<String, Object> map = new HashMap<>();
        map.put("xxx", 1);

        String out;
        out = StringUtil.map("test {xxx} \\{escaped} test", map);
        assertEquals("test 1 \\{escaped} test", out);

        map.put("unu", 1111.22233);
        map.put("doi", "two");
        map.put("trei", "'3");
        out = StringUtil.map("{unu}, '{doi}', '{trei}", map);

        assertEquals("1111.22233, 'two', ''3", out);

        T2 t2 = new T2();
        t2.testClass = new TestClass();
        t2.testClass.value = "T1";
        t2.string = "T3";
        map.put("t", t2);


        Object o;
        o = TypeUtil.search(new String[]{"t"}, map, 0);
        assertEquals(o, t2);

        o = TypeUtil.search(new String[]{"t", "testClass"}, map, 0);
        assertEquals(o, t2.testClass);

        o = TypeUtil.search(new String[]{"t", "testClass", "value"}, map, 0);
        assertEquals(o, "T1");

        o = TypeUtil.search(new String[]{"t", "string"}, map, 0);
        assertEquals(o, "T3");

        out = StringUtil.map("xxx {t.t.t1} zzx", map);
        assertEquals(out, "xxx null zzx");

        out = StringUtil.map("xxx {t.testClass.value} zzx", map);
        assertEquals(out, "xxx T1 zzx");

        out = StringUtil.map("xxx {t.string} zzx", map);
        assertEquals(out, "xxx T3 zzx");



    }


    public void testDecode(){
        StringUtil.URLDecodeResult result = StringUtil.urlDecode("http://192.168.1.4:8221/files/open?path=https%3A%2F%2Fwww.youtube.com%2Fwatch%3Fv%3DtBzfEceZtWI%26list%3DPL4zZspbjJ-iS1NLS15oe7LJGY3nngMrQC%26index%3D1%26t%3D4s");
        Assert.assertEquals("https%3A%2F%2Fwww.youtube.com%2Fwatch%3Fv%3DtBzfEceZtWI%26list%3DPL4zZspbjJ-iS1NLS15oe7LJGY3nngMrQC%26index%3D1%26t%3D4s", result.getQueryParams().get("path").get(0)   );

        result = StringUtil.urlDecode("http://192.168.1.6:8221/commands/exec?cmd=print&args=unu,doi,4");
        Assert.assertEquals(3, result.getQueryParams().get("args"));
        Assert.assertEquals("unu", result.getQueryParams().get("args").get(0));
        Assert.assertEquals("doi", result.getQueryParams().get("args").get(1));
        Assert.assertEquals("4", result.getQueryParams().get("args").get(2));
    }





    public static void main(String[] args) {
        StringUtilTest stringUtilTest = new StringUtilTest();
        stringUtilTest.testDecode();
        stringUtilTest.testParsing();
        stringUtilTest.testQuote();
        stringUtilTest.testToNumber();

    }

    class TestClass {
        String value;
    }

    class  T2 {
        TestClass testClass;
        String string;
    }
}