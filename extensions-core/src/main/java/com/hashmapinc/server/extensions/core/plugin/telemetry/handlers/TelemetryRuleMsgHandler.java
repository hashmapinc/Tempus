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

import com.hashmapinc.server.common.data.DataConstants;
import com.hashmapinc.server.common.data.id.DeviceId;
import com.hashmapinc.server.common.data.id.RuleId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.kv.*;
import com.hashmapinc.server.common.msg.core.*;
import com.hashmapinc.server.common.msg.kv.BasicAttributeKVMsg;
import com.hashmapinc.server.extensions.api.plugins.PluginCallback;
import com.hashmapinc.server.extensions.api.plugins.PluginContext;
import com.hashmapinc.server.extensions.api.plugins.handlers.DefaultRuleMsgHandler;
import com.hashmapinc.server.extensions.api.plugins.msg.*;
import com.hashmapinc.server.extensions.core.plugin.telemetry.SubscriptionManager;
import com.hashmapinc.server.extensions.core.plugin.telemetry.dataquality.MetaDataAggregator;
import com.hashmapinc.server.extensions.core.plugin.telemetry.sub.Subscription;
import com.hashmapinc.server.extensions.core.plugin.telemetry.sub.SubscriptionType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class TelemetryRuleMsgHandler extends DefaultRuleMsgHandler {

    private final SubscriptionManager subscriptionManager;

    public TelemetryRuleMsgHandler(SubscriptionManager subscriptionManager) {
        this.subscriptionManager = subscriptionManager;
    }

    @Override
    public void handleGetAttributesRequest(PluginContext ctx, TenantId tenantId, RuleId ruleId, GetAttributesRequestRuleToPluginMsg msg) {
        GetAttributesRequest request = msg.getPayload();

        BiPluginCallBack<List<AttributeKvEntry>, List<AttributeKvEntry>> callback = new BiPluginCallBack<List<AttributeKvEntry>, List<AttributeKvEntry>>() {

            @Override
            public void onSuccess(PluginContext ctx, List<AttributeKvEntry> clientAttributes, List<AttributeKvEntry> sharedAttributes) {
                BasicGetAttributesResponse response = BasicGetAttributesResponse.onSuccess(request.getMsgType(),
                        request.getRequestId(), BasicAttributeKVMsg.from(clientAttributes, sharedAttributes));
                ctx.reply(new ResponsePluginToRuleMsg(msg.getUid(), tenantId, ruleId, response));
            }

            @Override
            public void onFailure(PluginContext ctx, Exception e) {
                log.error("Failed to process get attributes request", e);
                ctx.reply(new ResponsePluginToRuleMsg(msg.getUid(), tenantId, ruleId, BasicStatusCodeResponse.onError(request.getMsgType(), request.getRequestId(), e)));
            }
        };

        getAttributeKvEntries(ctx, msg.getDeviceId(), DataConstants.CLIENT_SCOPE, request.getClientAttributeNames(), callback.getV1Callback());
        getAttributeKvEntries(ctx, msg.getDeviceId(), DataConstants.SHARED_SCOPE, request.getSharedAttributeNames(), callback.getV2Callback());
    }

    private void getAttributeKvEntries(PluginContext ctx, DeviceId deviceId, String scope, Optional<Set<String>> names, PluginCallback<List<AttributeKvEntry>> callback) {
        if (names.isPresent()) {
            if (!names.get().isEmpty()) {
                ctx.loadAttributes(deviceId, scope, new ArrayList<>(names.get()), callback);
            } else {
                ctx.loadAttributes(deviceId, scope, callback);
            }
        } else {
            callback.onSuccess(ctx, Collections.emptyList());
        }
    }

    @Override
    public void handleTelemetryUploadRequest(PluginContext ctx, TenantId tenantId, RuleId ruleId, TelemetryUploadRequestRuleToPluginMsg msg) {
        TelemetryUploadRequest request = msg.getPayload();
        for (Map.Entry<Long, List<KvEntry>> entry : request.getData().entrySet()) {
            List<KvEntry> kvEntries = entry.getValue();
            ctx.loadLatestTimeseries(msg.getDeviceId(), kvEntries, new PluginCallback<List<TsKvEntry>>() {
                @Override
                public void onSuccess(PluginContext ctx, List<TsKvEntry> data) {
                    List<TsKvEntry> tsKvEntries = new ArrayList<>();
                    if(data.isEmpty()){
                        for (KvEntry kv : kvEntries) {
                            BasicTsKvEntry basicTsKvEntry = new BasicTsKvEntry(entry.getKey(), kv);
                            basicTsKvEntry.setTsDiff(0);
                            tsKvEntries.add(basicTsKvEntry);
                        }
                    }else {
                        for (TsKvEntry tsKvEntry : data) {
                            for (KvEntry kv : kvEntries) {
                                if (kv.getKey().contentEquals(tsKvEntry.getKey())) {
                                    Long tsDiff = entry.getKey() - tsKvEntry.getTs();
                                    BasicTsKvEntry basicTsKvEntry = new BasicTsKvEntry(entry.getKey(), kv);
                                    basicTsKvEntry.setTsDiff(tsDiff);
                                    tsKvEntries.add(basicTsKvEntry);
                                }
                            }
                        }
                    }
                    ctx.saveTsData(msg.getTenantId(), msg.getDeviceId(), tsKvEntries, msg.getTtl(), new PluginCallback<Void>() {
                        @Override
                        public void onSuccess(PluginContext ctx, Void data) {
                            ctx.reply(new ResponsePluginToRuleMsg(msg.getUid(), tenantId, ruleId, BasicStatusCodeResponse.onSuccess(request.getMsgType(), request.getRequestId())));
                            subscriptionManager.onLocalSubscriptionUpdate(ctx, msg.getDeviceId(), SubscriptionType.TIMESERIES, s ->
                                    prepareSubscriptionUpdate(request, s)
                            );
                            MetaDataAggregator metaDataAggregator = new MetaDataAggregator(ctx, msg.getDeviceId());
                            metaDataAggregator.setAggregationPeriod(msg.getQualityTimeWindow());
                            metaDataAggregator.aggregateMetaData(entry.getKey(), tsKvEntries);
                        }

                        @Override
                        public void onFailure(PluginContext ctx, Exception e) {
                            log.error("Failed to process telemetry upload request", e);
                            ctx.reply(new ResponsePluginToRuleMsg(msg.getUid(), tenantId, ruleId, BasicStatusCodeResponse.onError(request.getMsgType(), request.getRequestId(), e)));
                        }
                    });
                }
                @Override
                public void onFailure(PluginContext ctx, Exception e){
                    log.info("Failed to fetch latest TsKvEntry.");
                }
            });
        }
    }

    private List<TsKvEntry> prepareSubscriptionUpdate(TelemetryUploadRequest request, Subscription s) {
        List<TsKvEntry> subscriptionUpdate = new ArrayList<>();
        for (Map.Entry<Long, List<KvEntry>> entry : request.getData().entrySet()) {
            for (KvEntry kv : entry.getValue()) {
                if (s.isAllKeys() || s.getKeyStates().containsKey((kv.getKey()))) {
                    subscriptionUpdate.add(new BasicTsKvEntry(entry.getKey(), kv));
                }
            }
        }
        return subscriptionUpdate;
    }

    @Override
    public void handleDepthTelemetryUploadRequest(PluginContext ctx, TenantId tenantId, RuleId ruleId, DepthTelemetryUploadRequestRuleToPluginMsg msg) {
        DepthTelemetryUploadRequest request = msg.getPayload();
        for (Map.Entry<Double, List<KvEntry>> entry : request.getData().entrySet()) {
            List<KvEntry> kvEntries = entry.getValue();
            ctx.loadLatestDepthSeries(msg.getDeviceId(), kvEntries, new PluginCallback<List<DsKvEntry>>() {
                @Override
                public void onSuccess(PluginContext ctx, List<DsKvEntry> data) {
                    List<DsKvEntry> dsKvEntries = new ArrayList<>();
                    if(data.isEmpty()){
                        for (KvEntry kv : kvEntries) {
                            BasicDsKvEntry basicDsKvEntry = new BasicDsKvEntry(entry.getKey(), kv);
                            basicDsKvEntry.setDsDiff(0.0);
                            dsKvEntries.add(basicDsKvEntry);
                        }
                    }else {
                        for (DsKvEntry dsKvEntry : data) {
                            for (KvEntry kv : kvEntries) {
                                if (kv.getKey().contentEquals(dsKvEntry.getKey())) {
                                    Double dsDiff = entry.getKey() - dsKvEntry.getDs();
                                    BasicDsKvEntry basicDsKvEntry = new BasicDsKvEntry(entry.getKey(), kv);
                                    basicDsKvEntry.setDsDiff(dsDiff);
                                    dsKvEntries.add(basicDsKvEntry);
                                }
                            }
                        }
                    }

                    ctx.saveDsData(msg.getDeviceId(), dsKvEntries, msg.getTtl(), new PluginCallback<Void>() {
                        @Override
                        public void onSuccess(PluginContext ctx, Void data) {
                            ctx.reply(new ResponsePluginToRuleMsg(msg.getUid(), tenantId, ruleId, BasicStatusCodeResponse.onSuccess(request.getMsgType(), request.getRequestId())));
                            subscriptionManager.onLocalSubscriptionUpdateForDepth(ctx, msg.getDeviceId(), SubscriptionType.DEPTHSERIES, s ->
                                    prepareSubscriptionUpdate(request, s)
                            );
                            MetaDataAggregator metaDataAggregator = new MetaDataAggregator(ctx, msg.getDeviceId());
                            metaDataAggregator.setDepthAggregationPeriod(msg.getQualityDepthWindow());
                            metaDataAggregator.aggregateDepthMetaData(entry.getKey(), dsKvEntries);
                        }

                        @Override
                        public void onFailure(PluginContext ctx, Exception e) {
                            log.error("Failed to process telemetry upload request", e);
                            ctx.reply(new ResponsePluginToRuleMsg(msg.getUid(), tenantId, ruleId, BasicStatusCodeResponse.onError(request.getMsgType(), request.getRequestId(), e)));
                        }
                    });
                }
                @Override
                public void onFailure(PluginContext ctx, Exception e){
                    log.info("Failed to fetch latest TsKvEntry.");
                }
            });
        }
    }

    private List<DsKvEntry> prepareSubscriptionUpdate(DepthTelemetryUploadRequest request, Subscription s){
        List<DsKvEntry> subscriptionUpdate = new ArrayList<DsKvEntry>();
        for (Map.Entry<Double, List<KvEntry>> entry : request.getData().entrySet()) {
            for (KvEntry kv : entry.getValue()) {
                if (s.isAllKeys() || s.getKeyStates().containsKey((kv.getKey()))) {
                    subscriptionUpdate.add(new BasicDsKvEntry(entry.getKey(), kv));
                }
            }
        }
        return subscriptionUpdate;
    }

    @Override
    public void handleUpdateAttributesRequest(PluginContext ctx, TenantId tenantId, RuleId ruleId, UpdateAttributesRequestRuleToPluginMsg msg) {
        UpdateAttributesRequest request = msg.getPayload();
        ctx.saveAttributes(msg.getTenantId(), msg.getDeviceId(), DataConstants.CLIENT_SCOPE, request.getAttributes().stream().collect(Collectors.toList()),
                new PluginCallback<Void>() {
                    @Override
                    public void onSuccess(PluginContext ctx, Void value) {
                        ctx.reply(new ResponsePluginToRuleMsg(msg.getUid(), tenantId, ruleId, BasicStatusCodeResponse.onSuccess(request.getMsgType(), request.getRequestId())));

                        subscriptionManager.onLocalSubscriptionUpdate(ctx, msg.getDeviceId(), SubscriptionType.ATTRIBUTES, s -> {
                            List<TsKvEntry> subscriptionUpdate = new ArrayList<>();
                            for (AttributeKvEntry kv : request.getAttributes()) {
                                if (s.isAllKeys() || s.getKeyStates().containsKey(kv.getKey())) {
                                    subscriptionUpdate.add(new BasicTsKvEntry(kv.getLastUpdateTs(), kv));
                                }
                            }
                            return subscriptionUpdate;
                        });
                    }

                    @Override
                    public void onFailure(PluginContext ctx, Exception e) {
                        log.error("Failed to process attributes update request", e);
                        ctx.reply(new ResponsePluginToRuleMsg(msg.getUid(), tenantId, ruleId, BasicStatusCodeResponse.onError(request.getMsgType(), request.getRequestId(), e)));
                    }
                });
    }
}