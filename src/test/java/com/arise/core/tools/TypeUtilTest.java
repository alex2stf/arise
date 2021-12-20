package com.arise.core.tools;

import com.arise.core.tools.TypeUtil.IteratorHandler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class TypeUtilTest {

  public void testDates(){

    Map<String, Object> map = TypeUtil.objectToMap(new Date());
    System.out.println(map);
  }


  public void testSimpleIterator(){

    int src[] = new int[]{0, 1, 2, 3, 4};
    TypeUtil.forEach(src, new IteratorHandler() {
      @Override
      public void found(Object key, Object value, int index) {
        Assert.assertEquals(key, index);
        Assert.assertEquals((int) value, index);
        Assert.assertEquals((int)value, (int)key);
      }
    });

    String str = "01234";
    TypeUtil.forEach(str, new IteratorHandler() {
      @Override
      public void found(Object key, Object value, int index) {
        Assert.assertTrue(key instanceof Character);
        Assert.assertTrue(value instanceof Character);
        Assert.assertEquals(Integer.valueOf(key.toString()).intValue(), index);
        Assert.assertEquals(Integer.valueOf(value.toString()).intValue(), index);
        Assert.assertEquals(Integer.valueOf(value.toString()).intValue(), Integer.valueOf(key.toString()).intValue());
      }
    });


  }

  public void testIgnoreNulls(){
    final List<Entry> entryList = new ArrayList<>();
    Integer nullables[] = new Integer[]{1, 2, 3, null, 4};
    TypeUtil.forEach(nullables, new IteratorHandler() {
      @Override
      public void found(Object key, Object value, int index) {
        entryList.add(new Entry(key, value, index));
      }
    }, true);

    Assert.assertEquals(4, entryList.size());
    Assert.assertEquals(3, (int)entryList.get(3).key);
    Assert.assertEquals(4, (int)entryList.get(3).value);

    entryList.clear();

    TypeUtil.forEach(nullables, new IteratorHandler() {
      @Override
      public void found(Object key, Object value, int index) {
        entryList.add(new Entry(key, value, index));
      }
    }, false);

    Assert.assertEquals(5, entryList.size());
    Assert.assertEquals(3, (int)entryList.get(3).key);
    Assert.assertEquals(4, (int)entryList.get(4).key);
    Assert.assertEquals(4, (int)entryList.get(4).value);
    Assert.assertNull(entryList.get(3).value);


    entryList.clear();

    List<String> strings = new ArrayList<>();
    strings.add("0");
    strings.add("1");
    strings.add("2");
    strings.add(null);
    strings.add("3");

    TypeUtil.forEach(strings, new IteratorHandler() {
      @Override
      public void found(Object key, Object value, int index) {
        entryList.add(new Entry(key, value, index));
      }
    }, true);

    Assert.assertEquals(4, entryList.size());
  }

  public void testObjectIterator(){
    Base base = new Base();
    base.baseStringPackageProtected = "baseStringPackageProtected_val";
    base.baseStringPublic = "baseStringPublic_v";
    base.baseStringPrivate = "baseStringPrivate_v";
    base.baseStringProtected = "baseStringProtected_v";


    final List<Entry> entries = new ArrayList<>();

    TypeUtil.forEach(base, new IteratorHandler() {
      @Override
      public void found(Object key, Object value, int index) {
        System.out.println("FOUND " + key + " = " + value + " index " + index);
        entries.add(new Entry(key, value, index));
      }
    });

    Assert.assertEquals(5, entries.size());
  }


  class  Base {
    public String baseStringPublic;
    private String baseStringPrivate;
    protected String baseStringProtected;
    String baseStringPackageProtected;

    private final String baseStringWithGetter;

    Base() {
      this.baseStringWithGetter = "baseStringWithGetter";
    }

    public String getBaseStringWithGetter() {
      return baseStringWithGetter;
    }
  }

  class Derived extends Base {


  }


  class Entry{

    final Object key;
    final Object value;
    final Object index;

    Entry(Object key, Object value, Object index){
      this.key = key;
      this.value = value;
      this.index = index;
    }
  }
}