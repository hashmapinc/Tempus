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

import com.amazonaws.handlers.AsyncHandler;
import com.amazonaws.services.kinesisfirehose.AmazonKinesisFirehoseAsync;
import com.amazonaws.services.kinesisfirehose.model.PutRecordRequest;
import com.amazonaws.services.kinesisfirehose.model.PutRecordResult;
import com.hashmapinc.server.common.data.id.RuleId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.msg.core.BasicStatusCodeResponse;
import com.hashmapinc.server.extensions.api.plugins.PluginContext;
import com.hashmapinc.server.extensions.api.plugins.handlers.RuleMsgHandler;
import com.hashmapinc.server.extensions.api.plugins.msg.ResponsePluginToRuleMsg;
import com.hashmapinc.server.extensions.api.plugins.msg.RuleToPluginMsg;
import com.hashmapinc.server.extensions.api.rules.RuleException;
import com.hashmapinc.server.extensions.kinesis.action.KinesisActionMsg;
import com.hashmapinc.server.extensions.kinesis.action.KinesisActionPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Mitesh Rathore
 */

@RequiredArgsConstructor
@Slf4j
public class KinesisFirehoseMessageHandler implements RuleMsgHandler {

    private final AmazonKinesisFirehoseAsync firehoseKinesis;

    @Override
    public void process(PluginContext ctx, TenantId tenantId, RuleId ruleId, RuleToPluginMsg<?> msg) throws RuleException {
        if (!(msg instanceof KinesisActionMsg)) {
            throw new RuleException("Unsupported message type " + msg.getClass().getName() + "!");
        }

        KinesisActionPayload payload = ((KinesisActionMsg) msg).getPayload();
        log.debug("Processing Kinesis payload: {}", payload);

        try {
            PutRecordRequest putRecordRequest = KinesisFirehosePutRecordFactory.INSTANCE.create((KinesisActionMsg) msg);
            firehoseKinesis.putRecordAsync(putRecordRequest, new AsyncHandler<PutRecordRequest, PutRecordResult>() {

                @Override
                public void onError(Exception e) {
                    if (payload.isSync()) {
                        ctx.reply(new ResponsePluginToRuleMsg(msg.getUid(), tenantId, ruleId,
                                BasicStatusCodeResponse.onError(payload.getMsgType(), payload.getRequestId(), e)));
                    }
                    log.error("Processing Kinesis Firehose stream failed: {}", e.getMessage());
                }

                @Override
                public void onSuccess(PutRecordRequest request, PutRecordResult putRecordResult) {
                    if (payload.isSync()) {
                        ctx.reply(new ResponsePluginToRuleMsg(msg.getUid(), tenantId, ruleId,
                                BasicStatusCodeResponse.onSuccess(payload.getMsgType(), payload.getRequestId())));
                    }
                }
            });
        } catch (Exception e) {
            throw new RuleException(e.getMessage(), e);
        }
    }

}
