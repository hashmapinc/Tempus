package com.hashmapinc.server.transport.mqtt.sparkplugB.data;

import com.hashmapinc.server.common.data.kv.KvEntry;

import java.util.List;

public class SparkPlugDecodedMsg {
    private int requestId;
    private Long ts;
    private List<KvEntry> kvEntryList;

    public SparkPlugDecodedMsg(int requestId, Long ts, List<KvEntry> kvEntryList){
        this.requestId = requestId;
        this.ts = ts;
        this.kvEntryList = kvEntryList;
    }

    public int getRequestId() {
        return requestId;
    }

    public Long getTs() {
        return ts;
    }

    public List<KvEntry> getKvEntryList() {
        return kvEntryList;
    }
}
