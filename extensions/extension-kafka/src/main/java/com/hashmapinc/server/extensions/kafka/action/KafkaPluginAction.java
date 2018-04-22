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
package com.hashmapinc.server.extensions.kafka.action;

import com.hashmapinc.server.common.msg.session.FromDeviceRequestMsg;
import com.hashmapinc.server.extensions.api.component.Action;
import com.hashmapinc.server.extensions.api.plugins.msg.RuleToPluginMsg;
import com.hashmapinc.server.extensions.api.rules.RuleContext;
import com.hashmapinc.server.extensions.core.action.template.AbstractTemplatePluginAction;
import lombok.extern.slf4j.Slf4j;
import com.hashmapinc.server.common.msg.device.ToDeviceActorMsg;

import java.util.Optional;

@Action(name = "Kafka Plugin Action", descriptor = "KafkaActionDescriptor.json", configuration = KafkaPluginActionConfiguration.class)
@Slf4j
public class KafkaPluginAction extends AbstractTemplatePluginAction<KafkaPluginActionConfiguration> {

    @Override
    protected Optional<RuleToPluginMsg> buildRuleToPluginMsg(RuleContext ctx, ToDeviceActorMsg msg, FromDeviceRequestMsg payload) {
        KafkaActionPayload.KafkaActionPayloadBuilder builder = KafkaActionPayload.builder();
        builder.msgType(payload.getMsgType());
        builder.requestId(payload.getRequestId());
        builder.sync(configuration.isSync());
        builder.topic(configuration.getTopic());
        builder.msgBody(getMsgBody(ctx, msg));
        return Optional.of(new KafkaActionMsg(msg.getTenantId(),
                msg.getCustomerId(),
                msg.getDeviceId(),
                builder.build()));
    }
}
