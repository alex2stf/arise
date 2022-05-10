package com.arise.core.models;

public interface Handler<T> {
    void handle(T t);
}
