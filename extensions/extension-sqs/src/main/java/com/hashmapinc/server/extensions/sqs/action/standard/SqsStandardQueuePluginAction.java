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
package com.hashmapinc.server.extensions.sqs.action.standard;

import com.hashmapinc.server.extensions.api.plugins.msg.RuleToPluginMsg;
import com.hashmapinc.server.extensions.api.rules.RuleContext;
import com.hashmapinc.server.extensions.core.action.template.AbstractTemplatePluginAction;
import com.hashmapinc.server.common.msg.device.ToDeviceActorMsg;
import com.hashmapinc.server.common.msg.session.FromDeviceRequestMsg;
import com.hashmapinc.server.extensions.api.component.Action;

import java.util.Optional;

/**
 * Created by Valerii Sosliuk on 11/5/2017.
 */
@Action(name = "SQS Standard Queue Action", descriptor = "SqsStandardQueueActionDescriptor.json", configuration = SqsStandardQueuePluginActionConfiguration.class)
public class SqsStandardQueuePluginAction extends AbstractTemplatePluginAction<SqsStandardQueuePluginActionConfiguration> {

    @Override
    protected Optional<RuleToPluginMsg> buildRuleToPluginMsg(RuleContext ctx, ToDeviceActorMsg msg, FromDeviceRequestMsg payload) {
        SqsStandardQueueActionPayload.SqsStandardQueueActionPayloadBuilder builder = SqsStandardQueueActionPayload.builder();
        builder.msgType(payload.getMsgType());
        builder.requestId(payload.getRequestId());
        builder.queue(configuration.getQueue());
        builder.delaySeconds(configuration.getDelaySeconds());
        builder.msgBody(getMsgBody(ctx, msg));
        return Optional.of(new SqsStandardQueueActionMsg(msg.getTenantId(),
                msg.getCustomerId(),
                msg.getDeviceId(),
                builder.build()));
    }
}
