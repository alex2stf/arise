package com.arise.core.tools.models;

public interface Convertor<O, I> {
    O convert(I data);
}
