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
package com.hashmapinc.server.actors.shared.plugin;

import akka.actor.ActorContext;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.hashmapinc.server.actors.ActorSystemContext;
import com.hashmapinc.server.actors.plugin.PluginActor;
import com.hashmapinc.server.actors.service.ContextAwareActor;
import com.hashmapinc.server.common.data.id.PluginId;
import com.hashmapinc.server.common.data.id.TenantId;
import lombok.extern.slf4j.Slf4j;
import com.hashmapinc.server.common.data.page.PageDataIterable;
import com.hashmapinc.server.common.data.page.PageDataIterable.FetchFunction;
import com.hashmapinc.server.common.data.plugin.PluginMetaData;
import com.hashmapinc.server.dao.plugin.PluginService;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public abstract class PluginManager {

    protected final ActorSystemContext systemContext;
    protected final PluginService pluginService;
    protected final Map<PluginId, ActorRef> pluginActors;

    public PluginManager(ActorSystemContext systemContext) {
        this.systemContext = systemContext;
        this.pluginService = systemContext.getPluginService();
        this.pluginActors = new HashMap<>();
    }

    public void init(ActorContext context) {
        PageDataIterable<PluginMetaData> pluginIterator = new PageDataIterable<>(getFetchPluginsFunction(),
                ContextAwareActor.ENTITY_PACK_LIMIT);
        for (PluginMetaData plugin : pluginIterator) {
            log.debug("[{}] Creating plugin actor", plugin.getId());
            getOrCreatePluginActor(context, plugin.getId());
            log.debug("Plugin actor created.");
        }
    }

    abstract FetchFunction<PluginMetaData> getFetchPluginsFunction();

    abstract TenantId getTenantId();

    abstract String getDispatcherName();

    public ActorRef getOrCreatePluginActor(ActorContext context, PluginId pluginId) {
        return pluginActors.computeIfAbsent(pluginId, pId ->
                context.actorOf(Props.create(PluginActor.class, new PluginActor.ActorCreator(systemContext, getTenantId(), pId))
                        .withDispatcher(getDispatcherName()), pId.toString()));
    }

    public void broadcast(Object msg) {
        pluginActors.values().forEach(actorRef -> actorRef.tell(msg, ActorRef.noSender()));
    }

    public void remove(PluginId id) {
        pluginActors.remove(id);
    }
}
