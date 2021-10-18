package com.arise.core.serializers.parser;

import com.arise.core.serializers.parser.Groot.Syntax;
import com.arise.core.tools.TypeUtil;
//import com.fasterxml.jackson.annotation.JsonIgnore;
//import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.Assert;
import org.junit.Test;

//import javax.persistence.Transient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GrootTest {


  private static void assertTest(Object expect, String in){
    com.arise.core.tools.Assert.assertEquals(
            Groot.decodeBytes(in), expect
    );
  }

  public static void main(String[] args) {

    Map map;

    map = (Map) Groot.decodeBytes("{\"a\": {\"b\": [1, true, false, 56, {\"x\": 2, \"y\": 3}, [true, {\"z\": 45}] ]}}");


    System.out.println(map);


    map = (Map) Groot.decodeBytes("{\"a\": {\"b\": 1}}");
    com.arise.core.tools.Assert.assertEquals(1, ((Map)map.get("a")).get("b"));



    map = (Map) Groot.decodeBytes("{\"a\": true}");
    com.arise.core.tools.Assert.assertEquals(true, map.get("a"));



    assertTest("ORACLE_CLIENT", "\"ORACLE_CLIENT\" ");
    assertTest("xxx", "\"xxx\"");
    assertTest("xxx", "\"xxx\"   \n");
    assertTest("xxx", "   \t \"xxx\"   \n");

    GrootTest grootTest = new GrootTest();
    grootTest.jsonTest1();

    assertTest(43, " 43 ");
    assertTest(224, " 224 \n\n\n\n\n\n\n\n\n");
    assertTest(3456, " \t       \n \n\t 3456 \n\n\n\n\n\n\n\n\n\t");
    assertTest(98, "98");


    assertTest(false, " false ");
    assertTest(false, " false \n\n\n\n\n\n\n\n\n");
    assertTest(false, " \t       \n \n\t false \n\n\n\n\n\n\n\n\n\t");
    assertTest(false, "false");


    com.arise.core.tools.Assert.assertNull(Groot.decodeBytes("null"));
    com.arise.core.tools.Assert.assertNull(Groot.decodeBytes("   null    "));
    com.arise.core.tools.Assert.assertNull(Groot.decodeBytes("   null    \n\t"));
    com.arise.core.tools.Assert.assertNull(Groot.decodeBytes("   null"));


    assertTest(true, " true ");
    assertTest(true, " true \n\n\n\n\n\n\n\n\n");
    assertTest(true, " \t       \n \n\t true \n\n\n\n\n\n\n\n\n\t");
    assertTest(true, "true");

  }


  private void testToDoubleFromBytes(String in, int from, int to, double expect){
    Assert.assertEquals(expect, TypeUtil.toDouble(in.getBytes(), from, to).doubleValue(), 0);
  }

  private void testToDoubleFromBytes(String in, double expect){
    testToDoubleFromBytes(in, 0, in.length(), expect);
  }


  @Test
  public void testMapSerialization(){
    Map map = new HashMap();
    map.put("1", "1");
    map.put("text", "true");
    map.put("false", false);
    map.put("num", 23);
    map.put(false, 23);
    String txt = Groot.toJson(map);
    System.out.println(Groot.toJson(false));
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
    Map obj =  (Map) Groot.decodeBytes("{}");
    Assert.assertNotNull(obj);
    Assert.assertTrue(obj.isEmpty());

    obj = (Map) Groot.decodeBytes("{ }");
    Assert.assertNotNull(obj);
    Assert.assertTrue(obj.isEmpty());


    obj = (Map) Groot.decodeBytes(" { } ");
    Assert.assertNotNull(obj);
    Assert.assertTrue(obj.isEmpty());

    Assert.assertTrue((boolean) Groot.decodeBytes("true"));
    Assert.assertFalse((boolean) Groot.decodeBytes("false"));
    Assert.assertEquals(false, Groot.decodeBytes("false"));

    List arr = (List) Groot.decodeBytes("[]");
    Assert.assertNotNull(arr);
    Assert.assertTrue(arr.isEmpty());

    Integer intg = (Integer) Groot.decodeBytes("123".getBytes(), 0, "123".length() - 1, Syntax.STANDARD);
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

    String data = "{\"i\":10,\"d\":[{\"A\":\"Brand Music\",\"F\":\"Samsung\",\"T\":\"Over the Horizon\",\"P\":\"%2Fstorage%2Femulated%2F0%2FSamsung%2FMusic%2FOver_the_Horizon.mp3\"},{\"P\":\"%2Fstorage%2Femulated%2F0%2FMusic%2FBruce+Springsteen+-+White+Lies.mp3\"},{\"A\":\"Greatest Hits\",\"F\":\"Bruce Springsteen\",\"X\":\"Bruce Springsteen\",\"T\":\"Better Days\",\"P\":\"%2Fstorage%2Femulated%2F0%2FMusic%2FBruce+Springsteen+-+Better+Days.mp3\"},{\"A\":\"Born In The U.S.A.\",\"F\":\"Bruce Springsteen\",\"X\":\"Bruce Springsteen\",\"T\":\"Bobby Jean\",\"P\":\"%2Fstorage%2Femulated%2F0%2FMusic%2FBruce+Springsteen+-+Bobby+Jean.mp3\"},{\"A\":\"Born In The U.S.A.\",\"F\":\"Bruce Springsteen\",\"X\":\"Bruce Springsteen\",\"T\":\"Cover Me\",\"P\":\"%2Fstorage%2Femulated%2F0%2FMusic%2FBruce+Springsteen+-+Cover+Me.mp3\"},{\"A\":\"Born In The U.S.A.\",\"F\":\"Bruce Springsteen\",\"X\":\"Bruce Springsteen\",\"T\":\"Downbound Train\",\"P\":\"%2Fstorage%2Femulated%2F0%2FMusic%2FBruce+Springsteen+-+Downbound+Train.mp3\"},{\"A\":\"1999 - 18 tracks\",\"F\":\"Bruce Springsteen\",\"T\":\"Growin'up\",\"P\":\"%2Fstorage%2Femulated%2F0%2FMusic%2FBruce+Springsteen+-+Growin%27up.mp3\"},{\"A\":\"1999 - 18 tracks\",\"F\":\"Bruce Springsteen\",\"T\":\"Hearts of stone\",\"P\":\"%2Fstorage%2Femulated%2F0%2FMusic%2FBruce+Springsteen+-+Hearts+of+stone.mp3\"},{\"P\":\"%2Fstorage%2Femulated%2F0%2FMusic%2FBruce+Springsteen+-+I+hung+my+head.mp3\"},{\"A\":\"Born In The U.S.A.\",\"F\":\"Bruce Springsteen\",\"X\":\"Bruce Springsteen\",\"T\":\"I'm Goin' Down\",\"P\":\"%2Fstorage%2Femulated%2F0%2FMusic%2FBruce+Springsteen+-+I%27m+Goin%27+Down.mp3\"}]}";
//   data = data.replaceAll("//s+", "");
    Object m = (Groot.decodeBytes(data));
    System.out.println(m);
  }



  class TestClass{
    String packageString;

//    @JsonProperty(value = "property_name", required = true)
    String customNamed;

//    @JsonIgnore
    private String neverShowed;


//    @Transient
    public String transientMarked = "transientMarket";
//    @JsonIgnore
    public String getFieldJsonIgnore(){ return "ignored_json_field"; }
    public String getName(){
      return "WTF";
    }
  }

}