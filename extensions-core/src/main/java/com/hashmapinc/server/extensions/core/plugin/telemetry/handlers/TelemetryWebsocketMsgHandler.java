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
package com.hashmapinc.server.extensions.core.plugin.telemetry.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hashmapinc.server.common.data.DataConstants;
import com.hashmapinc.server.common.data.id.EntityId;
import com.hashmapinc.server.common.data.id.EntityIdFactory;
import com.hashmapinc.server.common.data.kv.*;
import com.hashmapinc.server.extensions.api.exception.UnauthorizedException;
import com.hashmapinc.server.extensions.api.plugins.PluginCallback;
import com.hashmapinc.server.extensions.api.plugins.PluginContext;
import com.hashmapinc.server.extensions.api.plugins.handlers.DefaultWebsocketMsgHandler;
import com.hashmapinc.server.extensions.api.plugins.ws.PluginWebsocketSessionRef;
import com.hashmapinc.server.extensions.api.plugins.ws.WsSessionMetaData;
import com.hashmapinc.server.extensions.api.plugins.ws.msg.BinaryPluginWebSocketMsg;
import com.hashmapinc.server.extensions.api.plugins.ws.msg.PluginWebsocketMsg;
import com.hashmapinc.server.extensions.api.plugins.ws.msg.TextPluginWebSocketMsg;
import com.hashmapinc.server.extensions.core.plugin.telemetry.SubscriptionManager;
import com.hashmapinc.server.extensions.core.plugin.telemetry.cmd.*;
import com.hashmapinc.server.extensions.core.plugin.telemetry.sub.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import com.hashmapinc.server.common.data.kv.*;
import com.hashmapinc.server.extensions.core.plugin.telemetry.cmd.*;
import com.hashmapinc.server.extensions.core.plugin.telemetry.sub.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
public class TelemetryWebsocketMsgHandler extends DefaultWebsocketMsgHandler {

    private static final int UNKNOWN_SUBSCRIPTION_ID = 0;
    public static final int DEFAULT_LIMIT = 100;
    public static final Aggregation DEFAULT_AGGREGATION = Aggregation.NONE;
    public static final DepthAggregation DEFAULT_DEPTH_AGGREGATION = DepthAggregation.NONE;
    public static final String FAILED_TO_FETCH_DATA = "Failed to fetch data!";
    public static final String FAILED_TO_FETCH_ATTRIBUTES = "Failed to fetch attributes!";
    public static final String SESSION_META_DATA_NOT_FOUND = "Session meta-data not found!";

    private final SubscriptionManager subscriptionManager;

    public TelemetryWebsocketMsgHandler(SubscriptionManager subscriptionManager) {
        this.subscriptionManager = subscriptionManager;
    }

    @Override
    protected void handleWebSocketMsg(PluginContext ctx, PluginWebsocketSessionRef sessionRef, PluginWebsocketMsg<?> wsMsg) {
        try {
            TelemetryPluginCmdsWrapper cmdsWrapper = null;
            if (wsMsg instanceof TextPluginWebSocketMsg) {
                TextPluginWebSocketMsg textMsg = (TextPluginWebSocketMsg) wsMsg;
                log.debug("TextPluginWebSocketMsg txtMsg.payload " + textMsg.getPayload() + " and msg " + textMsg);
                cmdsWrapper = jsonMapper.readValue(textMsg.getPayload(), TelemetryPluginCmdsWrapper.class);
            } else if (wsMsg instanceof BinaryPluginWebSocketMsg) {
                throw new IllegalStateException("Not Implemented!");
                // TODO: add support of BSON here based on
                // https://github.com/michel-kraemer/bson4jackson
            }
            if (cmdsWrapper != null) {
                if (cmdsWrapper.getAttrSubCmds() != null) {
                    cmdsWrapper.getAttrSubCmds().forEach(cmd -> handleWsAttributesSubscriptionCmd(ctx, sessionRef, cmd));
                }
                if (cmdsWrapper.getTsSubCmds() != null) {
                    cmdsWrapper.getTsSubCmds().forEach(cmd -> handleWsTimeseriesSubscriptionCmd(ctx, sessionRef, cmd));
                }
                if (cmdsWrapper.getDsSubCmds() != null) {
                    cmdsWrapper.getDsSubCmds().forEach(cmd -> handleWsDepthSeriesSubscriptionCmd(ctx, sessionRef, cmd));
                }
                if (cmdsWrapper.getHistoryCmds() != null) {
                    cmdsWrapper.getHistoryCmds().forEach(cmd -> handleWsHistoryCmd(ctx, sessionRef, cmd));
                }
                if (cmdsWrapper.getDepthHistoryCmds() != null) {
                    cmdsWrapper.getDepthHistoryCmds().forEach(cmd -> handleWsDepthHistoryCmd(ctx, sessionRef, cmd));
                }
            }
        } catch (IOException e) {
            log.warn("Failed to decode subscription cmd: {}", e.getMessage(), e);
            SubscriptionUpdate update = new SubscriptionUpdate(UNKNOWN_SUBSCRIPTION_ID, SubscriptionErrorCode.INTERNAL_ERROR,
                    SESSION_META_DATA_NOT_FOUND);
            sendWsMsg(ctx, sessionRef, update);
        }
    }

    @Override
    protected void cleanupWebSocketSession(PluginContext ctx, String sessionId) {
        subscriptionManager.cleanupLocalWsSessionSubscriptions(ctx, sessionId);
    }

