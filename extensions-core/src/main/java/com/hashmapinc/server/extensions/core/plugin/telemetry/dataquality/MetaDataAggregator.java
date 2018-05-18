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
package com.hashmapinc.server.extensions.core.plugin.telemetry.dataquality;

import com.hashmapinc.server.common.data.EntityType;
import com.hashmapinc.server.common.data.TagMetaData;
import com.hashmapinc.server.common.data.id.EntityId;
import com.hashmapinc.server.common.data.kv.*;
import com.hashmapinc.server.extensions.api.plugins.PluginCallback;
import com.hashmapinc.server.extensions.api.plugins.PluginContext;
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
    private final int limit = 10000;

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

    public void aggregateMetaData(Long endTs, List<TsKvEntry> tsKvEntries){
        Set<String> keys = new HashSet<>();
        for (TsKvEntry entry: tsKvEntries){
            keys.add(entry.getKey());
        }
        List<TsKvQuery> queries = keys.stream().map(key -> new BaseTsKvQuery(key,endTs - aggregationPeriod, endTs, aggregationPeriod, limit, Aggregation.NONE)).collect(Collectors.toList());
        ctx.loadTimeseries(entityId, queries, new PluginCallback<List<TsKvEntry>>() {
            @Override
            public void onSuccess(PluginContext ctx, List<TsKvEntry> data) {
                if(!data.isEmpty()) {
                    for (String key : keys) {
                        AggregationFunc<TsKvEntry> aggregationFunc = new AggregationFunc<>(data);
                        Double avg = aggregationFunc.calAvg(key);
                        double min = aggregationFunc.calMin(key);
                        double max = aggregationFunc.calMax(key);
                        Double median = aggregationFunc.calMedian(key);
                        saveToTagMetaData(avg, min, max, median, key);
                    }
                }
            }
            @Override
            public void onFailure(PluginContext ctx, Exception e) {
                log.info("Failed to fetch TsKvEntry List.");
            }
        });
    }

    public void aggregateDepthMetaData(double endDs, List<DsKvEntry> dsKvEntries){
        Set<String> keys = new HashSet<>();
        for (DsKvEntry entry: dsKvEntries){
            keys.add(entry.getKey());
        }
        List<DsKvQuery> queries = keys.stream().map(key -> new BaseDsKvQuery(key,endDs - depthAggregationPeriod, endDs, depthAggregationPeriod, limit, DepthAggregation.NONE)).collect(Collectors.toList());
        ctx.loadDepthSeries(entityId, queries, new PluginCallback<List<DsKvEntry>>() {
            @Override
            public void onSuccess(PluginContext ctx, List<DsKvEntry> data) {
                if(!data.isEmpty()) {
                    for (String key : keys) {
                        AggregationFunc<DsKvEntry> aggregationFunc = new AggregationFunc<>(data);
                        Double avg = aggregationFunc.calAvg(key);
                        double min = aggregationFunc.calMin(key);
                        double max = aggregationFunc.calMax(key);
                        Double median = aggregationFunc.calMedian(key);
                        saveToTagMetaData(avg, min, max, median, key);
                    }
                }
            }
            @Override
            public void onFailure(PluginContext ctx, Exception e) {
                log.info("Failed to fetch TsKvEntry List.");
            }
        });
    }

    private void saveToTagMetaData(Double avg, double min, double max, double median,String key){
        TagMetaData tagMetaData = new TagMetaData();
        tagMetaData.setEntityId(entityId.getId().toString());
        tagMetaData.setEntityType(EntityType.DEVICE);
        tagMetaData.setAvgFrequency(avg);
        tagMetaData.setKey(key);
        tagMetaData.setMaxFrequency(max);
        tagMetaData.setMinFrequency(min);
        tagMetaData.setMeanFrequency(avg);
        tagMetaData.setMedianFrequency(median);
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
