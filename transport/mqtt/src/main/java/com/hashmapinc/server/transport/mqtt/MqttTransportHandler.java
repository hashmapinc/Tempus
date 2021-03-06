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
package com.hashmapinc.server.transport.mqtt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hashmapinc.server.common.data.Device;
import com.hashmapinc.server.common.data.Event;
import com.hashmapinc.server.common.data.security.DeviceTokenCredentials;
import com.hashmapinc.server.common.data.security.DeviceX509Credentials;
import com.hashmapinc.server.common.msg.session.AdaptorToSessionActorMsg;
import com.hashmapinc.server.common.msg.session.BasicToDeviceActorSessionMsg;
import com.hashmapinc.server.common.msg.session.EventToDeviceResponseMsg;
import com.hashmapinc.server.common.msg.session.MsgType;
import com.hashmapinc.server.common.msg.session.ctrl.SessionCloseMsg;
import com.hashmapinc.server.common.msg.session.ex.SessionException;
import com.hashmapinc.server.common.transport.SessionMsgProcessor;
import com.hashmapinc.server.common.transport.adaptor.AdaptorException;
import com.hashmapinc.server.common.transport.auth.DeviceAuthService;
import com.hashmapinc.server.common.transport.quota.QuotaService;
import com.hashmapinc.server.dao.EncryptionUtil;
import com.hashmapinc.server.dao.asset.AssetService;
import com.hashmapinc.server.dao.attributes.AttributesService;
import com.hashmapinc.server.dao.device.DeviceService;
import com.hashmapinc.server.dao.event.EventService;
import com.hashmapinc.server.dao.mail.MailService;
import com.hashmapinc.server.dao.relation.RelationService;
import com.hashmapinc.server.transport.mqtt.adaptors.JsonMqttAdaptor;
import com.hashmapinc.server.transport.mqtt.adaptors.MqttTransportAdaptor;
import com.hashmapinc.server.transport.mqtt.session.DeviceSessionCtx;
import com.hashmapinc.server.transport.mqtt.session.GatewaySessionCtx;
import com.hashmapinc.server.transport.mqtt.sparkplug.SparkPlugDecodeService;
import com.hashmapinc.server.transport.mqtt.sparkplug.SparkPlugMsgTypes;
import com.hashmapinc.server.transport.mqtt.sparkplug.SparkPlugUtils;
import com.hashmapinc.server.transport.mqtt.sparkplug.data.SparkPlugMetaData;
import com.hashmapinc.server.transport.mqtt.util.SslUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.mqtt.*;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.security.cert.X509Certificate;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import static com.hashmapinc.server.common.msg.session.MsgType.*;
import static com.hashmapinc.server.transport.mqtt.MqttTopics.DEVICE_RPC_REQUESTS_SUB_TOPIC;
import static io.netty.handler.codec.mqtt.MqttConnectReturnCode.*;
import static io.netty.handler.codec.mqtt.MqttMessageType.*;
import static io.netty.handler.codec.mqtt.MqttQoS.*;


@Slf4j
public class MqttTransportHandler extends ChannelInboundHandlerAdapter implements GenericFutureListener<Future<? super Void>> {

    public static final MqttQoS MAX_SUPPORTED_QOS_LVL = AT_LEAST_ONCE;
    public static final String TOPIC = "topic";

    private final DeviceSessionCtx deviceSessionCtx;
    private final String sessionId;
    private final MqttTransportAdaptor adaptor;
    private final SessionMsgProcessor processor;
    private final DeviceService deviceService;
    private final DeviceAuthService authService;
    private final RelationService relationService;
    private final QuotaService quotaService;
    private final AttributesService attributesService;
    private final AssetService assetService;
    private final SslHandler sslHandler;
    private final MailService mailService;
    private final EventService eventService;
    private static final String SPARK_PLUG_NAME_SPACE = "spBv1.0";
    private volatile boolean connected;
    private volatile GatewaySessionCtx gatewaySessionCtx;
    private SparkPlugDecodeService sparkPlugDecodeService;
    private SparkPlugUtils sparkPlugUtils;