    /*private void handleWsAttributesSubscriptionCmd(PluginContext ctx, PluginWebsocketSessionRef sessionRef, AttributesSubscriptionCmd cmd) {
        String sessionId = sessionRef.getSessionId();
        log.debug("[{}] Processing: {}", sessionId, cmd);

        if (validateSessionMetadata(ctx, sessionRef, cmd, sessionId)) {
            if (cmd.isUnsubscribe()) {
                unsubscribe(ctx, cmd, sessionId);
            } else if (validateSubscriptionCmd(ctx, sessionRef, cmd)) {
                EntityId entityId = EntityIdFactory.getByTypeAndId(cmd.getEntityType(), cmd.getEntityId());
                log.debug("[{}] fetching latest attributes ({}) values for device: {}", sessionId, cmd.getKeys(), entityId);
                Optional<Set<String>> keysOptional = getKeys(cmd);
                if (keysOptional.isPresent()) {
                    List<String> keys = new ArrayList<>(keysOptional.get());
                    handleWsAttributesSubscriptionByKeys(ctx, sessionRef, cmd, sessionId, entityId, keys);
                } else {
                    handleWsAttributesSubscription(ctx, sessionRef, cmd, sessionId, entityId);
                }
            }
        }
    }*/

    private void handleWsAttributesSubscriptionCmd(PluginContext ctx, PluginWebsocketSessionRef sessionRef, AttributesSubscriptionCmd cmd) {
        String sessionId = sessionRef.getSessionId();
        log.debug("[{}] Processing: {}", sessionId, cmd);

        if (validateSessionMetadata(ctx, sessionRef, cmd, sessionId)) {
            if (cmd.isUnsubscribe()) {
                unsubscribe(ctx, cmd, sessionId);
            } else if (validateSubscriptionCmd(ctx, sessionRef, cmd)) {
                EntityId entityId = EntityIdFactory.getByTypeAndId(cmd.getEntityType(), cmd.getEntityId());
                log.debug("[{}] fetching latest attributes ({}) values for device: {}", sessionId, cmd.getKeys(), entityId);
                Optional<Set<String>> keysOptional = getKeys(cmd);
                SubscriptionState sub;
                if (keysOptional.isPresent()) {
                    List<String> keys = new ArrayList<>(keysOptional.get());

                    PluginCallback<List<AttributeKvEntry>> callback = new PluginCallback<List<AttributeKvEntry>>() {
                        @Override
                        public void onSuccess(PluginContext ctx, List<AttributeKvEntry> data) {
                            List<TsKvEntry> attributesData = data.stream().map(d -> new BasicTsKvEntry(d.getLastUpdateTs(), d)).collect(Collectors.toList());
                            sendWsMsg(ctx, sessionRef, new SubscriptionUpdate(cmd.getCmdId(), attributesData));

                            Map<String, Long> subState = new HashMap<>(keys.size());
                            keys.forEach(key -> subState.put(key, 0L));
                            attributesData.forEach(v -> subState.put(v.getKey(), v.getTs()));

                            SubscriptionState<Long> sub = new SubscriptionState(sessionId, cmd.getCmdId(), entityId, SubscriptionType.ATTRIBUTES, false, subState,cmd.getScope());
                            subscriptionManager.addLocalWsSubscription(ctx, sessionId, entityId, sub);
                        }

                        @Override
                        public void onFailure(PluginContext ctx, Exception e) {
                            log.error("Failed to fetch attributes!", e);
                            SubscriptionUpdate update;
                            if (UnauthorizedException.class.isInstance(e)) {
                                update = new SubscriptionUpdate(cmd.getCmdId(), SubscriptionErrorCode.UNAUTHORIZED,
                                        SubscriptionErrorCode.UNAUTHORIZED.getDefaultMsg());
                            } else {
                                update = new SubscriptionUpdate(cmd.getCmdId(), SubscriptionErrorCode.INTERNAL_ERROR,
                                        "Failed to fetch attributes!");
                            }
                            sendWsMsg(ctx, sessionRef, update);
                        }
                    };

                    if (StringUtils.isEmpty(cmd.getScope())) {
                        ctx.loadAttributes(entityId, Arrays.asList(DataConstants.allScopes()), keys, callback);
                    } else {
                        ctx.loadAttributes(entityId, cmd.getScope(), keys, callback);
                    }
                } else {
                    PluginCallback<List<AttributeKvEntry>> callback = new PluginCallback<List<AttributeKvEntry>>() {
                        @Override
                        public void onSuccess(PluginContext ctx, List<AttributeKvEntry> data) {
                            List<TsKvEntry> attributesData = data.stream().map(d -> new BasicTsKvEntry(d.getLastUpdateTs(), d)).collect(Collectors.toList());
                            sendWsMsg(ctx, sessionRef, new SubscriptionUpdate(cmd.getCmdId(), attributesData));

                            Map<String, Long> subState = new HashMap<>(attributesData.size());
                            attributesData.forEach(v -> subState.put(v.getKey(), v.getTs()));

                            SubscriptionState<Long> sub = new SubscriptionState(sessionId, cmd.getCmdId(), entityId, SubscriptionType.ATTRIBUTES, true, subState,cmd.getScope());
                            subscriptionManager.addLocalWsSubscription(ctx, sessionId, entityId, sub);
                        }

                        @Override
                        public void onFailure(PluginContext ctx, Exception e) {
                            log.error("Failed to fetch attributes!", e);
                            SubscriptionUpdate update = new SubscriptionUpdate(cmd.getCmdId(), SubscriptionErrorCode.INTERNAL_ERROR,
                                    "Failed to fetch attributes!");
                            sendWsMsg(ctx, sessionRef, update);
                        }
                    };

                    if (StringUtils.isEmpty(cmd.getScope())) {
                        ctx.loadAttributes(entityId, Arrays.asList(DataConstants.allScopes()), callback);
                    } else {
                        ctx.loadAttributes(entityId, cmd.getScope(), callback);
                    }
                }
            }
        }
    }

