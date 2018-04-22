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

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.hashmapinc.server.common.data.EntityType;
import com.hashmapinc.server.common.data.id.DeviceId;
import com.hashmapinc.server.common.data.id.EntityId;
import com.hashmapinc.server.common.data.id.EntityIdFactory;
import com.hashmapinc.server.common.data.kv.*;
import com.hashmapinc.server.common.msg.core.TelemetryUploadRequest;
import com.hashmapinc.server.common.transport.adaptor.JsonConverter;
import com.hashmapinc.server.extensions.api.exception.InvalidParametersException;
import com.hashmapinc.server.extensions.api.exception.ToErrorResponseEntity;
import com.hashmapinc.server.extensions.api.exception.UncheckedApiException;
import com.hashmapinc.server.extensions.api.plugins.PluginContext;
import com.hashmapinc.server.extensions.api.plugins.handlers.DefaultRestMsgHandler;
import com.hashmapinc.server.extensions.api.plugins.rest.PluginRestMsg;
import com.hashmapinc.server.extensions.api.plugins.rest.RestRequest;
import com.hashmapinc.server.extensions.core.plugin.telemetry.AttributeData;
import com.hashmapinc.server.extensions.core.plugin.telemetry.DsData;
import com.hashmapinc.server.extensions.core.plugin.telemetry.SubscriptionManager;
import com.hashmapinc.server.extensions.core.plugin.telemetry.TsData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import com.hashmapinc.server.common.data.DataConstants;
import com.hashmapinc.server.common.data.kv.*;
import com.hashmapinc.server.extensions.api.plugins.PluginCallback;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class TelemetryRestMsgHandler extends DefaultRestMsgHandler {

    private final SubscriptionManager subscriptionManager;

    public TelemetryRestMsgHandler(SubscriptionManager subscriptionManager) {
        this.subscriptionManager = subscriptionManager;
    }

    @Override
    public void handleHttpGetRequest(PluginContext ctx, PluginRestMsg msg) throws ServletException {
        RestRequest request = msg.getRequest();
        String[] pathParams = request.getPathParams();
        if (pathParams.length < 4) {
            msg.getResponseHolder().setResult(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
            return;
        }

        String entityType = pathParams[0];
        String entityIdStr = pathParams[1];
        String method = pathParams[2];
        TelemetryFeature feature = TelemetryFeature.forName(pathParams[3]);
        String scope = pathParams.length >= 5 ? pathParams[4] : null;
        if (StringUtils.isEmpty(entityType) || EntityType.valueOf(entityType) == null || StringUtils.isEmpty(entityIdStr) || StringUtils.isEmpty(method) || StringUtils.isEmpty(feature)) {
            msg.getResponseHolder().setResult(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
            return;
        }

        EntityId entityId = EntityIdFactory.getByTypeAndId(entityType, entityIdStr);

        if (method.equals("keys")) {
            handleHttpGetKeysMethod(ctx, msg, feature, scope, entityId);
        } else if (method.equals("values")) {

            handleHttpGetValuesMethod(ctx, msg, request, feature, scope, entityId);
            /*if (feature == TelemetryFeature.TIMESERIES) {
                log.error("HMDC inside telemetry values ");
                String keysStr = request.getParameter("keys");
                List<String> keys = Arrays.asList(keysStr.split(","));

                Optional<Long> startTs = request.getLongParamValue("startTs");
                Optional<Long> endTs = request.getLongParamValue("endTs");
                Optional<Long> interval = request.getLongParamValue("interval");
                Optional<Integer> limit = request.getIntParamValue("limit");

                if (startTs.isPresent() || endTs.isPresent() || interval.isPresent() || limit.isPresent()) {
                    if (!startTs.isPresent() || !endTs.isPresent() || !interval.isPresent()) {
                        msg.getResponseHolder().setResult(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
                        return;
                    }
                    Aggregation agg = Aggregation.valueOf(request.getParameter("agg", Aggregation.NONE.name()));

                    List<TsKvQuery> queries = keys.stream().map(key -> new BaseTsKvQuery(key, startTs.get(), endTs.get(), interval.get(), limit.orElse(TelemetryWebsocketMsgHandler.DEFAULT_LIMIT), agg))
                            .collect(Collectors.toList());
                    ctx.loadTimeseries(entityId, queries, getTsKvListCallback(msg));
                } else {
                    log.error("HMDC inside telemetry values ");
                    ctx.loadLatestTimeseries(entityId, keys, getTsKvListCallback(msg));
                }
            } else if(feature == TelemetryFeature.DEPTHSERIES){
                String keysStr = request.getParameter("keys");
                List<String> keys = Arrays.asList(keysStr.split(","));

                Optional<Double> startDs = request.getDoubleParamValue("startDs");
                Optional<Double> endDs = request.getDoubleParamValue("endDs");
                Optional<Double> interval = request.getDoubleParamValue("interval");
                Optional<Integer> limit = request.getIntParamValue("limit");

                if (startDs.isPresent() || endDs.isPresent() || interval.isPresent() || limit.isPresent()) {
                    if (!startDs.isPresent() || !endDs.isPresent() || !interval.isPresent()) {
                        msg.getResponseHolder().setResult(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
                        return;
                    }
                    DepthAggregation agg = DepthAggregation.valueOf(request.getParameter("agg", Aggregation.NONE.name()));

                    List<DsKvQuery> queries = keys.stream().map(key -> new BaseDsKvQuery(key, startDs.get(), endDs.get(), interval.get(), limit.orElse(TelemetryWebsocketMsgHandler.DEFAULT_LIMIT), agg))
                            .collect(Collectors.toList());
                    ctx.loadDepthSeries(entityId, queries, getDsKvListCallback(msg));
                } else {

                    ctx.loadLatestDepthSeries(entityId, keys, getDsKvListCallback(msg));
                }
            }

            else if (feature == TelemetryFeature.ATTRIBUTES) {
                String keys = request.getParameter("keys", "");
                log.debug("HMDC inside attriibute values ");

                PluginCallback<List<AttributeKvEntry>> callback = getAttributeKeysPluginCallback(msg);
                if (!StringUtils.isEmpty(scope)) {
                    if (!StringUtils.isEmpty(keys)) {
                        List<String> keyList = Arrays.asList(keys.split(","));
                        ctx.loadAttributes(entityId, scope, keyList, callback);
                    } else {
                        ctx.loadAttributes(entityId, scope, callback);
                    }
                } else {
                    if (!StringUtils.isEmpty(keys)) {
                        List<String> keyList = Arrays.asList(keys.split(","));
                        ctx.loadAttributes(entityId, Arrays.asList(DataConstants.allScopes()), keyList, callback);
                    } else {
                        ctx.loadAttributes(entityId, Arrays.asList(DataConstants.allScopes()), callback);
                    }
                }
            }*/
        }
           /* else if (feature == TelemetryFeature.ATTRIBUTES) {
                String keys = request.getParameter("keys", "");
                log.error("HMDC inside attriibute values ");

                PluginCallback<List<AttributeKvEntry>> callback = getAttributeValuesPluginCallback(msg);
                if (!StringUtils.isEmpty(scope)) {
                    if (!StringUtils.isEmpty(keys)) {
                        List<String> keyList = Arrays.asList(keys.split(","));
                        ctx.loadAttributes(entityId, scope, keyList, callback);
                    } else {
                        ctx.loadAttributes(entityId, scope, callback);
                    }
                } else {
                    if (!StringUtils.isEmpty(keys)) {
                        List<String> keyList = Arrays.asList(keys.split(","));
                        ctx.loadAttributes(entityId, Arrays.asList(DataConstants.allScopes()), keyList, callback);
                    } else {
                        ctx.loadAttributes(entityId, Arrays.asList(DataConstants.allScopes()), callback);
                    }
                }
            });
        } else if (feature == TelemetryFeature.ATTRIBUTES) {
            PluginCallback<List<AttributeKvEntry>> callback = getAttributeKeysPluginCallback(msg);
            if (!StringUtils.isEmpty(scope)) {
                ctx.loadAttributes(entityId, scope, callback);
            } else {
                ctx.loadAttributes(entityId, Arrays.asList(DataConstants.allScopes()), callback);
            }
        }*/
    }

    private void handleHttpGetKeysMethod(PluginContext ctx, PluginRestMsg msg, TelemetryFeature feature, String scope, EntityId entityId) {


        if (feature == TelemetryFeature.TIMESERIES) {
            log.debug("HMDC inside telemetry keys ");
            ctx.loadLatestTimeseries(entityId, new PluginCallback<List<TsKvEntry>>() {
                @Override
                public void onSuccess(PluginContext ctx, List<TsKvEntry> value) {
                    List<String> keys = value.stream().map(tsKv -> tsKv.getKey()).collect(Collectors.toList());
                    msg.getResponseHolder().setResult(new ResponseEntity<>(keys, HttpStatus.OK));
                }

                @Override
                public void onFailure(PluginContext ctx, Exception e) {
                    msg.getResponseHolder().setResult(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
                }
            });
        } else if(feature == TelemetryFeature.DEPTHSERIES){
            ctx.loadLatestDepthSeries(entityId, new PluginCallback<List<DsKvEntry>>() {
                @Override
                public void onSuccess(PluginContext ctx, List<DsKvEntry> value) {
                    List<String> keys = value.stream().map(dsKv -> dsKv.getKey()).collect(Collectors.toList());
                    msg.getResponseHolder().setResult(new ResponseEntity<>(keys, HttpStatus.OK));
                }

                @Override
                public void onFailure(PluginContext ctx, Exception e) {
                    msg.getResponseHolder().setResult(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
                }
            });
        }
        else if (feature == TelemetryFeature.ATTRIBUTES) {
            log.debug("HMDC inside Attributes keys ");
            PluginCallback<List<AttributeKvEntry>> callback = getAttributeKeysPluginCallback(msg);
            if (!StringUtils.isEmpty(scope)) {
                ctx.loadAttributes(entityId, scope, callback);
            } else {
                ctx.loadAttributes(entityId, Arrays.asList(DataConstants.allScopes()), callback);
            }
        }
    }

    private void handleHttpGetValuesMethod(PluginContext ctx, PluginRestMsg msg,
                                           RestRequest request, TelemetryFeature feature,
                                           String scope, EntityId entityId) throws ServletException {

        if (feature == TelemetryFeature.TIMESERIES) {
            log.debug("HMDC inside telemetry values ");
            String keysStr = request.getParameter("keys");
            List<String> keys = Arrays.asList(keysStr.split(","));

            Optional<Long> startTs = request.getLongParamValue("startTs");
            Optional<Long> endTs = request.getLongParamValue("endTs");
            Optional<Long> interval = request.getLongParamValue("interval");
            Optional<Integer> limit = request.getIntParamValue("limit");

            if (startTs.isPresent() || endTs.isPresent() || interval.isPresent() || limit.isPresent()) {
                if (!startTs.isPresent() || !endTs.isPresent() || !interval.isPresent()) {
                    msg.getResponseHolder().setResult(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
                    return;
                }
                Aggregation agg = Aggregation.valueOf(request.getParameter("agg", Aggregation.NONE.name()));

                List<TsKvQuery> queries = keys.stream().map(key -> new BaseTsKvQuery(key, startTs.get(), endTs.get(), interval.get(), limit.orElse(TelemetryWebsocketMsgHandler.DEFAULT_LIMIT), agg))
                        .collect(Collectors.toList());
                ctx.loadTimeseries(entityId, queries, getTsKvListCallback(msg));
            } else {
                log.debug("HMDC inside telemetry values ");
                ctx.loadLatestTimeseries(entityId, keys, getTsKvListCallback(msg));
            }
        } else if(feature == TelemetryFeature.DEPTHSERIES){
            String keysStr = request.getParameter("keys");
            List<String> keys = Arrays.asList(keysStr.split(","));

            Optional<Double> startDs = request.getDoubleParamValue("startDs");
            Optional<Double> endDs = request.getDoubleParamValue("endDs");
            Optional<Double> interval = request.getDoubleParamValue("interval");
            Optional<Integer> limit = request.getIntParamValue("limit");

            if (startDs.isPresent() || endDs.isPresent() || interval.isPresent() || limit.isPresent()) {
                if (!startDs.isPresent() || !endDs.isPresent() || !interval.isPresent()) {
                    msg.getResponseHolder().setResult(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
                    return;
                }
                DepthAggregation agg = DepthAggregation.valueOf(request.getParameter("agg", Aggregation.NONE.name()));

                List<DsKvQuery> queries = keys.stream().map(key -> new BaseDsKvQuery(key, startDs.get(), endDs.get(), interval.get(), limit.orElse(TelemetryWebsocketMsgHandler.DEFAULT_LIMIT), agg))
                        .collect(Collectors.toList());
                ctx.loadDepthSeries(entityId, queries, getDsKvListCallback(msg));
            } else {

                ctx.loadLatestDepthSeries(entityId, keys, getDsKvListCallback(msg));
            }
        }
        else if (feature == TelemetryFeature.ATTRIBUTES) {
            String keys = request.getParameter("keys", "");
            log.debug("HMDC inside attriibute values ");

            List<String> keyList = null;
            if (!StringUtils.isEmpty(keys)) {
                keyList = Arrays.asList(keys.split(","));
            }

            PluginCallback<List<AttributeKvEntry>> callback = getAttributeValuesPluginCallback(msg, scope, entityId, keyList);
            if (!StringUtils.isEmpty(scope)) {
                if (keyList != null && !keyList.isEmpty()) {
                    ctx.loadAttributes(entityId, scope, keyList, callback);
                } else {
                    ctx.loadAttributes(entityId, scope, callback);
                }
            } else {
                if (keyList != null && !keyList.isEmpty()) {
                    ctx.loadAttributes(entityId, Arrays.asList(DataConstants.allScopes()), keyList, callback);
                } else {
                    ctx.loadAttributes(entityId, Arrays.asList(DataConstants.allScopes()), callback);
                }
            }
        }

    }

    private void handleHttpGetTimeseriesValues(PluginContext ctx, PluginRestMsg msg, RestRequest request, EntityId entityId) throws ServletException {
        String keysStr = request.getParameter("keys");
        List<String> keys = Arrays.asList(keysStr.split(","));

        Optional<Long> startTs = request.getLongParamValue("startTs");
        Optional<Long> endTs = request.getLongParamValue("endTs");
        Optional<Long> interval = request.getLongParamValue("interval");
        Optional<Integer> limit = request.getIntParamValue("limit");

        // If some of these params are specified, they all must be
        if (startTs.isPresent() || endTs.isPresent() || interval.isPresent() || limit.isPresent()) {
            if (!startTs.isPresent() || !endTs.isPresent() || !interval.isPresent() || interval.get() < 0) {
                msg.getResponseHolder().setResult(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
                return;
            }

            // If interval is 0, convert this to a NONE aggregation, which is probably what the user really wanted
            Aggregation agg = (interval.isPresent() && interval.get() == 0) ? Aggregation.valueOf(Aggregation.NONE.name()) :
                    Aggregation.valueOf(request.getParameter("agg", Aggregation.NONE.name()));

            List<TsKvQuery> queries = keys.stream().map(key -> new BaseTsKvQuery(key, startTs.get(), endTs.get(), interval.get(), limit.orElse(TelemetryWebsocketMsgHandler.DEFAULT_LIMIT), agg))
                    .collect(Collectors.toList());
            ctx.loadTimeseries(entityId, queries, getTsKvListCallback(msg));
        } else {
            ctx.loadLatestTimeseries(entityId, keys, getTsKvListCallback(msg));
        }
    }

    private void handleHttpGetAttributesValues(PluginContext ctx, PluginRestMsg msg,
                                               RestRequest request, String scope, EntityId entityId) throws ServletException {
        String keys = request.getParameter("keys", "");
        List<String> keyList = null;
        if (!StringUtils.isEmpty(keys)) {
            keyList = Arrays.asList(keys.split(","));
        }
        PluginCallback<List<AttributeKvEntry>> callback = getAttributeValuesPluginCallback(msg, scope, entityId, keyList);
        if (!StringUtils.isEmpty(scope)) {
            if (keyList != null && !keyList.isEmpty()) {
                ctx.loadAttributes(entityId, scope, keyList, callback);
            } else {
                ctx.loadAttributes(entityId, scope, callback);
            }
        } else {
            if (keyList != null && !keyList.isEmpty()) {
                ctx.loadAttributes(entityId, Arrays.asList(DataConstants.allScopes()), keyList, callback);
            } else {
                ctx.loadAttributes(entityId, Arrays.asList(DataConstants.allScopes()), callback);
            }
        }
    }

    @Override
    public void handleHttpPostRequest(PluginContext ctx, PluginRestMsg msg) throws ServletException {
        RestRequest request = msg.getRequest();
        Exception error = null;
        try {
            String[] pathParams = request.getPathParams();
            EntityId entityId;
            String scope;
            long ttl = 0L;
            TelemetryFeature feature;
            if (pathParams.length == 2) {
                entityId = DeviceId.fromString(pathParams[0]);
                scope = pathParams[1];
                feature = TelemetryFeature.ATTRIBUTES;
            } else if (pathParams.length == 3) {
                entityId = EntityIdFactory.getByTypeAndId(pathParams[0], pathParams[1]);
                scope = pathParams[2];
                feature = TelemetryFeature.ATTRIBUTES;
            } else if (pathParams.length == 4) {
                entityId = EntityIdFactory.getByTypeAndId(pathParams[0], pathParams[1]);
                feature = TelemetryFeature.forName(pathParams[2].toUpperCase());
                scope = pathParams[3];
            } else if (pathParams.length == 5) {
                entityId = EntityIdFactory.getByTypeAndId(pathParams[0], pathParams[1]);
                feature = TelemetryFeature.forName(pathParams[2].toUpperCase());
                scope = pathParams[3];
                ttl = Long.parseLong(pathParams[4]);
            } else {
                msg.getResponseHolder().setResult(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
                return;
            }
            if (feature == TelemetryFeature.ATTRIBUTES) {
                if (handleHttpPostAttributes(ctx, msg, request, entityId, scope)) {
                    return;
                }
            } else if (feature == TelemetryFeature.TIMESERIES) {
                handleHttpPostTimeseries(ctx, msg, request, entityId, ttl);
                return;
            }
        } catch (IOException | RuntimeException e) {
            log.debug("Failed to process POST request due to exception", e);
            error = e;
        }
        handleError(error, msg, HttpStatus.BAD_REQUEST);
    }

    private boolean handleHttpPostAttributes(PluginContext ctx, PluginRestMsg msg, RestRequest request,
                                             EntityId entityId, String scope) throws ServletException, IOException {
        if (DataConstants.SERVER_SCOPE.equals(scope) ||
                DataConstants.SHARED_SCOPE.equals(scope)) {
            JsonNode jsonNode;
            try {
                jsonNode = jsonMapper.readTree(request.getRequestBody());
            } catch (Exception e) {
                throw new IllegalArgumentException("Unable to parse attributes payload: Invalid JSON body!");
            }
            if (jsonNode.isObject()) {
                List<AttributeKvEntry> attributes = extractRequestAttributes(jsonNode);
                if (attributes.isEmpty()) {
                    throw new IllegalArgumentException("No attributes data found in request body!");
                }

                ctx.saveAttributes(ctx.getSecurityCtx().orElseThrow(IllegalArgumentException::new).getTenantId(), entityId, scope, attributes, new PluginCallback<Void>() {
                    @Override
                    public void onSuccess(PluginContext ctx, Void value) {
                        ctx.logAttributesUpdated(msg.getSecurityCtx(), entityId, scope, attributes, null);
                        msg.getResponseHolder().setResult(new ResponseEntity<>(HttpStatus.OK));
                        subscriptionManager.onAttributesUpdateFromServer(ctx, entityId, scope, attributes);
                    }

                    @Override
                    public void onFailure(PluginContext ctx, Exception e) {
                        log.error("Failed to save attributes", e);
                        ctx.logAttributesUpdated(msg.getSecurityCtx(), entityId, scope, attributes, e);
                        handleError(e, msg, HttpStatus.BAD_REQUEST);
                    }
                });
                return true;
            }
        }
        return false;
    }

    private List<AttributeKvEntry> extractRequestAttributes(JsonNode jsonNode) {
        long ts = System.currentTimeMillis();
        List<AttributeKvEntry> attributes = new ArrayList<>();
        jsonNode.fields().forEachRemaining(entry -> {
            String key = entry.getKey();
            JsonNode value = entry.getValue();
            if (entry.getValue().isTextual()) {
                attributes.add(new BaseAttributeKvEntry(new StringDataEntry(key, value.textValue()), ts));
            } else if (entry.getValue().isBoolean()) {
                attributes.add(new BaseAttributeKvEntry(new BooleanDataEntry(key, value.booleanValue()), ts));
            } else if (entry.getValue().isDouble()) {
                attributes.add(new BaseAttributeKvEntry(new DoubleDataEntry(key, value.doubleValue()), ts));
            } else if (entry.getValue().isNumber()) {
                if (entry.getValue().isBigInteger()) {
                    throw new UncheckedApiException(new InvalidParametersException("Big integer values are not supported!"));
                } else {
                    attributes.add(new BaseAttributeKvEntry(new LongDataEntry(key, value.longValue()), ts));
                }
            }
        });
        return  attributes;
    }

    private void handleHttpPostTimeseries(PluginContext ctx, PluginRestMsg msg, RestRequest request, EntityId entityId, long ttl) {
        TelemetryUploadRequest telemetryRequest;
        JsonElement telemetryJson;
        try {
            telemetryJson = new JsonParser().parse(request.getRequestBody());
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to parse timeseries payload: Invalid JSON body!");
        }
        try {
            telemetryRequest = JsonConverter.convertToTelemetry(telemetryJson);
        } catch (JsonSyntaxException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
        List<TsKvEntry> entries = new ArrayList<>();
        for (Map.Entry<Long, List<KvEntry>> entry : telemetryRequest.getData().entrySet()) {
            for (KvEntry kv : entry.getValue()) {
                entries.add(new BasicTsKvEntry(entry.getKey(), kv));
            }
        }
        if (entries.isEmpty()) {
            throw new IllegalArgumentException("No timeseries data found in request body!");
        }
        ctx.saveTsData(entityId, entries, ttl, new PluginCallback<Void>() {
            @Override
            public void onSuccess(PluginContext ctx, Void value) {
                msg.getResponseHolder().setResult(new ResponseEntity<>(HttpStatus.OK));
                subscriptionManager.onTimeseriesUpdateFromServer(ctx, entityId, entries);
            }

            @Override
            public void onFailure(PluginContext ctx, Exception e) {
                log.error("Failed to save attributes", e);
                handleError(e, msg, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        });
    }

    @Override
    public void handleHttpDeleteRequest(PluginContext ctx, PluginRestMsg msg) throws ServletException {
        RestRequest request = msg.getRequest();
        Exception error = null;
        try {
            String[] pathParams = request.getPathParams();
            EntityId entityId;
            String scope;
            if (pathParams.length == 2) {
                entityId = DeviceId.fromString(pathParams[0]);
                scope = pathParams[1];
            } else if (pathParams.length == 3) {
                entityId = EntityIdFactory.getByTypeAndId(pathParams[0], pathParams[1]);
                scope = pathParams[2];
            } else {
                msg.getResponseHolder().setResult(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
                return;
            }

            if (DataConstants.SERVER_SCOPE.equals(scope) ||
                    DataConstants.SHARED_SCOPE.equals(scope) ||
                    DataConstants.CLIENT_SCOPE.equals(scope)) {
                String keysParam = request.getParameter("keys");
                if (!StringUtils.isEmpty(keysParam)) {
                    String[] keys = keysParam.split(",");
                    List<String> keyList = Arrays.asList(keys);
                    ctx.removeAttributes(ctx.getSecurityCtx().orElseThrow(IllegalArgumentException::new).getTenantId(), entityId, scope, keyList, new PluginCallback<Void>() {
                        @Override
                        public void onSuccess(PluginContext ctx, Void value) {
                            ctx.logAttributesDeleted(msg.getSecurityCtx(), entityId, scope, keyList, null);
                            msg.getResponseHolder().setResult(new ResponseEntity<>(HttpStatus.OK));
                        }

                        @Override
                        public void onFailure(PluginContext ctx, Exception e) {
                            log.error("Failed to remove attributes", e);
                            ctx.logAttributesDeleted(msg.getSecurityCtx(), entityId, scope, keyList, e);
                            handleError(e, msg, HttpStatus.INTERNAL_SERVER_ERROR);
                        }
                    });
                    return;
                }
            }
        } catch (RuntimeException e) {
            log.debug("Failed to process DELETE request due to Runtime exception", e);
            error = e;
        }
        handleError(error, msg, HttpStatus.BAD_REQUEST);
    }


    private PluginCallback<List<AttributeKvEntry>> getAttributeKeysPluginCallback(final PluginRestMsg msg) {
        return new PluginCallback<List<AttributeKvEntry>>() {
            @Override
            public void onSuccess(PluginContext ctx, List<AttributeKvEntry> attributes) {
                List<String> keys = attributes.stream().map(KvEntry::getKey).collect(Collectors.toList());
                msg.getResponseHolder().setResult(new ResponseEntity<>(keys, HttpStatus.OK));
            }

            @Override
            public void onFailure(PluginContext ctx, Exception e) {
                log.error("Failed to fetch attributes", e);
                handleError(e, msg, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        };
    }

    private PluginCallback<List<AttributeKvEntry>> getAttributeValuesPluginCallback(final PluginRestMsg msg, final String scope,
                                                                                    final EntityId entityId, final List<String> keyList) {
        return new PluginCallback<List<AttributeKvEntry>>() {
            @Override
            public void onSuccess(PluginContext ctx, List<AttributeKvEntry> attributes) {
                List<AttributeData> values = attributes.stream().map(attribute -> new AttributeData(attribute.getLastUpdateTs(),
                        attribute.getKey(), attribute.getValue())).collect(Collectors.toList());
                ctx.logAttributesRead(msg.getSecurityCtx(), entityId, scope, keyList, null);
                msg.getResponseHolder().setResult(new ResponseEntity<>(values, HttpStatus.OK));
            }

            @Override
            public void onFailure(PluginContext ctx, Exception e) {
                log.error("Failed to fetch attributes", e);
                ctx.logAttributesRead(msg.getSecurityCtx(), entityId, scope, keyList, e);
                handleError(e, msg, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        };
    }


    private PluginCallback<List<TsKvEntry>> getTsKvListCallback(final PluginRestMsg msg) {
        return new PluginCallback<List<TsKvEntry>>() {
            @Override
            public void onSuccess(PluginContext ctx, List<TsKvEntry> data) {
                Map<String, List<TsData>> result = new LinkedHashMap<>();
                for (TsKvEntry entry : data) {
                    List<TsData> vList = result.get(entry.getKey());
                    if (vList == null) {
                        vList = new ArrayList<>();
                        result.put(entry.getKey(), vList);
                    }
                    vList.add(new TsData(entry.getTs(), entry.getValueAsString()));
                }
                msg.getResponseHolder().setResult(new ResponseEntity<>(result, HttpStatus.OK));
            }

            @Override
            public void onFailure(PluginContext ctx, Exception e) {
                log.error("Failed to fetch historical data", e);
                handleError(e, msg, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        };
    }

    private PluginCallback<List<DsKvEntry>> getDsKvListCallback(final PluginRestMsg msg) {
        return new PluginCallback<List<DsKvEntry>>() {
            @Override
            public void onSuccess(PluginContext ctx, List<DsKvEntry> data) {
                Map<String, List<DsData>> result = new LinkedHashMap<>();
                for (DsKvEntry entry : data) {
                    List<DsData> vList = result.get(entry.getKey());
                    if (vList == null) {
                        vList = new ArrayList<>();
                        result.put(entry.getKey(), vList);
                    }
                    vList.add(new DsData(entry.getDs(), entry.getValueAsString()));
                }
                msg.getResponseHolder().setResult(new ResponseEntity<>(result, HttpStatus.OK));
            }

            @Override
            public void onFailure(PluginContext ctx, Exception e) {
                log.error("Failed to fetch historical data", e);
                msg.getResponseHolder().setResult(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
            }
        };
    }
    private void handleError(Exception e, PluginRestMsg msg, HttpStatus defaultErrorStatus) {
        ResponseEntity responseEntity;
        if (e != null && e instanceof ToErrorResponseEntity) {
            responseEntity = ((ToErrorResponseEntity) e).toErrorResponseEntity();
        } else if (e != null && e instanceof IllegalArgumentException) {
            responseEntity = new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } else {
            responseEntity = new ResponseEntity<>(defaultErrorStatus);
        }
        msg.getResponseHolder().setResult(responseEntity);
    }

}
