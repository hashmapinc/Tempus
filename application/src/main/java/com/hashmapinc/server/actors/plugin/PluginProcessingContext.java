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
package com.hashmapinc.server.actors.plugin;

import akka.actor.ActorRef;
import com.google.common.base.Function;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hashmapinc.server.common.data.*;
import com.hashmapinc.server.common.data.asset.Asset;
import com.hashmapinc.server.common.data.audit.ActionType;
import com.hashmapinc.server.common.data.id.*;
import com.hashmapinc.server.common.data.kv.*;
import com.hashmapinc.server.common.data.page.TextPageLink;
import com.hashmapinc.server.common.data.plugin.PluginMetaData;
import com.hashmapinc.server.common.data.relation.EntityRelation;
import com.hashmapinc.server.common.data.relation.RelationTypeGroup;
import com.hashmapinc.server.common.data.rule.RuleMetaData;
import com.hashmapinc.server.common.msg.cluster.ServerAddress;
import com.hashmapinc.server.extensions.api.device.DeviceAttributesEventNotificationMsg;
import com.hashmapinc.server.extensions.api.device.DeviceTelemetryEventNotificationMsg;
import com.hashmapinc.server.extensions.api.plugins.PluginApiCallSecurityContext;
import com.hashmapinc.server.extensions.api.plugins.PluginCallback;
import com.hashmapinc.server.extensions.api.plugins.PluginContext;
import com.hashmapinc.server.extensions.api.plugins.msg.*;
import com.hashmapinc.server.extensions.api.plugins.rpc.PluginRpcMsg;
import com.hashmapinc.server.extensions.api.plugins.rpc.RpcMsg;
import com.hashmapinc.server.extensions.api.plugins.ws.PluginWebsocketSessionRef;
import com.hashmapinc.server.extensions.api.plugins.ws.msg.PluginWebsocketMsg;
import com.hashmapinc.tempus.model.Quantity;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
public final class PluginProcessingContext implements PluginContext {

    private static final Executor executor = Executors.newSingleThreadExecutor();
    public static final String CUSTOMER_USER_IS_NOT_ALLOWED_TO_PERFORM_THIS_OPERATION = "Customer user is not allowed to perform this operation!";
    public static final String SYSTEM_ADMINISTRATOR_IS_NOT_ALLOWED_TO_PERFORM_THIS_OPERATION = "System administrator is not allowed to perform this operation!";
    public static final String DEVICE_WITH_REQUESTED_ID_NOT_FOUND = "Device with requested id wasn't found!";

    private final SharedPluginProcessingContext pluginCtx;
    private final Optional<PluginApiCallSecurityContext> securityCtx;
    private final String UNIT_SYSTEM_KEY = "unit_system";
    private final TenantId nullTenantId = new TenantId(UUIDConverter.fromString("1e7461259eab8808080808080808080"));

    public PluginProcessingContext(SharedPluginProcessingContext pluginCtx, PluginApiCallSecurityContext securityCtx) {
        super();
        this.pluginCtx = pluginCtx;
        this.securityCtx = Optional.ofNullable(securityCtx);
    }

    public void persistError(String method, Exception e) {
        pluginCtx.persistError(method, e);
    }

    @Override
    public void sendPluginRpcMsg(RpcMsg msg) {
        this.pluginCtx.rpcService.tell(new PluginRpcMsg(pluginCtx.tenantId, pluginCtx.pluginId, msg));
    }

    @Override
    public void send(PluginWebsocketMsg<?> wsMsg) throws IOException {
        pluginCtx.msgEndpoint.send(wsMsg);
    }

    @Override
    public void close(PluginWebsocketSessionRef sessionRef) throws IOException {
        pluginCtx.msgEndpoint.close(sessionRef);
    }

    @Override
    public void saveAttributes(final TenantId tenantId, final EntityId entityId, final String scope, final List<AttributeKvEntry> attributes, final PluginCallback<Void> callback) {
        validate(entityId, new ValidationCallback(callback, ctx -> {
            ListenableFuture<List<Void>> futures = pluginCtx.attributesService.save(entityId, scope, attributes);
            Futures.addCallback(futures, getListCallback(callback, v -> {
                if (entityId.getEntityType() == EntityType.DEVICE) {
                    onDeviceAttributesChanged(tenantId, new DeviceId(entityId.getId()), scope, attributes);
                }
                return null;
            }), executor);
        }));
    }

