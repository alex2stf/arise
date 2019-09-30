package com.arise.astox.net.models;

public interface PayloadSerializer<T> {
  byte[] serialize(T obj);
}