    private void handleWsAttributesSubscriptionByKeys(PluginContext ctx, PluginWebsocketSessionRef sessionRef,
                                                      AttributesSubscriptionCmd cmd, String sessionId, EntityId entityId,
                                                      List<String> keys) {
        PluginCallback<List<AttributeKvEntry>> callback = new PluginCallback<List<AttributeKvEntry>>() {
            @Override
            public void onSuccess(PluginContext ctx, List<AttributeKvEntry> data) {
                List<TsKvEntry> attributesData = data.stream().map(d -> new BasicTsKvEntry(d.getLastUpdateTs(), d)).collect(Collectors.toList());
                sendWsMsg(ctx, sessionRef, new SubscriptionUpdate(cmd.getCmdId(), attributesData));

                Map<String, Long> subState = new HashMap<>(keys.size());
                keys.forEach(key -> subState.put(key, 0L));
                attributesData.forEach(v -> subState.put(v.getKey(), v.getTs()));

                SubscriptionState sub = new SubscriptionState(sessionId, cmd.getCmdId(), entityId, SubscriptionType.ATTRIBUTES, false, subState, cmd.getScope());
                subscriptionManager.addLocalWsSubscription(ctx, sessionId, entityId, sub);
            }

            @Override
            public void onFailure(PluginContext ctx, Exception e) {
                log.error(FAILED_TO_FETCH_ATTRIBUTES, e);
                SubscriptionUpdate update;
                if (UnauthorizedException.class.isInstance(e)) {
                    update = new SubscriptionUpdate(cmd.getCmdId(), SubscriptionErrorCode.UNAUTHORIZED,
                            SubscriptionErrorCode.UNAUTHORIZED.getDefaultMsg());
                } else {
                    update = new SubscriptionUpdate(cmd.getCmdId(), SubscriptionErrorCode.INTERNAL_ERROR,
                            FAILED_TO_FETCH_ATTRIBUTES);
                }
                sendWsMsg(ctx, sessionRef, update);
            }
        };

        if (StringUtils.isEmpty(cmd.getScope())) {
            ctx.loadAttributes(entityId, Arrays.asList(DataConstants.allScopes()), keys, callback);
        } else {
            ctx.loadAttributes(entityId, cmd.getScope(), keys, callback);
        }
    }


   /* private void handleWsAttributesSubscriptionByKeys(PluginContext ctx, PluginWebsocketSessionRef sessionRef,
                                                      AttributesSubscriptionCmd cmd, String sessionId, EntityId entityId,
                                                      List<String> keys) {
        PluginCallback<List<AttributeKvEntry>> callback = new PluginCallback<List<AttributeKvEntry>>() {
            @Override
            public void onSuccess(PluginContext ctx, List<AttributeKvEntry> data) {
                List<TsKvEntry> attributesData = data.stream().map(d -> new BasicTsKvEntry(d.getLastUpdateTs(), d)).collect(Collectors.toList());
                sendWsMsg(ctx, sessionRef, new SubscriptionUpdate(cmd.getCmdId(), attributesData));

                Map<String, Long> subState = new HashMap<>(keys.size());
                keys.forEach(key -> subState.put(key, 0L));
                attributesData.forEach(v -> subState.put(v.getKey(), v.getTs()));

                    PluginCallback<List<AttributeKvEntry>> callback = new PluginCallback<List<AttributeKvEntry>>() {
                        @Override
                        public void onSuccess(PluginContext ctx, List<AttributeKvEntry> data) {
                            List<TsKvEntry> attributesData = data.stream().map(d -> new BasicTsKvEntry(d.getLastUpdateTs(), d)).collect(Collectors.toList());
                            sendWsMsg(ctx, sessionRef, new SubscriptionUpdate(cmd.getCmdId(), attributesData));

                            Map<String, Long> subState = new HashMap<>(keys.size());
                            keys.forEach(key -> subState.put(key, 0L));
                            attributesData.forEach(v -> subState.put(v.getKey(), v.getTs()));
                            SubscriptionState sub = new SubscriptionState(sessionId, cmd.getCmdId(), entityId, SubscriptionType.ATTRIBUTES, false, subState, cmd.getScope());
                            subscriptionManager.addLocalWsSubscription(ctx, sessionId, entityId, sub);
                        }

                        @Override
                        public void onFailure(PluginContext ctx, Exception e) {
                            log.error("Failed to fetch attributes!", e);
                            SubscriptionUpdate update;
                            if (UnauthorizedException.class.isInstance(e)) {
                                update = new SubscriptionUpdate(cmd.getCmdId(), SubscriptionErrorCode.UNAUTHORIZED,
                                        SubscriptionErrorCode.UNAUTHORIZED.getDefaultMsg());
                            } else {
                                update = new SubscriptionUpdate(cmd.getCmdId(), SubscriptionErrorCode.INTERNAL_ERROR,
                                        "Failed to fetch attributes!");
                            }
                            sendWsMsg(ctx, sessionRef, update);
                        }
                    };

                    if (StringUtils.isEmpty(cmd.getScope())) {
                        ctx.loadAttributes(entityId, Arrays.asList(DataConstants.allScopes()), keys, callback);
                    } else {
                        ctx.loadAttributes(entityId, cmd.getScope(), keys, callback);
                    }
                } else {
                    PluginCallback<List<AttributeKvEntry>> callback = new PluginCallback<List<AttributeKvEntry>>() {
                        @Override
                        public void onSuccess(PluginContext ctx, List<AttributeKvEntry> data) {
                            List<TsKvEntry> attributesData = data.stream().map(d -> new BasicTsKvEntry(d.getLastUpdateTs(), d)).collect(Collectors.toList());
                            sendWsMsg(ctx, sessionRef, new SubscriptionUpdate(cmd.getCmdId(), attributesData));

                            Map<String, Long> subState = new HashMap<>(attributesData.size());
                            attributesData.forEach(v -> subState.put(v.getKey(), v.getTs()));

                            SubscriptionState sub = new SubscriptionState(sessionId, cmd.getCmdId(), entityId, SubscriptionType.ATTRIBUTES, false, subState, cmd.getScope());
                            subscriptionManager.addLocalWsSubscription(ctx, sessionId, entityId, sub);
                        }

                        @Override
                        public void onFailure(PluginContext ctx, Exception e) {
                            log.error("Failed to fetch attributes!", e);
                            SubscriptionUpdate update = new SubscriptionUpdate(cmd.getCmdId(), SubscriptionErrorCode.INTERNAL_ERROR,
                                    "Failed to fetch attributes!");
                            sendWsMsg(ctx, sessionRef, update);
                        }
                    };

                    if (StringUtils.isEmpty(cmd.getScope())) {
                        ctx.loadAttributes(entityId, Arrays.asList(DataConstants.allScopes()), callback);
                    } else {
                        ctx.loadAttributes(entityId, cmd.getScope(), callback);
                    }
                SubscriptionState sub = new SubscriptionState(sessionId, cmd.getCmdId(), entityId, SubscriptionType.ATTRIBUTES, false, subState, cmd.getScope());
                subscriptionManager.addLocalWsSubscription(ctx, sessionId, entityId, sub);
            }

            @Override
            public void onFailure(PluginContext ctx, Exception e) {
                log.error(FAILED_TO_FETCH_ATTRIBUTES, e);
                SubscriptionUpdate update;
                if (UnauthorizedException.class.isInstance(e)) {
                    update = new SubscriptionUpdate(cmd.getCmdId(), SubscriptionErrorCode.UNAUTHORIZED,
                            SubscriptionErrorCode.UNAUTHORIZED.getDefaultMsg());
                } else {
                    update = new SubscriptionUpdate(cmd.getCmdId(), SubscriptionErrorCode.INTERNAL_ERROR,
                            FAILED_TO_FETCH_ATTRIBUTES);
                }
                sendWsMsg(ctx, sessionRef, update);
            }
        };

        if (StringUtils.isEmpty(cmd.getScope())) {
            ctx.loadAttributes(entityId, Arrays.asList(DataConstants.allScopes()), keys, callback);
        } else {
            ctx.loadAttributes(entityId, cmd.getScope(), keys, callback);
        }
    }*/