    @Override
    public void removeAttributes(final TenantId tenantId, final EntityId entityId, final String scope, final List<String> keys, final PluginCallback<Void> callback) {
        validate(entityId, new ValidationCallback(callback, ctx -> {
            ListenableFuture<List<Void>> futures = pluginCtx.attributesService.removeAll(entityId, scope, keys);
            Futures.addCallback(futures, getCallback(callback, v -> null), executor);
            if (entityId.getEntityType() == EntityType.DEVICE) {
                onDeviceAttributesDeleted(tenantId, new DeviceId(entityId.getId()), keys.stream().map(key -> new AttributeKey(scope, key)).collect(Collectors.toSet()));
            }
        }));
    }

    @Override
    public void loadAttribute(EntityId entityId, String attributeType, String attributeKey, final PluginCallback<Optional<AttributeKvEntry>> callback) {
        validate(entityId, new ValidationCallback(callback, ctx -> {
            ListenableFuture<Optional<AttributeKvEntry>> future = pluginCtx.attributesService.find(entityId, attributeType, attributeKey);
            final ListenableFuture<Optional<AttributeKvEntry>> transform = Futures.transform(future, transformAttributeKvEntryUnits());
            Futures.addCallback(transform, getCallback(callback, v -> v), executor);
        }));
    }

    private Function<Optional<AttributeKvEntry>, Optional<AttributeKvEntry>> transformAttributeKvEntryUnits() {
        return entry -> {
            List<AttributeKvEntry> collect;
            if (entry.isPresent()) {
                collect = Collections.singletonList(entry.get());
                List<AttributeKvEntry> attributeKvEntries = convertKvEntriesToUnitSystem(collect, BaseAttributeKvEntry.class);
                if (attributeKvEntries != null && !attributeKvEntries.isEmpty())
                    return Optional.ofNullable(attributeKvEntries.get(0));
            }
            return Optional.empty();
        };
    }

    @Override
    public void loadAttributes(EntityId entityId, String attributeType, Collection<String> attributeKeys, final PluginCallback<List<AttributeKvEntry>> callback) {
        validate(entityId, new ValidationCallback(callback, ctx -> {
            ListenableFuture<List<AttributeKvEntry>> future = pluginCtx.attributesService.find(entityId, attributeType, attributeKeys);
            ListenableFuture<List<AttributeKvEntry>> transformedKvEntries = Futures.transform(future , (Function<List<AttributeKvEntry>, List<AttributeKvEntry>>)entries -> convertKvEntriesToUnitSystem(entries, BaseAttributeKvEntry.class));
            Futures.addCallback(transformedKvEntries, getCallback(callback, v -> v), executor);
        }));
    }

    @Override
    public void loadAttributes(EntityId entityId, String attributeType, PluginCallback<List<AttributeKvEntry>> callback) {
        validate(entityId, new ValidationCallback(callback, ctx -> {
            ListenableFuture<List<AttributeKvEntry>> future = pluginCtx.attributesService.findAll(entityId, attributeType);
            ListenableFuture<List<AttributeKvEntry>> transformedKvEntries = Futures.transform(future , (Function<List<AttributeKvEntry>, List<AttributeKvEntry>>) entries -> convertKvEntriesToUnitSystem(entries, BaseAttributeKvEntry.class));
            Futures.addCallback(transformedKvEntries, getCallback(callback, v -> v), executor);
        }));
    }

    @Override
    public void loadAttributes(final EntityId entityId, final Collection<String> attributeTypes, final PluginCallback<List<AttributeKvEntry>> callback) {
        validate(entityId, new ValidationCallback(callback, ctx -> {
            List<ListenableFuture<List<AttributeKvEntry>>> futures = new ArrayList<>();
            attributeTypes.forEach(attributeType -> {
                ListenableFuture<List<AttributeKvEntry>> future = pluginCtx.attributesService.findAll(entityId , attributeType);
                ListenableFuture<List<AttributeKvEntry>> transformedKvEntries = Futures.transform(future , (Function<List<AttributeKvEntry>, List<AttributeKvEntry>>) entries -> convertKvEntriesToUnitSystem(entries, BaseAttributeKvEntry.class));
                futures.add(transformedKvEntries);
            });
            convertFuturesAndAddCallback(callback, futures);
        }));
    }

