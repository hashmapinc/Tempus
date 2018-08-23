/**
 * Copyright © 2016-2018 The Thingsboard Authors
 * Modifications © 2017-2018 Hashmap, Inc
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
package com.hashmapinc.server.extensions.rabbitmq.plugin;

import com.hashmapinc.server.extensions.api.plugins.msg.ResponsePluginToRuleMsg;
import com.hashmapinc.server.extensions.api.plugins.msg.RuleToPluginMsg;
import com.hashmapinc.server.extensions.rabbitmq.action.RabbitMqActionPayload;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import lombok.RequiredArgsConstructor;
import com.hashmapinc.server.common.data.id.RuleId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.msg.core.BasicStatusCodeResponse;
import com.hashmapinc.server.extensions.api.plugins.PluginContext;
import com.hashmapinc.server.extensions.api.plugins.handlers.RuleMsgHandler;
import com.hashmapinc.server.extensions.api.rules.RuleException;
import com.hashmapinc.server.extensions.rabbitmq.action.RabbitMqActionMsg;

import java.io.IOException;
import java.nio.charset.Charset;


@RequiredArgsConstructor
public class RabbitMqMsgHandler implements RuleMsgHandler {
    private static final Charset UTF8 = Charset.forName("UTF-8");

    private final Channel channel;

    @Override
    public void process(PluginContext ctx, TenantId tenantId, RuleId ruleId, RuleToPluginMsg<?> msg) throws RuleException {
        if (!(msg instanceof RabbitMqActionMsg)) {
            throw new RuleException("Unsupported message type " + msg.getClass().getName() + "!");
        }
        RabbitMqActionPayload payload = ((RabbitMqActionMsg) msg).getPayload();
        AMQP.BasicProperties properties = convert(payload.getMessageProperties());
        try {
            channel.basicPublish(
                    payload.getExchange() != null ? payload.getExchange() : "",
                    payload.getQueueName(),
                    properties,
                    payload.getPayload().getBytes(UTF8));
            if (payload.isSync()) {
                ctx.reply(new ResponsePluginToRuleMsg(msg.getUid(), tenantId, ruleId,
                        BasicStatusCodeResponse.onSuccess(payload.getMsgType(), payload.getRequestId())));
            }
        } catch (IOException e) {
            throw new RuleException(e.getMessage(), e);
        }
    }

    private static AMQP.BasicProperties convert(String name) throws RuleException {
        switch (name) {
            case "BASIC":
                return MessageProperties.BASIC;
            case "TEXT_PLAIN":
                return MessageProperties.TEXT_PLAIN;
            case "MINIMAL_BASIC":
                return MessageProperties.MINIMAL_BASIC;
            case "MINIMAL_PERSISTENT_BASIC":
                return MessageProperties.MINIMAL_PERSISTENT_BASIC;
            case "PERSISTENT_BASIC":
                return MessageProperties.PERSISTENT_BASIC;
            case "PERSISTENT_TEXT_PLAIN":
                return MessageProperties.PERSISTENT_TEXT_PLAIN;
            default:
                throw new RuleException("Message Properties: '" + name + "' is undefined!");
        }
    }

}
