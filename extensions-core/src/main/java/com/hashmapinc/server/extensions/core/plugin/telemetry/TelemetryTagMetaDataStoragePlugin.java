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
package com.hashmapinc.server.extensions.core.plugin.telemetry;

import com.hashmapinc.server.extensions.api.component.EmptyComponentConfiguration;
import com.hashmapinc.server.extensions.api.component.Plugin;
import com.hashmapinc.server.extensions.api.plugins.PluginContext;
import com.hashmapinc.server.extensions.api.plugins.handlers.RestMsgHandler;
import com.hashmapinc.server.extensions.api.plugins.handlers.RpcMsgHandler;
import com.hashmapinc.server.extensions.api.plugins.handlers.RuleMsgHandler;
import com.hashmapinc.server.extensions.api.plugins.handlers.WebsocketMsgHandler;
import com.hashmapinc.server.extensions.core.action.telemetry.TelemetryTagPluginAction;
import com.hashmapinc.server.extensions.core.plugin.telemetry.handlers.*;
import lombok.extern.slf4j.Slf4j;
import com.hashmapinc.server.common.msg.cluster.ServerAddress;
import com.hashmapinc.server.extensions.api.plugins.AbstractPlugin;
import com.hashmapinc.server.extensions.core.action.telemetry.TelemetryPluginAction;

@Plugin(name = "Telemetry Tag Plugin", actions = {TelemetryTagPluginAction.class})
@Slf4j
public class TelemetryTagMetaDataStoragePlugin extends AbstractPlugin<EmptyComponentConfiguration> {

    private final TelemetryTagRuleMsgHandler ruleMsgHandler;

    public TelemetryTagMetaDataStoragePlugin() {
        this.ruleMsgHandler = new TelemetryTagRuleMsgHandler();
    }

    @Override
    public void init(EmptyComponentConfiguration configuration) {
        //Do nothing
    }

    @Override
    protected RestMsgHandler getRestMsgHandler() {
        return null;
    }

    @Override
    protected RuleMsgHandler getRuleMsgHandler() {
        return ruleMsgHandler;
    }

    @Override
    protected WebsocketMsgHandler getWebsocketMsgHandler() {
        return null;
    }

    @Override
    protected RpcMsgHandler getRpcMsgHandler() {
        return null;
    }

    @Override
    public void onServerAdded(PluginContext ctx, ServerAddress server) {

    }

    @Override
    public void onServerRemoved(PluginContext ctx, ServerAddress server) {

    }


    @Override
    public void resume(PluginContext ctx) {
        log.info("Plugin activated!");
    }

    @Override
    public void suspend(PluginContext ctx) {
        log.info("Plugin suspended!");
    }

    @Override
    public void stop(PluginContext ctx) {
        log.info("Plugin stopped!");
    }
}
