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
package com.hashmapinc.server.extensions.sns.plugin;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishRequest;
import com.hashmapinc.server.common.data.id.RuleId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.msg.core.BasicStatusCodeResponse;
import com.hashmapinc.server.extensions.api.plugins.PluginContext;
import com.hashmapinc.server.extensions.api.plugins.handlers.RuleMsgHandler;
import com.hashmapinc.server.extensions.api.plugins.msg.ResponsePluginToRuleMsg;
import com.hashmapinc.server.extensions.api.plugins.msg.RuleToPluginMsg;
import com.hashmapinc.server.extensions.api.rules.RuleException;
import com.hashmapinc.server.extensions.sns.action.SnsTopicActionPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.hashmapinc.server.extensions.sns.action.SnsTopicActionMsg;

/**
 * Created by Valerii Sosliuk on 11/6/2017.
 */
@RequiredArgsConstructor
@Slf4j
public class SnsMessageHandler implements RuleMsgHandler {

    private final AmazonSNS sns;

    @Override
    public void process(PluginContext ctx, TenantId tenantId, RuleId ruleId, RuleToPluginMsg<?> msg) throws RuleException {
        if (msg instanceof SnsTopicActionMsg) {
            SnsTopicActionPayload payload = ((SnsTopicActionMsg) msg).getPayload();
            PublishRequest publishRequest = new PublishRequest()
                    .withTopicArn(payload.getTopicArn())
                    .withMessage(payload.getMsgBody());
            sns.publish(publishRequest);
            if (payload.isSync()) {
                ctx.reply(new ResponsePluginToRuleMsg(msg.getUid(), tenantId, ruleId,
                        BasicStatusCodeResponse.onSuccess(payload.getMsgType(), payload.getRequestId()), msg.getDeliveryId()));
            }
           return;
        }
        throw new RuleException("Unsupported message type " + msg.getClass().getName() + "!");

    }

}
