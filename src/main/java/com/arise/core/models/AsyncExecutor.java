package com.arise.core.models;

import java.util.Map;

public interface AsyncExecutor {
    void put(String returnName, Provider<Object> provider);
    Map<String, Object> results();
}
