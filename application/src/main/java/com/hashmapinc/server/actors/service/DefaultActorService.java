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
package com.hashmapinc.server.actors.service;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.pattern.Patterns;
import com.hashmapinc.server.actors.ActorSystemContext;
import com.hashmapinc.server.actors.app.AppActor;
import com.hashmapinc.server.actors.cluster.DeleteNodeEntryMsg;
import com.hashmapinc.server.actors.cluster.NodeMetricActor;
import com.hashmapinc.server.actors.cluster.RegisterNodeMsg;
import com.hashmapinc.server.actors.rpc.RpcSessionCreateRequestMsg;
import com.hashmapinc.server.actors.rpc.RpcSessionTellMsg;
import com.hashmapinc.server.actors.stats.StatsActor;
import com.hashmapinc.server.common.data.id.*;
import com.hashmapinc.server.common.data.plugin.ComponentLifecycleEvent;
import com.hashmapinc.server.common.msg.aware.SessionAwareMsg;
import com.hashmapinc.server.common.msg.cluster.ClusterEventMsg;
import com.hashmapinc.server.common.msg.cluster.ToAllNodesMsg;
import com.hashmapinc.server.extensions.api.plugins.msg.ToPluginActorMsg;
import com.hashmapinc.server.extensions.api.plugins.ws.msg.PluginWebsocketMsg;
import com.hashmapinc.server.service.cluster.discovery.DiscoveryService;
import com.hashmapinc.server.service.cluster.discovery.ServerInstance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.hashmapinc.server.actors.rpc.RpcBroadcastMsg;
import com.hashmapinc.server.actors.rpc.RpcManagerActor;
import com.hashmapinc.server.actors.session.SessionManagerActor;
import com.hashmapinc.server.common.msg.cluster.ServerAddress;
import com.hashmapinc.server.common.msg.computation.ComputationMsg;
import com.hashmapinc.server.common.msg.core.ToDeviceSessionActorMsg;
import com.hashmapinc.server.common.msg.device.ToDeviceActorMsg;
import com.hashmapinc.server.common.msg.plugin.ComponentLifecycleMsg;
import com.hashmapinc.server.extensions.api.device.DeviceCredentialsUpdateNotificationMsg;
import com.hashmapinc.server.extensions.api.device.DeviceNameOrTypeUpdateMsg;
import com.hashmapinc.server.extensions.api.device.ToDeviceActorNotificationMsg;
import com.hashmapinc.server.extensions.api.plugins.rest.PluginRestMsg;
import com.hashmapinc.server.service.cluster.rpc.ClusterRpcService;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Optional;

@Service
@Slf4j
public class DefaultActorService implements ActorService {

    private static final String ACTOR_SYSTEM_NAME = "Akka";

    public static final String APP_DISPATCHER_NAME = "app-dispatcher";
    public static final String CORE_DISPATCHER_NAME = "core-dispatcher";
    public static final String SYSTEM_RULE_DISPATCHER_NAME = "system-rule-dispatcher";
    public static final String SYSTEM_PLUGIN_DISPATCHER_NAME = "system-plugin-dispatcher";
    public static final String TENANT_RULE_DISPATCHER_NAME = "rule-dispatcher";
    public static final String TENANT_PLUGIN_DISPATCHER_NAME = "plugin-dispatcher";
    public static final String SESSION_DISPATCHER_NAME = "session-dispatcher";
    public static final String RPC_DISPATCHER_NAME = "rpc-dispatcher";

    @Autowired
    private ActorSystemContext actorContext;

    @Autowired
    private ClusterRpcService rpcService;

    @Autowired
    private DiscoveryService discoveryService;

    private ActorSystem system;

    private ActorRef appActor;

    private ActorRef sessionManagerActor;

    private ActorRef rpcManagerActor;

    private ActorRef nodeMetricActor;

