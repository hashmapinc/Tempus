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
package com.hashmapinc.server.extensions.sqs.plugin;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.hashmapinc.server.common.data.id.RuleId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.extensions.api.plugins.PluginContext;
import com.hashmapinc.server.extensions.api.plugins.msg.ResponsePluginToRuleMsg;
import com.hashmapinc.server.extensions.api.plugins.msg.RuleToPluginMsg;
import com.hashmapinc.server.extensions.sqs.action.fifo.SqsFifoQueueActionMsg;
import com.hashmapinc.server.extensions.sqs.action.fifo.SqsFifoQueueActionPayload;
import com.hashmapinc.server.extensions.sqs.action.standard.SqsStandardQueueActionMsg;
import com.hashmapinc.server.extensions.sqs.action.standard.SqsStandardQueueActionPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.hashmapinc.server.common.msg.core.BasicStatusCodeResponse;
import com.hashmapinc.server.extensions.api.plugins.handlers.RuleMsgHandler;
import com.hashmapinc.server.extensions.api.rules.RuleException;

/**
 * Created by Valerii Sosliuk on 11/15/2017.
 */
@RequiredArgsConstructor
@Slf4j
public class SqsMessageHandler implements RuleMsgHandler {

    private final AmazonSQS sqs;

    @Override
    public void process(PluginContext ctx, TenantId tenantId, RuleId ruleId, RuleToPluginMsg<?> msg) throws RuleException {
        if (msg instanceof SqsStandardQueueActionMsg) {
            sendMessageToStandardQueue(ctx, tenantId, ruleId, msg);
            return;
        }
        if (msg instanceof SqsFifoQueueActionMsg) {
            sendMessageToFifoQueue(ctx, tenantId, ruleId, msg);
            return;
        }
        throw new RuleException("Unsupported message type " + msg.getClass().getName() + "!");
    }

    private void sendMessageToStandardQueue(PluginContext ctx, TenantId tenantId, RuleId ruleId, RuleToPluginMsg<?> msg) {
        SqsStandardQueueActionPayload payload = ((SqsStandardQueueActionMsg) msg).getPayload();
        SendMessageRequest sendMsgRequest = new SendMessageRequest()
                .withDelaySeconds(payload.getDelaySeconds())
                .withQueueUrl(payload.getQueue())
                .withMessageBody(payload.getMsgBody());
        sqs.sendMessage(sendMsgRequest);
        if (payload.isSync()) {
            ctx.reply(new ResponsePluginToRuleMsg(msg.getUid(), tenantId, ruleId,
                    BasicStatusCodeResponse.onSuccess(payload.getMsgType(), payload.getRequestId()), msg.getDeliveryId()));
        }
    }

    private void sendMessageToFifoQueue(PluginContext ctx, TenantId tenantId, RuleId ruleId, RuleToPluginMsg<?> msg) {
        SqsFifoQueueActionPayload payload = ((SqsFifoQueueActionMsg) msg).getPayload();
        SendMessageRequest sendMsgRequest =  new SendMessageRequest()
                .withQueueUrl(payload.getQueue())
                .withMessageBody(payload.getMsgBody())
                .withMessageGroupId(payload.getDeviceId());
        sqs.sendMessage(sendMsgRequest);
        if (payload.isSync()) {
            ctx.reply(new ResponsePluginToRuleMsg(msg.getUid(), tenantId, ruleId,
                    BasicStatusCodeResponse.onSuccess(payload.getMsgType(), payload.getRequestId()), msg.getDeliveryId()));
        }
    }
}
