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
package com.hashmapinc.server.common.transport.adaptor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.google.gson.*;
import com.hashmapinc.server.common.data.kv.*;
import com.hashmapinc.server.common.msg.core.*;
import com.hashmapinc.server.common.msg.kv.AttributesKVMsg;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
public class JsonConverter {

    private static final Gson GSON = new Gson();
    public static final String CAN_T_PARSE_VALUE = "Can't parse value: ";
    public static final String VALUES = "values";
    public static final String VALUE = "value";

    private JsonConverter() {}

    public static TelemetryUploadRequest convertToTelemetry(JsonElement jsonObject) throws JsonSyntaxException {
        return convertToTelemetry(jsonObject, BasicRequest.DEFAULT_REQUEST_ID);
    }

    public static TelemetryUploadRequest convertToTelemetry(JsonElement jsonObject, int requestId) throws JsonSyntaxException {
        BasicTelemetryUploadRequest request = new BasicTelemetryUploadRequest(requestId);
        long systemTs = System.currentTimeMillis();
        if (jsonObject.isJsonObject()) {
            parseObject(request, systemTs, jsonObject);
        } else if (jsonObject.isJsonArray()) {
            jsonObject.getAsJsonArray().forEach(je -> {
                if (je.isJsonObject()) {
                    parseObject(request, systemTs, je.getAsJsonObject());
                } else {
                    throw new JsonSyntaxException(CAN_T_PARSE_VALUE + je);
                }
            });
        } else {
            throw new JsonSyntaxException(CAN_T_PARSE_VALUE + jsonObject);
        }
        return request;
    }

    //  test telemetry depth.
    public static DepthTelemetryUploadRequest convertToTelemetryDepth(JsonElement jsonObject, int requestId) throws JsonSyntaxException {
        BasicDepthTelemetryUploadRequest request = new BasicDepthTelemetryUploadRequest(requestId);
        if (jsonObject.isJsonObject()) {
            parseObject(request, jsonObject);
        }
        else if (jsonObject.isJsonArray()) {
            jsonObject.getAsJsonArray().forEach(je -> {
                if (je.isJsonObject()) {
                    parseObject(request,je.getAsJsonObject());
                } else {
                    throw new JsonSyntaxException(CAN_T_PARSE_VALUE + je);
                }
            });
        } else {
            throw new JsonSyntaxException(CAN_T_PARSE_VALUE + jsonObject);
        }
        return request;
    }

    public static ToServerRpcRequestMsg convertToServerRpcRequest(JsonElement json, int requestId) throws JsonSyntaxException {
        JsonObject object = json.getAsJsonObject();
        return new ToServerRpcRequestMsg(requestId, object.get("method").getAsString(), GSON.toJson(object.get("params")));
    }

    private static void parseObject(BasicTelemetryUploadRequest request, long systemTs, JsonElement jsonObject) {
        JsonObject jo = jsonObject.getAsJsonObject();
        if (jo.has("ts") && jo.has(VALUES)) {
            parseWithTs(request, jo);
        } else {
            parseWithoutTs(request, systemTs, jo);
        }
    }

    //  telemetry ds
    private static void parseObject(BasicDepthTelemetryUploadRequest request, JsonElement jsonObject) {
        JsonObject jo = jsonObject.getAsJsonObject();
        if (jo.has("ds") && jo.has(VALUES)) {
            parseWithDepth(request, jo);
        }
    }

    private static void parseWithoutTs(BasicTelemetryUploadRequest request, long systemTs, JsonObject jo) {
        for (KvEntry entry : parseValues(jo)) {
            request.add(systemTs, entry);
        }
    }

    public static void parseWithTs(BasicTelemetryUploadRequest request, JsonObject jo) {
        long ts = jo.get("ts").getAsLong();
        JsonObject valuesObject = jo.get(VALUES).getAsJsonObject();
        for (KvEntry entry : parseValues(valuesObject)) {
            request.add(ts, entry);
        }
    }

    //  telemetry DS
    public static void parseWithDepth(BasicDepthTelemetryUploadRequest request, JsonObject jo) {
        Double ds = jo.get("ds").getAsDouble();
        JsonObject valuesObject = jo.get(VALUES).getAsJsonObject();
        for (KvEntry entry : parseValues(valuesObject)) {
            request.addDs(ds, entry);
        }
    }