    public MqttTransportHandler(SessionMsgProcessor processor, DeviceService deviceService, DeviceAuthService authService, RelationService relationService,
                                MqttTransportAdaptor adaptor, SslHandler sslHandler, QuotaService quotaService , AttributesService attributesService,
                                AssetService assetService, EventService eventService, MailService mailService) {
        this.processor = processor;
        this.deviceService = deviceService;
        this.relationService = relationService;
        this.authService = authService;
        this.adaptor = adaptor;
        this.deviceSessionCtx = new DeviceSessionCtx(processor, authService, adaptor);
        this.sessionId = deviceSessionCtx.getSessionId().toUidStr();
        this.sslHandler = sslHandler;
        this.quotaService = quotaService;
        this.attributesService = attributesService;
        this.assetService = assetService;
        this.mailService = mailService;
        this.eventService = eventService;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        log.trace("[{}] Processing msg: {}", sessionId, msg);
        if (msg instanceof MqttMessage) {
            processMqttMsg(ctx, (MqttMessage) msg);
        }
    }

    private void processMqttMsg(ChannelHandlerContext ctx, MqttMessage msg) {
        InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
        if (msg.fixedHeader() == null) {
            log.info("[{}:{}] Invalid message received", address.getHostName(), address.getPort());
            processDisconnect(ctx);
            return;
        }

        if (quotaService.isQuotaExceeded(address.getHostName())) {
            log.warn("MQTT Quota exceeded for [{}:{}] . Disconnect", address.getHostName(), address.getPort());
            processDisconnect(ctx);
        }
        else {
            deviceSessionCtx.setChannel(ctx);
            switch (msg.fixedHeader().messageType()) {
                case CONNECT:
                    processConnect(ctx, (MqttConnectMessage) msg);
                    break;
                case PUBLISH:
                    processPublish(ctx, (MqttPublishMessage) msg);
                    break;
                case SUBSCRIBE:
                    processSubscribe(ctx, (MqttSubscribeMessage) msg);
                    break;
                case UNSUBSCRIBE:
                    processUnsubscribe(ctx, (MqttUnsubscribeMessage) msg);
                    break;
                case PINGREQ:
                    if (checkConnected(ctx)) {
                        ctx.writeAndFlush(new MqttMessage(new MqttFixedHeader(PINGRESP, false, AT_MOST_ONCE, false, 0)));
                    }
                    break;
                case DISCONNECT:
                    if (checkConnected(ctx)) {
                        processDisconnect(ctx);
                    }
                    break;
                default: break;
            }
        }

    }

    private void processPublish(ChannelHandlerContext ctx, MqttPublishMessage mqttMsg) {
        if (!checkConnected(ctx)) {
            return;
        }
        String topicName = mqttMsg.variableHeader().topicName();
        int msgId = mqttMsg.variableHeader().messageId();
        log.trace("[{}] Processing publish msg [{}][{}]!", sessionId, topicName, msgId);

        if (topicName.startsWith(MqttTopics.BASE_GATEWAY_API_TOPIC)) {
            if (gatewaySessionCtx != null) {
                gatewaySessionCtx.setChannel(ctx);
                handleMqttPublishMsg(topicName, msgId, mqttMsg);
            }
        }else if(topicName.startsWith(SPARK_PLUG_NAME_SPACE)){
            if (gatewaySessionCtx != null) {
                gatewaySessionCtx.setChannel(ctx);
                sparkPlugDecodeService.processSparkPlugbPostTelemetry(gatewaySessionCtx, mqttMsg);
            }
        }
        else {
            processDevicePublish(ctx, mqttMsg, topicName, msgId);
        }
    }

    private void handleMqttPublishMsg(String topicName, int msgId, MqttPublishMessage mqttMsg) {
        try {
            if (topicName.equals(MqttTopics.GATEWAY_TELEMETRY_TOPIC)) {
                gatewaySessionCtx.onDeviceTelemetry(mqttMsg);
            } else if (topicName.equals(MqttTopics.GATEWAY_DEPTH_TELEMETRY_TOPIC)) {
                gatewaySessionCtx.onDeviceDepthTelemetry(mqttMsg);
            } else if (topicName.equals(MqttTopics.GATEWAY_EVENTS_TOPIC)) {
                gatewaySessionCtx.onDeviceEventMsg(mqttMsg);
            } else if (topicName.equals(MqttTopics.GATEWAY_ATTRIBUTES_TOPIC)) {
                gatewaySessionCtx.onDeviceAttributes(mqttMsg);
            } else if (topicName.equals(MqttTopics.GATEWAY_ATTRIBUTES_REQUEST_TOPIC)) {
                gatewaySessionCtx.onDeviceAttributesRequest(mqttMsg);
            } else if (topicName.equals(MqttTopics.GATEWAY_RPC_TOPIC)) {
                gatewaySessionCtx.onDeviceRpcResponse(mqttMsg);
            } else if (topicName.equals(MqttTopics.GATEWAY_CONNECT_TOPIC)) {
                gatewaySessionCtx.onDeviceConnect(mqttMsg);
            } else if (topicName.equals(MqttTopics.GATEWAY_DISCONNECT_TOPIC)) {
                gatewaySessionCtx.onDeviceDisconnect(mqttMsg);
            }
        } catch (RuntimeException | AdaptorException e) {
            log.warn("[{}] Failed to process publish msg [{}][{}]", sessionId, topicName, msgId, e);
        }
    }