    private void handleWsAttributesSubscription(PluginContext ctx, PluginWebsocketSessionRef sessionRef,
                                                AttributesSubscriptionCmd cmd, String sessionId, EntityId entityId) {
        PluginCallback<List<AttributeKvEntry>> callback = new PluginCallback<List<AttributeKvEntry>>() {
            @Override
            public void onSuccess(PluginContext ctx, List<AttributeKvEntry> data) {
                List<TsKvEntry> attributesData = data.stream().map(d -> new BasicTsKvEntry(d.getLastUpdateTs(), d)).collect(Collectors.toList());
                sendWsMsg(ctx, sessionRef, new SubscriptionUpdate(cmd.getCmdId(), attributesData));

                Map<String, Long> subState = new HashMap<>(attributesData.size());
                attributesData.forEach(v -> subState.put(v.getKey(), v.getTs()));

                SubscriptionState sub = new SubscriptionState(sessionId, cmd.getCmdId(), entityId, SubscriptionType.ATTRIBUTES, true, subState, cmd.getScope());
                subscriptionManager.addLocalWsSubscription(ctx, sessionId, entityId, sub);
            }

            @Override
            public void onFailure(PluginContext ctx, Exception e) {
                log.error(FAILED_TO_FETCH_ATTRIBUTES, e);
                SubscriptionUpdate update = new SubscriptionUpdate(cmd.getCmdId(), SubscriptionErrorCode.INTERNAL_ERROR,
                        FAILED_TO_FETCH_ATTRIBUTES);
                sendWsMsg(ctx, sessionRef, update);
            }
        };

        if (StringUtils.isEmpty(cmd.getScope())) {
            ctx.loadAttributes(entityId, Arrays.asList(DataConstants.allScopes()), callback);
        } else {
            ctx.loadAttributes(entityId, cmd.getScope(), callback);
        }
    }

