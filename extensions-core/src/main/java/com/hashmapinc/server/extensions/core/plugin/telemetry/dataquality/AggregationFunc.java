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

import com.hashmapinc.server.common.data.kv.*;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class AggregationFunc<T> {
    List<T> list;
    AggregationFunc(List<T> list){
        this.list = list;
    }
    public double calAvg(String key){
        double avg = 0.0;
        double sum = 0.0;
        int count = 0;
        for (T entry: list) {
            if(entry instanceof TsKvEntry){
                BasicTsKvEntry basicTsKvEntry = ((BasicTsKvEntry)entry);
                if(basicTsKvEntry.getKey().contentEquals(key)){
                    long diff = basicTsKvEntry.getTsDiff();
                    sum += diff;
                    count++;
                }
            }
            else if(entry instanceof DsKvEntry){
                BasicDsKvEntry basicDsKvEntry = ((BasicDsKvEntry)entry);
                if(basicDsKvEntry.getKey().contentEquals(key)){
                    double diff = basicDsKvEntry.getDsDiff();
                    sum += diff;
                    count++;
                }
            }
        }

        if(count > 0)
            avg = (sum/count);

        return avg;
    }

    public double calMin(String key){
        double min = 9999999999.0;

        for (T entry: list) {
            if(entry instanceof TsKvEntry){
                BasicTsKvEntry basicTsKvEntry = ((BasicTsKvEntry)entry);
                if(basicTsKvEntry.getKey().contentEquals(key)){
                    long diff = basicTsKvEntry.getTsDiff();
                    if (diff < min)
                        min = diff;
                }
            }
            else if(entry instanceof DsKvEntry){
                BasicDsKvEntry basicDsKvEntry = ((BasicDsKvEntry)entry);
                if(basicDsKvEntry.getKey().contentEquals(key)){
                    double diff = basicDsKvEntry.getDsDiff();
                    if (diff < min)
                        min = diff;
                }
            }
        }

        return min;
    }

    public double calMax(String key){
        double max = -99999999.0;

        for (T entry: list) {
            if(entry instanceof TsKvEntry){
                BasicTsKvEntry basicTsKvEntry = ((BasicTsKvEntry)entry);
                if(basicTsKvEntry.getKey().contentEquals(key)){
                    long diff = basicTsKvEntry.getTsDiff();
                    if (diff > max)
                        max = diff;
                }
            }
            else if(entry instanceof DsKvEntry){
                BasicDsKvEntry basicDsKvEntry = ((BasicDsKvEntry)entry);
                if(basicDsKvEntry.getKey().contentEquals(key)){
                    double diff = basicDsKvEntry.getDsDiff();
                    if (diff > max)
                        max = diff;
                }
            }
        }

        return max;
    }

    public double calMedian(String key){
        Double median = 0.0;
        List<Double> keyFrequencyList = new ArrayList<>();
        for (T entry: list) {
            if(entry instanceof TsKvEntry){
                BasicTsKvEntry basicTsKvEntry = ((BasicTsKvEntry)entry);
                if(basicTsKvEntry.getKey().contentEquals(key)){
                    double diff = basicTsKvEntry.getTsDiff();
                    keyFrequencyList.add(diff);
                }
            }
            else if(entry instanceof DsKvEntry){
                BasicDsKvEntry basicDsKvEntry = ((BasicDsKvEntry)entry);
                if(basicDsKvEntry.getKey().contentEquals(key)){
                    double diff = basicDsKvEntry.getDsDiff();
                    keyFrequencyList.add(diff);
                }
            }
        }

        if(!keyFrequencyList.isEmpty()){
            int listSize = keyFrequencyList.size();
            if(listSize % 2 == 0){
                int index = listSize / 2;
                median = ((keyFrequencyList.get(index)) + (keyFrequencyList.get(index - 1)))/2;
            }else {
                median = (keyFrequencyList.get(listSize / 2));
            }
        }
        return median;
    }

}
