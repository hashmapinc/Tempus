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
package com.hashmapinc.server.extensions.kinesis.plugin;



import com.amazonaws.services.kinesis.model.PutRecordRequest;
import com.amazonaws.services.kinesis.model.PutRecordResult;
import com.amazonaws.services.kinesisfirehose.AmazonKinesisFirehoseAsync;
import com.hashmapinc.server.common.data.id.RuleId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.extensions.api.plugins.PluginContext;
import com.hashmapinc.server.extensions.api.plugins.msg.ResponsePluginToRuleMsg;
import com.hashmapinc.server.extensions.api.plugins.msg.RuleToPluginMsg;

import com.hashmapinc.server.extensions.kinesis.action.firehose.KinesisFirehoseActionMsg;
import com.hashmapinc.server.extensions.kinesis.action.firehose.KinesisFirehoseActionPayload;
import com.hashmapinc.server.extensions.kinesis.action.streams.KinesisStreamActionMsg;
import com.hashmapinc.server.extensions.kinesis.action.streams.KinesisStreamActionPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.hashmapinc.server.common.msg.core.BasicStatusCodeResponse;
import com.hashmapinc.server.extensions.api.plugins.handlers.RuleMsgHandler;
import com.hashmapinc.server.extensions.api.rules.RuleException;
import com.amazonaws.handlers.AsyncHandler;
import com.amazonaws.services.kinesis.AmazonKinesisAsync;



/**
 * @author Mitesh Rathore
 */
@RequiredArgsConstructor
@Slf4j
public class KinesisMessageHandler implements RuleMsgHandler {

    private final AmazonKinesisAsync streamKinesis;
    private final AmazonKinesisFirehoseAsync firehoseKinesis;



    @Override
    public void process(PluginContext ctx, TenantId tenantId, RuleId ruleId, RuleToPluginMsg<?> msg) throws RuleException {
        if (msg instanceof KinesisStreamActionMsg || msg instanceof KinesisFirehoseActionMsg ) {
            if (msg instanceof KinesisStreamActionMsg) {
                sendMessageToKinesisStream(ctx, tenantId, ruleId, msg);
                return;
            }
            if (msg instanceof KinesisFirehoseActionMsg) {
                sendMessageToKinesisFirehose(ctx, tenantId, ruleId, msg);
                return;
            }
        }else{
            throw new RuleException("Unsupported message type " + msg.getClass().getName() + "!");
        }
    }

    private void sendMessageToKinesisStream(PluginContext ctx, TenantId tenantId, RuleId ruleId, RuleToPluginMsg<?> msg) {
        KinesisStreamActionPayload payload = ((KinesisStreamActionMsg) msg).getPayload();
        log.debug("Processing Kinesis stream payload: {}", payload);
            PutRecordRequest putRecordRequest = KinesisStreamPutRecordFactory.INSTANCE.create((KinesisStreamActionMsg) msg);
            streamKinesis.putRecordAsync(putRecordRequest, new AsyncHandler<PutRecordRequest, PutRecordResult>() {

                @Override
                public void onError(Exception e) {
                    if (payload.isSync()) {
                        ctx.reply(new ResponsePluginToRuleMsg(msg.getUid(), tenantId, ruleId,
                                BasicStatusCodeResponse.onError(payload.getMsgType(), payload.getRequestId(), e)));
                    }
                    log.error("Processing Kinesis data stream failed: {}", e);
                }

                @Override
                public void onSuccess(PutRecordRequest request, PutRecordResult putRecordResult) {
                    if (payload.isSync()) {
                        ctx.reply(new ResponsePluginToRuleMsg(msg.getUid(), tenantId, ruleId,
                                BasicStatusCodeResponse.onSuccess(payload.getMsgType(), payload.getRequestId())));
                    }
                }
            });
    }

    private void sendMessageToKinesisFirehose(PluginContext ctx, TenantId tenantId, RuleId ruleId, RuleToPluginMsg<?> msg) {
        KinesisFirehoseActionPayload payload = ((KinesisFirehoseActionMsg) msg).getPayload();
        log.debug("Processing Kinesis firehose payload: {}", payload);
        com.amazonaws.services.kinesisfirehose.model.PutRecordRequest putRecordRequest = KinesisFirehosePutRecordFactory.INSTANCE.create((KinesisFirehoseActionMsg) msg);
        firehoseKinesis.putRecordAsync(putRecordRequest, new AsyncHandler<com.amazonaws.services.kinesisfirehose.model.PutRecordRequest, com.amazonaws.services.kinesisfirehose.model.PutRecordResult>() {

            @Override
            public void onError(Exception e) {
                if (payload.isSync()) {
                    ctx.reply(new ResponsePluginToRuleMsg(msg.getUid(), tenantId, ruleId,
                            BasicStatusCodeResponse.onError(payload.getMsgType(), payload.getRequestId(), e)));
                }
                log.error("Processing Kinesis firehose stream failed: {}", e.getMessage());
            }

            @Override
            public void onSuccess(com.amazonaws.services.kinesisfirehose.model.PutRecordRequest request, com.amazonaws.services.kinesisfirehose.model.PutRecordResult putRecordResult) {
                if (payload.isSync()) {
                    ctx.reply(new ResponsePluginToRuleMsg(msg.getUid(), tenantId, ruleId,
                            BasicStatusCodeResponse.onSuccess(payload.getMsgType(), payload.getRequestId())));
                }
            }
        });
    }


    }