    private void handleWsTimeseriesSubscriptionCmd(PluginContext ctx, PluginWebsocketSessionRef sessionRef, TimeseriesSubscriptionCmd cmd) {
        String sessionId = sessionRef.getSessionId();
        log.debug("[{}] Processing: {}", sessionId, cmd);

        if (validateSessionMetadata(ctx, sessionRef, cmd, sessionId)) {
            if (cmd.isUnsubscribe()) {
                unsubscribe(ctx, cmd, sessionId);
            } else if (validateSubscriptionCmd(ctx, sessionRef, cmd)) {
                EntityId entityId = EntityIdFactory.getByTypeAndId(cmd.getEntityType(), cmd.getEntityId());
                Optional<Set<String>> keysOptional = getKeys(cmd);
                if (keysOptional.isPresent()) {
                    long startTs;
                    if (cmd.getTimeWindow() > 0) {
                        List<String> keys = new ArrayList<>(getKeys(cmd).orElse(Collections.emptySet()));
                        log.info("[{}] fetching timeseries data for last {} ms for keys: ({}) for device : {}", sessionId, cmd.getTimeWindow(), cmd.getKeys(), entityId);
                        startTs = cmd.getStartTs();
                        long endTs = cmd.getStartTs() + cmd.getTimeWindow();
                        List<TsKvQuery> queries = keys.stream().map(key -> new BaseTsKvQuery(key, startTs, endTs, cmd.getInterval(), getLimit(cmd.getLimit()), getAggregation(cmd.getAgg()))).collect(Collectors.toList());
                        ctx.loadTimeseries(entityId, queries, getSubscriptionCallback(sessionRef, cmd, sessionId, entityId, startTs, keys));
                    } else {
                        List<String> keys = new ArrayList<>(getKeys(cmd).orElse(Collections.emptySet()));
                        startTs = System.currentTimeMillis();
                        log.info("[{}] fetching latest timeseries data for keys: ({}) for device : {}", sessionId, cmd.getKeys(), entityId);
                        ctx.loadLatestTimeseries(entityId, keys, getSubscriptionCallback(sessionRef, cmd, sessionId, entityId, startTs, keys));
                    }
                } else {
                    ctx.loadLatestTimeseries(entityId, new PluginCallback<List<TsKvEntry>>() {
                        @Override
                        public void onSuccess(PluginContext ctx, List<TsKvEntry> data) {
                            sendWsMsg(ctx, sessionRef, new SubscriptionUpdate(cmd.getCmdId(), data));
                            Map<String, Long> subState = new HashMap<>(data.size());
                            data.forEach(v -> subState.put(v.getKey(), v.getTs()));
                            SubscriptionState<Long> sub = new SubscriptionState(sessionId, cmd.getCmdId(), entityId, SubscriptionType.TIMESERIES, true, subState,cmd.getScope());
                            subscriptionManager.addLocalWsSubscription(ctx, sessionId, entityId, sub);
                        }

                        @Override
                        public void onFailure(PluginContext ctx, Exception e) {
                            SubscriptionUpdate update;
                            if (UnauthorizedException.class.isInstance(e)) {
                                update = new SubscriptionUpdate(cmd.getCmdId(), SubscriptionErrorCode.UNAUTHORIZED,
                                        SubscriptionErrorCode.UNAUTHORIZED.getDefaultMsg());
                            } else {
                                update = new SubscriptionUpdate(cmd.getCmdId(), SubscriptionErrorCode.INTERNAL_ERROR,
                                        "Failed to fetch data!");
                            }
                            sendWsMsg(ctx, sessionRef, update);
                        }
                    });
                }
            }
        }
    }

    private void handleWsDepthSeriesSubscriptionCmd(PluginContext ctx, PluginWebsocketSessionRef sessionRef, DepthSeriesSubscriptionCmd cmd) {
        String sessionId = sessionRef.getSessionId();
        log.debug("[{}] Processing: {}", sessionId, cmd);

        if (validateSessionMetadata(ctx, sessionRef, cmd, sessionId)) {
            if (cmd.isUnsubscribe()) {
                unsubscribe(ctx, cmd, sessionId);
            } else if (validateSubscriptionCmd(ctx, sessionRef, cmd)) {
                EntityId entityId = EntityIdFactory.getByTypeAndId(cmd.getEntityType(), cmd.getEntityId());
                Optional<Set<String>> keysOptional = getKeys(cmd);
                if (keysOptional.isPresent()) {
                    log.debug(" optional present");
                    Double startDs;
                    if (cmd.getDepthWindow() > 0) {
                        List<String> keys = new ArrayList<>(getKeys(cmd).orElse(Collections.emptySet()));
                        log.debug("[{}] fetching depthseries data for last {}  for keys: ({}) for device : {}", sessionId, cmd.getDepthWindow(), cmd.getKeys(), entityId);
                        startDs = cmd.getStartDs();
                        Double endDs = cmd.getStartDs() + cmd.getDepthWindow();
                        List<DsKvQuery> queries = keys.stream().map(key -> new BaseDsKvQuery(key, startDs, endDs, cmd.getInterval(), getLimit(cmd.getLimit()), getDepthAggregation(cmd.getAgg()))).collect(Collectors.toList());
                        ctx.loadDepthSeries(entityId, queries, getSubscriptionCallback(sessionRef, cmd, sessionId, entityId, startDs, keys));
                    } else {
                        List<String> keys = new ArrayList<>(getKeys(cmd).orElse(Collections.emptySet()));
                        startDs = 0.0;
                        log.debug("[{}] fetching latest depth series data for keys: ({}) for device : {}", sessionId, cmd.getKeys(), entityId);
                        ctx.loadLatestDepthSeries(entityId, keys, getSubscriptionCallback(sessionRef, cmd, sessionId, entityId, startDs, keys));
                    }
                } else {
                    ctx.loadLatestDepthSeries(entityId, new PluginCallback<List<DsKvEntry>>() {
                        @Override
                        public void onSuccess(PluginContext ctx, List<DsKvEntry> data) {
                            log.debug("load latest success  " + data);
                            sendWsMsg(ctx, sessionRef, new DepthSubscriptionUpdate(cmd.getCmdId(), data));
                            Map<String, Double> subState = new HashMap<>(data.size());
                            data.forEach(v -> subState.put(v.getKey(), v.getDs()));
                            SubscriptionState<Double> sub = new SubscriptionState(sessionId, cmd.getCmdId(), entityId, SubscriptionType.DEPTHSERIES
                                    , true, subState,cmd.getScope());
                            subscriptionManager.addLocalWsDepthSubscription(ctx, sessionId, entityId, sub);
                        }

                        @Override
                        public void onFailure(PluginContext ctx, Exception e) {
                            DepthSubscriptionUpdate update;
                            log.debug("load latest failure ");
                            if (UnauthorizedException.class.isInstance(e)) {
                                update = new DepthSubscriptionUpdate(cmd.getCmdId(), SubscriptionErrorCode.UNAUTHORIZED,
                                        SubscriptionErrorCode.UNAUTHORIZED.getDefaultMsg());
                            } else {
                                update = new DepthSubscriptionUpdate(cmd.getCmdId(), SubscriptionErrorCode.INTERNAL_ERROR,
                                        "Failed to fetch data!");
                            }
                            sendWsMsg(ctx, sessionRef, update);
                        }
                    });
                }
            }
        }
    }

