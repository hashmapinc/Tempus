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
package com.hashmapinc.server.transport.mqtt.session;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.hashmapinc.server.common.data.DataConstants;
import com.hashmapinc.server.common.data.Device;
import com.hashmapinc.server.common.data.Event;
import com.hashmapinc.server.common.data.asset.Asset;
import com.hashmapinc.server.common.data.exception.TempusException;
import com.hashmapinc.server.common.data.id.SessionId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.kv.AttributeKvEntry;
import com.hashmapinc.server.common.data.kv.BaseAttributeKvEntry;
import com.hashmapinc.server.common.data.kv.KvEntry;
import com.hashmapinc.server.common.data.relation.EntityRelation;
import com.hashmapinc.server.common.data.relation.RelationTypeGroup;
import com.hashmapinc.server.common.msg.core.*;
import com.hashmapinc.server.common.msg.exception.TempusRuntimeException;
import com.hashmapinc.server.common.msg.session.BasicAdaptorToSessionActorMsg;
import com.hashmapinc.server.common.msg.session.BasicToDeviceActorSessionMsg;
import com.hashmapinc.server.common.msg.session.EventToDeviceResponseMsg;
import com.hashmapinc.server.common.msg.session.ctrl.SessionCloseMsg;
import com.hashmapinc.server.common.msg.session.ex.SessionException;
import com.hashmapinc.server.common.transport.SessionMsgProcessor;
import com.hashmapinc.server.common.transport.adaptor.AdaptorException;
import com.hashmapinc.server.common.transport.adaptor.JsonConverter;
import com.hashmapinc.server.common.transport.auth.DeviceAuthService;
import com.hashmapinc.server.dao.asset.AssetService;
import com.hashmapinc.server.dao.attributes.AttributesService;
import com.hashmapinc.server.dao.device.DeviceService;
import com.hashmapinc.server.dao.event.EventService;
import com.hashmapinc.server.dao.mail.MailService;
import com.hashmapinc.server.dao.relation.RelationService;
import com.hashmapinc.server.transport.mqtt.MqttTransportHandler;
import com.hashmapinc.server.transport.mqtt.sparkplug.data.SparkPlugDecodedMsg;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.hashmapinc.server.transport.mqtt.adaptors.JsonMqttAdaptor.validateJsonPayload;

/**
 * Created by ashvayka on 19.01.17.
 */
@Slf4j
public class GatewaySessionCtx {

    private static final String DEFAULT_DEVICE_TYPE = "default";
    public static final String CAN_T_PARSE_VALUE = "Can't parse value: ";
    public static final String DEVICE_PROPERTY = "device";
    private static final String SPARKPLUG_DEVICE_TYPE = "sparkplug";
    public static final String PARENT_ASSET = "parent_asset";
    private final Device gateway;
    private final SessionId gatewaySessionId;
    private final SessionMsgProcessor processor;
    private final DeviceService deviceService;
    private final DeviceAuthService authService;
    private final RelationService relationService;
    private final AttributesService attributesService;
    private final AssetService assetService;
    private final MailService mailService;
    private final Map<String, GatewayDeviceSessionCtx> devices;
    private ChannelHandlerContext channel;
    private final EventService eventService;

    public GatewaySessionCtx(SessionMsgProcessor processor, DeviceService deviceService, DeviceAuthService authService,
                             RelationService relationService, EventService eventService, DeviceSessionCtx gatewaySessionCtx,
                             AttributesService attributesService , AssetService assetService, MailService mailService) {
        this.processor = processor;
        this.deviceService = deviceService;
        this.authService = authService;
        this.relationService = relationService;
        this.eventService = eventService;
        this.gateway = gatewaySessionCtx.getDevice();
        this.gatewaySessionId = gatewaySessionCtx.getSessionId();
        this.attributesService = attributesService;
        this.assetService = assetService;
        this.mailService = mailService;
        this.devices = new HashMap<>();
    }

    public void onDeviceConnect(MqttPublishMessage msg) throws AdaptorException {
        JsonElement json = getJson(msg);
        String deviceName = checkDeviceName(getDeviceName(json));
        String deviceType = getDeviceType(json);
        onDeviceConnect(deviceName, deviceType);
        ack(msg);
    }

