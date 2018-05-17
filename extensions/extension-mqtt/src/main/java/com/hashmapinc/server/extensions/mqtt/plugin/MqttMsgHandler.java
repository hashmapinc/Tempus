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
package com.hashmapinc.server.extensions.mqtt.plugin;

import com.hashmapinc.server.extensions.api.plugins.msg.ResponsePluginToRuleMsg;
import com.hashmapinc.server.extensions.api.plugins.msg.RuleToPluginMsg;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import com.hashmapinc.server.common.data.id.RuleId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.msg.core.BasicStatusCodeResponse;
import com.hashmapinc.server.extensions.api.plugins.PluginContext;
import com.hashmapinc.server.extensions.api.plugins.handlers.RuleMsgHandler;
import com.hashmapinc.server.extensions.api.rules.RuleException;
import com.hashmapinc.server.extensions.mqtt.action.MqttActionMsg;
import com.hashmapinc.server.extensions.mqtt.action.MqttActionPayload;

import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
@Slf4j
public class MqttMsgHandler implements RuleMsgHandler {

    private final MqttAsyncClient mqttClient;

    @Override
    public void process(PluginContext ctx, TenantId tenantId, RuleId ruleId, RuleToPluginMsg<?> msg) throws RuleException {
        if (!(msg instanceof MqttActionMsg)) {
            throw new RuleException("Unsupported message type " + msg.getClass().getName() + "!");
        }
        MqttActionPayload payload = ((MqttActionMsg) msg).getPayload();
        MqttMessage mqttMsg = new MqttMessage(payload.getMsgBody().getBytes(StandardCharsets.UTF_8));
        try {
            mqttClient.publish(payload.getTopic(), mqttMsg, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    log.debug("Message [{}] was successfully delivered to topic [{}]!", msg.toString(), payload.getTopic());
                    if (payload.isSync()) {
                        ctx.reply(new ResponsePluginToRuleMsg(msg.getUid(), tenantId, ruleId,
                                BasicStatusCodeResponse.onSuccess(payload.getMsgType(), payload.getRequestId()), msg.getDeliveryId()));
                    }
                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable e) {
                    log.warn("Failed to deliver message [{}] to topic [{}]!", msg.toString(), payload.getTopic());
                    if (payload.isSync()) {
                        ctx.reply(new ResponsePluginToRuleMsg(msg.getUid(), tenantId, ruleId,
                                BasicStatusCodeResponse.onError(payload.getMsgType(), payload.getRequestId(), new Exception(e)), msg.getDeliveryId()));
                    }
                }
            });
        } catch (MqttException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
