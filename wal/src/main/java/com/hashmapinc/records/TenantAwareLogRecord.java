package com.hashmapinc.records;

import java.util.UUID;

public abstract class TenantAwareLogRecord<T> implements LogRecord<Long>{

    private UUID tenantId;
    private T key;
    private byte[] message;
    private Long logId;

    public TenantAwareLogRecord(Long logId, TenantAwareLogRecord<T> record){
        this.logId = logId;
        this.tenantId = record.tenantId;
        this.key = record.key;
        this.message = record.message;
    }

    public TenantAwareLogRecord(final UUID tenantId, final T key, final byte[] message){
        this.tenantId = tenantId;
        this.key = key;
        this.message = message;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public T getKey() {
        return key;
    }

    public byte[] getMessage() {
        return message;
    }

    public Long getLogId() {
        return logId;
    }
}
