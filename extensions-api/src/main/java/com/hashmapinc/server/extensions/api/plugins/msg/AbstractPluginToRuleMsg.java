/**
 * Copyright © 2017-2018 Hashmap, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hashmapinc.server.extensions.api.plugins.msg;

import com.hashmapinc.server.common.data.id.RuleId;
import com.hashmapinc.server.common.data.id.TenantId;

import java.io.Serializable;
import java.util.UUID;

public class AbstractPluginToRuleMsg<T extends Serializable> implements PluginToRuleMsg<T> {

    private static final long serialVersionUID = 1L;

    private final UUID uid;
    private final TenantId tenantId;
    private final RuleId ruleId;
    private final T payload;
    private final Long deliveryId;

    public AbstractPluginToRuleMsg(UUID uid, TenantId tenantId, RuleId ruleId, T payload, Long deliveryId) {
        super();
        this.uid = uid;
        this.tenantId = tenantId;
        this.ruleId = ruleId;
        this.payload = payload;
        this.deliveryId = deliveryId;
    }

    @Override
    public UUID getUid() {
        return uid;
    }

    @Override
    public TenantId getTenantId() {
        return tenantId;
    }

    @Override
    public T getPayload() {
        return payload;
    }

    @Override
    public Long getDeliveryId() {
        return deliveryId;
    }

    @Override
    public RuleId getRuleId() {
        return ruleId;
    }



}
