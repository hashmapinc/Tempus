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

import com.hashmapinc.server.common.data.id.EntityId;
import com.hashmapinc.server.common.data.kv.*;
import com.hashmapinc.server.extensions.api.plugins.PluginContext;
import com.hashmapinc.server.extensions.core.plugin.telemetry.handlers.TelemetryRpcMsgHandler;
import com.hashmapinc.server.extensions.core.plugin.telemetry.sub.*;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import com.hashmapinc.server.common.data.DataConstants;
import com.hashmapinc.server.common.data.kv.*;
import com.hashmapinc.server.common.msg.cluster.ServerAddress;
import com.hashmapinc.server.extensions.api.plugins.PluginCallback;
import com.hashmapinc.server.extensions.core.plugin.telemetry.handlers.TelemetryWebsocketMsgHandler;
import com.hashmapinc.server.extensions.core.plugin.telemetry.sub.*;

import java.util.*;
import java.util.function.Function;


@Slf4j
public class SubscriptionManager {

    private final Map<EntityId, Set<Subscription>> subscriptionsByEntityId = new HashMap<>();

    private final Map<String, Map<Integer, Subscription>> subscriptionsByWsSessionId = new HashMap<>();

    @Setter
    private TelemetryWebsocketMsgHandler websocketHandler;
    @Setter
    private TelemetryRpcMsgHandler rpcHandler;

    public void addLocalWsSubscription(PluginContext ctx, String sessionId, EntityId entityId, SubscriptionState sub) {
        Optional<ServerAddress> server = ctx.resolve(entityId);

        Subscription<Long> subscription;
        if (server.isPresent()) {
            ServerAddress address = server.get();
            log.trace("[{}] Forwarding subscription [{}] for device [{}] to [{}]", sessionId, sub.getSubscriptionId(), entityId, address);
            subscription = new Subscription(sub, true, address);
            rpcHandler.onNewSubscription(ctx, address, sessionId, subscription);
        } else {
            log.trace("[{}] Registering local subscription [{}] for device [{}]", sessionId, sub.getSubscriptionId(), entityId);
            subscription = new Subscription(sub, true);
            /*if(sub.getType() == SubscriptionType.DEPTHSERIES) {
                depthSubscriptionState = (DepthSubscriptionState) sub;
                subscription = new DepthSubscription(sub,true,depthSubscriptionState);
            }
            else {
                subscription = new Subscription(sub, true);
            }*/
        }
        registerSubscription(sessionId, entityId, subscription);
    }

    public void addLocalWsDepthSubscription(PluginContext ctx, String sessionId, EntityId entityId, SubscriptionState sub) {
        Optional<ServerAddress> server = ctx.resolve(entityId);
        Subscription<Double> subscription;
        if (server.isPresent()) {
            ServerAddress address = server.get();
            log.trace("[{}] Forwarding subscription [{}] for device [{}] to [{}]", sessionId, sub.getSubscriptionId(), entityId, address);
            subscription = new Subscription(sub, true, address);
            //rpcHandler.onNewSubscription(ctx, address, sessionId, subscription);
        } else {
            log.trace("[{}] Registering local subscription [{}] for device [{}]", sessionId, sub.getSubscriptionId(), entityId);
            //subscription = new Subscription(sub, true,(DepthSubscriptionState) sub);
            subscription = new Subscription(sub, true);
        }
        registerSubscription(sessionId, entityId, subscription);
    }

