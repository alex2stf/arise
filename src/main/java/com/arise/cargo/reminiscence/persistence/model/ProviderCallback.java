package com.arise.cargo.reminiscence.persistence.model;

import java.util.List;

public class ProviderCallback {


    public void error(Throwable t){
        t.printStackTrace();
    }

    public static abstract class SingleItem<T> extends ProviderCallback{
        public abstract void found(T item);
    }

    public static abstract class ItemsList<T> extends ProviderCallback{
        public abstract void found(List<T> items);
    }
}
