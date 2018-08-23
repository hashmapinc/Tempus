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
package com.hashmapinc.server.dao.depthseries;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hashmapinc.server.common.data.kv.*;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Created by ashvayka on 20.02.17.
 */
@Slf4j
public class AggregatePartitionsFunction implements com.google.common.base.Function<List<ResultSet>, Optional<DsKvEntry>> {

    private static final int LONG_CNT_POS = 0;
    private static final int DOUBLE_CNT_POS = 1;
    private static final int BOOL_CNT_POS = 2;
    private static final int STR_CNT_POS = 3;
    private static final int JSON_CNT_POS = 4;
    private static final int LONG_POS = 5;
    private static final int DOUBLE_POS = 6;
    private static final int BOOL_POS = 7;
    private static final int STR_POS = 8;
    private static final int JSON_POS = 9;

    private final DepthAggregation depthAggregation;
    private final String key;
    private final Double ds;

    public AggregatePartitionsFunction(DepthAggregation depthAggregation, String key, Double ds) {
        this.depthAggregation = depthAggregation;
        this.key = key;
        this.ds = ds;
    }

    @Override
    public Optional<DsKvEntry> apply(@Nullable List<ResultSet> rsList) {
        try {
            log.trace("[{}][{}][{}] Going to aggregate data", key, ds, depthAggregation);
            if (rsList == null || rsList.isEmpty()) {
                return Optional.empty();
            }

            AggregationResult aggResult = new AggregationResult();

            for (ResultSet rs : rsList) {
                for (Row row : rs.all()) {
                   processResultSetRow(row,aggResult);
                }
            }
            return processAggregationResult(aggResult);
        }catch (Exception e){
            log.error("[{}][{}][{}] Failed to aggregate data", key, ds, depthAggregation, e);
            return Optional.empty();
        }
    }

    private void processResultSetRow(Row row, AggregationResult aggResult) {
        long curCount;

        Long curLValue = null;
        Double curDValue = null;
        Boolean curBValue = null;
        String curSValue = null;
        JsonNode curJValue = null;

        long longCount = row.getLong(LONG_CNT_POS);
        long doubleCount = row.getLong(DOUBLE_CNT_POS);
        long boolCount = row.getLong(BOOL_CNT_POS);
        long strCount = row.getLong(STR_CNT_POS);
        long jsonCount = row.getLong(JSON_CNT_POS);

        if (longCount > 0) {
            aggResult.dataType = DataType.LONG;
            curCount = longCount;
            curLValue = getLongValue(row);
        } else if (doubleCount > 0) {
            aggResult.dataType = DataType.DOUBLE;
            curCount = doubleCount;
            curDValue = getDoubleValue(row);
        } else if (boolCount > 0) {
            aggResult.dataType = DataType.BOOLEAN;
            curCount = boolCount;
            curBValue = getBooleanValue(row);
        } else if (strCount > 0) {
            aggResult.dataType = DataType.STRING;
            curCount = strCount;
            curSValue = getStringValue(row);
        } else if (jsonCount > 0) {
            aggResult.dataType = DataType.JSON;
            curCount = jsonCount;
            curJValue = getJsonValue(row);
        } else {
            return;
        }

        if (depthAggregation == DepthAggregation.COUNT) {
            aggResult.count += curCount;
        } else if (depthAggregation == DepthAggregation.AVG || depthAggregation == DepthAggregation.SUM) {
            processAvgOrSumAggregation(aggResult, curCount, curLValue, curDValue);
        } else if (depthAggregation == DepthAggregation.MIN) {
            processMinAggregation(aggResult, curLValue, curDValue, curBValue, curSValue, curJValue);
        } else if (depthAggregation == DepthAggregation.MAX) {
            processMaxAggregation(aggResult, curLValue, curDValue, curBValue, curSValue, curJValue);
        }

    }

    private void processMaxAggregation(AggregationResult aggResult, Long curLValue, Double curDValue, Boolean curBValue, String curSValue, JsonNode curJValue) {
        if (curDValue != null) {
            aggResult.dValue = aggResult.dValue == null ? curDValue : Math.max(aggResult.dValue, curDValue);
        } else if (curLValue != null) {
            aggResult.lValue = aggResult.lValue == null ? curLValue : Math.max(aggResult.lValue, curLValue);
        } else if (curBValue != null) {
            aggResult.bValue = aggResult.bValue == null ? curBValue : aggResult.bValue || curBValue;
        } else if (curSValue != null) {
            if (aggResult.sValue == null || curSValue.compareTo(aggResult.sValue) > 0) {
                aggResult.sValue = curSValue;
            }
        } else if (curJValue != null) {
            aggResult.jValue = curJValue;
        }
    }

    private void processMinAggregation(AggregationResult aggResult, Long curLValue, Double curDValue, Boolean curBValue, String curSValue, JsonNode curJValue) {
        if (curDValue != null) {
            aggResult.dValue = aggResult.dValue == null ? curDValue : Math.min(aggResult.dValue, curDValue);
        } else if (curLValue != null) {
            aggResult.lValue = aggResult.lValue == null ? curLValue : Math.min(aggResult.lValue, curLValue);
        } else if (curBValue != null) {
            aggResult.bValue = aggResult.bValue == null ? curBValue : aggResult.bValue && curBValue;
        } else if (curSValue != null) {
            if (aggResult.sValue == null || curSValue.compareTo(aggResult.sValue) < 0) {
                aggResult.sValue = curSValue;
            }
        } else if (curJValue != null) {
            aggResult.jValue = curJValue;
        }
    }