    public void addRemoteWsSubscription(PluginContext ctx, ServerAddress address, String sessionId, Subscription<Long> subscription) {
        EntityId entityId = subscription.getEntityId();
        log.trace("[{}] Registering remote subscription [{}] for device [{}] to [{}]", sessionId, subscription.getSubscriptionId(), entityId, address);
        registerSubscription(sessionId, entityId, subscription);
        if (subscription.getType() == SubscriptionType.ATTRIBUTES) {
            final Map<String, Long> keyStates = subscription.getKeyStates();
            ctx.loadAttributes(entityId, DataConstants.CLIENT_SCOPE, keyStates.keySet(), new PluginCallback<List<AttributeKvEntry>>() {
                @Override
                public void onSuccess(PluginContext ctx, List<AttributeKvEntry> values) {
                    List<TsKvEntry> missedUpdates = new ArrayList<>();
                    values.forEach(latestEntry -> {
                        if (latestEntry.getLastUpdateTs() > keyStates.get(latestEntry.getKey())) {
                            missedUpdates.add(new BasicTsKvEntry(latestEntry.getLastUpdateTs(), latestEntry));
                        }
                    });
                    if (!missedUpdates.isEmpty()) {
                        rpcHandler.onSubscriptionUpdate(ctx, address, sessionId, new SubscriptionUpdate(subscription.getSubscriptionId(), missedUpdates));
                    }
                }

                @Override
                public void onFailure(PluginContext ctx, Exception e) {
                    log.error("Failed to fetch missed updates.", e);
                }
            });
        } else if (subscription.getType() == SubscriptionType.TIMESERIES) {
            long curTs = System.currentTimeMillis();
            List<TsKvQuery> queries = new ArrayList<>();
            subscription.getKeyStates().entrySet().forEach(e -> {
                queries.add(new BaseTsKvQuery(e.getKey(), e.getValue() + 1L, curTs));
            });

            ctx.loadTimeseries(entityId, queries, new PluginCallback<List<TsKvEntry>>() {
                @Override
                public void onSuccess(PluginContext ctx, List<TsKvEntry> missedUpdates) {
                    if (!missedUpdates.isEmpty()) {
                        rpcHandler.onSubscriptionUpdate(ctx, address, sessionId, new SubscriptionUpdate(subscription.getSubscriptionId(), missedUpdates));
                    }
                }

                @Override
                public void onFailure(PluginContext ctx, Exception e) {
                    log.error("Failed to fetch missed updates.", e);
                }
            });
        }

    }

    private void registerSubscription(String sessionId, EntityId entityId, Subscription subscription) {
        Set<Subscription> deviceSubscriptions = subscriptionsByEntityId.get(subscription.getEntityId());
        if (deviceSubscriptions == null) {
            deviceSubscriptions = new HashSet<>();
            subscriptionsByEntityId.put(entityId, deviceSubscriptions);
        }
        deviceSubscriptions.add(subscription);
        Map<Integer, Subscription> sessionSubscriptions = subscriptionsByWsSessionId.get(sessionId);
        if (sessionSubscriptions == null) {
            sessionSubscriptions = new HashMap<>();
            subscriptionsByWsSessionId.put(sessionId, sessionSubscriptions);
        }
        log.debug("subscriptionsByWsSessionId " + subscriptionsByWsSessionId);
        sessionSubscriptions.put(subscription.getSubscriptionId(), subscription);
    }

    public void removeSubscription(PluginContext ctx, String sessionId, Integer subscriptionId) {
        log.debug("[{}][{}] Going to remove subscription.", sessionId, subscriptionId);
        Map<Integer, Subscription> sessionSubscriptions = subscriptionsByWsSessionId.get(sessionId);
        if (sessionSubscriptions != null) {
            Subscription subscription = sessionSubscriptions.remove(subscriptionId);
            if (subscription != null) {
                processSubscriptionRemoval(ctx, sessionId, sessionSubscriptions, subscription);
            } else {
                log.debug("[{}][{}] Subscription not found!", sessionId, subscriptionId);
            }
        } else {
            log.debug("[{}] No session subscriptions found!", sessionId);
        }
    }

    private void processSubscriptionRemoval(PluginContext ctx, String sessionId,
                                            Map<Integer, Subscription> sessionSubscriptions, Subscription subscription) {
        EntityId entityId = subscription.getEntityId();
        if (subscription.isLocal() && subscription.getServer() != null) {
            rpcHandler.onSubscriptionClose(ctx, subscription.getServer(), sessionId, subscription.getSubscriptionId());
        }
        if (sessionSubscriptions.isEmpty()) {
            log.debug("[{}] Removed last subscription for particular session.", sessionId);
            subscriptionsByWsSessionId.remove(sessionId);
        } else {
            log.debug("[{}] Removed session subscription.", sessionId);
        }
        Set<Subscription> deviceSubscriptions = subscriptionsByEntityId.get(entityId);
        if (deviceSubscriptions != null) {
            boolean result = deviceSubscriptions.remove(subscription);
            if (result) {
                if (deviceSubscriptions.size() == 0) {
                    log.debug("[{}] Removed last subscription for particular device.", sessionId);
                    subscriptionsByEntityId.remove(entityId);
                } else {
                    log.debug("[{}] Removed device subscription.", sessionId);
                }
            } else {
                log.debug("[{}] Subscription not found!", sessionId);
            }
        } else {
            log.debug("[{}] No device subscriptions found!", sessionId);
        }
    }

