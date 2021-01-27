package com.arise.core.tools;

public interface FilterCriteria<T> {
    boolean isAcceptable(T data);
}
