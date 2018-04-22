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
package com.hashmapinc.server.extensions.core.plugin.rpc;

import com.hashmapinc.server.extensions.api.plugins.msg.TimeoutMsg;
import lombok.extern.slf4j.Slf4j;
import com.hashmapinc.server.extensions.api.component.Plugin;
import com.hashmapinc.server.extensions.api.plugins.AbstractPlugin;
import com.hashmapinc.server.extensions.api.plugins.PluginContext;
import com.hashmapinc.server.extensions.api.plugins.handlers.RestMsgHandler;
import com.hashmapinc.server.extensions.api.plugins.handlers.RuleMsgHandler;
import com.hashmapinc.server.extensions.api.plugins.msg.FromDeviceRpcResponse;
import com.hashmapinc.server.extensions.core.action.rpc.ServerSideRpcCallAction;
import com.hashmapinc.server.extensions.core.plugin.rpc.handlers.RpcRestMsgHandler;
import com.hashmapinc.server.extensions.core.plugin.rpc.handlers.RpcRuleMsgHandler;

/**
 * @author Andrew Shvayka
 */
@Plugin(name = "RPC Plugin", actions = {ServerSideRpcCallAction.class}, descriptor = "RpcPluginDescriptor.json", configuration = RpcPluginConfiguration.class)
@Slf4j
public class RpcPlugin extends AbstractPlugin<RpcPluginConfiguration> {

    private final RpcManager rpcManager;
    private final RpcRestMsgHandler restMsgHandler;

    public RpcPlugin() {
        this.rpcManager = new RpcManager();
        this.restMsgHandler = new RpcRestMsgHandler(rpcManager);
        this.rpcManager.setRestHandler(restMsgHandler);
    }

    @Override
    public void process(PluginContext ctx, FromDeviceRpcResponse msg) {
        rpcManager.process(ctx, msg);
    }

    @Override
    public void process(PluginContext ctx, TimeoutMsg<?> msg) {
        rpcManager.process(ctx, msg);
    }

    @Override
    protected RestMsgHandler getRestMsgHandler() {
        return restMsgHandler;
    }

    @Override
    public void init(RpcPluginConfiguration configuration) {
        restMsgHandler.setDefaultTimeout(configuration.getDefaultTimeout());
    }

    @Override
    protected RuleMsgHandler getRuleMsgHandler() {
        return new RpcRuleMsgHandler();
    }

    @Override
    public void resume(PluginContext ctx) {
        //Do nothing
    }

    @Override
    public void suspend(PluginContext ctx) {
        //Do nothing
    }

    @Override
    public void stop(PluginContext ctx) {
        //Do nothing
    }
}
