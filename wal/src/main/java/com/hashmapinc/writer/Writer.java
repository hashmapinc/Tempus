package com.hashmapinc.writer;

import com.hashmapinc.records.LogRecord;

public interface Writer<T extends LogRecord> {

    void append(T record);

}
