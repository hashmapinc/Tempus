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
package com.hashmapinc.server.extensions.core.action.rpc;

import com.hashmapinc.server.common.msg.core.ToServerRpcRequestMsg;
import com.hashmapinc.server.common.msg.device.ToDeviceActorMsg;
import com.hashmapinc.server.common.msg.session.FromDeviceMsg;
import com.hashmapinc.server.common.msg.session.MsgType;
import com.hashmapinc.server.common.msg.session.ToDeviceMsg;
import com.hashmapinc.server.extensions.api.component.Action;
import com.hashmapinc.server.extensions.api.component.EmptyComponentConfiguration;
import com.hashmapinc.server.extensions.api.plugins.PluginAction;
import com.hashmapinc.server.extensions.api.plugins.msg.PluginToRuleMsg;
import com.hashmapinc.server.extensions.api.plugins.msg.RpcRequestRuleToPluginMsg;
import com.hashmapinc.server.extensions.api.plugins.msg.RpcResponsePluginToRuleMsg;
import com.hashmapinc.server.extensions.api.plugins.msg.RuleToPluginMsg;
import com.hashmapinc.server.extensions.api.rules.RuleContext;
import com.hashmapinc.server.extensions.api.rules.RuleProcessingMetaData;
import com.hashmapinc.server.extensions.api.rules.SimpleRuleLifecycleComponent;

import java.util.Optional;

@Action(name = "RPC Plugin Action")
public class RpcPluginAction extends SimpleRuleLifecycleComponent implements PluginAction<EmptyComponentConfiguration> {

    public void init(EmptyComponentConfiguration configuration) {
        //Do nothing
    }

    @Override
    public Optional<RuleToPluginMsg> convert(RuleContext ctx, ToDeviceActorMsg toDeviceActorMsg, RuleProcessingMetaData deviceMsgMd) {
        FromDeviceMsg msg = toDeviceActorMsg.getPayload();
        if (msg.getMsgType() == MsgType.TO_SERVER_RPC_REQUEST) {
            ToServerRpcRequestMsg payload = (ToServerRpcRequestMsg) msg;
            return Optional.of(new RpcRequestRuleToPluginMsg(toDeviceActorMsg.getTenantId(), toDeviceActorMsg.getCustomerId(),
                    toDeviceActorMsg.getDeviceId(), payload));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<ToDeviceMsg> convert(PluginToRuleMsg<?> response) {
        if (response instanceof RpcResponsePluginToRuleMsg) {
            return Optional.of(((RpcResponsePluginToRuleMsg) response).getPayload());
        }
        return Optional.empty();
    }

    @Override
    public boolean isOneWayAction() {
        return false;
    }

}
