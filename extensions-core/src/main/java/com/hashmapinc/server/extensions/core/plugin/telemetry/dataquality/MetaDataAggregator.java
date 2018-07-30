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
package com.hashmapinc.server.extensions.core.plugin.telemetry.dataquality;

import com.hashmapinc.server.common.data.EntityType;
import com.hashmapinc.server.common.data.TagMetaData;
import com.hashmapinc.server.common.data.id.EntityId;
import com.hashmapinc.server.common.data.kv.*;
import com.hashmapinc.server.extensions.api.plugins.PluginCallback;
import com.hashmapinc.server.extensions.api.plugins.PluginContext;
import com.hashmapinc.server.extensions.core.plugin.telemetry.dataquality.data.AggregatedMetaData;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class MetaDataAggregator {
    private long aggregationPeriod;
    private double depthAggregationPeriod;
    private PluginContext ctx;
    private EntityId entityId;
    private static final int LIMIT = 10000;

    public MetaDataAggregator(PluginContext ctx, EntityId entityId){
        this.ctx = ctx;
        this.entityId = entityId;
    }

    public void setAggregationPeriod(long aggregationPeriod) {
        this.aggregationPeriod = aggregationPeriod;
    }

    public void setDepthAggregationPeriod(double depthAggregationPeriod) {
        this.depthAggregationPeriod = depthAggregationPeriod;
    }

    public void aggregateMetaData(Long endTs, List<KvEntry> kvEntries){
        Set<String> keys = new HashSet<>();
        for (KvEntry entry: kvEntries){
            keys.add(entry.getKey());
        }
        List<TsKvQuery> queries = keys.stream().map(key -> new BaseTsKvQuery(key,endTs - aggregationPeriod, endTs, aggregationPeriod, LIMIT, Aggregation.NONE)).collect(Collectors.toList());
        ctx.loadTimeseries(entityId, queries, new PluginCallback<List<TsKvEntry>>() {
            @Override
            public void onSuccess(PluginContext ctx, List<TsKvEntry> data) {
                if(!data.isEmpty()) {
                    AggregationFunc<TsKvEntry> aggregationFunc = new AggregationFunc<>(data);
                    for (KvEntry entry: kvEntries) {
                        aggregateForKvEntry(aggregationFunc, entry);
                    }
                }
            }
            @Override
            public void onFailure(PluginContext ctx, Exception e) {
                log.info("Failed to fetch TsKvEntry List.");
            }
        });
    }

    public void aggregateDepthMetaData(double endDs, List<KvEntry> kvEntries){
        Set<String> keys = new HashSet<>();
        for (KvEntry entry: kvEntries){
            keys.add(entry.getKey());
        }
        List<DsKvQuery> queries = keys.stream().map(key -> new BaseDsKvQuery(key,endDs - depthAggregationPeriod, endDs, depthAggregationPeriod, LIMIT, DepthAggregation.NONE)).collect(Collectors.toList());
        ctx.loadDepthSeries(entityId, queries, new PluginCallback<List<DsKvEntry>>() {
            @Override
            public void onSuccess(PluginContext ctx, List<DsKvEntry> data) {
                if(!data.isEmpty()) {
                    AggregationFunc<DsKvEntry> aggregationFunc = new AggregationFunc<>(data);
                    for (KvEntry entry : kvEntries) {
                        aggregateForKvEntry(aggregationFunc, entry);
                    }
                }
            }
            @Override
            public void onFailure(PluginContext ctx, Exception e) {
                log.info("Failed to fetch TsKvEntry List.");
            }
        });
    }

    private void aggregateForKvEntry(AggregationFunc aggregationFunc, KvEntry entry){
        Double avg = aggregationFunc.calAvg(entry.getKey());
        double min = aggregationFunc.calMin(entry.getKey());
        double max = aggregationFunc.calMax(entry.getKey());
        Double median = aggregationFunc.calMedian(entry.getKey());
        String unit = "no unit";
        if(((BasicKvEntry)entry).getUnit().isPresent()){
            unit = ((BasicKvEntry)entry).getUnit().get();
        }
        AggregatedMetaData aggregatedMetaData = new AggregatedMetaData(avg, max, min, avg, median);
        saveToTagMetaData(aggregatedMetaData, entry.getKey(), unit);
    }

    private void saveToTagMetaData(AggregatedMetaData aggregatedMetaData, String key, String unit){
        TagMetaData tagMetaData = new TagMetaData();
        tagMetaData.setEntityId(entityId.getId().toString());
        tagMetaData.setEntityType(EntityType.DEVICE);
        tagMetaData.setAvgFrequency(aggregatedMetaData.getAvgFrequency());
        tagMetaData.setMaxFrequency(aggregatedMetaData.getMaxFrequency());
        tagMetaData.setMinFrequency(aggregatedMetaData.getMinFrequency());
        tagMetaData.setMeanFrequency(aggregatedMetaData.getMeanFrequency());
        tagMetaData.setMedianFrequency(aggregatedMetaData.getMedianFrequency());
        tagMetaData.setKey(key);
        tagMetaData.setUnit(unit);

        ctx.saveTagMetaData(entityId, tagMetaData, new PluginCallback<Void>() {
            @Override
            public void onSuccess(PluginContext ctx, Void value) {
                log.debug("Saved data to TagMetaData");
            }

            @Override
            public void onFailure(PluginContext ctx, Exception e) {
                log.info("Unable to save to tagMetaData");
            }
        });
    }
}