    private void onDeviceConnect(String deviceName, String deviceType) {
        if (!devices.containsKey(deviceName)) {
            Device device = deviceService.findDeviceByTenantIdAndName(gateway.getTenantId(), deviceName);
            if (device == null) {
                device = new Device();
                device.setTenantId(gateway.getTenantId());
                device.setName(deviceName);
                device.setType(deviceType);
                device.setCustomerId(gateway.getCustomerId());
                device.setDataModelObjectId(gateway.getDataModelObjectId());
                device = deviceService.saveDevice(device);
                relationService.saveRelationAsync(new EntityRelation(gateway.getId(), device.getId(), "Created"));
            }
            GatewayDeviceSessionCtx ctx = new GatewayDeviceSessionCtx(this, device);
            devices.put(deviceName, ctx);
            log.debug("[{}] Added device [{}] to the gateway session", gatewaySessionId, deviceName);
            processor.process(new BasicToDeviceActorSessionMsg(device, new BasicAdaptorToSessionActorMsg(ctx, new AttributesSubscribeMsg())));
            processor.process(new BasicToDeviceActorSessionMsg(device, new BasicAdaptorToSessionActorMsg(ctx, new RpcSubscribeMsg())));
        }
    }

    private void onSparkPlugDeviceConnect(String deviceName, String deviceType, String topic) {
        if (!devices.containsKey(deviceName)) {
            Device device = deviceService.findDeviceByTenantIdAndName(gateway.getTenantId(), deviceName);
            if (device == null) {
                device = new Device();
                device.setTenantId(gateway.getTenantId());
                device.setName(deviceName);
                device.setType(deviceType);
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode additionalInfo = mapper.readTree("{\"topic\":\"" + topic + "\"}");
                    device.setAdditionalInfo(additionalInfo);
                }
                catch (IOException e){
                    log.error("Topic could not be parsed to additional info " + e);
                }
                device = deviceService.saveDevice(device);
                relationService.saveRelation(new EntityRelation(gateway.getId(), device.getId(), "Created"));
            }
            GatewayDeviceSessionCtx ctx = new GatewayDeviceSessionCtx(this, device);
            devices.put(deviceName, ctx);
            log.debug("[{}] Added device [{}] to the gateway session", gatewaySessionId, deviceName);
            processor.process(new BasicToDeviceActorSessionMsg(device, new BasicAdaptorToSessionActorMsg(ctx, new AttributesSubscribeMsg())));
            processor.process(new BasicToDeviceActorSessionMsg(device, new BasicAdaptorToSessionActorMsg(ctx, new RpcSubscribeMsg())));
        }
    }

    public void onDeviceDisconnect(MqttPublishMessage msg) throws AdaptorException {
        String deviceName = checkDeviceName(getDeviceName(getJson(msg)));
        GatewayDeviceSessionCtx deviceSessionCtx = devices.remove(deviceName);
        if (deviceSessionCtx != null) {
            SessionCloseMsg sessionCloseMsg = SessionCloseMsg.onDisconnect(deviceSessionCtx.getSessionId());
            sessionCloseMsg.setDeviceId(deviceSessionCtx.getDevice().getId());
            processor.process(sessionCloseMsg);
            deviceSessionCtx.setClosed(true);
            log.debug("[{}] Removed device [{}] from the gateway session", gatewaySessionId, deviceName);
        } else {
            log.debug("[{}] Device [{}] was already removed from the gateway session", gatewaySessionId, deviceName);
        }
        ack(msg);
    }

    public void onGatewayDisconnect() {
        devices.forEach((k, v) -> {
            SessionCloseMsg sessionCloseMsg = SessionCloseMsg.onDisconnect(v.getSessionId());
            sessionCloseMsg.setDeviceId(v.getDevice().getId());
            processor.process(sessionCloseMsg);
        });
    }

    public void onDeviceTelemetry(MqttPublishMessage mqttMsg) throws AdaptorException {
        JsonElement json = validateJsonPayload(gatewaySessionId, mqttMsg.payload());
        int requestId = mqttMsg.variableHeader().packetId();
        if (json.isJsonObject()) {
            JsonObject jsonObj = json.getAsJsonObject();
            for (Map.Entry<String, JsonElement> deviceEntry : jsonObj.entrySet()) {
                String deviceName = checkDeviceConnected(deviceEntry.getKey());
                if (!deviceEntry.getValue().isJsonArray()) {
                    throw new JsonSyntaxException(CAN_T_PARSE_VALUE + json);
                }
                BasicTelemetryUploadRequest request = new BasicTelemetryUploadRequest(requestId);
                JsonArray deviceData = deviceEntry.getValue().getAsJsonArray();
                for (JsonElement element : deviceData) {
                    JsonConverter.parseWithTs(request, element.getAsJsonObject());
                }
                GatewayDeviceSessionCtx deviceSessionCtx = devices.get(deviceName);
                processor.process(new BasicToDeviceActorSessionMsg(deviceSessionCtx.getDevice(),
                        new BasicAdaptorToSessionActorMsg(deviceSessionCtx, request)));
            }
        } else {
            throw new JsonSyntaxException(CAN_T_PARSE_VALUE + json);
        }
    }

    public void onSparkPlugDecodedMsg(SparkPlugDecodedMsg sparkPlugDecodedMsg, String topic, String deviceName) {
        int requestId = sparkPlugDecodedMsg.getRequestId();
        checkSparkPlugDeviceConnected(deviceName, topic);
        List<KvEntry> kvEntryList = sparkPlugDecodedMsg.getKvEntryList();
        Long ts = sparkPlugDecodedMsg.getTs();
        BasicTelemetryUploadRequest request = new BasicTelemetryUploadRequest(requestId);
        for (KvEntry entry : kvEntryList) {
            request.add(ts, entry);
        }
        GatewayDeviceSessionCtx deviceSessionCtx = devices.get(deviceName);
        processor.process(new BasicToDeviceActorSessionMsg(deviceSessionCtx.getDevice(),
                new BasicAdaptorToSessionActorMsg(deviceSessionCtx, request)));
    }

    public void onDeviceDepthTelemetry(MqttPublishMessage mqttMsg) throws AdaptorException {
        JsonElement json = validateJsonPayload(gatewaySessionId, mqttMsg.payload());
        int requestId = mqttMsg.variableHeader().packetId();
        if (json.isJsonObject()) {
            JsonObject jsonObj = json.getAsJsonObject();
            for (Map.Entry<String, JsonElement> deviceEntry : jsonObj.entrySet()) {
                String deviceName = checkDeviceConnected(deviceEntry.getKey());
                if (!deviceEntry.getValue().isJsonArray()) {
                    throw new JsonSyntaxException(CAN_T_PARSE_VALUE+ json);
                }
                BasicDepthTelemetryUploadRequest request = new BasicDepthTelemetryUploadRequest(requestId);
                JsonArray deviceData = deviceEntry.getValue().getAsJsonArray();
                for (JsonElement element : deviceData) {
                    JsonConverter.parseWithDepth(request, element.getAsJsonObject());
                }
                GatewayDeviceSessionCtx deviceSessionCtx = devices.get(deviceName);
                processor.process(new BasicToDeviceActorSessionMsg(deviceSessionCtx.getDevice(),
                        new BasicAdaptorToSessionActorMsg(deviceSessionCtx, request)));
            }
        } else {
            throw new JsonSyntaxException(CAN_T_PARSE_VALUE + json);
        }
    }

    public void onDeviceEventMsg(MqttPublishMessage mqttMsg) throws AdaptorException {
        JsonElement json = validateJsonPayload(gatewaySessionId, mqttMsg.payload());
        int requestId = mqttMsg.variableHeader().packetId();
        if (json.isJsonObject()) {
            JsonObject jsonObj = json.getAsJsonObject();
            for (Map.Entry<String, JsonElement> deviceEntry : jsonObj.entrySet()) {
                Device device = deviceService.findDeviceByTenantIdAndName(gateway.getTenantId(), deviceEntry.getKey());
                if (device != null) {
                    GatewayDeviceSessionCtx ctx = new GatewayDeviceSessionCtx(this, device);
                    devices.put(device.getName(), ctx);
                    saveDeviceEventInfo(device, deviceEntry.getValue().toString(), requestId);
                }
            }
        } else {
            throw new JsonSyntaxException(CAN_T_PARSE_VALUE + json);
        }
    }

    public void saveDeviceEventInfo(Device device, String strEventInfo, int requestId) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode eventInfo = mapper.readTree(strEventInfo);
            Event event = new Event();
            event.setEntityId(device.getId());
            event.setTenantId(device.getTenantId());
            event.setType("QUALITY_EVENT");
            event.setBody(eventInfo);
            eventService.save(event);
            GatewayDeviceSessionCtx deviceSessionCtx = devices.get(device.getName());
            deviceSessionCtx.onMsg(new EventToDeviceResponseMsg(requestId));
        } catch (IOException e) {
            log.info("Object mapping exception {}", e);
        } catch (SessionException e) {
            log.info("Session exception {}", e);
        }
    }

    public void onDeviceRpcResponse(MqttPublishMessage mqttMsg) throws AdaptorException {
        JsonElement json = validateJsonPayload(gatewaySessionId, mqttMsg.payload());
        if (json.isJsonObject()) {
            JsonObject jsonObj = json.getAsJsonObject();
            String deviceName = checkDeviceConnected(jsonObj.get(DEVICE_PROPERTY).getAsString());
            Integer requestId = jsonObj.get("id").getAsInt();
            String data = jsonObj.get("data").toString();
            GatewayDeviceSessionCtx deviceSessionCtx = devices.get(deviceName);
            processor.process(new BasicToDeviceActorSessionMsg(deviceSessionCtx.getDevice(),
                    new BasicAdaptorToSessionActorMsg(deviceSessionCtx, new ToDeviceRpcResponseMsg(requestId, data))));
        } else {
            throw new JsonSyntaxException(CAN_T_PARSE_VALUE + json);
        }
    }

    public void onDeviceAttributes(MqttPublishMessage mqttMsg) throws AdaptorException {
        JsonElement json = validateJsonPayload(gatewaySessionId, mqttMsg.payload());
        int requestId = mqttMsg.variableHeader().packetId();
        if (json.isJsonObject()) {
            JsonObject jsonObj = json.getAsJsonObject();
            for (Map.Entry<String, JsonElement> deviceEntry : jsonObj.entrySet()) {
                String deviceName = checkDeviceConnected(deviceEntry.getKey());
                if (!deviceEntry.getValue().isJsonObject()) {
                    throw new JsonSyntaxException(CAN_T_PARSE_VALUE + json);
                }
                long ts = System.currentTimeMillis();
                BasicUpdateAttributesRequest request = new BasicUpdateAttributesRequest(requestId);
                JsonObject deviceData = deviceEntry.getValue().getAsJsonObject();
                request.add(JsonConverter.parseValues(deviceData).stream().map(kv -> new BaseAttributeKvEntry(kv, ts)).collect(Collectors.toList()));
                Set<AttributeKvEntry> attributeKvEntries = request.getAttributes();

                Optional<AttributeKvEntry> parentAssetAttribute = findParentAssetAttribute(attributeKvEntries);

                Device device = deviceService.findDeviceByTenantIdAndName(gateway.getTenantId(), deviceName);
                try {
                    Optional<AttributeKvEntry> savedParentAssetAttribute = attributesService.find(device.getId(), DataConstants.CLIENT_SCOPE, PARENT_ASSET).get();

                    if (parentAssetAttribute.isPresent()) {
                        savedParentAssetAttribute.ifPresent(attributeKvEntry -> deleteParentAssetRelation(device, attributeKvEntry));
                        createParentAssetRelationWithDevice(device, parentAssetAttribute.get().getValueAsString());
                    }
                    else if (savedParentAssetAttribute.isEmpty())
                        sendParentAssetMissingEmail(deviceName, gateway.getTenantId());

                }catch (Exception exp){
                    log.warn("Failed to fetch parentAssetAttribute : [{}]", parentAssetAttribute.get().getKey());
                }

                GatewayDeviceSessionCtx deviceSessionCtx = devices.get(deviceName);
                processor.process(new BasicToDeviceActorSessionMsg(deviceSessionCtx.getDevice(),
                        new BasicAdaptorToSessionActorMsg(deviceSessionCtx, request)));
            }
        } else {
            throw new JsonSyntaxException(CAN_T_PARSE_VALUE + json);
        }
    }


    private void sendParentAssetMissingEmail(String deviceName , TenantId tenantId) {
        try {
            mailService.sendAttributeMissingMail(deviceName,tenantId);
        }catch (TempusException tempusException) {
            log.warn("Failed to send the  parentAssetMissingEmail of deviceName: [{}]",deviceName);
        }
    }

    private void createParentAssetRelationWithDevice(Device device,String assetName) {
        Optional<Asset> asset = assetService.findAssetByTenantIdAndName(gateway.getTenantId(),assetName);
        asset.ifPresent(asset1 -> {
            EntityRelation relation = new EntityRelation(device.getId(), asset1.getId(), EntityRelation.CONTAINS_TYPE);
            relationService.saveRelation(relation);
        });

        if(asset.isEmpty()){
            try {
                sendAssetNotPresentEmail(device.getName(),assetName, gateway.getTenantId());
            }catch (TempusException tempsuException){
                log.warn("Failed to send the  parentAssetAttributeMissingEmail of deviceName: [{}]",device.getName()) ;
            }
        }
    }

    private void sendAssetNotPresentEmail(String deviceName ,String assetName, TenantId tenantId) throws TempusException {
        mailService.sendAssetNotPresentMail(deviceName,assetName,tenantId);
    }

    private void deleteParentAssetRelation(Device device ,AttributeKvEntry  parentAssetAttribute) {
        Optional<Asset> savedAsset = assetService.findAssetByTenantIdAndName(gateway.getTenantId(), parentAssetAttribute.getValueAsString());
        savedAsset.ifPresent(asset -> relationService.deleteRelation(device.getId(), asset.getId(), EntityRelation.CONTAINS_TYPE, RelationTypeGroup.COMMON));
    }

    public void onDeviceAttributesRequest(MqttPublishMessage msg) throws AdaptorException {
        JsonElement json = validateJsonPayload(gatewaySessionId, msg.payload());
        if (json.isJsonObject()) {
            JsonObject jsonObj = json.getAsJsonObject();
            int requestId = jsonObj.get("id").getAsInt();
            String deviceName = jsonObj.get(DEVICE_PROPERTY).getAsString();
            boolean clientScope = jsonObj.get("client").getAsBoolean();
            Set<String> keys;
            if (jsonObj.has("key")) {
                keys = Collections.singleton(jsonObj.get("key").getAsString());
            } else {
                JsonArray keysArray = jsonObj.get("keys").getAsJsonArray();
                keys = new HashSet<>();
                for (JsonElement keyObj : keysArray) {
                    keys.add(keyObj.getAsString());
                }
            }

            BasicGetAttributesRequest request;
            if (clientScope) {
                request = new BasicGetAttributesRequest(requestId, keys, null);
            } else {
                request = new BasicGetAttributesRequest(requestId, null, keys);
            }
            GatewayDeviceSessionCtx deviceSessionCtx = devices.get(deviceName);
            processor.process(new BasicToDeviceActorSessionMsg(deviceSessionCtx.getDevice(),
                    new BasicAdaptorToSessionActorMsg(deviceSessionCtx, request)));
            ack(msg);
        } else {
            throw new JsonSyntaxException(CAN_T_PARSE_VALUE + json);
        }
    }

    private String checkDeviceConnected(String deviceName) {
        if (!devices.containsKey(deviceName)) {
            log.debug("[{}] Missing device [{}] for the gateway session", gatewaySessionId, deviceName);
            onDeviceConnect(deviceName, DEFAULT_DEVICE_TYPE);
        }
        return deviceName;
    }

    private String checkSparkPlugDeviceConnected(String deviceName, String topic) {
        if (!devices.containsKey(deviceName)) {
            log.debug("[{}] Missing device [{}] for the gateway session", gatewaySessionId, deviceName);
            onSparkPlugDeviceConnect(deviceName, SPARKPLUG_DEVICE_TYPE, topic);
        }
        return deviceName;
    }

    private String checkDeviceName(String deviceName) {
        if (StringUtils.isEmpty(deviceName)) {
            throw new TempusRuntimeException("Device name is empty!");
        } else {
            return deviceName;
        }
    }

    private String getDeviceName(JsonElement json) {
        return json.getAsJsonObject().get(DEVICE_PROPERTY).getAsString();
    }

    private String getDeviceType(JsonElement json) {
        JsonElement type = json.getAsJsonObject().get("type");
        return type == null ? DEFAULT_DEVICE_TYPE : type.getAsString();
    }

    private JsonElement getJson(MqttPublishMessage mqttMsg) throws AdaptorException {
        return validateJsonPayload(gatewaySessionId, mqttMsg.payload());
    }

    protected SessionMsgProcessor getProcessor() {
        return processor;
    }

    DeviceAuthService getAuthService() {
        return authService;
    }

    public void setChannel(ChannelHandlerContext channel) {
        this.channel = channel;
    }

    private void ack(MqttPublishMessage msg) {
        if (msg.variableHeader().packetId() > 0) {
            writeAndFlush(MqttTransportHandler.createMqttPubAckMsg(msg.variableHeader().packetId()));
        }
    }

    void writeAndFlush(MqttMessage mqttMessage) {
        channel.writeAndFlush(mqttMessage);
    }

    private Optional<AttributeKvEntry> findParentAssetAttribute(Set<AttributeKvEntry> attributeKvEntries){
        for (AttributeKvEntry attributeKvEntry : attributeKvEntries) {
            if(attributeKvEntry.getKey().equals(PARENT_ASSET)){
                return Optional.of(attributeKvEntry);
            }
        }
        return Optional.empty();
    }

}
