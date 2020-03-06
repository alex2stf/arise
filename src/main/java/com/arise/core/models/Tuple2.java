package com.arise.core.models;

public class Tuple2<A, B> {
    final A a;
    final B b;

    public Tuple2(A first, B second) {
        this.a = first;
        this.b = second;
    }

    public A first() {
        return a;
    }

    public B second() {
        return b;
    }
}