    public void onLocalSubscriptionUpdate(PluginContext ctx, EntityId entityId, SubscriptionType type, Function<Subscription<Long>, List<TsKvEntry>> f) {
        Set<Subscription> deviceSubscriptions = subscriptionsByEntityId.get(entityId);
        if (deviceSubscriptions != null) {
            deviceSubscriptions.stream().filter(s -> type == s.getType()).forEach(s -> {
                log.debug("\n Inside foreach " + s);
                String sessionId = s.getWsSessionId();
                List<TsKvEntry> subscriptionUpdate = f.apply(s);
                if (!subscriptionUpdate.isEmpty()) {
                    log.debug("subscriptionId TS " + s.getSubscriptionId());
                    SubscriptionUpdate update = new SubscriptionUpdate(s.getSubscriptionId(), subscriptionUpdate);
                    if (s.isLocal()) {
                        updateSubscriptionState(sessionId, s, update);
                        log.debug("on local update for ts "+update);
                        websocketHandler.sendWsMsg(ctx, sessionId, update);
                    } else {
                        rpcHandler.onSubscriptionUpdate(ctx, s.getServer(), sessionId, update);
                    }
                }
            });
        } else {
            log.debug("[{}] No device subscriptions to process!", entityId);
        }
    }

    public void onLocalSubscriptionUpdateForDepth(PluginContext ctx, EntityId entityId, SubscriptionType type, Function<Subscription<Double>, List<DsKvEntry>> f) {
        Set<Subscription> deviceSubscriptions = subscriptionsByEntityId.get(entityId);
        if (deviceSubscriptions != null) {
            log.debug("Subscriptions by entity id "+ deviceSubscriptions);
            deviceSubscriptions.stream().filter(s -> type == s.getType()).forEach(s -> {
                //DepthSubscription depthSubscription = (DepthSubscription)s;
                String sessionId = s.getWsSessionId();
                List<DsKvEntry> subscriptionUpdate = f.apply(s);
                if (!subscriptionUpdate.isEmpty()) {
                    DepthSubscriptionUpdate update = new DepthSubscriptionUpdate(s.getSubscriptionId(), subscriptionUpdate);
                    if (s.isLocal()) {
                        updateDepthSubscriptionState(sessionId, s, update);
                        websocketHandler.sendWsMsg(ctx, sessionId, update);
                    } else {
                        //rpcHandler.onSubscriptionUpdate(ctx, s.getServer(), sessionId, update);
                    }
                }
            });
        } else {
            log.debug("No device subscriptions to process! for " + entityId);
        }
    }

    public void onRemoteSubscriptionUpdate(PluginContext ctx, String sessionId, SubscriptionUpdate update) {
        log.trace("[{}] Processing remote subscription onUpdate [{}]", sessionId, update);
        Optional<Subscription> subOpt = getSubscription(sessionId, update.getSubscriptionId());
        if (subOpt.isPresent()) {
            updateSubscriptionState(sessionId, subOpt.get(), update);
            websocketHandler.sendWsMsg(ctx, sessionId, update);
        }
    }