    private void processDevicePublish(ChannelHandlerContext ctx, MqttPublishMessage mqttMsg, String topicName, int msgId) {
        AdaptorToSessionActorMsg msg = null;
        try {
            if (topicName.equals(MqttTopics.DEVICE_TELEMETRY_TOPIC)) {
                msg = adaptor.convertToActorMsg(deviceSessionCtx, MsgType.POST_TELEMETRY_REQUEST, mqttMsg);
            } else if (topicName.equals(MqttTopics.DEVICE_DEPTH_TELEMETRY_TOPIC)){
                msg = adaptor.convertToActorMsg(deviceSessionCtx, MsgType.POST_TELEMETRY_REQUEST_DEPTH, mqttMsg);
            } else if (topicName.equals(MqttTopics.DEVICE_ATTRIBUTES_TOPIC)) {
                msg = adaptor.convertToActorMsg(deviceSessionCtx, MsgType.POST_ATTRIBUTES_REQUEST, mqttMsg);
            } else if (topicName.equals(MqttTopics.DEVICE_EVENT_TOPIC)) {
                saveEventForDevice(mqttMsg);
            } else if (topicName.startsWith(MqttTopics.DEVICE_ATTRIBUTES_REQUEST_TOPIC_PREFIX)) {
                msg = adaptor.convertToActorMsg(deviceSessionCtx, MsgType.GET_ATTRIBUTES_REQUEST, mqttMsg);
                if (msgId >= 0) {
                    ctx.writeAndFlush(createMqttPubAckMsg(msgId));
                }
            } else if (topicName.startsWith(MqttTopics.DEVICE_RPC_RESPONSE_TOPIC)) {
                msg = adaptor.convertToActorMsg(deviceSessionCtx, MsgType.TO_DEVICE_RPC_RESPONSE, mqttMsg);
                if (msgId >= 0) {
                    ctx.writeAndFlush(createMqttPubAckMsg(msgId));
                }
            } else if (topicName.startsWith(MqttTopics.DEVICE_RPC_REQUESTS_TOPIC)) {
                msg = adaptor.convertToActorMsg(deviceSessionCtx, MsgType.TO_SERVER_RPC_REQUEST, mqttMsg);
                if (msgId >= 0) {
                    ctx.writeAndFlush(createMqttPubAckMsg(msgId));
                }
            }
        } catch (AdaptorException e) {
            log.warn("[{}] Failed to process publish msg [{}][{}]", sessionId, topicName, msgId, e);
        }
        if (msg != null) {
            processor.process(new BasicToDeviceActorSessionMsg(deviceSessionCtx.getDevice(), msg));
        } else {
            log.info("[{}] Closing current session due to invalid publish msg [{}][{}]", sessionId, topicName, msgId);
            ctx.close();
        }
    }