    @Override
    public void loadAttributes(final EntityId entityId, final Collection<String> attributeTypes, final Collection<String> attributeKeys, final PluginCallback<List<AttributeKvEntry>> callback) {
        validate(entityId, new ValidationCallback(callback, ctx -> {
            List<ListenableFuture<List<AttributeKvEntry>>> futures = new ArrayList<>();
            attributeTypes.forEach(attributeType -> {
                ListenableFuture<List<AttributeKvEntry>> future = pluginCtx.attributesService.find(entityId , attributeType , attributeKeys);
                ListenableFuture<List<AttributeKvEntry>> transformedKvEntries = Futures.transform(future , (Function<List<AttributeKvEntry>, List<AttributeKvEntry>>) entries -> convertKvEntriesToUnitSystem(entries, BaseAttributeKvEntry.class));
                futures.add(transformedKvEntries);
            });
            convertFuturesAndAddCallback(callback, futures);
        }));
    }

    @Override
    public void saveTsData(final TenantId tenantId, final EntityId entityId, final TsKvEntry entry, final PluginCallback<Void> callback) {
        validate(entityId, new ValidationCallback(callback, ctx -> {
            ListenableFuture<List<Void>> rsListFuture = pluginCtx.tsService.save(entityId, entry);
            Futures.addCallback(rsListFuture, getListCallback(callback, v -> null), executor);
        }));
    }

    @Override
    public void saveTsData(final TenantId tenantId, final EntityId entityId, final List<TsKvEntry> entries, final PluginCallback<Void> callback) {
        saveTsData(tenantId, entityId, entries, 0L, callback);
    }

    @Override
    public void saveTsData(final TenantId tenantId, final EntityId entityId, final List<TsKvEntry> entries, long ttl, final PluginCallback<Void> callback) {
        validate(entityId, new ValidationCallback(callback, ctx -> {
            ListenableFuture<List<Void>> rsListFuture = pluginCtx.tsService.save(entityId, entries, ttl);
            Futures.addCallback(rsListFuture, getListCallback(callback, v -> {
                if (entityId.getEntityType() == EntityType.DEVICE) {
                    onDeviceTelemetryChanged(tenantId, new DeviceId(entityId.getId()), entries);
                }
                return null;
            }), executor);
        }));
    }

    @Override
    public void saveDsData(final EntityId entityId, final List<DsKvEntry> entries, long ttl, final PluginCallback<Void> callback) {
        validate(entityId, new ValidationCallback(callback, ctx -> {
            ListenableFuture<List<Void>> rsListFuture = pluginCtx.dsService.save(entityId, entries, ttl);
            Futures.addCallback(rsListFuture, getListCallback(callback, v -> null), executor);
        }));
    }

    @Override
    public void loadTimeseries(final EntityId entityId, final List<TsKvQuery> queries, final PluginCallback<List<TsKvEntry>> callback) {
        validate(entityId, new ValidationCallback(callback, ctx -> {
            ListenableFuture<List<TsKvEntry>> future = pluginCtx.tsService.findAll(entityId, queries);
            ListenableFuture<List<TsKvEntry>> transformedKvEntries = Futures.transform(future , (Function<List<TsKvEntry>, List<TsKvEntry>>) entries -> convertKvEntriesToUnitSystem(entries, BasicTsKvEntry.class));
            Futures.addCallback(transformedKvEntries, getCallback(callback, v -> v), executor);
        }));
    }

    @Override
    public void loadDepthSeries(final EntityId entityId, final List<DsKvQuery> queries, final PluginCallback<List<DsKvEntry>> callback) {
        validate(entityId, new ValidationCallback(callback, ctx -> {
            ListenableFuture<List<DsKvEntry>> future = pluginCtx.dsService.findAll(entityId, queries);
            ListenableFuture<List<DsKvEntry>> transformedKvEntries = Futures.transform(future , (Function<List<DsKvEntry>, List<DsKvEntry>>) entries -> convertKvEntriesToUnitSystem(entries, BasicDsKvEntry.class));
            Futures.addCallback(transformedKvEntries, getCallback(callback, v -> v), executor);
        }));
    }

    @Override
    public void loadLatestTimeseries(final EntityId entityId, final PluginCallback<List<TsKvEntry>> callback) {
        validate(entityId, new ValidationCallback(callback, ctx -> {
            ListenableFuture<List<TsKvEntry>> future = pluginCtx.tsService.findAllLatest(entityId);
            ListenableFuture<List<TsKvEntry>> transformedKvEntries = Futures.transform(future , (Function<List<TsKvEntry>, List<TsKvEntry>>) entries -> convertKvEntriesToUnitSystem(entries, BasicTsKvEntry.class));
            Futures.addCallback(transformedKvEntries, getCallback(callback, v -> v), executor);
        }));
    }

