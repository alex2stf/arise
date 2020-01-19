package com.arise.core.tools.models;

public interface FilterCriteria<T> {
    boolean isAcceptable(T data);
}
