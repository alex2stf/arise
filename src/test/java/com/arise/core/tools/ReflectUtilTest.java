package com.arise.core.tools;

import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.Assert.assertTrue;

public class ReflectUtilTest {

  @TestAnnotation1
  public String test;


  @Test
  public void testReflection(){
    Method method1 = ReflectUtil.searchMethodInClass(A.class, "read", new Class[]{String.class});
    System.out.println(method1);
    Method method2 = ReflectUtil.searchMethodInClass(C.class, "read", new Class[]{String.class});
    Assert.assertEquals(method1, method2);
  }

  @Test
  public void annotationsMatch() throws NoSuchFieldException {
    Field s = ReflectUtilTest.class.getDeclaredField("test");
    assertTrue(ReflectUtil.annotationsMatch(s.getDeclaredAnnotations(), new String[]{"com.arise.core.tools.TestAnnotation1"}));
    assertTrue(ReflectUtil.hasAnyOfTheAnnotations(s, "com.arise.core.tools.TestAnnotation1"));
  }

  public static void main(String[] args) {
    new ReflectUtilTest().testReflection();
  }


  class A {
    void read(String s){
      System.out.println("test");
    }
  }

  class B extends A {

  }

  class  C extends B {

  }
}