    @Override
    public void logAttributesUpdated(PluginApiCallSecurityContext ctx, EntityId entityId, String attributeType,
                                     List<AttributeKvEntry> attributes, Exception e) {
        pluginCtx.auditLogService.logEntityAction(
                ctx.getTenantId(),
                ctx.getCustomerId(),
                ctx.getUserId(),
                ctx.getUserName(),
                (UUIDBased & EntityId)entityId,
                null,
                ActionType.ATTRIBUTES_UPDATED,
                e,
                attributeType,
                attributes);
    }

    @Override
    public void logAttributesDeleted(PluginApiCallSecurityContext ctx, EntityId entityId, String attributeType, List<String> keys, Exception e) {
        pluginCtx.auditLogService.logEntityAction(
                ctx.getTenantId(),
                ctx.getCustomerId(),
                ctx.getUserId(),
                ctx.getUserName(),
                (UUIDBased & EntityId)entityId,
                null,
                ActionType.ATTRIBUTES_DELETED,
                e,
                attributeType,
                keys);
    }

    @Override
    public void logAttributesRead(PluginApiCallSecurityContext ctx, EntityId entityId, String attributeType, List<String> keys, Exception e) {
        pluginCtx.auditLogService.logEntityAction(
                ctx.getTenantId(),
                ctx.getCustomerId(),
                ctx.getUserId(),
                ctx.getUserName(),
                (UUIDBased & EntityId)entityId,
                null,
                ActionType.ATTRIBUTES_READ,
                e,
                attributeType,
                keys);
    }

    @Override
    public void loadLatestTimeseries(final EntityId entityId, final Collection<String> keys, final PluginCallback<List<TsKvEntry>> callback) {
        validate(entityId, new ValidationCallback(callback, ctx -> {
            ListenableFuture<List<TsKvEntry>> rsListFuture = pluginCtx.tsService.findLatest(entityId, keys);
            ListenableFuture<List<TsKvEntry>> transformedKvEntries = Futures.transform(rsListFuture , (Function<List<TsKvEntry>, List<TsKvEntry>>) entries -> convertKvEntriesToUnitSystem(entries, BasicTsKvEntry.class));
            Futures.addCallback(transformedKvEntries, getCallback(callback, v -> v), executor);
        }));
    }

    @Override
    public void loadLatestTimeseries(final EntityId entityId, final List<KvEntry> kvEntries, final PluginCallback<List<TsKvEntry>> callback) {
        Set<String> keys = new HashSet<>();
        for (KvEntry kv : kvEntries) {
            keys.add(kv.getKey());
        }
        validate(entityId, new ValidationCallback(callback, ctx -> {
            ListenableFuture<List<TsKvEntry>> rsListFuture = pluginCtx.tsService.findLatest(entityId, keys);
            ListenableFuture<List<TsKvEntry>> transformedKvEntries = Futures.transform(rsListFuture , (Function<List<TsKvEntry>, List<TsKvEntry>>) entries -> convertKvEntriesToUnitSystem(entries, BasicTsKvEntry.class));
            Futures.addCallback(transformedKvEntries, getCallback(callback, v -> v), executor);
        }));
    }

    @Override
    public void loadLatestDepthSeries(final EntityId entityId, final PluginCallback<List<DsKvEntry>> callback) {
        validate(entityId, new ValidationCallback(callback, ctx -> {
            ListenableFuture<List<DsKvEntry>> future = pluginCtx.dsService.findAllLatest(entityId);
            ListenableFuture<List<DsKvEntry>> transformedKvEntries = Futures.transform(future , (Function<List<DsKvEntry>, List<DsKvEntry>>) entries -> convertKvEntriesToUnitSystem(entries, BasicDsKvEntry.class));
            Futures.addCallback(transformedKvEntries, getCallback(callback, v -> v), executor);
        }));
    }

    @Override
    public void loadLatestDepthSeries(final EntityId entityId, final Collection<String> keys, final PluginCallback<List<DsKvEntry>> callback) {
        validate(entityId, new ValidationCallback(callback, ctx -> {
            ListenableFuture<List<DsKvEntry>> rsListFuture = pluginCtx.dsService.findLatest(entityId, keys);
            ListenableFuture<List<DsKvEntry>> transformedKvEntries = Futures.transform(rsListFuture , (Function<List<DsKvEntry>, List<DsKvEntry>>) entries -> convertKvEntriesToUnitSystem(entries, BasicDsKvEntry.class));
            Futures.addCallback(transformedKvEntries, getCallback(callback, v -> v), executor);
        }));
    }

