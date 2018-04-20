package com.hashmapinc.server.transport.mqtt.sparkplugB;

import com.cirruslink.sparkplug.SparkplugException;
import com.cirruslink.sparkplug.message.SparkplugBPayloadEncoder;
import com.cirruslink.sparkplug.message.model.Metric;
import com.cirruslink.sparkplug.message.model.MetricDataType;
import com.cirruslink.sparkplug.message.model.SparkplugBPayload;
import lombok.extern.slf4j.Slf4j;
import com.hashmapinc.server.common.data.kv.BasicTsKvEntry;
import com.hashmapinc.server.common.data.kv.DataType;
import com.hashmapinc.server.common.data.kv.TsKvEntry;
import com.hashmapinc.server.common.msg.kv.TelemetryKVMsg;
import com.hashmapinc.server.transport.mqtt.session.DeviceSessionCtx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
public class SparkPlugEncodeService {

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
}
