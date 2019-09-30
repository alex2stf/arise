package com.arise.core.tools;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import org.junit.Test;

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