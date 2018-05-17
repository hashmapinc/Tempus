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
package com.hashmapinc.server.actors.plugin;

import akka.actor.ActorContext;
import akka.actor.ActorRef;
import akka.japi.JavaPartialFunction;
import com.hashmapinc.server.extensions.api.plugins.msg.TimeoutMsg;
import com.hashmapinc.server.actors.ActorSystemContext;
import com.hashmapinc.server.actors.service.ComponentActor;
import com.hashmapinc.server.actors.service.ContextBasedCreator;
import com.hashmapinc.server.actors.stats.StatsPersistTick;
import com.hashmapinc.server.common.data.id.PluginId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.msg.cluster.ClusterEventMsg;
import com.hashmapinc.server.common.msg.plugin.ComponentLifecycleMsg;
import com.hashmapinc.server.extensions.api.plugins.msg.ToPluginRpcResponseDeviceMsg;
import com.hashmapinc.server.extensions.api.plugins.rest.PluginRestMsg;
import com.hashmapinc.server.extensions.api.plugins.rpc.PluginRpcMsg;
import com.hashmapinc.server.extensions.api.plugins.ws.msg.PluginWebsocketMsg;
import com.hashmapinc.server.extensions.api.rules.RuleException;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

public class PluginActor extends ComponentActor<PluginId, PluginActorMessageProcessor> {

    private PluginActor(ActorSystemContext systemContext, TenantId tenantId, PluginId pluginId) {
        super(systemContext, tenantId, pluginId);
        setProcessor(new PluginActorMessageProcessor(tenantId, pluginId, systemContext,
                logger, context().parent(), context().self()));
    }

    @Override
    public void onReceive(Object msg) throws Exception {
        if (msg instanceof PluginWebsocketMsg) {
            onWebsocketMsg((PluginWebsocketMsg<?>) msg);
        } else if (msg instanceof PluginRestMsg) {
            onRestMsg((PluginRestMsg) msg);
        } else if (msg instanceof PluginCallbackMessage) {
            onPluginCallback((PluginCallbackMessage) msg);
        } else if (msg instanceof RuleToPluginMsgWrapper) {
            onRuleToPluginMsg((RuleToPluginMsgWrapper) msg);
        } else if (msg instanceof PluginRpcMsg) {
            onRpcMsg((PluginRpcMsg) msg);
        } else if (msg instanceof ClusterEventMsg) {
            onClusterEventMsg((ClusterEventMsg) msg);
        } else if (msg instanceof ComponentLifecycleMsg) {
            onComponentLifecycleMsg((ComponentLifecycleMsg) msg);
        } else if (msg instanceof ToPluginRpcResponseDeviceMsg) {
            onRpcResponse((ToPluginRpcResponseDeviceMsg) msg);
        } else if (msg instanceof PluginTerminationMsg) {
            logger.info("[{}][{}] Going to terminate plugin actor.", tenantId, id);
            context().parent().tell(msg, ActorRef.noSender());
            context().stop(self());
        } else if (msg instanceof TimeoutMsg) {
            onTimeoutMsg(context(), (TimeoutMsg) msg);
        } else if (msg instanceof StatsPersistTick) {
            onStatsPersistTick(id);
        } else {
            logger.debug("[{}][{}] Unknown msg type.", tenantId, id, msg.getClass().getName());
        }
    }

    private void onPluginCallback(PluginCallbackMessage msg) {
        try {
            processor.onPluginCallbackMsg(msg);
        } catch (Exception e) {
            logAndPersist("onPluginCallbackMsg", e);
        }
    }

    private void onTimeoutMsg(ActorContext context, TimeoutMsg msg) {
        processor.onTimeoutMsg(context, msg);
    }

    private void onRpcResponse(ToPluginRpcResponseDeviceMsg msg) {
        processor.onDeviceRpcMsg(msg.getResponse());
    }

    private void onRuleToPluginMsg(RuleToPluginMsgWrapper msg) throws RuleException {
        logger.debug("[{}] Going to process rule msg: {}", id, msg.getMsg());
        try {
            processor.onRuleToPluginMsg(msg);
            increaseMessagesProcessedCount();
        } catch (Exception e) {
            logAndPersist("onRuleMsg", e);
        }
    }

    private void onWebsocketMsg(PluginWebsocketMsg<?> msg) {
        logger.debug("[{}] Going to process web socket msg: {}", id, msg);
        try {
            processor.onWebsocketMsg(msg);
            increaseMessagesProcessedCount();
        } catch (Exception e) {
            logAndPersist("onWebsocketMsg", e);
        }
    }

    private void onRestMsg(PluginRestMsg msg) {
        logger.debug("[{}] Going to process rest msg: {}", id, msg);
        try {
            processor.onRestMsg(msg);
            increaseMessagesProcessedCount();
        } catch (Exception e) {
            logAndPersist("onRestMsg", e);
        }
    }

    private void onRpcMsg(PluginRpcMsg msg) {
        try {
            logger.debug("[{}] Going to process rpc msg: {}", id, msg);
            processor.onRpcMsg(msg);
        } catch (Exception e) {
            logAndPersist("onRpcMsg", e);
        }
    }

    public static class ActorCreator extends ContextBasedCreator<PluginActor> {
        private static final long serialVersionUID = 1L;

        private final TenantId tenantId;
        private final PluginId pluginId;

        public ActorCreator(ActorSystemContext context, TenantId tenantId, PluginId pluginId) {
            super(context);
            this.tenantId = tenantId;
            this.pluginId = pluginId;
        }

        @Override
        public PluginActor create() throws Exception {
            return new PluginActor(context, tenantId, pluginId);
        }
    }

    @Override
    protected long getErrorPersistFrequency() {
        return systemContext.getPluginErrorPersistFrequency();
    }
}
