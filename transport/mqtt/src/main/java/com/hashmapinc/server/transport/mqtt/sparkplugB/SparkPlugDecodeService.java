package com.hashmapinc.server.transport.mqtt.sparkplugB;

import com.cirruslink.sparkplug.message.SparkplugBPayloadDecoder;
import com.cirruslink.sparkplug.message.model.Metric;
import com.cirruslink.sparkplug.message.model.MetricDataType;
import com.cirruslink.sparkplug.message.model.SparkplugBPayload;
import com.hashmapinc.server.transport.mqtt.sparkplugB.data.SparkPlugDecodedMsg;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import lombok.extern.slf4j.Slf4j;
import com.hashmapinc.server.common.data.kv.*;
import com.hashmapinc.server.transport.mqtt.session.GatewaySessionCtx;


import java.util.*;

import static com.hashmapinc.server.transport.mqtt.sparkplugB.SparkPlugMsgTypes.DBIRTH;
import static com.hashmapinc.server.transport.mqtt.sparkplugB.SparkPlugMsgTypes.DDATA;

@Slf4j
public class SparkPlugDecodeService extends SparkPlugUtils {

    private Map<String, Boolean> deviceMap = new HashMap<>();

    public void processSparkPlugbPostTelemetry(GatewaySessionCtx ctx, MqttPublishMessage inbound){
        String topicName = inbound.variableHeader().topicName();

        if(topicName.contains(DBIRTH)){
            String device = topicName.replace(SparkPlugMsgTypes.DBIRTH + "/","");
            if(!deviceMap.containsKey(device)) {
                deviceMap.put(device, true);
                convertToPostTelemetry(ctx, inbound);
            }

        }
        else if(topicName.contains(DDATA)){
            String device = topicName.replace(SparkPlugMsgTypes.DDATA + "/","");
            if(deviceMap.containsKey(device) && deviceMap.get(device) == true) {
                convertToPostTelemetry(ctx, inbound);
            }
        }

    }

    private void convertToPostTelemetry(GatewaySessionCtx ctx, MqttPublishMessage inbound){
        try {
            SparkplugBPayload sparkplugBPayload = extractSparkPlugBPayload(inbound.payload());
            Long ts = sparkplugBPayload.getTimestamp().getTime();
            List<KvEntry> kvEntryList = extractKvEntryFromSparkPlugBPayload(sparkplugBPayload);
            int requestId = inbound.variableHeader().messageId();
            SparkPlugDecodedMsg sparkPlugDecodedMsg = new SparkPlugDecodedMsg(requestId, ts, kvEntryList);
            String topic = extractTopicWithoutMsgType(inbound.variableHeader().topicName());
            String deviceName = extractDeviceName(inbound.variableHeader().topicName());
            ctx.onSparkPlugDecodedMsg(sparkPlugDecodedMsg, topic, deviceName);
        } catch (Exception e) {
            log.error("Exception occurred while extracting sparkplugBPayload : " + e );
        }
    }

    private SparkplugBPayload extractSparkPlugBPayload(ByteBuf inboundPayload) throws Exception{
        byte[] payloadByteArray = createByteArray(inboundPayload);
        SparkplugBPayloadDecoder decoder = new SparkplugBPayloadDecoder();
        SparkplugBPayload sparkplugBPayload = decoder.buildFromByteArray(payloadByteArray);
        return sparkplugBPayload;
    }

    private byte[] createByteArray(ByteBuf inboundPayload){
        int bytesCount = inboundPayload.readableBytes();
        byte[] payload = new byte[bytesCount];
        for(int byteIndex = 0; byteIndex < bytesCount; byteIndex++){
            payload[byteIndex] = inboundPayload.getByte(byteIndex);
        }
        return payload;
    }

    private List<KvEntry> extractKvEntryFromSparkPlugBPayload(SparkplugBPayload sparkplugBPayload) throws Exception{
        List<Metric> metricList = sparkplugBPayload.getMetrics();
        List<KvEntry> kvEntryList = new ArrayList<>();
        for (Metric metric : metricList) {
            createKvEntryByDatatype(metric, kvEntryList);
        }
        return kvEntryList;
    }

    private void createKvEntryByDatatype(Metric metric, List<KvEntry> kvEntryList){
        MetricDataType dataType = metric.getDataType();
        if(dataType.getClazz() == String.class){
            kvEntryList.add(new StringDataEntry(metric.getName(), (String) metric.getValue()));
        }else if(dataType.getClazz() == Long.class){
            kvEntryList.add(new LongDataEntry(metric.getName(), (Long) metric.getValue()));
        }else if(dataType.getClazz() == Double.class){
            kvEntryList.add(new DoubleDataEntry(metric.getName(),(Double) metric.getValue()));
        }else if(dataType.getClazz() == Boolean.class){
            kvEntryList.add(new BooleanDataEntry(metric.getName(),(Boolean)metric.getValue()));
        }
    }

    public void updateDeviceMapState(){
        deviceMap.forEach((k, v) -> {
            deviceMap.put(k, false);
        });
    }

}
