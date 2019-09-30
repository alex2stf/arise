package com.arise.astox.net.models;

public interface PayloadConverter<T> extends PayloadSerializer<T> {

  T deserialize(byte [] bytes);
}
