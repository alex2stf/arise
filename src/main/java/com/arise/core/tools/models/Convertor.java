package com.arise.core.tools.models;

public interface Convertor<T, O> {
    T convert(O data);
}
