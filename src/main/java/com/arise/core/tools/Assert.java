package com.arise.core.tools;

public class Assert {

    public static void assertEquals(String s1, String s2){
        try {
            assert s1.equals(s2);
        } catch (Throwable e){
            throwException(s1, s2, e);
        }
    }

    public static void assertEquals(int s1, int s2){
        try {
            assert s1 == s2;
        } catch (Throwable e){
            throwException(s1, s2, e);
        }
    }

    public static void throwException(Object a, Object b, Throwable c){
        System.out.println("Expected " + a + " given " + b);
        throw new AssertException(a, b, c);

    }

    public static final class AssertException extends RuntimeException {

        public AssertException(Object expect, Object given, Throwable cause){
            super("Expected " + expect + " given " + given, cause);
        }
    }
}
