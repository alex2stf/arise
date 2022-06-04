package com.arise.core.tools;

import java.util.concurrent.atomic.AtomicInteger;

public class Assert {

    private static final Mole log = Mole.getInstance(Assert.class);

    private  static final AtomicInteger cnt = new AtomicInteger();

    public static void assertEquals(Object s1, Object s2){
        try {
            assert s1.equals(s2);
            tick();
        } catch (Throwable e){
            throwException(s1, s2, e);
        }
    }

    private static void tick() {
        StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        if (ste == null || ste.length < 3){
            return;
        }
        StackTraceElement as = ste[2];
        StackTraceElement cm = ste[3];
        int c = cnt.incrementAndGet();
        log.info(
                "T(" + c + ") " + as.getMethodName() + " passed at " + cm.getClassName() + "#" + cm.getMethodName()
        );

        /*
        * 0 - currentThread().getStackTrace
        * 1 - Assert#tick
        * 2 - Assert#equals
        * 3 - method
        * */

    }

    public static void assertEquals(boolean s1, boolean s2){
        try {
            assert s1 == s2;
            tick();
        } catch (Throwable e){
            throwException(s1, s2, e);
        }
    }


    public static void assertEquals(String s1, String s2){
        try {
            assert s1.equals(s2);
            tick();
            if (!s1.equals(s2)){
                throwException(s1, s2, new RuntimeException("assert disabled"));
            }
        } catch (Throwable e){
            throwException(s1, s2, e);
        }
    }

    public static void assertEquals(int s1, int s2){
        if (s1 != s2){
            throwException(s1, s2);
        }
        tick();
    }

    public static void assertEquals(double s1, double s2, int decimals){
        try {
            assert Double.compare(s1, s2) == 0;
            tick();
        } catch (Throwable e){
            throwException(s1, s2, e);
        }
    }

    public static void throwException(Object a, Object b, Throwable c){
        System.out.println("Expected " + a + " given " + b);
        throw new AssertException(a, b, c);
    }

    public static void throwException(Object a, Object b){
        throwException(a, b, new AssertException(a, b));
    }

    public static void assertNull(Object aNull) {
        try {
            assert  null == aNull;
            tick();
        } catch (Throwable e){
            throwException("null", aNull, e);
        }
    }

    public static void assertTrue(boolean given) {
        if (!given){
            throwException(true, false);
        }
        tick();
    }

    public static void assertFalse(boolean given) {
        if (given){
            throwException(true, false);
        }
        tick();
    }

    public static void assertNotNull(Object given) {
        try {
            assert  null != given;
            tick();
        } catch (Throwable e){
            throwException("null", "not null object", e);
        }
    }

    public static void assertThrows(Runnable r, Class<? extends Throwable> t) {
        try {
            r.run();
        }catch (Exception e){
            if (!e.getClass().isAssignableFrom(t)){
                throwException(e.getClass(), t);
            }
            tick();
        }
    }

    public static void assertFailed(Exception e) {
        e.printStackTrace();
        System.exit(-1);
    }

    public static final class AssertException extends RuntimeException {

        public AssertException(Object expect, Object given, Throwable cause){
            super("Expected " + expect + " given " + given, cause);
        }

        AssertException(Object expect, Object given){
            super("Expected " + expect + " given " + given);
        }
    }
}
