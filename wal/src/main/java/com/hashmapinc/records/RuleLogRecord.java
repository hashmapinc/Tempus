package com.hashmapinc.records;

import java.util.UUID;

public class RuleLogRecord extends TenantAwareLogRecord<UUID>{

    public RuleLogRecord(Long logId, TenantAwareLogRecord record) {
        super(logId, record);
    }

    public RuleLogRecord(UUID tenantId, UUID key, byte[] message) {
        super(tenantId, key, message);
    }

    @Override
    public Long getMsgId() {
        return getLogId();
    }
}
