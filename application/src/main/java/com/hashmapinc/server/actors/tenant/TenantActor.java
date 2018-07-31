/**
 * Copyright © 2016-2018 The Thingsboard Authors
 * Modifications © 2017-2018 Hashmap, Inc
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
package com.hashmapinc.server.actors.tenant;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.hashmapinc.server.actors.ActorSystemContext;
import com.hashmapinc.server.actors.device.DeviceActor;
import com.hashmapinc.server.actors.plugin.PluginTerminationMsg;
import com.hashmapinc.server.actors.rule.ComplexRuleActorChain;
import com.hashmapinc.server.actors.rule.RuleActorChain;
import com.hashmapinc.server.actors.service.ContextAwareActor;
import com.hashmapinc.server.actors.service.ContextBasedCreator;
import com.hashmapinc.server.actors.service.DefaultActorService;
import com.hashmapinc.server.actors.shared.computation.TenantComputationManager;
import com.hashmapinc.server.actors.shared.plugin.PluginManager;
import com.hashmapinc.server.actors.shared.plugin.TenantPluginManager;
import com.hashmapinc.server.actors.shared.rule.RuleManager;
import com.hashmapinc.server.actors.shared.rule.TenantRuleManager;
import com.hashmapinc.server.common.data.computation.ComputationType;
import com.hashmapinc.server.common.data.id.*;
import com.hashmapinc.server.common.msg.cluster.ClusterEventMsg;
import com.hashmapinc.server.common.msg.device.ToDeviceActorMsg;
import com.hashmapinc.server.common.msg.plugin.ComponentLifecycleMsg;
import com.hashmapinc.server.extensions.api.device.ToDeviceActorNotificationMsg;
import com.hashmapinc.server.extensions.api.plugins.msg.ToPluginActorMsg;
import com.hashmapinc.server.extensions.api.rules.ToRuleActorMsg;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TenantActor extends ContextAwareActor {

    private final LoggingAdapter logger = Logging.getLogger(getContext().system(), this);

    private final TenantId tenantId;
    private final RuleManager ruleManager;
    private final PluginManager pluginManager;
    private final TenantComputationManager computationManager;
    private final Map<DeviceId, ActorRef> deviceActors;

    private TenantActor(ActorSystemContext systemContext, TenantId tenantId) {
        super(systemContext);
        this.tenantId = tenantId;
        this.ruleManager = new TenantRuleManager(systemContext, tenantId);
        this.pluginManager = new TenantPluginManager(systemContext, tenantId);
        this.computationManager = new TenantComputationManager(systemContext, tenantId);
        this.deviceActors = new HashMap<>();
    }

    @Override
    public void preStart() {
        logger.info("[{}] Starting tenant actor.", tenantId);
        try {
            ruleManager.init(this.context());
            pluginManager.init(this.context());
            computationManager.init(this.context());
            logger.info("[{}] Tenant actor started.", tenantId);
        } catch (Exception e) {
            logger.error(e, "[{}] Unknown failure", tenantId);
        }
    }

    @Override
    public void onReceive(Object msg) throws Exception {
        logger.debug("[{}] Received message: {}", tenantId, msg);
        if (msg instanceof RuleChainDeviceMsg) {
            logger.debug(" msg type RuleChainDeviceMsg -->");
            process((RuleChainDeviceMsg) msg);
        } else if (msg instanceof ToDeviceActorMsg) {
            logger.debug(" msg type ToDeviceActorMsg -->");
            onToDeviceActorMsg((ToDeviceActorMsg) msg);
        } else if (msg instanceof ToPluginActorMsg) {
            logger.debug(" msg type ToPluginActorMsg -->");
            onToPluginMsg((ToPluginActorMsg) msg);
        } else if (msg instanceof ToRuleActorMsg) {
            logger.debug(" msg type ToRuleActorMsg -->");
            onToRuleMsg((ToRuleActorMsg) msg);
        } else if (msg instanceof ToDeviceActorNotificationMsg) {
            logger.debug(" msg type ToDeviceActorNotificationMsg -->");
            onToDeviceActorMsg((ToDeviceActorNotificationMsg) msg);
        } else if (msg instanceof ClusterEventMsg) {
            logger.debug(" msg type ClusterEventMsg -->");
            broadcast(msg);
        } else if (msg instanceof ComponentLifecycleMsg) {
            logger.debug(" msg type ComponentLifecycleMsg -->");
            onComponentLifecycleMsg((ComponentLifecycleMsg) msg);
        } else if (msg instanceof PluginTerminationMsg) {
            logger.debug(" msg type PluginTerminationMsg -->");
            onPluginTerminated((PluginTerminationMsg) msg);
        } else {
            logger.warning("[{}] Unknown message: {}!", tenantId, msg);
        }
    }

    private void broadcast(Object msg) {
        pluginManager.broadcast(msg);
        deviceActors.values().forEach(actorRef -> actorRef.tell(msg, ActorRef.noSender()));
    }

    private void onToDeviceActorMsg(ToDeviceActorMsg msg) {
        getOrCreateDeviceActor(msg.getDeviceId()).tell(msg, ActorRef.noSender());
    }

    private void onToDeviceActorMsg(ToDeviceActorNotificationMsg msg) {
        getOrCreateDeviceActor(msg.getDeviceId()).tell(msg, ActorRef.noSender());
    }

    private void onToRuleMsg(ToRuleActorMsg msg) {
        ActorRef target = ruleManager.getOrCreateRuleActor(this.context(), msg.getRuleId());
        target.tell(msg, ActorRef.noSender());
    }

    private void onToPluginMsg(ToPluginActorMsg msg) {
        if (msg.getPluginTenantId().equals(tenantId)) {
            ActorRef pluginActor = pluginManager.getOrCreatePluginActor(this.context(), msg.getPluginId());
            pluginActor.tell(msg, ActorRef.noSender());
        } else {
            context().parent().tell(msg, ActorRef.noSender());
        }
    }

    private void onComponentLifecycleMsg(ComponentLifecycleMsg msg) {
        if (msg.getPluginId().isPresent()) {
            msg.getPluginId().ifPresent(pluginId -> onComponentLifecycleMsgForPlugin(msg, pluginId));
        } else if (msg.getRuleId().isPresent()) {
            msg.getRuleId().ifPresent(ruleId -> onComponentLifecycleMsgForRule(msg, ruleId));
        } else if(msg.getComputationId().isPresent()){
            msg.getComputationId().ifPresent(computationId -> onComponentLifecycleMsgForComputation(msg, computationId));
        }else {
            logger.debug("[{}] Invalid component lifecycle msg.", tenantId);
        }
    }

    private void onComponentLifecycleMsgForComputation(ComponentLifecycleMsg msg, ComputationId computationId) {
        ActorRef computationActor = computationManager.getOrCreateComputationActor(this.context(), computationId, ComputationType.SPARK);
        computationActor.tell(msg, ActorRef.noSender());
    }

    private void onComponentLifecycleMsgForRule(ComponentLifecycleMsg msg, RuleId ruleId) {
        ActorRef target;
        Optional<ActorRef> ref = ruleManager.update(this.context(), ruleId, msg.getEvent());
        if (ref.isPresent()) {
            target = ref.get();
        } else {
            logger.debug("Failed to find actor for rule: [{}]", ruleId);
            return;
        }
        target.tell(msg, ActorRef.noSender());
    }

    private void onComponentLifecycleMsgForPlugin(ComponentLifecycleMsg msg, PluginId pluginId) {
        ActorRef pluginActor = pluginManager.getOrCreatePluginActor(this.context(), pluginId);
        pluginActor.tell(msg, ActorRef.noSender());
    }

    private void onPluginTerminated(PluginTerminationMsg msg) {
        pluginManager.remove(msg.getId());
    }

    private void process(RuleChainDeviceMsg msg) {
        ToDeviceActorMsg toDeviceActorMsg = msg.getToDeviceActorMsg();
        ActorRef deviceActor = getOrCreateDeviceActor(toDeviceActorMsg.getDeviceId());
        RuleActorChain tenantChain = ruleManager.getRuleChain(this.context());
        RuleActorChain chain = new ComplexRuleActorChain(msg.getRuleChain(), tenantChain);
        deviceActor.tell(new RuleChainDeviceMsg(toDeviceActorMsg, chain), context().self());
    }

    private ActorRef getOrCreateDeviceActor(DeviceId deviceId) {
        return deviceActors.computeIfAbsent(deviceId, k -> context().actorOf(Props.create(new DeviceActor.ActorCreator(systemContext, tenantId, deviceId))
                .withDispatcher(DefaultActorService.CORE_DISPATCHER_NAME), deviceId.toString()));
    }

    public static class ActorCreator extends ContextBasedCreator<TenantActor> {
        private static final long serialVersionUID = 1L;

        private final TenantId tenantId;

        public ActorCreator(ActorSystemContext context, TenantId tenantId) {
            super(context);
            this.tenantId = tenantId;
        }

        @Override
        public TenantActor create() throws Exception {
            return new TenantActor(context, tenantId);
        }
    }

}
