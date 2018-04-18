package com.hashmapinc.server.common.msg.kv;

import com.hashmapinc.server.common.data.kv.TsKvEntry;

import java.io.Serializable;
import java.util.List;

public interface TelemetryKVMsg extends Serializable {
    List<TsKvEntry> getDeviceTelemetry();
}
