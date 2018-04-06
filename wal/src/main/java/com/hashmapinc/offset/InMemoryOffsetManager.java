package com.hashmapinc.offset;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryOffsetManager implements OffsetManager<UUID, Long>{

    private final UUID tenantId;

    public InMemoryOffsetManager(UUID tenantId){
        this.tenantId = tenantId;
    }

    private ConcurrentHashMap<UUID, Long> offsets = new ConcurrentHashMap<>();

    @Override
    public void commit(Long msgId, UUID key) {
        offsets.put(key, msgId);
    }

    @Override
    public Long current(UUID key) {
        return offsets.get(key);
    }
}