    private void saveEventForDevice(MqttPublishMessage inbound) throws AdaptorException {
        String payload = JsonMqttAdaptor.validatePayload(deviceSessionCtx.getSessionId(), inbound.payload());
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode eventInfo = mapper.readTree(payload);
            Event event = new Event();
            Device device = deviceSessionCtx.getDevice();
            event.setEntityId(device.getId());
            event.setTenantId(device.getTenantId());
            event.setType("QUALITY_EVENT");
            event.setBody(eventInfo);
            eventService.save(event);
            deviceSessionCtx.onMsg(new EventToDeviceResponseMsg(inbound.variableHeader().packetId()));
        } catch (IOException e) {
            log.info("IOException occurred : {}", e);
            throw new AdaptorException(e);
        } catch (SessionException e) {
            log.info("Session execption : {}", e);
            throw new AdaptorException(e);
        }
    }

    private void processSubscribe(ChannelHandlerContext ctx, MqttSubscribeMessage mqttMsg) {
        if (!checkConnected(ctx)) {
            return;
        }
        log.trace("[{}] Processing subscription [{}]!", sessionId, mqttMsg.variableHeader().messageId());
        List<Integer> grantedQoSList = new ArrayList<>();
        for (MqttTopicSubscription subscription : mqttMsg.payload().topicSubscriptions()) {
            String topicName = subscription.topicName();
            //TODO: handle this qos level.
            MqttQoS reqQoS = subscription.qualityOfService();
            try {
                if (topicName.equals(MqttTopics.DEVICE_ATTRIBUTES_TOPIC)) {
                    AdaptorToSessionActorMsg msg = adaptor.convertToActorMsg(deviceSessionCtx, MsgType.SUBSCRIBE_ATTRIBUTES_REQUEST, mqttMsg);
                    processor.process(new BasicToDeviceActorSessionMsg(deviceSessionCtx.getDevice(), msg));
                    grantedQoSList.add(getMinSupportedQos(reqQoS));
                } else if (topicName.startsWith(SPARK_PLUG_NAME_SPACE)) {
                    handleSubscriberForTopicSparkPlugNameSpace(mqttMsg, grantedQoSList, topicName, reqQoS);
                } else if (topicName.equals(DEVICE_RPC_REQUESTS_SUB_TOPIC)) {
                    AdaptorToSessionActorMsg msg = adaptor.convertToActorMsg(deviceSessionCtx, SUBSCRIBE_RPC_COMMANDS_REQUEST, mqttMsg);
                    processor.process(new BasicToDeviceActorSessionMsg(deviceSessionCtx.getDevice(), msg));
                    grantedQoSList.add(getMinSupportedQos(reqQoS));
                } else if (topicName.equals(MqttTopics.DEVICE_RPC_RESPONSE_SUB_TOPIC)) {
                    grantedQoSList.add(getMinSupportedQos(reqQoS));
                } else if (topicName.equals(MqttTopics.DEVICE_ATTRIBUTES_RESPONSES_TOPIC)) {
                    deviceSessionCtx.setAllowAttributeResponses();
                    grantedQoSList.add(getMinSupportedQos(reqQoS));
                } else if (topicName.equals(MqttTopics.GATEWAY_ATTRIBUTES_TOPIC)) {
                    grantedQoSList.add(getMinSupportedQos(reqQoS));
                } else {
                    log.warn("[{}] Failed to subscribe to [{}][{}]", sessionId, topicName, reqQoS);
                    grantedQoSList.add(FAILURE.value());
                }
            } catch (AdaptorException e) {
                log.warn("[{}] Failed to subscribe to [{}][{}]", sessionId, topicName, reqQoS);
                grantedQoSList.add(FAILURE.value());
            }
        }
        ctx.writeAndFlush(createSubAckMessage(mqttMsg.variableHeader().messageId(), grantedQoSList));
    }

    private void handleSubscriberForTopicSparkPlugNameSpace(MqttSubscribeMessage mqttMsg, List<Integer> grantedQoSList, String topicName, MqttQoS reqQoS) throws AdaptorException {
        if(topicName.contains(SparkPlugMsgTypes.DDEATH)) {
            AdaptorToSessionActorMsg msg = adaptor.convertToActorMsg(deviceSessionCtx, SPARKPLUG_DEATH_SUBSCRIBE, mqttMsg);
            String topic = processAndGetSparkPlugSubscriptionTopic(topicName);
            String addInfoTopic = deviceSessionCtx.getDevice().getAdditionalInfo().get(TOPIC).asText();
            if (addInfoTopic.contentEquals(topic))
                processor.process(new BasicToDeviceActorSessionMsg(deviceSessionCtx.getDevice(), msg));
            grantedQoSList.add(getMinSupportedQos(reqQoS));
        }
        else {
            AdaptorToSessionActorMsg msg = adaptor.convertToActorMsg(deviceSessionCtx, SUBSCRIBE_SPARKPLUG_TELEMETRY_REQUEST, mqttMsg);
            String topic = processAndGetSparkPlugSubscriptionTopic(topicName);
            String addInfoTopic = deviceSessionCtx.getDevice().getAdditionalInfo().get(TOPIC).asText();
            if (addInfoTopic.contentEquals(topic))
                processor.process(new BasicToDeviceActorSessionMsg(deviceSessionCtx.getDevice(), msg));
            grantedQoSList.add(getMinSupportedQos(reqQoS));
        }
    }

    private void processUnsubscribe(ChannelHandlerContext ctx, MqttUnsubscribeMessage mqttMsg) {
        if (!checkConnected(ctx)) {
            return;
        }
        log.trace("[{}] Processing subscription [{}]!", sessionId, mqttMsg.variableHeader().messageId());
        for (String topicName : mqttMsg.payload().topics()) {
            try {
                if (topicName.equals(MqttTopics.DEVICE_ATTRIBUTES_TOPIC)) {
                    AdaptorToSessionActorMsg msg = adaptor.convertToActorMsg(deviceSessionCtx, MsgType.UNSUBSCRIBE_ATTRIBUTES_REQUEST, mqttMsg);
                    processor.process(new BasicToDeviceActorSessionMsg(deviceSessionCtx.getDevice(), msg));
                } else if (topicName.equals(DEVICE_RPC_REQUESTS_SUB_TOPIC)) {
                    AdaptorToSessionActorMsg msg = adaptor.convertToActorMsg(deviceSessionCtx, MsgType.UNSUBSCRIBE_RPC_COMMANDS_REQUEST, mqttMsg);
                    processor.process(new BasicToDeviceActorSessionMsg(deviceSessionCtx.getDevice(), msg));
                } else if (topicName.equals(MqttTopics.DEVICE_ATTRIBUTES_RESPONSES_TOPIC)) {
                    deviceSessionCtx.setDisallowAttributeResponses();
                }
            } catch (AdaptorException e) {
                log.warn("[{}] Failed to process unsubscription [{}] to [{}]", sessionId, mqttMsg.variableHeader().messageId(), topicName);
            }
        }
        ctx.writeAndFlush(createUnSubAckMessage(mqttMsg.variableHeader().messageId()));
    }

    private MqttMessage createUnSubAckMessage(int msgId) {
        MqttFixedHeader mqttFixedHeader =
                new MqttFixedHeader(UNSUBACK, false, AT_LEAST_ONCE, false, 0);
        MqttMessageIdVariableHeader mqttMessageIdVariableHeader = MqttMessageIdVariableHeader.from(msgId);
        return new MqttMessage(mqttFixedHeader, mqttMessageIdVariableHeader);
    }

    private void processConnect(ChannelHandlerContext ctx, MqttConnectMessage msg) {
        log.info("[{}] Processing connect msg for client: {}!", sessionId, msg.payload().clientIdentifier());
        X509Certificate cert;
        if (sslHandler != null && (cert = getX509Certificate()) != null) {
            processX509CertConnect(ctx, cert, msg);
        } else {
            processAuthTokenConnect(ctx, msg);
        }
    }

    private void processAuthTokenConnect(ChannelHandlerContext ctx, MqttConnectMessage msg) {
        String userName = msg.payload().userName();
        if (StringUtils.isEmpty(userName)) {
            ctx.writeAndFlush(createMqttConnAckMsg(CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD));
            ctx.close();
        } else if (!deviceSessionCtx.login(new DeviceTokenCredentials(msg.payload().userName()))) {
            ctx.writeAndFlush(createMqttConnAckMsg(CONNECTION_REFUSED_NOT_AUTHORIZED));
            ctx.close();
        } else {
            ctx.writeAndFlush(createMqttConnAckMsg(CONNECTION_ACCEPTED));
            connected = true;
            checkGatewaySession(msg);
        }
    }

    private void processX509CertConnect(ChannelHandlerContext ctx, X509Certificate cert, MqttConnectMessage msg) {
        try {
            String strCert = SslUtil.getX509CertificateString(cert);
            String sha3Hash = EncryptionUtil.getSha3Hash(strCert);
            if (deviceSessionCtx.login(new DeviceX509Credentials(sha3Hash))) {
                ctx.writeAndFlush(createMqttConnAckMsg(CONNECTION_ACCEPTED));
                connected = true;
                checkGatewaySession(msg);
            } else {
                ctx.writeAndFlush(createMqttConnAckMsg(CONNECTION_REFUSED_NOT_AUTHORIZED));
                ctx.close();
            }
        } catch (Exception e) {
            ctx.writeAndFlush(createMqttConnAckMsg(CONNECTION_REFUSED_NOT_AUTHORIZED));
            ctx.close();
        }
    }

    private X509Certificate getX509Certificate() {
        try {
            X509Certificate[] certChain = sslHandler.engine().getSession().getPeerCertificateChain();
            if (certChain.length > 0) {
                return certChain[0];
            }
        } catch (SSLPeerUnverifiedException e) {
            log.warn(e.getMessage());
            return null;
        }
        return null;
    }

    private void processDisconnect(ChannelHandlerContext ctx) {
        ctx.close();
        if (connected) {
            SessionCloseMsg sessionCloseMsg = SessionCloseMsg.onDisconnect(deviceSessionCtx.getSessionId());
            sessionCloseMsg.setDeviceId(deviceSessionCtx.getDevice().getId());
            processor.process(sessionCloseMsg);
            if (gatewaySessionCtx != null) {
                gatewaySessionCtx.onGatewayDisconnect();
                if (sparkPlugDecodeService != null)
                    sparkPlugDecodeService.updateDeviceMapState();
            }
        }
    }

    private MqttConnAckMessage createMqttConnAckMsg(MqttConnectReturnCode returnCode) {
        MqttFixedHeader mqttFixedHeader =
                new MqttFixedHeader(CONNACK, false, AT_MOST_ONCE, false, 0);
        MqttConnAckVariableHeader mqttConnAckVariableHeader =
                new MqttConnAckVariableHeader(returnCode, true);
        return new MqttConnAckMessage(mqttFixedHeader, mqttConnAckVariableHeader);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("[{}] Unexpected Exception", sessionId, cause);
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        processDisconnect(ctx);
    }

    private static MqttSubAckMessage createSubAckMessage(Integer msgId, List<Integer> grantedQoSList) {
        MqttFixedHeader mqttFixedHeader =
                new MqttFixedHeader(SUBACK, false, AT_LEAST_ONCE, false, 0);
        MqttMessageIdVariableHeader mqttMessageIdVariableHeader = MqttMessageIdVariableHeader.from(msgId);
        MqttSubAckPayload mqttSubAckPayload = new MqttSubAckPayload(grantedQoSList);
        return new MqttSubAckMessage(mqttFixedHeader, mqttMessageIdVariableHeader, mqttSubAckPayload);
    }

    private static int getMinSupportedQos(MqttQoS reqQoS) {
        return Math.min(reqQoS.value(), MAX_SUPPORTED_QOS_LVL.value());
    }

    public static MqttPubAckMessage createMqttPubAckMsg(int requestId) {
        MqttFixedHeader mqttFixedHeader =
                new MqttFixedHeader(PUBACK, false, AT_LEAST_ONCE, false, 0);
        MqttMessageIdVariableHeader mqttMsgIdVariableHeader =
                MqttMessageIdVariableHeader.from(requestId);
        return new MqttPubAckMessage(mqttFixedHeader, mqttMsgIdVariableHeader);
    }

    private boolean checkConnected(ChannelHandlerContext ctx) {
        if (connected) {
            return true;
        } else {
            log.info("[{}] Closing current session due to invalid msg order [{}][{}]", sessionId);
            ctx.close();
            return false;
        }
    }

    private void checkGatewaySession(MqttConnectMessage msg) {
        Device device = deviceSessionCtx.getDevice();
        JsonNode infoNode = device.getAdditionalInfo();
        if (infoNode != null) {
            JsonNode gatewayNode = infoNode.get("gateway");
            JsonNode topic = infoNode.get(TOPIC);
            if (gatewayNode != null && gatewayNode.asBoolean()) {
                gatewaySessionCtx = new GatewaySessionCtx(processor, deviceService, authService, relationService, eventService,
                        deviceSessionCtx, attributesService, assetService, mailService);
                if((msg.payload().willTopic() != null) && msg.payload().willTopic().startsWith(SPARK_PLUG_NAME_SPACE)){
                    sparkPlugDecodeService = new SparkPlugDecodeService();
                }
            }

            if((msg.payload().willTopic() != null) &&
                    msg.payload().willTopic().startsWith(SPARK_PLUG_NAME_SPACE) && topic != null) {
                sparkPlugUtils = new SparkPlugUtils();
            }
        }
    }

    private String processAndGetSparkPlugSubscriptionTopic(String topicName){
        if (sparkPlugUtils != null) {
            String msgType = sparkPlugUtils.extractMsgType(topicName);
            SparkPlugMetaData sparkPlugMetaData = new SparkPlugMetaData(msgType, 0);
            deviceSessionCtx.setSparkPlugMetaData(sparkPlugMetaData);
            return sparkPlugUtils.extractTopicWithoutMsgType(topicName);
        }
        return "";
    }

    @Override
    public void operationComplete(Future<? super Void> future) throws Exception {
        processor.process(SessionCloseMsg.onError(deviceSessionCtx.getSessionId()));
    }
}
