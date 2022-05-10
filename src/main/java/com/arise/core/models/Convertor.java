package com.arise.core.models;

public interface Convertor<O, I> {
    O convert(I data);
}
