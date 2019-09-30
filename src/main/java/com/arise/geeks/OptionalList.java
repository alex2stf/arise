package com.arise.geeks;

import java.util.ArrayList;

public class OptionalList<T> extends ArrayList<T> {
    @Override
    public T get(int i) {
        if (size() == 0){
            return null;
        }
        if (i > size() -1){
            return super.get(size() - 1);
        }
        return super.get(i);
    }
}