    /*public void onAttributesUpdateFromServer(PluginContext ctx, EntityId entityId, String scope, List<AttributeKvEntry> attributes) {
        Optional<ServerAddress> serverAddress = ctx.resolve(entityId);
        if (!serverAddress.isPresent()) {
            onLocalSubscriptionUpdate(ctx, entityId, s -> SubscriptionType.ATTRIBUTES == s.getType() && (StringUtils.isEmpty(s.getScope()) || scope.equals(s.getScope())), s -> {
                List<TsKvEntry> subscriptionUpdate = new ArrayList<TsKvEntry>();
                for (AttributeKvEntry kv : attributes) {
                    if (s.isAllKeys() || s.getKeyStates().containsKey(kv.getKey())) {
                        subscriptionUpdate.add(new BasicTsKvEntry(kv.getLastUpdateTs(), kv));
                    }
                }
                return subscriptionUpdate;
            });
        } else {
            rpcHandler.onAttributesUpdate(ctx, serverAddress.get(), entityId, scope, attributes);
        }
    }*/

    public void onAttributesUpdateFromServer(PluginContext ctx, EntityId entityId, String scope, List<AttributeKvEntry> attributes) {
        Optional<ServerAddress> serverAddress = ctx.resolve(entityId);
        if (!serverAddress.isPresent()) {
            onLocalSubscriptionUpdate(ctx, entityId, SubscriptionType.ATTRIBUTES, s -> {
                List<TsKvEntry> subscriptionUpdate = new ArrayList<TsKvEntry>();
                for (AttributeKvEntry kv : attributes) {
                    if (s.isAllKeys() || s.getKeyStates().containsKey(kv.getKey())) {
                        subscriptionUpdate.add(new BasicTsKvEntry(kv.getLastUpdateTs(), kv));
                    }
                }
                return subscriptionUpdate;
            });
        } else {
            rpcHandler.onAttributesUpdate(ctx, serverAddress.get(), entityId, scope, attributes);
        }
    }

    public void onTimeseriesUpdateFromServer(PluginContext ctx, EntityId entityId, List<TsKvEntry> entries) {
        Optional<ServerAddress> serverAddress = ctx.resolve(entityId);
        if (!serverAddress.isPresent()) {
            onLocalSubscriptionUpdate(ctx, entityId, SubscriptionType.TIMESERIES, s -> {
                List<TsKvEntry> subscriptionUpdate = new ArrayList<TsKvEntry>();
                for (TsKvEntry kv : entries) {
                    if (s.isAllKeys() || s.getKeyStates().containsKey((kv.getKey()))) {
                        subscriptionUpdate.add(kv);
                    }
                }
                return subscriptionUpdate;
            });
        } else {
            rpcHandler.onTimeseriesUpdate(ctx, serverAddress.get(), entityId, entries);
        }
    }

    private void updateSubscriptionState(String sessionId, Subscription<Long> subState, SubscriptionUpdate update) {
        log.trace("[{}] updating subscription state {} using onUpdate {}", sessionId, subState, update);
        update.getLatestValues().entrySet().forEach(e -> subState.setKeyState(e.getKey(), e.getValue()));
    }

    private void updateDepthSubscriptionState(String sessionId, Subscription<Double> subState, DepthSubscriptionUpdate update) {
        log.trace("[{}] updating subscription state {} using onUpdate {}", sessionId, subState, update);
        //DepthSubscription depthSubState = (DepthSubscription) subState;
        update.getLatestValues().entrySet().forEach(e -> subState.setKeyState(e.getKey(), e.getValue()));
    }

    private Optional<Subscription> getSubscription(String sessionId, int subscriptionId) {
        Subscription state = null;
        Map<Integer, Subscription> subMap = subscriptionsByWsSessionId.get(sessionId);
        if (subMap != null) {
            state = subMap.get(subscriptionId);
        }
        return Optional.ofNullable(state);
    }

    public void cleanupLocalWsSessionSubscriptions(PluginContext ctx, String sessionId) {
        cleanupWsSessionSubscriptions(ctx, sessionId, true);
    }

    public void cleanupRemoteWsSessionSubscriptions(PluginContext ctx, String sessionId) {
        cleanupWsSessionSubscriptions(ctx, sessionId, false);
    }

