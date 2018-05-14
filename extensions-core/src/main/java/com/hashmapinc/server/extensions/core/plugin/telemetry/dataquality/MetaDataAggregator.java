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
import com.hashmapinc.server.common.data.TagMetaDataQuality;
import com.hashmapinc.server.common.data.id.EntityId;
import com.hashmapinc.server.common.data.kv.*;
import com.hashmapinc.server.extensions.api.plugins.PluginCallback;
import com.hashmapinc.server.extensions.api.plugins.PluginContext;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class MetaDataAggregator {
    private long aggregationPeriod;
    private PluginContext ctx;
    private EntityId entityId;
    private final int limit = 10000;

    public MetaDataAggregator(PluginContext ctx, long aggregationPeriod, EntityId entityId){
        this.aggregationPeriod = aggregationPeriod;
        this.ctx = ctx;
        this.entityId = entityId;
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
                        log.debug("Key " + key);
                        Double avg = calAvg(data, key);
                        log.debug("Avg " + avg);
                        Long min = calMin(data, key);
                        log.debug("Min " + min);
                        Long max = calMax(data, key);
                        log.debug("Max " + max);
                        Double median = calMedian(data, key);
                        log.debug("Median " + median);
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


    private Double calAvg(List<TsKvEntry> tsKvEntries, String key){
        Double avg = 0.0;
        long sum = 0;
        int count = 0;
        for (TsKvEntry entry: tsKvEntries){
            if(entry.getKey().contentEquals(key)) {
                long diff = ((BasicTsKvEntry) entry).getTsDiff();
                sum += diff;
                count++;
            }
        }

        if(count > 0)
            avg = (double)(sum/count);

        return avg;
    }

    private Long calMin(List<TsKvEntry> tsKvEntries, String key){
        long min = 9999999999L;
        for (TsKvEntry entry: tsKvEntries){
            if(entry.getKey().contentEquals(key)) {
                long diff = ((BasicTsKvEntry) entry).getTsDiff();
                if (diff < min)
                    min = diff;
            }
        }

        return min;
    }

    private Long calMax(List<TsKvEntry> tsKvEntries, String key){
        long max = -1;
        for (TsKvEntry entry: tsKvEntries){
            if(entry.getKey().contentEquals(key)) {
                long diff = ((BasicTsKvEntry) entry).getTsDiff();
                if (diff > max)
                    max = diff;
            }
        }

        return max;
    }

    private Double calMedian(List<TsKvEntry> tsKvEntries, String key){
        Double median = 0.0;
        List<Long> keyFrequencyList = new ArrayList<>();
        for (TsKvEntry entry: tsKvEntries){
            if(entry.getKey().contentEquals(key)) {
                long diff = ((BasicTsKvEntry) entry).getTsDiff();
                keyFrequencyList.add(diff);
            }
        }
        if(!keyFrequencyList.isEmpty()){
            int listSize = keyFrequencyList.size();
            if(listSize % 2 == 0){
                int index = listSize / 2;
                median = ((double)(keyFrequencyList.get(index)) + (double)(keyFrequencyList.get(index - 1)))/2;
            }else {
                median = (double)(keyFrequencyList.get(listSize / 2));
            }
        }
        return median;
    }

    private void saveToTagMetaData(Double avg, Long min, Long max, Double median,String key){
        TagMetaDataQuality tagMetaDataQuality = new TagMetaDataQuality();
        tagMetaDataQuality.setEntityId(entityId.getId().toString());
        tagMetaDataQuality.setEntityType(EntityType.DEVICE);
        tagMetaDataQuality.setAvgFrequency(avg);
        tagMetaDataQuality.setKey(key);
        tagMetaDataQuality.setMaxFrequency(max);
        tagMetaDataQuality.setMinFrequency(min);
        tagMetaDataQuality.setMeanFrequency(avg);
        tagMetaDataQuality.setMedianFrequency(median);
        ctx.saveTagMetaData(entityId, tagMetaDataQuality, new PluginCallback<Void>() {
            @Override
            public void onSuccess(PluginContext ctx, Void value) {
                log.debug("Saved data to TagMetaDataQuality");
            }

            @Override
            public void onFailure(PluginContext ctx, Exception e) {
                log.info("Unable to save to tagMetaDataQuality");
            }
        });
    }
}
