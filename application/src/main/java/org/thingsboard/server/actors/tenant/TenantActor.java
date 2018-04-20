/**
 * Copyright © 2016-2018 Hashmap, Inc
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
package org.thingsboard.server.actors.tenant;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.thingsboard.server.actors.ActorSystemContext;
import org.thingsboard.server.actors.application.ComputationDeleteMessage;
import org.thingsboard.server.actors.application.ComputationJobDeleteMessage;
import org.thingsboard.server.actors.application.DashboardDeleteMessage;
import org.thingsboard.server.actors.application.RuleDeleteMessage;
import org.thingsboard.server.actors.device.DeviceActor;
import org.thingsboard.server.actors.plugin.PluginTerminationMsg;
import org.thingsboard.server.actors.rule.ComplexRuleActorChain;
import org.thingsboard.server.actors.rule.RuleActorChain;
import org.thingsboard.server.actors.service.ContextAwareActor;
import org.thingsboard.server.actors.service.ContextBasedCreator;
import org.thingsboard.server.actors.service.DefaultActorService;
import org.thingsboard.server.actors.shared.application.ApplicationManager;
import org.thingsboard.server.actors.shared.application.TenantApplicationManager;
import org.thingsboard.server.actors.shared.computation.TenantComputationManager;
import org.thingsboard.server.actors.shared.plugin.PluginManager;
import org.thingsboard.server.actors.shared.plugin.TenantPluginManager;
import org.thingsboard.server.actors.shared.rule.RuleManager;
import org.thingsboard.server.actors.shared.rule.TenantRuleManager;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.id.PluginId;
import org.thingsboard.server.common.data.id.RuleId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.plugin.ComponentLifecycleEvent;
import org.thingsboard.server.common.msg.cluster.ClusterEventMsg;
import org.thingsboard.server.common.msg.device.ToDeviceActorMsg;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import org.thingsboard.server.common.msg.plugin.ComponentLifecycleMsg;
import org.thingsboard.server.extensions.api.device.ToDeviceActorNotificationMsg;
import org.thingsboard.server.extensions.api.plugins.msg.ToPluginActorMsg;
import org.thingsboard.server.extensions.api.rules.ToRuleActorMsg;

public class TenantActor extends ContextAwareActor {

    private final LoggingAdapter logger = Logging.getLogger(getContext().system(), this);

    private final TenantId tenantId;
    private final RuleManager ruleManager;
    private final PluginManager pluginManager;
    private final ApplicationManager applicationManager;
    private final TenantComputationManager computationManager;
    private final Map<DeviceId, ActorRef> deviceActors;

    private TenantActor(ActorSystemContext systemContext, TenantId tenantId) {
        super(systemContext);
        this.tenantId = tenantId;
        this.ruleManager = new TenantRuleManager(systemContext, tenantId);
        this.pluginManager = new TenantPluginManager(systemContext, tenantId);
        this.applicationManager = new TenantApplicationManager(systemContext, tenantId);
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
        Optional<PluginId> pluginId = msg.getPluginId();
        Optional<RuleId> ruleId = msg.getRuleId();
        if (pluginId.isPresent()) {
            ActorRef pluginActor = pluginManager.getOrCreatePluginActor(this.context(), pluginId.get());
            pluginActor.tell(msg, ActorRef.noSender());
        } else if (msg.getRuleId().isPresent()) {
            if(msg.getEvent().equals(ComponentLifecycleEvent.DELETED)) {
                ActorRef applicationActor = applicationManager.getOrCreateApplicationActor(this.context());
                RuleDeleteMessage ruleDeleteMessage = new RuleDeleteMessage(msg.getRuleId().get());
                applicationActor.tell(ruleDeleteMessage, ActorRef.noSender());
            }
            ActorRef target;
            Optional<ActorRef> ref = ruleManager.update(this.context(), ruleId.get(), msg.getEvent());
            if (ref.isPresent()) {
                target = ref.get();
            } else {
                logger.debug("Failed to find actor for rule: [{}]", ruleId);
                return;
            }
            target.tell(msg, ActorRef.noSender());
        } else if (msg.getDashboardId().isPresent()){
            if(msg.getEvent().equals(ComponentLifecycleEvent.DELETED)) {
                ActorRef applicationActor = applicationManager.getOrCreateApplicationActor(this.context());
                DashboardDeleteMessage dashboardDeleteMessage = new DashboardDeleteMessage(msg.getDashboardId().get());
                applicationActor.tell(dashboardDeleteMessage, ActorRef.noSender());
            }
        } else if(msg.getComputationId().isPresent()){
            if(msg.getEvent().equals(ComponentLifecycleEvent.DELETED)) {
                ActorRef applicationActor = applicationManager.getOrCreateApplicationActor(this.context());
                if(msg.getComputationJobId().isPresent()) {
                    ComputationJobDeleteMessage computationJobDeleteMessage = new ComputationJobDeleteMessage(msg.getComputationJobId().get());
                    applicationActor.tell(computationJobDeleteMessage, ActorRef.noSender());
                } /*else if(msg.getComputationId().isPresent()){
                    ComputationDeleteMessage computationDeleteMessage = new ComputationDeleteMessage(msg.getComputationId().get());
                    applicationActor.tell(computationDeleteMessage, ActorRef.noSender());
                }*/ else {
                    logger.debug("Invalid message: Computation delete message without computationId or ComputationJobId");
                }
            }
            ActorRef computationActor = computationManager.getOrCreateComputationActor(this.context(), msg.getComputationId().get());
            computationActor.tell(msg, ActorRef.noSender());
        }else {
            logger.debug("[{}] Invalid component lifecycle msg.", tenantId);
        }
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
