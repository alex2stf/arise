package com.arise.core.models;

public interface FilterCriteria<T> {
    boolean isAcceptable(T data);
}