    private PluginCallback<List<TsKvEntry>> getSubscriptionCallback(final PluginWebsocketSessionRef sessionRef, final TimeseriesSubscriptionCmd cmd, final String sessionId, final EntityId entityId, final long startTs, final List<String> keys) {
        return new PluginCallback<List<TsKvEntry>>() {
            @Override
            public void onSuccess(PluginContext ctx, List<TsKvEntry> data) {
                if(data.isEmpty()){
                    ctx.loadLatestTimeseries(entityId, keys, new PluginCallback<List<TsKvEntry>>() {
                        @Override
                        public void onSuccess(PluginContext ctx, List<TsKvEntry> data) {
                            log.info("Latest TsKvEntry [{}]", data);
                            long endTs = data.get(0).getTs();
                            long startTs = endTs - cmd.getTimeWindow();
                            List<TsKvQuery> queries = keys.stream().map(key -> new BaseTsKvQuery(key, startTs, endTs, cmd.getInterval(), getLimit(cmd.getLimit()), getAggregation(cmd.getAgg()))).collect(Collectors.toList());
                            ctx.loadTimeseries(entityId, queries, new PluginCallback<List<TsKvEntry>>() {
                                @Override
                                public void onSuccess(PluginContext ctx, List<TsKvEntry> data) {
                                    log.debug("TsKvEntry List [{}]", data);
                                    sendWsMsg(ctx, sessionRef, new SubscriptionUpdate(cmd.getCmdId(), data));
                                }
                                @Override
                                public void onFailure(PluginContext ctx, Exception e) {
                                    log.info("Failed to fetch TsKvEntry List.");
                                }
                            });
                        }
                        @Override
                        public void onFailure(PluginContext ctx, Exception e){
                            log.info("Failed to fetch latest TsKvEntry.");
                        }
                    });
                }
                else
                    sendWsMsg(ctx, sessionRef, new SubscriptionUpdate(cmd.getCmdId(), data));

                Map<String, Long> subState = new HashMap<>(keys.size());
                keys.forEach(key -> subState.put(key, startTs));
                data.forEach(v -> subState.put(v.getKey(), v.getTs()));
                SubscriptionState sub = new SubscriptionState(sessionId, cmd.getCmdId(), entityId, SubscriptionType.TIMESERIES, false, subState, cmd.getScope());
                subscriptionManager.addLocalWsSubscription(ctx, sessionId, entityId, sub);
            }

            @Override
            public void onFailure(PluginContext ctx, Exception e) {
                log.error(FAILED_TO_FETCH_DATA, e);
                SubscriptionUpdate update = new SubscriptionUpdate(cmd.getCmdId(), SubscriptionErrorCode.INTERNAL_ERROR,
                        FAILED_TO_FETCH_DATA);
                sendWsMsg(ctx, sessionRef, update);
            }
        };
    }

    private PluginCallback<List<DsKvEntry>> getSubscriptionCallback(final PluginWebsocketSessionRef sessionRef, final DepthSeriesSubscriptionCmd cmd, final String sessionId, final EntityId entityId, final Double startDs, final List<String> keys) {
        return new PluginCallback<List<DsKvEntry>>() {
            @Override
            public void onSuccess(PluginContext ctx, List<DsKvEntry> data) {
                log.debug("on success getSubscriptionCallback " + data );
                if(data.isEmpty()){
                    ctx.loadLatestDepthSeries(entityId, keys, new PluginCallback<List<DsKvEntry>>() {
                        @Override
                        public void onSuccess(PluginContext ctx, List<DsKvEntry> data) {
                            log.info("Latest DsKvEntry [{}]", data);
                            Double endDs = data.get(0).getDs();
                            log.info("endDs = " + endDs);
                            Double startDs = endDs - cmd.getDepthWindow();
                            List<DsKvQuery> queries = keys.stream().map(key -> new BaseDsKvQuery(key, startDs, endDs, cmd.getInterval(), getLimit(cmd.getLimit()), getDepthAggregation(cmd.getAgg()))).collect(Collectors.toList());
                            ctx.loadDepthSeries(entityId, queries, new PluginCallback<List<DsKvEntry>>() {
                                @Override
                                public void onSuccess(PluginContext ctx, List<DsKvEntry> data) {
                                    log.debug("DsKvEntry List [{}]", data);
                                    sendWsMsg(ctx, sessionRef, new DepthSubscriptionUpdate(cmd.getCmdId(), data));
                                }
                                @Override
                                public void onFailure(PluginContext ctx, Exception e) {
                                    log.info("Failed to fetch DsKvEntry List.");
                                }
                            });
                        }
                        @Override
                        public void onFailure(PluginContext ctx, Exception e){
                            log.info("Failed to fetch latest DsKvEntry.");
                        }
                    });
                }
                else
                    sendWsMsg(ctx, sessionRef, new DepthSubscriptionUpdate(cmd.getCmdId(), data));

                Map<String, Double> subState = new HashMap<>(keys.size());
                keys.forEach(key -> subState.put(key, startDs));
                data.forEach(v -> subState.put( v.getKey(), v.getDs()));
                SubscriptionState<Double> sub = new SubscriptionState(sessionId, cmd.getCmdId(), entityId, SubscriptionType.DEPTHSERIES, false, subState,cmd.getScope());
                subscriptionManager.addLocalWsDepthSubscription(ctx, sessionId, entityId, sub);
            }

            @Override
            public void onFailure(PluginContext ctx, Exception e) {
                log.debug("Failed to fetch data!", e);
                DepthSubscriptionUpdate update = new DepthSubscriptionUpdate(cmd.getCmdId(), SubscriptionErrorCode.INTERNAL_ERROR,
                        "Failed to fetch data!");
                sendWsMsg(ctx, sessionRef, update);
            }
        };
    }

