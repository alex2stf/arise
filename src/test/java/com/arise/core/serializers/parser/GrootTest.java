package com.arise.core.serializers.parser;

import com.arise.core.serializers.parser.Groot.Syntax;
import com.arise.core.tools.Assert;
import com.arise.core.tools.TypeUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.arise.core.serializers.parser.Groot.decodeBytes;
import static com.arise.core.tools.Assert.*;
import static com.arise.core.tools.TypeUtil.isNumericSequence;

public class GrootTest {


    private static void assertTest(Object expect, String in) {
        assertEquals(
                decodeBytes(in), expect
        );
    }

    public static void main(String[] args) {

        Map map = (Map) decodeBytes("{\"a\": {\"b\": 1}}");
        assertEquals(1, ((Map) map.get("a")).get("b"));


        map = (Map) decodeBytes("{\"a\": true}");
        assertEquals(true, map.get("a"));


        assertTest("ORACLE_CLIENT", "\"ORACLE_CLIENT\" ");
        assertTest("xxx", "\"xxx\"");
        assertTest("xxx", "\"xxx\"   \n");
        assertTest("xxx", "   \t \"xxx\"   \n");

        assertTest(43, " 43 ");
        assertTest(224, " 224 \n\n\n\n\n\n\n\n\n");
        assertTest(3456, " \t       \n \n\t 3456 \n\n\n\n\n\n\n\n\n\t");
        assertTest(98, "98");


        assertTest(false, " false ");
        assertTest(false, " false \n\n\n\n\n\n\n\n\n");
        assertTest(false, " \t       \n \n\t false \n\n\n\n\n\n\n\n\n\t");
        assertTest(false, "false");


        assertNull(decodeBytes("null"));
        assertNull(decodeBytes("   null    "));
        assertNull(decodeBytes("   null    \n\t"));
        assertNull(decodeBytes("   null"));


        assertTest(true, " true ");
        assertTest(true, " true \n\n\n\n\n\n\n\n\n");
        assertTest(true, " \t       \n \n\t true \n\n\n\n\n\n\n\n\n\t");
        assertTest(true, "true");

        testMapSerialization();
        testNumericSequence();
        jsonTest1();
        jsonTest2();
        testProducts();
    }


    private static void testToDoubleFromBytes(String in, int from, int to, double expect) {
        assertEquals(expect, TypeUtil.toDouble(in.getBytes(), from, to).doubleValue(), 0);
    }

    private static void testToDoubleFromBytes(String in, double expect) {
        testToDoubleFromBytes(in, 0, in.length(), expect);
    }


    public static void testMapSerialization() {
        Map map = new HashMap();
        map.put("1", "1");
        map.put("text", "true");
        map.put("false", false);
        map.put("num", 23);
        map.put(false, 23);
    }

    public static void testNumericSequence() {
        assertTrue(isNumericSequence("1234"));
        assertTrue(isNumericSequence("123.4"));
        assertTrue(isNumericSequence("-45123.4"));
        assertFalse(isNumericSequence("123.4.5"));
        assertFalse(isNumericSequence("--12453"));
        assertFalse(isNumericSequence("r0393"));
        assertFalse(isNumericSequence("123".getBytes(), 4, 1));

        String in = "x123.45x";
        testToDoubleFromBytes(in, 1, in.length() - 1, 123.45);

        in = "-123";
        testToDoubleFromBytes(in, -123);

        in = "-123x";
        testToDoubleFromBytes(in, 0, in.length() - 1, -123);


        in = "456";
        testToDoubleFromBytes(in, 0, in.length(), 456);
    }

    public static void jsonTest1() {
        Map obj = (Map) decodeBytes("{}");
        Assert.assertNotNull(obj);
        assertTrue(obj.isEmpty());

        obj = (Map) decodeBytes("{ }");
        Assert.assertNotNull(obj);
        assertTrue(obj.isEmpty());


        obj = (Map) decodeBytes(" { } ");
        Assert.assertNotNull(obj);
        assertTrue(obj.isEmpty());

        assertTrue((boolean) decodeBytes("true"));
        assertFalse((boolean) decodeBytes("false"));
        assertEquals(false, decodeBytes("false"));

        List arr = (List) decodeBytes("[]");
        Assert.assertNotNull(arr);
        assertTrue(arr.isEmpty());

        Integer intg = (Integer) decodeBytes("123".getBytes(), 0, "123".length() - 1, Syntax.STANDARD);
        assertEquals(123, intg.intValue());


        intg = (Integer) decodeBytes("1123");
        assertEquals(1123, intg.intValue());

        intg = (Integer) decodeBytes("-5123");
        assertEquals(-5123, intg.intValue());

        Double dbl = (Double) decodeBytes("123.45");
        assertEquals(123.45, dbl.doubleValue(), 0);


        dbl = (Double) decodeBytes("-123.453545");
        assertEquals(-123.453545, dbl.doubleValue(), 0);

    }


    public static void jsonTest2() {
        Map r;
        r = (Map) decodeBytes(" {\"a\":2,\n\"c\":{\"a\":4},\"d\":15}");

        assertEquals(2, r.get("a"));
        assertEquals(15, r.get("d"));
        assertEquals(4, ((Map) r.get("c")).get("a"));

        r = (Map) decodeBytes("{\"list\": [1,true,{\"g\":45}, null]}");

        List list = (List) r.get("list");

        assertEquals(4, list.size());
        assertEquals(1, list.get(0));
        assertEquals(true, list.get(1));
        assertEquals(45, ((Map) list.get(2)).get("g"));
        assertNull(list.get(3));

    }


    public static void testProducts() {

        String products = "[{\n"
                + "  \"_id\": {\n"
                + "    \"$oid\": \"5968dd23fc13ae04d9000001\"\n"
                + "  },\n"
                + "  \"product_name\": \"sildenafil citrate\",\n"
                + "  \"supplier\": \"Wisozk Inc\",\n"
                + "  \"quantity\": 261,\n"
                + "  \"unit_cost\": \"$10.47\"\n"
                + "}, {\n"
                + "  \"_id\": {\n"
                + "    \"$oid\": \"5968dd23fc13ae04d9000002\"\n"
                + "  },\n"
                + "  \"product_name\": \"Mountain Juniperus ashei\",\n"
                + "  \"supplier\": \"Keebler-Hilpert\",\n"
                + "  \"quantity\": 292,\n"
                + "  \"unit_cost\": \"$8.74\"\n"
                + "}, {\n"
                + "  \"_id\": {\n"
                + "    \"$oid\": \"5968dd23fc13ae04d9000003\"\n"
                + "  },\n"
                + "  \"product_name\": \"Dextromathorphan HBr\",\n"
                + "  \"supplier\": \"Schmitt-Weissnat\",\n"
                + "  \"quantity\": 211,\n"
                + "  \"unit_cost\": \"$20.53\"\n"
                + "}]";

        List response = (List) decodeBytes(products);

        assertEquals("5968dd23fc13ae04d9000001",
                ((Map) ((Map) response.get(0)).get("_id")).get("$oid")
        );


    }


    class TestClass {
        public String transientMarked = "transientMarket";
        String packageString;
        String customNamed;
        private String neverShowed;

        public String getFieldJsonIgnore() {
            return "ignored_json_field";
        }

        public String getName() {
            return "WTF";
        }
    }

}