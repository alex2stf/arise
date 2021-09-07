package com.arise.core.models;

import com.arise.core.tools.StringUtil;

public class Tuple2<A, B> {
    final A a;
    final B b;

    public Tuple2(A a, B b) {
        this.a = a;
        this.b = b;
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
}