    @Override
    public void loadLatestDepthSeries(final EntityId entityId, final List<KvEntry> kvEntries, final PluginCallback<List<DsKvEntry>> callback) {
        Set<String> keys = new HashSet<>();
        for (KvEntry kv : kvEntries) {
            keys.add(kv.getKey());
        }
        validate(entityId, new ValidationCallback(callback, ctx -> {
            ListenableFuture<List<DsKvEntry>> rsListFuture = pluginCtx.dsService.findLatest(entityId, keys);
            ListenableFuture<List<DsKvEntry>> transformedKvEntries = Futures.transform(rsListFuture , (Function<List<DsKvEntry>, List<DsKvEntry>>) entries -> convertKvEntriesToUnitSystem(entries, BasicDsKvEntry.class));
            Futures.addCallback(transformedKvEntries, getCallback(callback, v -> v), executor);
        }));
    }


    @Override
    public void reply(PluginToRuleMsg<?> msg) {
        pluginCtx.parentActor.tell(msg, ActorRef.noSender());
    }

    @Override
    public PluginId getPluginId() {
        return pluginCtx.pluginId;
    }

    @Override
    public Optional<PluginApiCallSecurityContext> getSecurityCtx() {
        return securityCtx;
    }

    private void onDeviceAttributesDeleted(TenantId tenantId, DeviceId deviceId, Set<AttributeKey> keys) {
        pluginCtx.toDeviceActor(DeviceAttributesEventNotificationMsg.onDelete(tenantId, deviceId, keys));
    }

    private void onDeviceAttributesChanged(TenantId tenantId, DeviceId deviceId, String scope, List<AttributeKvEntry> values) {
        pluginCtx.toDeviceActor(DeviceAttributesEventNotificationMsg.onUpdate(tenantId, deviceId, scope, values));
    }

    private void onDeviceTelemetryChanged(TenantId tenantId, DeviceId deviceId, List<TsKvEntry> values) {
        pluginCtx.toDeviceActor(DeviceTelemetryEventNotificationMsg.onUpdate(tenantId, deviceId, values));
    }

    private <T, R> FutureCallback<List<T>> getListCallback(final PluginCallback<R> callback, Function<List<T>, R> transformer) {
        return new FutureCallback<List<T>>() {
            @Override
            public void onSuccess(@Nullable List<T> result) {
                pluginCtx.self().tell(PluginCallbackMessage.onSuccess(callback, transformer.apply(result)), ActorRef.noSender());
            }

            @Override
            public void onFailure(Throwable t) {
                if (t instanceof Exception) {
                    pluginCtx.self().tell(PluginCallbackMessage.onError(callback, (Exception) t), ActorRef.noSender());
                } else {
                    log.error("Critical error: {}", t.getMessage(), t);
                }
            }
        };
    }

