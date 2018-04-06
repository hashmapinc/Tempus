package com.hashmapinc.offset;

public interface OffsetManager<K, O> {

    void commit(O msgId, K key);

    O current(K key);
}
