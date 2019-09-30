package com.arise.core.serializers.parser;

import com.arise.core.models.DeviceStat;
import com.arise.core.serializers.parser.Groot.Arr;
import com.arise.core.serializers.parser.Groot.Obj;
import com.arise.core.serializers.parser.Groot.Syntax;
import com.arise.core.tools.SYSUtils.OS;
import com.arise.core.tools.TypeUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

import javax.persistence.Transient;

public class GrootTest {



  private void testToDoubleFromBytes(String in, int from, int to, double expect){
    Assert.assertEquals(expect, TypeUtil.toDouble(in.getBytes(), from, to).doubleValue(), 0);
  }

  private void testToDoubleFromBytes(String in, double expect){
    testToDoubleFromBytes(in, 0, in.length(), expect);
  }


  @Test
  public void testNumericSequence(){
    Assert.assertTrue(TypeUtil.isNumericSequence("1234"));
    Assert.assertTrue(TypeUtil.isNumericSequence("123.4"));
    Assert.assertTrue(TypeUtil.isNumericSequence("-45123.4"));
    Assert.assertFalse(TypeUtil.isNumericSequence("123.4.5"));
    Assert.assertFalse(TypeUtil.isNumericSequence("--12453"));
    Assert.assertFalse(TypeUtil.isNumericSequence("r0393"));
    Assert.assertFalse(TypeUtil.isNumericSequence("123".getBytes(), 4, 1));

    String in = "x123.45x";
    testToDoubleFromBytes(in, 1, in.length() -1 , 123.45);

    in = "-123";
    testToDoubleFromBytes(in, -123);

    in = "-123x";
    testToDoubleFromBytes(in, 0, in.length() -1, -123);


    in = "456";
    testToDoubleFromBytes(in, 0, in.length(), 456);
  }

  @Test
  public void jsonTest1(){
    Obj obj = (Obj) Groot.decodeBytes("{}");
    Assert.assertNotNull(obj);
    Assert.assertTrue(obj.isEmpty());

    obj = (Obj) Groot.decodeBytes("{ }");
    Assert.assertNotNull(obj);
    Assert.assertTrue(obj.isEmpty());


    obj = (Obj) Groot.decodeBytes(" { } ");
    Assert.assertNotNull(obj);
    Assert.assertTrue(obj.isEmpty());

    Assert.assertTrue((boolean) Groot.decodeBytes("true"));
    Assert.assertFalse((boolean) Groot.decodeBytes("false"));
    Assert.assertEquals(false, Groot.decodeBytes("false"));

    Arr arr = (Arr) Groot.decodeBytes("[]");
    Assert.assertNotNull(arr);
    Assert.assertTrue(arr.isEmpty());

    Integer intg = (Integer) Groot.decodeBytes("123".getBytes(), 0, "123".length(), Syntax.STANDARD);
    Assert.assertEquals(123, intg.intValue());


    intg = (Integer) Groot.decodeBytes("1123");
    Assert.assertEquals(1123, intg.intValue());

    intg = (Integer) Groot.decodeBytes("-5123");
    Assert.assertEquals(-5123, intg.intValue());

    Double dbl = (Double) Groot.decodeBytes("123.45");
    Assert.assertEquals(123.45, dbl.doubleValue(), 0);



    dbl = (Double) Groot.decodeBytes("-123.453545");
    Assert.assertEquals(-123.453545, dbl.doubleValue(), 0);

  }


  @Test
  public void jsonTest2(){
    Map r;
    r = (Map) Groot.decodeBytes(" {\"a\":2,\n\"c\":{\"a\":4},\"d\":15}");

    Assert.assertEquals(2, r.get("a"));
    Assert.assertEquals(15, r.get("d"));
    Assert.assertEquals(4, ((Map) r.get("c")).get("a") );

    r = (Map) Groot.decodeBytes("{\"list\": [1,true,{\"g\":45}, null]}");

    List list = (List) r.get("list");

    Assert.assertEquals(4, list.size());
    Assert.assertEquals(1, list.get(0));
    Assert.assertEquals(true, list.get(1));
    Assert.assertEquals(45, ((Map)list.get(2)).get("g")  );
    Assert.assertNull(list.get(3));
    System.out.println(r);

  }


  @Test
  public void testProducts(){

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

    List response = (List) Groot.decodeBytes(products);

    Assert.assertEquals("5968dd23fc13ae04d9000001",
        ((Map)((Map)response.get(0)).get("_id")).get("$oid")
        );



    System.out.println(response);

  }

  @Test
  public void testProperties(){
    TestClass testClass = new TestClass();
    testClass.packageString = "packageString_v";
    testClass.customNamed = "custom_named_property";

    Assert.assertEquals(
            "{\"packageString\":\"packageString_v\",\"customNamed\":\"custom_named_property\",\"name\":\"WTF\"}",
            Groot.toJson(testClass)
    );
  }


  @Test
  public void  testDeviceStat(){
    DeviceStat deviceStat = new DeviceStat();
    deviceStat.setProp("one", "two");
    deviceStat.setBatteryLevel(23);

    System.out.println(Groot.toJson(deviceStat));

  }

  class TestClass{
    String packageString;

    @JsonProperty(value = "property_name", required = true)
    String customNamed;
    private String neverShowed;


    @Transient
    public String transientMarked = "transientMarket";
    @JsonIgnore
    public String getFieldJsonIgnore(){ return "ignored_json_field"; }
    public String getName(){
      return "WTF";
    }
  }

}