    private void processAvgOrSumAggregation(AggregationResult aggResult, long curCount, Long curLValue, Double curDValue) {
        aggResult.count += curCount;
        if (curDValue != null) {
            aggResult.dValue = aggResult.dValue == null ? curDValue : aggResult.dValue + curDValue;
        } else if (curLValue != null) {
            aggResult.lValue = aggResult.lValue == null ? curLValue : aggResult.lValue + curLValue;
        }
    }

    private Optional<DsKvEntry> processAggregationResult(AggregationResult aggResult) {
        Optional<DsKvEntry> result;

        if (aggResult.dataType == null) {
            result = Optional.empty();
        } else if (depthAggregation == DepthAggregation.COUNT) {
            result = Optional.of(new BasicDsKvEntry(ds, new LongDataEntry(key, (long) aggResult.count)));
        } else if (depthAggregation == DepthAggregation.AVG || depthAggregation == DepthAggregation.SUM) {
            result = processAvgOrSumResult(aggResult);
        } else if (depthAggregation == DepthAggregation.MIN || depthAggregation == DepthAggregation.MAX) {
           result = processMinOrMaxResult(aggResult);
        } else {
            result = Optional.empty();
        }

        if (!result.isPresent()) {
            log.trace("[{}][{}][{}] Aggregated data is empty.", key, ds, depthAggregation);
        }
        return result;
    }

    private Optional<DsKvEntry> processAvgOrSumResult(AggregationResult aggResult) {
        if (aggResult.count == 0 || (aggResult.dataType == DataType.DOUBLE && aggResult.dValue == null) || (aggResult.dataType == DataType.LONG && aggResult.lValue == null)) {
            return Optional.empty();
        } else if (aggResult.dataType == DataType.DOUBLE) {
            return Optional.of(new BasicDsKvEntry(ds, new DoubleDataEntry(key, depthAggregation == DepthAggregation.SUM ? aggResult.dValue : (aggResult.dValue / aggResult.count))));
        } else if (aggResult.dataType == DataType.LONG) {
            return Optional.of(new BasicDsKvEntry(ds, new LongDataEntry(key, depthAggregation == DepthAggregation.SUM ? aggResult.lValue : (aggResult.lValue / aggResult.count))));
        }
        return Optional.empty();
    }

    private Optional<DsKvEntry> processMinOrMaxResult(AggregationResult aggResult) {
        if (aggResult.dataType == DataType.DOUBLE) {
            return Optional.of(new BasicDsKvEntry(ds, new DoubleDataEntry(key, aggResult.dValue)));
        } else if (aggResult.dataType == DataType.LONG) {
            return Optional.of(new BasicDsKvEntry(ds, new LongDataEntry(key, aggResult.lValue)));
        } else if (aggResult.dataType == DataType.STRING) {
            return Optional.of(new BasicDsKvEntry(ds, new StringDataEntry(key, aggResult.sValue)));
        } else if (aggResult.dataType == DataType.BOOLEAN){
            return Optional.of(new BasicDsKvEntry(ds, new BooleanDataEntry(key, aggResult.bValue)));
        } else if (aggResult.dataType == DataType.JSON) {
            return Optional.of(new BasicDsKvEntry(ds, new JsonDataEntry(key, aggResult.jValue)));
        }
        return Optional.empty();
    }



        private Boolean getBooleanValue(Row row) {
        if (depthAggregation == DepthAggregation.MIN || depthAggregation == DepthAggregation.MAX) {
            return row.getBool(BOOL_POS);
        } else {
            return false;
        }
    }

    private JsonNode getJsonValue(Row row) {
        if (depthAggregation == DepthAggregation.MIN || depthAggregation == DepthAggregation.MAX) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                return mapper.readTree(row.getString(JSON_POS));
            } catch (IOException ex) {
                log.error(ex.getMessage(),ex);
                return null;
            }
        } else {
            return null;
        }
    }

    private String getStringValue(Row row) {
        if (depthAggregation == DepthAggregation.MIN || depthAggregation == DepthAggregation.MAX) {
            return row.getString(STR_POS);
        } else {
            return null;
        }
    }

    private Long getLongValue(Row row) {
        if (depthAggregation == DepthAggregation.MIN || depthAggregation == DepthAggregation.MAX
                || depthAggregation == DepthAggregation.SUM || depthAggregation == DepthAggregation.AVG) {
            return row.getLong(LONG_POS);
        } else {
            return null;
        }
    }

    private Double getDoubleValue(Row row) {
        if (depthAggregation == DepthAggregation.MIN || depthAggregation == DepthAggregation.MAX
                || depthAggregation == DepthAggregation.SUM || depthAggregation == DepthAggregation.AVG) {
            return row.getDouble(DOUBLE_POS);
        } else {
            return null;
        }
    }

    private class AggregationResult {
        DataType dataType = null;
        Boolean bValue = null;
        String sValue = null;
        Double dValue = null;
        Long lValue = null;
        JsonNode jValue = null;
        long count = 0;
    }

}
