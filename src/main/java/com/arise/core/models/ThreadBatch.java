package com.arise.core.models;

import java.util.HashMap;
import java.util.Map;

public class ThreadBatch implements AsyncExecutor {

    Map<String, Provider> providerMap = new HashMap<>();

    @Override
    public void put(String returnName, Provider<Object> p) {
        providerMap.put(returnName, p);
    }

    @Override
    public Map<String, Object> results() {
        Map<String, Object> res = new HashMap<>();
        for (Map.Entry<String, Provider> entry: providerMap.entrySet()){
            res.put(entry.getKey(), entry.getValue().get());
        }
        return res;
    }
}