    @PostConstruct
    public void initActorSystem() {
        log.info("Initializing Actor system. {}", actorContext.getRuleService());
        actorContext.setActorService(this);
        system = ActorSystem.create(ACTOR_SYSTEM_NAME, actorContext.getConfig());
        actorContext.setActorSystem(system);

        appActor = system.actorOf(Props.create(new AppActor.ActorCreator(actorContext)).withDispatcher(APP_DISPATCHER_NAME), "appActor");
        actorContext.setAppActor(appActor);

        String host = discoveryService.getCurrentServer().getHost();
        int port = discoveryService.getCurrentServer().getPort();
        nodeMetricActor = system.actorOf(Props.create(new NodeMetricActor.ActorCreator(actorContext, host, port)));
        nodeMetricActor.tell(new RegisterNodeMsg(), ActorRef.noSender());

        sessionManagerActor = system.actorOf(Props.create(new SessionManagerActor.ActorCreator(actorContext, nodeMetricActor)).withDispatcher(CORE_DISPATCHER_NAME),
                "sessionManagerActor");
        actorContext.setSessionManagerActor(sessionManagerActor);

        rpcManagerActor = system.actorOf(Props.create(new RpcManagerActor.ActorCreator(actorContext, nodeMetricActor)).withDispatcher(CORE_DISPATCHER_NAME),
                "rpcManagerActor");

        ActorRef statsActor = system.actorOf(Props.create(new StatsActor.ActorCreator(actorContext)).withDispatcher(CORE_DISPATCHER_NAME), "statsActor");
        actorContext.setStatsActor(statsActor);

        rpcService.init(this);

        discoveryService.addListener(this);
        log.info("Actor system initialized.");
    }

    @PreDestroy
    public void stopActorSystem() {
        Future<Terminated> status = system.terminate();
        try {
            Terminated terminated = Await.result(status, Duration.Inf());
            log.info("Actor system terminated: {}", terminated);
        } catch (Exception e) {
            log.error("Failed to terminate actor system.", e);
        }
    }

    @Override
    public void process(SessionAwareMsg msg) {
        log.debug("Processing session aware msg: {}", msg);
        sessionManagerActor.tell(msg, ActorRef.noSender());
    }

    @Override
    public void process(PluginWebsocketMsg<?> msg) {
        log.debug("Processing websocket msg: {}", msg);
        appActor.tell(msg, ActorRef.noSender());
    }

    @Override
    public void process(PluginRestMsg msg) {
        log.debug("Processing rest msg: {}", msg);
        appActor.tell(msg, ActorRef.noSender());
    }

    @Override
    public void onMsg(ToPluginActorMsg msg) {
        log.trace("Processing plugin rpc msg: {}", msg);
        appActor.tell(msg, ActorRef.noSender());
    }

    @Override
    public void onMsg(ToDeviceActorMsg msg) {
        log.trace("Processing device rpc msg: {}", msg);
        appActor.tell(msg, ActorRef.noSender());
    }

    @Override
    public void onMsg(ToDeviceActorNotificationMsg msg) {
        log.trace("Processing notification rpc msg: {}", msg);
        appActor.tell(msg, ActorRef.noSender());
    }

    @Override
    public void onMsg(ToDeviceSessionActorMsg msg) {
        log.trace("Processing session rpc msg: {}", msg);
        sessionManagerActor.tell(msg, ActorRef.noSender());
    }

    @Override
    public void onMsg(ToAllNodesMsg msg) {
        log.trace("Processing broadcast rpc msg: {}", msg);
        appActor.tell(msg, ActorRef.noSender());
    }

    @Override
    public void onMsg(RpcSessionCreateRequestMsg msg) {
        log.trace("Processing session create msg: {}", msg);
        rpcManagerActor.tell(msg, ActorRef.noSender());
    }

    @Override
    public void onMsg(RpcSessionTellMsg msg) {
        log.trace("Processing session rpc msg: {}", msg);
        rpcManagerActor.tell(msg, ActorRef.noSender());
    }

    @Override
    public void onMsg(RpcBroadcastMsg msg) {
        log.trace("Processing broadcast rpc msg: {}", msg);
        rpcManagerActor.tell(msg, ActorRef.noSender());
    }

