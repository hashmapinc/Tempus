package com.hashmapinc.server.actors.shared.application;

import com.hashmapinc.server.actors.ActorSystemContext;
import com.hashmapinc.server.common.data.id.TenantId;

public class TenantApplicationManager extends ApplicationManager{

    private final TenantId tenantId;

    @Override
    TenantId getTenantId() {
        return tenantId;
    }

    public TenantApplicationManager(ActorSystemContext systemContext, TenantId tenantId) {
        super(systemContext);
        this.tenantId = tenantId;
    }
}
