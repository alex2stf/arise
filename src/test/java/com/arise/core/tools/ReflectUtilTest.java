package com.arise.core.tools;

import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertTrue;

public class ReflectUtilTest {

  @TestAnnotation1
  public String test;


  @Test
  public void annotationsMatch() throws NoSuchFieldException {
    Field s = ReflectUtilTest.class.getDeclaredField("test");
    assertTrue(ReflectUtil.annotationsMatch(s.getDeclaredAnnotations(), new String[]{"com.arise.core.tools.TestAnnotation1"}));
    assertTrue(ReflectUtil.hasAnyOfTheAnnotations(s, "com.arise.core.tools.TestAnnotation1"));
  }
}