    public static List<KvEntry> parseValues(JsonObject valuesObject) {
        List<KvEntry> result = new ArrayList<>();
        for (Entry<String, JsonElement> valueEntry : valuesObject.entrySet()) {
            JsonElement element = valueEntry.getValue();
            if (element.isJsonPrimitive()) {
                JsonPrimitive value = element.getAsJsonPrimitive();
                createResultForJsonPrimitive(result, valueEntry, value);
            } else if (element.isJsonObject() || element.isJsonArray()) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode value;
                try {
                    value = mapper.readTree(element.toString());
                    if(!isUnitPresentInJson(valueEntry.getKey(), value, result)){
                        result.add(new JsonDataEntry(valueEntry.getKey(), value));
                    }
                } catch (IOException ex) {
                    log.error(ex.getMessage());
                }
            } else {
                throw new JsonSyntaxException(CAN_T_PARSE_VALUE + element);
            }
        }
        return result;
    }

    private static void createResultForJsonPrimitive(List<KvEntry> result, Entry<String, JsonElement> valueEntry, JsonPrimitive value) {
        if (value.isString()) {
            result.add(new StringDataEntry(valueEntry.getKey(), value.getAsString()));
        } else if (value.isBoolean()) {
            result.add(new BooleanDataEntry(valueEntry.getKey(), value.getAsBoolean()));
        } else if (value.isNumber()) {
            parseNumericValue(result, valueEntry, value);
        } else {
            throw new JsonSyntaxException(CAN_T_PARSE_VALUE + value);
        }
    }

    private static boolean isUnitPresentInJson(String key, JsonNode value, List<KvEntry> result){
        boolean unitPresent = false;
        if (value.has(VALUE) && value.has("unit")){
            if(value.get(VALUE).getNodeType() == JsonNodeType.BOOLEAN){
                result.add(new BooleanDataEntry(key, value.get("unit").asText(), value.get(VALUE).asBoolean()));
            }else if(value.get(VALUE).getNodeType() == JsonNodeType.STRING){
                result.add(new StringDataEntry(key, value.get("unit").asText(), value.get(VALUE).asText()));
            }else if(value.get(VALUE).getNodeType() == JsonNodeType.NUMBER){
                parseNumericValue(result, key, value.get("unit").asText(), value.get(VALUE));
            }
            unitPresent = true;
        }
        return unitPresent;
    }

    private static void parseNumericValue(List<KvEntry> result, Entry<String, JsonElement> valueEntry, JsonPrimitive value) {
        if (value.getAsString().contains(".")) {
            result.add(new DoubleDataEntry(valueEntry.getKey(), value.getAsDouble()));
        } else {
            try {
                long longValue = Long.parseLong(value.getAsString());
                result.add(new LongDataEntry(valueEntry.getKey(), longValue));
            } catch (NumberFormatException e) {
                throw new JsonSyntaxException("Big integer values are not supported!");
            }
        }
    }

    private static void parseNumericValue(List<KvEntry> result, String key, String unit, JsonNode value) {
        if (value.asText().contains(".")) {
            result.add(new DoubleDataEntry(key, unit, value.asDouble()));
        } else {
            try {
                long longValue = value.asLong();
                result.add(new LongDataEntry(key, longValue));
            } catch (NumberFormatException e) {
                throw new JsonSyntaxException("Big integer values are not supported!");
            }
        }
    }

    public static UpdateAttributesRequest convertToAttributes(JsonElement element) {
        return convertToAttributes(element, BasicRequest.DEFAULT_REQUEST_ID);
    }

    public static UpdateAttributesRequest convertToAttributes(JsonElement element, int requestId) {
        if (element.isJsonObject()) {
            BasicUpdateAttributesRequest request = new BasicUpdateAttributesRequest(requestId);
            long ts = System.currentTimeMillis();
            request.add(parseValues(element.getAsJsonObject()).stream().map(kv -> new BaseAttributeKvEntry(kv, ts)).collect(Collectors.toList()));
            return request;
        } else {
            throw new JsonSyntaxException(CAN_T_PARSE_VALUE + element);
        }
    }

    public static JsonObject toJson(AttributesKVMsg payload, boolean asMap) {
        JsonObject result = new JsonObject();
        if (asMap) {
            if (!payload.getClientAttributes().isEmpty()) {
                JsonObject attrObject = new JsonObject();
                payload.getClientAttributes().forEach(addToObject(attrObject));
                result.add("client", attrObject);
            }
            if (!payload.getSharedAttributes().isEmpty()) {
                JsonObject attrObject = new JsonObject();
                payload.getSharedAttributes().forEach(addToObject(attrObject));
                result.add("shared", attrObject);
            }
        } else {
            payload.getClientAttributes().forEach(addToObject(result));
            payload.getSharedAttributes().forEach(addToObject(result));
        }
        if (!payload.getDeletedAttributes().isEmpty()) {
            JsonArray attrObject = new JsonArray();
            payload.getDeletedAttributes().forEach(addToObject(attrObject));
            result.add("deleted", attrObject);
        }
        return result;
    }

    private static Consumer<AttributeKey> addToObject(JsonArray result) {
        return key -> result.add(key.getKey());

    }

    private static Consumer<AttributeKvEntry> addToObject(JsonObject result) {
        return de -> {
            JsonElement value;
            switch (de.getDataType()) {
                case BOOLEAN:
                    value = new JsonPrimitive(de.getBooleanValue().get());
                    break;
                case DOUBLE:
                    value = new JsonPrimitive(de.getDoubleValue().get());
                    break;
                case LONG:
                    value = new JsonPrimitive(de.getLongValue().get());
                    break;
                case STRING:
                    value = new JsonPrimitive(de.getStrValue().get());
                    break;
                case JSON:
                    String jsonString = de.getJsonValue().get().toString();
                    JsonParser parser = new JsonParser();
                    value = parser.parse(jsonString);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported data type: " + de.getDataType());
            }
            result.add(de.getKey(), value);
        };
    }

    public static JsonObject toJson(ToDeviceRpcRequestMsg msg, boolean includeRequestId) {
        JsonObject result = new JsonObject();
        if (includeRequestId) {
            result.addProperty("id", msg.getRequestId());
        }
        result.addProperty("method", msg.getMethod());
        result.add("params", new JsonParser().parse(msg.getParams()));
        return result;
    }

    public static JsonElement toJson(ToServerRpcResponseMsg msg) {
        return new JsonParser().parse(msg.getData());
    }

    public static JsonElement toErrorJson(String errorMsg) {
        JsonObject error = new JsonObject();
        error.addProperty("error", errorMsg);
        return error;
    }
}