    @Override
    public Future<Object> onMsg(ComputationMsg msg) {
        log.warn("Processing Computation message: {}", msg);
        return Patterns.ask(appActor, msg, 10 * 1000L);
    }

    @Override
    public void onServerAdded(ServerInstance server) {
        log.trace("Processing onServerAdded msg: {}", server);
        broadcast(new ClusterEventMsg(server.getServerAddress(), true));
    }

    @Override
    public void onServerUpdated(ServerInstance server) {
        //Do nothing
    }

    @Override
    public void onServerRemoved(ServerInstance server) {
        log.trace("Processing onServerRemoved msg: {}", server);
        broadcast(new ClusterEventMsg(server.getServerAddress(), false));
    }

    @Override
    public void onPluginStateChange(TenantId tenantId, PluginId pluginId, ComponentLifecycleEvent state) {
        log.trace("[{}] Processing onPluginStateChange event: {}", pluginId, state);
        broadcast(ComponentLifecycleMsg.forPlugin(tenantId, pluginId, state));
    }

    @Override
    public void onRuleStateChange(TenantId tenantId, RuleId ruleId, ComponentLifecycleEvent state) {
        log.trace("[{}] Processing onRuleStateChange event: {}", ruleId, state);
        broadcast(ComponentLifecycleMsg.forRule(tenantId, ruleId, state));
    }

    @Override
    public void onDashboardStateChange(TenantId tenantId, DashboardId dashboardId, ComponentLifecycleEvent event) {
        log.trace("[{}] Processing onDashboardStateChange event: {}", dashboardId, event);
        appActor.tell(ComponentLifecycleMsg.forDashboard(tenantId, dashboardId, event) , ActorRef.noSender());
    }

    @Override
    public void onComputationJobStateChange(TenantId tenantId, ComputationId computationId, ComputationJobId computationJobId, ComponentLifecycleEvent state) {
        log.trace("[{}] Processing onComputationJobStateChange event: {}", computationJobId, state);
        broadcast(ComponentLifecycleMsg.forComputationJob(tenantId, computationId, computationJobId, state));
    }

    @Override
    public void onCredentialsUpdate(TenantId tenantId, DeviceId deviceId) {
        DeviceCredentialsUpdateNotificationMsg msg = new DeviceCredentialsUpdateNotificationMsg(tenantId, deviceId);
        Optional<ServerAddress> address = actorContext.getRoutingService().resolveById(deviceId);
        if (address.isPresent()) {
            rpcService.tell(address.get(), msg);
        } else {
            onMsg(msg);
        }
    }

    @Override
    public void onDeviceNameOrTypeUpdate(TenantId tenantId, DeviceId deviceId, String deviceName, String deviceType) {
        log.trace("[{}] Processing onDeviceNameOrTypeUpdate event, deviceName: {}, deviceType: {}", deviceId, deviceName, deviceType);
        DeviceNameOrTypeUpdateMsg msg = new DeviceNameOrTypeUpdateMsg(tenantId, deviceId, deviceName, deviceType);
        Optional<ServerAddress> address = actorContext.getRoutingService().resolveById(deviceId);
        if (address.isPresent()) {
            rpcService.tell(address.get(), msg);
        } else {
            onMsg(msg);
        }
    }

    public void broadcast(ToAllNodesMsg msg) {
        rpcService.broadcast(msg);
        appActor.tell(msg, ActorRef.noSender());
    }

    private void broadcast(ClusterEventMsg msg) {
        this.appActor.tell(msg, ActorRef.noSender());
        this.sessionManagerActor.tell(msg, ActorRef.noSender());
        this.rpcManagerActor.tell(msg, ActorRef.noSender());
        if (!msg.isAdded()) {
            nodeMetricActor.tell(new DeleteNodeEntryMsg(msg.getServerAddress().getHost(), msg.getServerAddress().getPort()), ActorRef.noSender());
        }
    }
}
