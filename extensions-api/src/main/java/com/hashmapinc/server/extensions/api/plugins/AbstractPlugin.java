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
package com.hashmapinc.server.extensions.api.plugins;

import com.hashmapinc.server.common.data.id.RuleId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.extensions.api.plugins.handlers.*;
import com.hashmapinc.server.extensions.api.plugins.msg.FromDeviceRpcResponse;
import com.hashmapinc.server.extensions.api.plugins.msg.RuleToPluginMsg;
import com.hashmapinc.server.extensions.api.plugins.msg.TimeoutMsg;
import com.hashmapinc.server.extensions.api.plugins.rest.PluginRestMsg;
import com.hashmapinc.server.extensions.api.plugins.rpc.RpcMsg;
import com.hashmapinc.server.extensions.api.plugins.ws.msg.PluginWebsocketMsg;
import com.hashmapinc.server.common.msg.cluster.ServerAddress;
import com.hashmapinc.server.extensions.api.plugins.handlers.*;
import com.hashmapinc.server.extensions.api.rules.RuleException;


public abstract class AbstractPlugin<T> implements Plugin<T> {

    @Override
    public void process(PluginContext ctx, PluginWebsocketMsg<?> wsMsg) {
        getWebsocketMsgHandler().process(ctx, wsMsg);
    }

    @Override
    public void process(PluginContext ctx, PluginRestMsg msg) {
        getRestMsgHandler().process(ctx, msg);
    }

    @Override
    public void process(PluginContext ctx, TenantId tenantId, RuleId ruleId, RuleToPluginMsg<?> msg) throws RuleException {
        getRuleMsgHandler().process(ctx, tenantId, ruleId, msg);
    }

    @Override
    public void process(PluginContext ctx, RpcMsg msg) {
        getRpcMsgHandler().process(ctx, msg);
    }

    @Override
    public void process(PluginContext ctx, FromDeviceRpcResponse msg) {
        throw new IllegalStateException("Device RPC messages is not handled in current plugin!");
    }

    @Override
    public void process(PluginContext ctx, TimeoutMsg<?> msg) {
        throw new IllegalStateException("Timeouts is not handled in current plugin!");
    }

    @Override
    public void onServerAdded(PluginContext ctx, ServerAddress server) {
    }

    @Override
    public void onServerRemoved(PluginContext ctx, ServerAddress server) {
    }

    protected RuleMsgHandler getRuleMsgHandler() {
        return new DefaultRuleMsgHandler();
    }

    protected RestMsgHandler getRestMsgHandler() {
        return new DefaultRestMsgHandler();
    }

    protected WebsocketMsgHandler getWebsocketMsgHandler() {
        return new DefaultWebsocketMsgHandler();
    }

    protected RpcMsgHandler getRpcMsgHandler() {
        return new DefaultRpcMsgHandler();
    }
}