    private void handleWsHistoryCmd(PluginContext ctx, PluginWebsocketSessionRef sessionRef, GetHistoryCmd cmd) {
        String sessionId = sessionRef.getSessionId();
        WsSessionMetaData sessionMD = wsSessionsMap.get(sessionId);
        if (sessionMD == null) {
            log.warn("[{}] Session meta data not found. ", sessionId);
            SubscriptionUpdate update = new SubscriptionUpdate(cmd.getCmdId(), SubscriptionErrorCode.INTERNAL_ERROR,
                    SESSION_META_DATA_NOT_FOUND);
            sendWsMsg(ctx, sessionRef, update);
            return;
        }
        if (cmd.getEntityId() == null || cmd.getEntityId().isEmpty() || cmd.getEntityType() == null || cmd.getEntityType().isEmpty()) {
            SubscriptionUpdate update = new SubscriptionUpdate(cmd.getCmdId(), SubscriptionErrorCode.BAD_REQUEST,
                    "Device id is empty!");
            sendWsMsg(ctx, sessionRef, update);
            return;
        }
        if (cmd.getKeys() == null || cmd.getKeys().isEmpty()) {
            SubscriptionUpdate update = new SubscriptionUpdate(cmd.getCmdId(), SubscriptionErrorCode.BAD_REQUEST,
                    "Keys are empty!");
            sendWsMsg(ctx, sessionRef, update);
            return;
        }
        EntityId entityId = EntityIdFactory.getByTypeAndId(cmd.getEntityType(), cmd.getEntityId());
        List<String> keys = new ArrayList<>(getKeys(cmd).orElse(Collections.emptySet()));
        List<TsKvQuery> queries = keys.stream().map(key -> new BaseTsKvQuery(key, cmd.getStartTs(), cmd.getEndTs(), cmd.getInterval(), getLimit(cmd.getLimit()), getAggregation(cmd.getAgg())))
                .collect(Collectors.toList());
        ctx.loadTimeseries(entityId, queries, new PluginCallback<List<TsKvEntry>>() {
            @Override
            public void onSuccess(PluginContext ctx, List<TsKvEntry> data) {
                sendWsMsg(ctx, sessionRef, new SubscriptionUpdate(cmd.getCmdId(), data));
            }

            @Override
            public void onFailure(PluginContext ctx, Exception e) {
                SubscriptionUpdate update;
                if (UnauthorizedException.class.isInstance(e)) {
                    update = new SubscriptionUpdate(cmd.getCmdId(), SubscriptionErrorCode.UNAUTHORIZED,
                            SubscriptionErrorCode.UNAUTHORIZED.getDefaultMsg());
                } else {
                    update = new SubscriptionUpdate(cmd.getCmdId(), SubscriptionErrorCode.INTERNAL_ERROR,
                            FAILED_TO_FETCH_DATA);
                }
                sendWsMsg(ctx, sessionRef, update);
            }
        });
    }

    private void handleWsDepthHistoryCmd(PluginContext ctx, PluginWebsocketSessionRef sessionRef, GetDepthHistoryCmd cmd) {
        String sessionId = sessionRef.getSessionId();
        WsSessionMetaData sessionMD = wsSessionsMap.get(sessionId);
        if (sessionMD == null) {
            log.warn("[{}] Session meta data not found. ", sessionId);
            DepthSubscriptionUpdate update = new DepthSubscriptionUpdate(cmd.getCmdId(), SubscriptionErrorCode.INTERNAL_ERROR,
                    "Session meta-data not found!");
            sendWsMsg(ctx, sessionRef, update);
            return;
        }
        if (cmd.getEntityId() == null || cmd.getEntityId().isEmpty() || cmd.getEntityType() == null || cmd.getEntityType().isEmpty()) {
            DepthSubscriptionUpdate update = new DepthSubscriptionUpdate(cmd.getCmdId(), SubscriptionErrorCode.BAD_REQUEST,
                    "Device id is empty!");
            sendWsMsg(ctx, sessionRef, update);
            return;
        }
        if (cmd.getKeys() == null || cmd.getKeys().isEmpty()) {
            DepthSubscriptionUpdate update = new DepthSubscriptionUpdate(cmd.getCmdId(), SubscriptionErrorCode.BAD_REQUEST,
                    "Keys are empty!");
            sendWsMsg(ctx, sessionRef, update);
            return;
        }
        EntityId entityId = EntityIdFactory.getByTypeAndId(cmd.getEntityType(), cmd.getEntityId());
        List<String> keys = new ArrayList<>(getKeys(cmd).orElse(Collections.emptySet()));
        List<DsKvQuery> queries = keys.stream().map(key -> new BaseDsKvQuery(key, cmd.getStartDs(), cmd.getEndDs(), cmd.getInterval(), getLimit(cmd.getLimit()), getDepthAggregation(cmd.getAgg())))
                .collect(Collectors.toList());
        ctx.loadDepthSeries(entityId, queries, new PluginCallback<List<DsKvEntry>>() {
            @Override
            public void onSuccess(PluginContext ctx, List<DsKvEntry> data) {
                sendWsMsg(ctx, sessionRef, new DepthSubscriptionUpdate(cmd.getCmdId(), data));
            }

            @Override
            public void onFailure(PluginContext ctx, Exception e) {
                DepthSubscriptionUpdate update;
                if (UnauthorizedException.class.isInstance(e)) {
                    update = new DepthSubscriptionUpdate(cmd.getCmdId(), SubscriptionErrorCode.UNAUTHORIZED,
                            SubscriptionErrorCode.UNAUTHORIZED.getDefaultMsg());
                } else {
                    update = new DepthSubscriptionUpdate(cmd.getCmdId(), SubscriptionErrorCode.INTERNAL_ERROR,
                            "Failed to fetch data!");
                }
                sendWsMsg(ctx, sessionRef, update);
            }
        });
    }

