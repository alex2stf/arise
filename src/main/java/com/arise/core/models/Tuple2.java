package com.arise.core.models;

public class Tuple2<A, B> {
    final A a;
    final B b;

    public Tuple2(A a, B b) {
        this.a = a;
        this.b = b;
    }

    //TODO make this cacheable based on hashCode
    public static <A, B> Tuple2<A, B> of(A a, B b) {
        return new Tuple2<>(a, b);
    }

    public A first() {
        return a;
    }

    public B second() {
        return b;
    }

    public static final Tuple2<String, String> str(String x, String y){
        return new Tuple2<>(x, y);
    }


    public static Tuple2<Integer, Integer>  numInt(int a, int b) {
        return new Tuple2<>(a, b);
    }
}
