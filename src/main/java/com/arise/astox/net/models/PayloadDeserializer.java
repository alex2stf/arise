package com.arise.astox.net.models;

public interface PayloadDeserializer<T> extends PayloadSerializer<T> {

  T deserialize(byte [] bytes);
}