    private static Aggregation getAggregation(String agg) {
        return StringUtils.isEmpty(agg) ? DEFAULT_AGGREGATION : Aggregation.valueOf(agg);
    }

    private static DepthAggregation getDepthAggregation(String agg) {
        return StringUtils.isEmpty(agg) ? DEFAULT_DEPTH_AGGREGATION : DepthAggregation.valueOf(agg);
    }

    private int getLimit(int limit) {
        return limit == 0 ? DEFAULT_LIMIT : limit;
    }

    private boolean validateSessionMetadata(PluginContext ctx, PluginWebsocketSessionRef sessionRef, SubscriptionCmd cmd, String sessionId) {
        WsSessionMetaData sessionMD = wsSessionsMap.get(sessionId);
        if (sessionMD == null) {
            log.warn("[{}] Session meta data not found. ", sessionId);
            SubscriptionUpdate update = new SubscriptionUpdate(cmd.getCmdId(), SubscriptionErrorCode.INTERNAL_ERROR,
                    SESSION_META_DATA_NOT_FOUND);
            sendWsMsg(ctx, sessionRef, update);
            return false;
        } else {
            return true;
        }
    }

    private void unsubscribe(PluginContext ctx, SubscriptionCmd cmd, String sessionId) {
        if (cmd.getEntityId() == null || cmd.getEntityId().isEmpty()) {
            cleanupWebSocketSession(ctx, sessionId);
        } else {
            subscriptionManager.removeSubscription(ctx, sessionId, cmd.getCmdId());
        }
    }

    private boolean validateSubscriptionCmd(PluginContext ctx, PluginWebsocketSessionRef sessionRef, SubscriptionCmd cmd) {
        if (cmd.getEntityId() == null || cmd.getEntityId().isEmpty()) {
            SubscriptionUpdate update = new SubscriptionUpdate(cmd.getCmdId(), SubscriptionErrorCode.BAD_REQUEST,
                    "Device id is empty!");
            sendWsMsg(ctx, sessionRef, update);
            return false;
        }
        return true;
    }

    private void sendWsMsg(PluginContext ctx, PluginWebsocketSessionRef sessionRef, SubscriptionUpdate update) {
        TextPluginWebSocketMsg reply;
        try {
            reply = new TextPluginWebSocketMsg(sessionRef, jsonMapper.writeValueAsString(update));
            log.debug("reply TS " + reply);
            ctx.send(reply);
        } catch (JsonProcessingException e) {
            log.warn("[{}] Failed to encode reply: {}", sessionRef.getSessionId(), update, e);
        } catch (IOException e) {
            log.warn("[{}] Failed to send reply: {}", sessionRef.getSessionId(), update, e);
        }
    }

    private void sendWsMsg(PluginContext ctx, PluginWebsocketSessionRef sessionRef, DepthSubscriptionUpdate update) {
        TextPluginWebSocketMsg reply;
        try {
            reply = new TextPluginWebSocketMsg(sessionRef, jsonMapper.writeValueAsString(update));
            log.debug("reply DS " + reply);
            ctx.send(reply);
        } catch (JsonProcessingException e) {
            log.warn("[{}] Failed to encode reply: {}", sessionRef.getSessionId(), update, e);
        } catch (IOException e) {
            log.warn("[{}] Failed to send reply: {}", sessionRef.getSessionId(), update, e);
        }
    }

    public static Optional<Set<String>> getKeys(TelemetryPluginCmd cmd) {
        if (!StringUtils.isEmpty(cmd.getKeys())) {
            Set<String> keys = new HashSet<>();
            for (String key : cmd.getKeys().split(",")) {
                keys.add(key);
            }
            return Optional.of(keys);
        } else {
            return Optional.empty();
        }
    }

    public void sendWsMsg(PluginContext ctx, String sessionId, SubscriptionUpdate update) {
        WsSessionMetaData md = wsSessionsMap.get(sessionId);
        if (md != null) {
            sendWsMsg(ctx, md.getSessionRef(), update);
        }
    }

    public void sendWsMsg(PluginContext ctx, String sessionId, DepthSubscriptionUpdate update) {
        WsSessionMetaData md = wsSessionsMap.get(sessionId);
        if (md != null) {
            sendWsMsg(ctx, md.getSessionRef(), update);
        }
    }
}