    private <T, R> FutureCallback<R> getCallback(final PluginCallback<T> callback, Function<R, T> transformer) {
        return new FutureCallback<R>() {
            @Override
            public void onSuccess(@Nullable R result) {
                try {
                    pluginCtx.self().tell(PluginCallbackMessage.onSuccess(callback, transformer.apply(result)), ActorRef.noSender());
                } catch (Exception e) {
                    pluginCtx.self().tell(PluginCallbackMessage.onError(callback, e), ActorRef.noSender());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                if (t instanceof Exception) {
                    pluginCtx.self().tell(PluginCallbackMessage.onError(callback, (Exception) t), ActorRef.noSender());
                } else {
                    log.error("Critical error: {}", t.getMessage(), t);
                }
            }
        };
    }

    @Override
    public void checkAccess(DeviceId deviceId, PluginCallback<Void> callback) {
        validate(deviceId, new ValidationCallback(callback, ctx -> callback.onSuccess(ctx, null)));
    }

    private void validate(EntityId entityId, ValidationCallback callback) {
        if (securityCtx.isPresent()) {
            final PluginApiCallSecurityContext ctx = securityCtx.get();
            switch (entityId.getEntityType()) {
                case DEVICE:
                    validateDevice(ctx, entityId, callback);
                    return;
                case ASSET:
                    validateAsset(ctx, entityId, callback);
                    return;
                case RULE:
                    validateRule(ctx, entityId, callback);
                    return;
                case PLUGIN:
                    validatePlugin(ctx, entityId, callback);
                    return;
                case CUSTOMER:
                    validateCustomer(ctx, entityId, callback);
                    return;
                case TENANT:
                    validateTenant(ctx, entityId, callback);
                    return;
                default:
                    //TODO: add support of other entities
                    throw new IllegalStateException("Not Implemented!");
            }
        } else {
            callback.onSuccess(this, ValidationResult.ok());
        }
    }

    private void validateDevice(final PluginApiCallSecurityContext ctx, EntityId entityId, ValidationCallback callback) {
        if (ctx.isSystemAdmin()) {
            callback.onSuccess(this, ValidationResult.accessDenied(SYSTEM_ADMINISTRATOR_IS_NOT_ALLOWED_TO_PERFORM_THIS_OPERATION));
        } else {
            ListenableFuture<Device> deviceFuture = pluginCtx.deviceService.findDeviceByIdAsync(new DeviceId(entityId.getId()));
            Futures.addCallback(deviceFuture, getCallback(callback, device -> {
                if (device == null) {
                    return ValidationResult.entityNotFound(DEVICE_WITH_REQUESTED_ID_NOT_FOUND);
                } else {
                    if (!device.getTenantId().equals(ctx.getTenantId())) {
                        return ValidationResult.accessDenied("Device doesn't belong to the current Tenant!");
                    } else if (ctx.isCustomerUser() && !device.getCustomerId().equals(ctx.getCustomerId())) {
                        return ValidationResult.accessDenied("Device doesn't belong to the current Customer!");
                    } else {
                        return ValidationResult.ok();
                    }
                }
            }));
        }
    }

    private void validateAsset(final PluginApiCallSecurityContext ctx, EntityId entityId, ValidationCallback callback) {
        if (ctx.isSystemAdmin()) {
            callback.onSuccess(this, ValidationResult.accessDenied(SYSTEM_ADMINISTRATOR_IS_NOT_ALLOWED_TO_PERFORM_THIS_OPERATION));
        } else {
            ListenableFuture<Asset> assetFuture = pluginCtx.assetService.findAssetByIdAsync(new AssetId(entityId.getId()));
            Futures.addCallback(assetFuture, getCallback(callback, asset -> {
                if (asset == null) {
                    return ValidationResult.entityNotFound("Asset with requested id wasn't found!");
                } else {
                    if (!asset.getTenantId().equals(ctx.getTenantId())) {
                        return ValidationResult.accessDenied("Asset doesn't belong to the current Tenant!");
                    } else if (ctx.isCustomerUser() && !asset.getCustomerId().equals(ctx.getCustomerId())) {
                        return ValidationResult.accessDenied("Asset doesn't belong to the current Customer!");
                    } else {
                        return ValidationResult.ok();
                    }
                }
            }));
        }
    }

    private void validateRule(final PluginApiCallSecurityContext ctx, EntityId entityId, ValidationCallback callback) {
        if (ctx.isCustomerUser()) {
            callback.onSuccess(this, ValidationResult.accessDenied(CUSTOMER_USER_IS_NOT_ALLOWED_TO_PERFORM_THIS_OPERATION));
        } else {
            ListenableFuture<RuleMetaData> ruleFuture = pluginCtx.ruleService.findRuleByIdAsync(new RuleId(entityId.getId()));
            Futures.addCallback(ruleFuture, getCallback(callback, rule -> {
                if (rule == null) {
                    return ValidationResult.entityNotFound("Rule with requested id wasn't found!");
                } else {
                    if (ctx.isTenantAdmin() && !rule.getTenantId().equals(ctx.getTenantId())) {
                        return ValidationResult.accessDenied("Rule doesn't belong to the current Tenant!");
                    } else if (ctx.isSystemAdmin() && !rule.getTenantId().isNullUid()) {
                        return ValidationResult.accessDenied("Rule is not in system scope!");
                    } else {
                        return ValidationResult.ok();
                    }
                }
            }));
        }
    }

    private void validatePlugin(final PluginApiCallSecurityContext ctx, EntityId entityId, ValidationCallback callback) {
        if (ctx.isCustomerUser()) {
            callback.onSuccess(this, ValidationResult.accessDenied(CUSTOMER_USER_IS_NOT_ALLOWED_TO_PERFORM_THIS_OPERATION));
        } else {
            ListenableFuture<PluginMetaData> pluginFuture = pluginCtx.pluginService.findPluginByIdAsync(new PluginId(entityId.getId()));
            Futures.addCallback(pluginFuture, getCallback(callback, plugin -> {
                if (plugin == null) {
                    return ValidationResult.entityNotFound("Plugin with requested id wasn't found!");
                } else {
                    if (ctx.isTenantAdmin() && !plugin.getTenantId().equals(ctx.getTenantId())) {
                        return ValidationResult.accessDenied("Plugin doesn't belong to the current Tenant!");
                    } else if (ctx.isSystemAdmin() && !plugin.getTenantId().isNullUid()) {
                        return ValidationResult.accessDenied("Plugin is not in system scope!");
                    } else {
                        return ValidationResult.ok();
                    }
                }
            }));
        }
    }

    private void validateCustomer(final PluginApiCallSecurityContext ctx, EntityId entityId, ValidationCallback callback) {
        if (ctx.isSystemAdmin()) {
            callback.onSuccess(this, ValidationResult.accessDenied(SYSTEM_ADMINISTRATOR_IS_NOT_ALLOWED_TO_PERFORM_THIS_OPERATION));
        } else {
            ListenableFuture<Customer> customerFuture = pluginCtx.customerService.findCustomerByIdAsync(new CustomerId(entityId.getId()));
            Futures.addCallback(customerFuture, getCallback(callback, customer -> {
                if (customer == null) {
                    return ValidationResult.entityNotFound("Customer with requested id wasn't found!");
                } else {
                    if (!customer.getTenantId().equals(ctx.getTenantId())) {
                        return ValidationResult.accessDenied("Customer doesn't belong to the current Tenant!");
                    } else if (ctx.isCustomerUser() && !customer.getId().equals(ctx.getCustomerId())) {
                        return ValidationResult.accessDenied("Customer doesn't relate to the currently authorized customer user!");
                    } else {
                        return ValidationResult.ok();
                    }
                }
            }));
        }
    }

    private void validateTenant(final PluginApiCallSecurityContext ctx, EntityId entityId, ValidationCallback callback) {
        if (ctx.isCustomerUser()) {
            callback.onSuccess(this, ValidationResult.accessDenied(CUSTOMER_USER_IS_NOT_ALLOWED_TO_PERFORM_THIS_OPERATION));
        } else if (ctx.isSystemAdmin()) {
            callback.onSuccess(this, ValidationResult.ok());
        } else {
            ListenableFuture<Tenant> tenantFuture = pluginCtx.tenantService.findTenantByIdAsync(new TenantId(entityId.getId()));
            Futures.addCallback(tenantFuture, getCallback(callback, tenant -> {
                if (tenant == null) {
                    return ValidationResult.entityNotFound("Tenant with requested id wasn't found!");
                } else if (!tenant.getId().equals(ctx.getTenantId())) {
                    return ValidationResult.accessDenied("Tenant doesn't relate to the currently authorized user!");
                } else {
                    return ValidationResult.ok();
                }
            }));
        }
    }

    @Override
    public ListenableFuture<List<EntityRelation>> findByFromAndType(EntityId from, String relationType) {
        return this.pluginCtx.relationService.findByFromAndTypeAsync(from, relationType, RelationTypeGroup.COMMON);
    }

    @Override
    public ListenableFuture<List<EntityRelation>> findByToAndType(EntityId from, String relationType) {
        return this.pluginCtx.relationService.findByToAndTypeAsync(from, relationType, RelationTypeGroup.COMMON);
    }

    @Override
    public Optional<ServerAddress> resolve(EntityId entityId) {
        return pluginCtx.routingService.resolveById(entityId);
    }

    @Override
    public void getDevice(DeviceId deviceId, PluginCallback<Device> callback) {
        ListenableFuture<Device> deviceFuture = pluginCtx.deviceService.findDeviceByIdAsync(deviceId);
        Futures.addCallback(deviceFuture, getCallback(callback, v -> v));
    }

    @Override
    public void getCustomerDevices(TenantId tenantId, CustomerId customerId, int limit, PluginCallback<List<Device>> callback) {
        //TODO: add caching here with async api.
        List<Device> devices = pluginCtx.deviceService.findDevicesByTenantIdAndCustomerId(tenantId, customerId, new TextPageLink(limit)).getData();
        pluginCtx.self().tell(PluginCallbackMessage.onSuccess(callback, devices), ActorRef.noSender());
    }

    @Override
    public void sendRpcRequest(ToDeviceRpcRequest msg) {
        pluginCtx.sendRpcRequest(msg);
    }

    @Override
    public void logRpcRequest(PluginApiCallSecurityContext ctx, DeviceId deviceId, ToDeviceRpcRequestBody body, boolean oneWay, Optional<RpcError> rpcError, Exception e) {
        String rpcErrorStr = "";
        if (rpcError.isPresent()) {
            rpcErrorStr = "RPC Error: " + rpcError.get().name();
        }
        String method = body.getMethod();
        String params = body.getParams();
        pluginCtx.auditLogService.logEntityAction(
                ctx.getTenantId(),
                ctx.getCustomerId(),
                ctx.getUserId(),
                ctx.getUserName(),
                deviceId,
                null,
                ActionType.RPC_CALL,
                e,
                rpcErrorStr,
                Boolean.valueOf(oneWay),
                method,
                params);
    }

    @Override
    public void scheduleTimeoutMsg(TimeoutMsg msg) {
        pluginCtx.scheduleTimeoutMsg(msg);
    }


    private void convertFuturesAndAddCallback(PluginCallback<List<AttributeKvEntry>> callback, List<ListenableFuture<List<AttributeKvEntry>>> futures) {
        ListenableFuture<List<AttributeKvEntry>> future = Futures.transform(Futures.successfulAsList(futures),
                (Function<? super List<List<AttributeKvEntry>>, ? extends List<AttributeKvEntry>>) input -> {
                    List<AttributeKvEntry> result = new ArrayList<>();
                    input.forEach(result::addAll);
                    return result;
                }, executor);
        Futures.addCallback(future, getCallback(callback, v -> v), executor);
    }

    private <E extends KvEntry, T extends E> List<E> convertKvEntriesToUnitSystem(List<E> kvEntries, Class<T> tClass) {
        if (securityCtx.isPresent() && (securityCtx.get().getTenantId() != null) ) {
            return convertKvEntriesToUnitSystemByTenantId(kvEntries , securityCtx.get().getTenantId(), tClass);
        } else {
            return convertKvEntriesToUnitSystemByTenantId(kvEntries , nullTenantId, tClass);
        }
    }

    @Override
    public <E extends KvEntry, T extends E> List<E> convertKvEntriesToUnitSystemByTenantId(List<E> kvEntries , TenantId tenantId , Class<T> tClass) {
        List<E> unitSystemKvEntries = new ArrayList<>();
        String unitSystem = getUnitSystem(tenantId);
        for (E kvEntry:  kvEntries) {
            if (checkDataTypeAndIsUnitPresent(kvEntry)) {
                Double value = getDoubleValue(kvEntry);
                DoubleDataEntry dataEntry = convert(unitSystem, kvEntry.getKey(), value, kvEntry.getUnit().orElse(null), kvEntry.getSourceUnit().orElse(null));
                if (tClass.isAssignableFrom(BasicDsKvEntry.class)) {
                    unitSystemKvEntries.add((E)new BasicDsKvEntry(((DsKvEntry)kvEntry).getDs(), dataEntry));
                } else if (tClass.isAssignableFrom(BasicTsKvEntry.class)) {
                    unitSystemKvEntries.add((E)new BasicTsKvEntry(((TsKvEntry) kvEntry).getTs() , dataEntry));
                } else if (tClass.isAssignableFrom(BaseAttributeKvEntry.class)) {
                    unitSystemKvEntries.add((E)new BaseAttributeKvEntry(dataEntry, ((AttributeKvEntry)kvEntry).getLastUpdateTs()));
                }
            } else {
                unitSystemKvEntries.add(kvEntry);
            }
        }
        return unitSystemKvEntries;
    }

    private Double getDoubleValue(KvEntry kvEntry) {
        if (kvEntry.getDataType() == DataType.LONG) {
            return ((Long)(kvEntry.getValue())).doubleValue();
        } else {
            return (Double) kvEntry.getValue();
        }
    }

    private boolean checkDataTypeAndIsUnitPresent(KvEntry kvEntry) {
        return ((kvEntry.getDataType() == DataType.DOUBLE || kvEntry.getDataType() == DataType.LONG) && kvEntry.getUnit().isPresent());
    }

    private DoubleDataEntry convert(String unitSystem, String key, Double value, String unit, String sourceUnit) {
        String targetUnit = pluginCtx.unitConversionService.getUnitFor(unitSystem, sourceUnit);
        Quantity quantity = pluginCtx.unitConversionService.convertToTargetUnit(new Quantity(value, unit), targetUnit);
        return new DoubleDataEntry(key, quantity.getUnit(), unit, quantity.getValue());
    }

    private String getUnitSystem(TenantId tenantId) {
        String unitSystemJson = pluginCtx.tenantService.findUnitSystemByTenantId(tenantId);
        System.out.println("unitSystemJson : " + unitSystemJson);
        JsonObject unitSytemJsonObj = new JsonParser().parse(unitSystemJson).getAsJsonObject();
        return unitSytemJsonObj.get(UNIT_SYSTEM_KEY).getAsString().toUpperCase();
    }
}
