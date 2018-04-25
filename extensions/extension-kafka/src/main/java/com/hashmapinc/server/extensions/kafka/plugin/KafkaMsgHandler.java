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
package com.hashmapinc.server.extensions.kafka.plugin;

import com.hashmapinc.server.common.data.id.RuleId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.msg.core.BasicStatusCodeResponse;
import com.hashmapinc.server.extensions.api.plugins.PluginContext;
import com.hashmapinc.server.extensions.api.plugins.handlers.RuleMsgHandler;
import com.hashmapinc.server.extensions.api.plugins.msg.ResponsePluginToRuleMsg;
import com.hashmapinc.server.extensions.api.plugins.msg.RuleToPluginMsg;
import com.hashmapinc.server.extensions.api.rules.RuleException;
import com.hashmapinc.server.extensions.kafka.action.KafkaActionMsg;
import com.hashmapinc.server.extensions.kafka.action.KafkaActionPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

@RequiredArgsConstructor
@Slf4j
public class KafkaMsgHandler implements RuleMsgHandler {

    private final Producer<?, String> producer;

    @Override
    public void process(PluginContext ctx, TenantId tenantId, RuleId ruleId, RuleToPluginMsg<?> msg) throws RuleException {
        if (!(msg instanceof KafkaActionMsg)) {
            throw new RuleException("Unsupported message type " + msg.getClass().getName() + "!");
        }
        KafkaActionPayload payload = ((KafkaActionMsg) msg).getPayload();
        log.debug("Processing kafka payload: {}", payload);
        try {
            producer.send(new ProducerRecord<>(payload.getTopic(), payload.getMsgBody()),
                    (metadata, e) -> {
                        if (payload.isSync()) {
                            if (metadata != null) {
                                ctx.reply(new ResponsePluginToRuleMsg(msg.getUid(), tenantId, ruleId,
                                        BasicStatusCodeResponse.onSuccess(payload.getMsgType(), payload.getRequestId()), msg.getDeliveryId()));
                            } else {
                                ctx.reply(new ResponsePluginToRuleMsg(msg.getUid(), tenantId, ruleId,
                                        BasicStatusCodeResponse.onError(payload.getMsgType(), payload.getRequestId(), e), msg.getDeliveryId()));
                            }
                        }
                    });
        } catch (Exception e) {
            throw new RuleException(e.getMessage(), e);
        }
    }
}
