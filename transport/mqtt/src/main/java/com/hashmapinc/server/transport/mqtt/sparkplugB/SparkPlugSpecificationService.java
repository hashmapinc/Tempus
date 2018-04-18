package com.hashmapinc.server.transport.mqtt.sparkplugB;

import com.cirruslink.sparkplug.SparkplugException;
import com.cirruslink.sparkplug.message.SparkplugBPayloadDecoder;
import com.cirruslink.sparkplug.message.SparkplugBPayloadEncoder;
import com.cirruslink.sparkplug.message.model.Metric;
import com.cirruslink.sparkplug.message.model.MetricDataType;
import com.cirruslink.sparkplug.message.model.SparkplugBPayload;
import com.hashmapinc.server.transport.mqtt.session.DeviceSessionCtx;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import lombok.extern.slf4j.Slf4j;
import com.hashmapinc.server.common.data.kv.*;
import com.hashmapinc.server.transport.mqtt.session.GatewaySessionCtx;
import com.hashmapinc.server.common.msg.kv.TelemetryKVMsg;

import java.io.IOException;
import java.util.*;

import static com.hashmapinc.server.transport.mqtt.sparkplugB.SparkPlugMsgTypes.DBIRTH;
import static com.hashmapinc.server.transport.mqtt.sparkplugB.SparkPlugMsgTypes.DDATA;

@Slf4j
public class SparkPlugSpecificationService {

    private Map<String, Boolean> deviceMap = new HashMap<>();

    public static byte[] createSparkPlugPayload(DeviceSessionCtx ctx, TelemetryKVMsg payload){
        List<TsKvEntry> tsKvEntries = payload.getDeviceTelemetry();
        byte[] payloadByteArray = null;
        try {
            List<Metric> metricList = createMetricFromTsKvEntries(tsKvEntries);
            SparkplugBPayload sparkplugBPayload = new SparkplugBPayload(new Date(), metricList, getSeqNum(ctx),
                    newUUID(),
                    null);
            payloadByteArray = encodeSparkPlugBPayload(sparkplugBPayload);

        }catch (SparkplugException e){
            log.error("Problem occured in creating metric from tsKvEntries exception [{}]", e);
        }
        return payloadByteArray;
    }

    private static List<Metric> createMetricFromTsKvEntries(List<TsKvEntry> tsKvEntries) throws SparkplugException{
        List<Metric> metricList = new ArrayList<>();
        for (TsKvEntry tsKvEntry :tsKvEntries) {
            MetricDataType metricDataType = inferTsKvEntryType(tsKvEntry);
            Metric.MetricBuilder metricBuilder = new Metric.MetricBuilder(tsKvEntry.getKey(), metricDataType, tsKvEntry.getValue());
            metricBuilder.timestamp(new Date(tsKvEntry.getTs()));
            metricList.add(metricBuilder.createMetric());
        }
        return metricList;
    }

    private static MetricDataType inferTsKvEntryType(TsKvEntry tsKvEntry){
        BasicTsKvEntry basicTsKvEntry = (BasicTsKvEntry) tsKvEntry;
        if(basicTsKvEntry.getDataType() == DataType.STRING)
            return MetricDataType.String;
        else if(basicTsKvEntry.getDataType() == DataType.DOUBLE)
            return MetricDataType.Double;
        else if(basicTsKvEntry.getDataType() == DataType.LONG)
            return MetricDataType.Int64;
        else if(basicTsKvEntry.getDataType() == DataType.BOOLEAN)
            return MetricDataType.Boolean;

        return MetricDataType.Unknown;
    }

    private static int getSeqNum(DeviceSessionCtx ctx) {
        if(ctx.getSparkPlugMetaData().getSeq() == 256){
            ctx.getSparkPlugMetaData().setSeq(0);
        }
        int seq = ctx.getSparkPlugMetaData().getSeq();
        ctx.getSparkPlugMetaData().setSeq(seq + 1);
        return seq;
    }

    private static String newUUID() {
        return java.util.UUID.randomUUID().toString();
    }

    private static byte[] encodeSparkPlugBPayload(SparkplugBPayload sparkplugBPayload){
        byte[] payloadByteArray = null;
        try {
            payloadByteArray = new SparkplugBPayloadEncoder().getBytes(sparkplugBPayload);
        }catch (IOException e){
            log.error("Exception occured in converting sparkplugBPayload to bytes, exception [{}]", e);
        }
        return payloadByteArray;
    }

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
            ctx.onSparkPlugDecodedMsg(ts, inbound, kvEntryList);
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
