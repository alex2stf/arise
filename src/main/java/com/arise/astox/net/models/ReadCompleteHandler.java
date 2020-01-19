package com.arise.astox.net.models;

public abstract class ReadCompleteHandler<T> {
    public abstract void onReadComplete(T data);

    public void onError(){

    };
}
