/**
 * Copyright © 2016-2018 The Thingsboard Authors
 * Modifications © 2017-2018 Hashmap, Inc
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hashmapinc.server.extensions.eventhub.plugin;

import com.hashmapinc.server.common.msg.exception.TempusRuntimeException;
import com.hashmapinc.server.extensions.api.component.Plugin;
import com.hashmapinc.server.extensions.api.plugins.AbstractPlugin;
import com.hashmapinc.server.extensions.api.plugins.PluginContext;
import com.hashmapinc.server.extensions.api.plugins.handlers.RuleMsgHandler;
import com.hashmapinc.server.extensions.eventhub.action.EventHubActionMsg;
import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.EventHubClient;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;

@Plugin(name = "Event Hub Plugin", actions = {EventHubActionMsg.class},
        descriptor = "EventHubPluginDescriptor.json", configuration = EventHubPluginConfiguration.class)
@Slf4j
public class EventHubPlugin extends AbstractPlugin<EventHubPluginConfiguration> {

    private EventHubMsgHandler handler;
    private EventHubClient eventHubClient;
    private ConnectionStringBuilder connectionStringBuilder;

    @Override
    public void init(EventHubPluginConfiguration configuration) {
        this.connectionStringBuilder = new ConnectionStringBuilder()
                .setNamespaceName(configuration.getEventHubNamespace())
                .setEventHubName(configuration.getEventHubName())
                .setSasKeyName(configuration.getEventHubPolicyName())
                .setSasKey(configuration.getEventHubSasKey());
        init();
    }

    @Override
    protected RuleMsgHandler getRuleMsgHandler() {
        return handler;
    }

    @Override
    public void resume(PluginContext ctx) {
        init();
    }

    @Override
    public void suspend(PluginContext ctx) {
        destroy();
    }

    @Override
    public void stop(PluginContext ctx) {
        destroy();
    }

    private void init() {
        try {
            this.eventHubClient = EventHubClient.createSync(connectionStringBuilder.toString(), Executors.newCachedThreadPool());
            this.handler = new EventHubMsgHandler(this.eventHubClient);
        } catch (Exception e) {
            log.error("Failed to create event hub client producer", e);
            throw new TempusRuntimeException(e);
        }
    }

    private void destroy() {
        try {
            this.handler = null;
            this.eventHubClient.closeSync();
        } catch (Exception e) {
            log.error("Failed to close event hub client during destroy()", e);
            throw new TempusRuntimeException(e);
        }
    }
}
