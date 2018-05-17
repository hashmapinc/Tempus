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
package com.hashmapinc.server.extensions.rabbitmq.action;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hashmapinc.server.common.data.id.CustomerId;
import com.hashmapinc.server.common.data.id.DeviceId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.extensions.api.plugins.msg.AbstractRuleToPluginMsg;
import com.hashmapinc.server.extensions.api.plugins.msg.RuleToPluginMsg;

import java.util.UUID;


public class RabbitMqActionMsg extends AbstractRuleToPluginMsg<RabbitMqActionPayload> {

    public RabbitMqActionMsg(TenantId tenantId, CustomerId customerId, DeviceId deviceId, RabbitMqActionPayload payload) {
        super(tenantId, customerId, deviceId, payload);
    }

    @JsonCreator
    private RabbitMqActionMsg(@JsonProperty("uid") UUID id,
                              @JsonProperty("tenantId") TenantId tenantId,
                              @JsonProperty("customerId") CustomerId customerId,
                              @JsonProperty("deviceId") DeviceId deviceId,
                              @JsonProperty("payload") RabbitMqActionPayload payload,
                              @JsonProperty("deliveryId") Long deliveryId){
        super(id, tenantId, customerId, deviceId, payload, deliveryId);
    }

    @Override
    public RuleToPluginMsg<RabbitMqActionPayload> copyDeliveryId(Long deliveryId) {
        return new RabbitMqActionMsg(this.getUid(), this.getTenantId(), this.getCustomerId(),
                this.getDeviceId(), this.getPayload(), deliveryId);
    }
}
