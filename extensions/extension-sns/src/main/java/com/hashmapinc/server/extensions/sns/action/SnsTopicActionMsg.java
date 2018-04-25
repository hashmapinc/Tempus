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
package com.hashmapinc.server.extensions.sns.action;

import com.hashmapinc.server.common.data.id.CustomerId;
import com.hashmapinc.server.common.data.id.DeviceId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.extensions.api.plugins.msg.AbstractRuleToPluginMsg;
import com.hashmapinc.server.extensions.api.plugins.msg.RuleToPluginMsg;

/**
 * Created by Valerii Sosliuk on 11/15/2017.
 */
public class SnsTopicActionMsg extends AbstractRuleToPluginMsg<SnsTopicActionPayload> {

    public SnsTopicActionMsg(TenantId tenantId, CustomerId customerId, DeviceId deviceId, SnsTopicActionPayload payload) {
        super(tenantId, customerId, deviceId, payload);
    }

    private SnsTopicActionMsg(SnsTopicActionMsg msg, Long deliveryId){
        super(msg, deliveryId);
    }

    @Override
    public RuleToPluginMsg<SnsTopicActionPayload> copyDeliveryId(Long deliveryId) {
        return new SnsTopicActionMsg(this, deliveryId);
    }
}