    private void cleanupWsSessionSubscriptions(PluginContext ctx, String sessionId, boolean localSession) {
        log.debug("[{}] Removing all subscriptions for particular session.", sessionId);
        Map<Integer, Subscription> sessionSubscriptions = subscriptionsByWsSessionId.get(sessionId);
        if (sessionSubscriptions != null) {
            int sessionSubscriptionSize = sessionSubscriptions.size();

            for (Subscription subscription : sessionSubscriptions.values()) {
                EntityId entityId = subscription.getEntityId();
                Set<Subscription> deviceSubscriptions = subscriptionsByEntityId.get(entityId);
                deviceSubscriptions.remove(subscription);
                if (deviceSubscriptions.isEmpty()) {
                    subscriptionsByEntityId.remove(entityId);
                }
            }
            subscriptionsByWsSessionId.remove(sessionId);
            log.debug("[{}] Removed {} subscriptions for particular session.", sessionId, sessionSubscriptionSize);

            if (localSession) {
                notifyWsSubscriptionClosed(ctx, sessionId, sessionSubscriptions);
            }
        } else {
            log.debug("[{}] No subscriptions found!", sessionId);
        }
    }

    private void notifyWsSubscriptionClosed(PluginContext ctx, String sessionId, Map<Integer, Subscription> sessionSubscriptions) {
         Set<ServerAddress> affectedServers = new HashSet<>();
            for (Subscription subscription : sessionSubscriptions.values()) {
                if (subscription.getServer() != null) {
                    affectedServers.add(subscription.getServer());
                }
            }
            for (ServerAddress address : affectedServers) {
                log.debug("[{}] Going to onSubscriptionUpdate [{}] server about session close event", sessionId, address);
                rpcHandler.onSessionClose(ctx, address, sessionId);
         }
    }

    public void onClusterUpdate(PluginContext ctx) {
        log.trace("Processing cluster onUpdate msg!");
        Iterator<Map.Entry<EntityId, Set<Subscription>>> deviceIterator = subscriptionsByEntityId.entrySet().iterator();
        while (deviceIterator.hasNext()) {
            Map.Entry<EntityId, Set<Subscription>> e = deviceIterator.next();
            Set<Subscription> subscriptions = e.getValue();
            Optional<ServerAddress> newAddressOptional = ctx.resolve(e.getKey());
            if (newAddressOptional.isPresent()) {
                checkSubsciptionsNewAddress(ctx, newAddressOptional, subscriptions);
            } else {
                checkSubsciptionsPrevAddress(subscriptions);
            }
            if (subscriptions.size() == 0) {
                log.trace("[{}] No more subscriptions for this device on current server.", e.getKey());
                deviceIterator.remove();
            }
        }
    }

    private void checkSubsciptionsNewAddress(PluginContext ctx, Optional<ServerAddress> newAddressOptional, Set<Subscription> subscriptions) {
        if (newAddressOptional.isPresent()) {
            ServerAddress newAddress = newAddressOptional.get();
            Iterator<Subscription> subscriptionIterator = subscriptions.iterator();
            while (subscriptionIterator.hasNext()) {
                Subscription s = subscriptionIterator.next();
                if (s.isLocal()) {
                    if (!newAddress.equals(s.getServer())) {
                        log.trace("[{}] Local subscription is now handled on new server [{}]", s.getWsSessionId(), newAddress);
                        s.setServer(newAddress);
                        rpcHandler.onNewSubscription(ctx, newAddress, s.getWsSessionId(), s);
                    }
                } else {
                    log.trace("[{}] Remote subscription is now handled on new server address: [{}]", s.getWsSessionId(), newAddress);
                    subscriptionIterator.remove();
                    //TODO: onUpdate state of subscription by WsSessionId and other maps.
                }
            }
        }
    }

    private void checkSubsciptionsPrevAddress(Set<Subscription> subscriptions) {
        Iterator<Subscription> subscriptionIterator = subscriptions.iterator();
        while (subscriptionIterator.hasNext()) {
            Subscription s = subscriptionIterator.next();
            if (s.isLocal()) {
                if (s.getServer() != null) {
                    log.trace("[{}] Local subscription is no longer handled on remote server address [{}]", s.getWsSessionId(), s.getServer());
                    s.setServer(null);
                }
            } else {
                log.trace("[{}] Remote subscription is on up to date server address.", s.getWsSessionId());
            }
        }
    }

    public void clear() {
        subscriptionsByWsSessionId.clear();
        subscriptionsByEntityId.clear();
    }
}