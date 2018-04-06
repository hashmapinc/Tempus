package com.hashmapinc.reader;

public interface Reader<T, W> {
    W next(T key);
}
