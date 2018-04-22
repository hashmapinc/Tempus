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
package com.hashmapinc.server.dao.timeseries;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hashmapinc.server.common.data.kv.*;
import lombok.extern.slf4j.Slf4j;
import com.hashmapinc.server.common.data.kv.*;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Created by ashvayka on 20.02.17.
 */
@Slf4j
public class AggregatePartitionsFunction implements com.google.common.base.Function<List<ResultSet>, Optional<TsKvEntry>> {

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

    private final Aggregation aggregation;
    private final String key;
    private final long ts;

    public AggregatePartitionsFunction(Aggregation aggregation, String key, long ts) {
        this.aggregation = aggregation;
        this.key = key;
        this.ts = ts;
    }

    @Override
    public Optional<TsKvEntry> apply(@Nullable List<ResultSet> rsList) {
        try {
            log.trace("[{}][{}][{}] Going to aggregate data", key, ts, aggregation);
            if (rsList == null || rsList.isEmpty()) {
                return Optional.empty();
            }

            Boolean bValue = null;
            String sValue = null;
            Double dValue = null;
            Long lValue = null;
            JsonNode jValue = null;
            DataType dataType = null;
            long count = 0;

            for (ResultSet rs : rsList) {
                for (Row row : rs.all()) {
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
                        dataType = DataType.LONG;
                        curCount = longCount;
                        curLValue = getLongValue(row);
                    } else if (doubleCount > 0) {
                        dataType = DataType.DOUBLE;
                        curCount = doubleCount;
                        curDValue = getDoubleValue(row);
                    } else if (boolCount > 0) {
                        dataType = DataType.BOOLEAN;
                        curCount = boolCount;
                        curBValue = getBooleanValue(row);
                    } else if (strCount > 0) {
                        dataType = DataType.STRING;
                        curCount = strCount;
                        curSValue = getStringValue(row);
                    } else if (jsonCount > 0) {
                        dataType = DataType.JSON;
                        curCount = jsonCount;
                        curJValue = getJsonValue(row);
                    } else {
                        continue;
                    }

                    if (aggregation == Aggregation.COUNT) {
                        count += curCount;
                    } else if (aggregation == Aggregation.AVG || aggregation == Aggregation.SUM) {
                        count += curCount;
                        if (curDValue != null) {
                            dValue = dValue == null ? curDValue : dValue + curDValue;
                        } else if (curLValue != null) {
                            lValue = lValue == null ? curLValue : lValue + curLValue;
                        }
                    } else if (aggregation == Aggregation.MIN) {
                        if (curDValue != null) {
                            dValue = dValue == null ? curDValue : Math.min(dValue, curDValue);
                        } else if (curLValue != null) {
                            lValue = lValue == null ? curLValue : Math.min(lValue, curLValue);
                        } else if (curBValue != null) {
                            bValue = bValue == null ? curBValue : bValue && curBValue;
                        } else if (curSValue != null) {
                            if (sValue == null || curSValue.compareTo(sValue) < 0) {
                                sValue = curSValue;
                            }
                        } else if (curJValue != null) {
                            if (jValue == null || curJValue.toString().compareTo(jValue.toString()) < 0) {
                                jValue = curJValue;
                            }
                        }
                    } else if (aggregation == Aggregation.MAX) {
                        if (curDValue != null) {
                            dValue = dValue == null ? curDValue : Math.max(dValue, curDValue);
                        } else if (curLValue != null) {
                            lValue = lValue == null ? curLValue : Math.max(lValue, curLValue);
                        } else if (curBValue != null) {
                            bValue = bValue == null ? curBValue : bValue || curBValue;
                        } else if (curSValue != null) {
                            if (sValue == null || curSValue.compareTo(sValue) > 0) {
                                sValue = curSValue;
                            }
                        } else if (curJValue != null) {
                            if (jValue == null || curJValue.toString().compareTo(jValue.toString()) > 0) {
                                jValue = curJValue;
                            }
                        }
                    }
                }
            }
            if (dataType == null) {
                return Optional.empty();
            } else if (aggregation == Aggregation.COUNT) {
                return Optional.of(new BasicTsKvEntry(ts, new LongDataEntry(key, (long) count)));
            } else if (aggregation == Aggregation.AVG || aggregation == Aggregation.SUM) {
                if (count == 0 || (dataType == DataType.DOUBLE && dValue == null) || (dataType == DataType.LONG && lValue == null)) {
                    return Optional.empty();
                } else if (dataType == DataType.DOUBLE) {
                    return Optional.of(new BasicTsKvEntry(ts, new DoubleDataEntry(key, aggregation == Aggregation.SUM ? dValue : (dValue / count))));
                } else if (dataType == DataType.LONG) {
                    return Optional.of(new BasicTsKvEntry(ts, new LongDataEntry(key, aggregation == Aggregation.SUM ? lValue : (lValue / count))));
                }
            } else if (aggregation == Aggregation.MIN || aggregation == Aggregation.MAX) {
                if (dataType == DataType.DOUBLE) {
                    return Optional.of(new BasicTsKvEntry(ts, new DoubleDataEntry(key, dValue)));
                } else if (dataType == DataType.LONG) {
                    return Optional.of(new BasicTsKvEntry(ts, new LongDataEntry(key, lValue)));
                } else if (dataType == DataType.STRING) {
                    return Optional.of(new BasicTsKvEntry(ts, new StringDataEntry(key, sValue)));
                } else if (dataType == DataType.BOOLEAN) {
                    return Optional.of(new BasicTsKvEntry(ts, new BooleanDataEntry(key, bValue)));
                } else if (dataType == DataType.JSON) {
                    return Optional.of(new BasicTsKvEntry(ts, new JsonDataEntry(key, jValue)));
                }
            }
            log.trace("[{}][{}][{}] Aggregated data is empty.", key, ts, aggregation);
            return Optional.empty();
        }catch (Exception e){
            log.error("[{}][{}][{}] Failed to aggregate data", key, ts, aggregation, e);
            return Optional.empty();
        }
    }

    private void processResultSetRow(Row row, AggregationResult aggResult) {
        long curCount;

        Long curLValue = null;
        Double curDValue = null;
        Boolean curBValue = null;
        String curSValue = null;

        long longCount = row.getLong(LONG_CNT_POS);
        long doubleCount = row.getLong(DOUBLE_CNT_POS);
        long boolCount = row.getLong(BOOL_CNT_POS);
        long strCount = row.getLong(STR_CNT_POS);

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
        } else {
            return;
        }

        if (aggregation == Aggregation.COUNT) {
            aggResult.count += curCount;
        } else if (aggregation == Aggregation.AVG || aggregation == Aggregation.SUM) {
            processAvgOrSumAggregation(aggResult, curCount, curLValue, curDValue);
        } else if (aggregation == Aggregation.MIN) {
            processMinAggregation(aggResult, curLValue, curDValue, curBValue, curSValue);
        } else if (aggregation == Aggregation.MAX) {
            processMaxAggregation(aggResult, curLValue, curDValue, curBValue, curSValue);
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

    private void processMinAggregation(AggregationResult aggResult, Long curLValue, Double curDValue, Boolean curBValue, String curSValue) {
        if (curDValue != null) {
            aggResult.dValue = aggResult.dValue == null ? curDValue : Math.min(aggResult.dValue, curDValue);
        } else if (curLValue != null) {
            aggResult.lValue = aggResult.lValue == null ? curLValue : Math.min(aggResult.lValue, curLValue);
        } else if (curBValue != null) {
            aggResult.bValue = aggResult.bValue == null ? curBValue : aggResult.bValue && curBValue;
        } else if (curSValue != null && (aggResult.sValue == null || curSValue.compareTo(aggResult.sValue) < 0)) {
            aggResult.sValue = curSValue;
        }
    }

    private void processMaxAggregation(AggregationResult aggResult, Long curLValue, Double curDValue, Boolean curBValue, String curSValue) {
        if (curDValue != null) {
            aggResult.dValue = aggResult.dValue == null ? curDValue : Math.max(aggResult.dValue, curDValue);
        } else if (curLValue != null) {
            aggResult.lValue = aggResult.lValue == null ? curLValue : Math.max(aggResult.lValue, curLValue);
        } else if (curBValue != null) {
            aggResult.bValue = aggResult.bValue == null ? curBValue : aggResult.bValue || curBValue;
        } else if (curSValue != null && (aggResult.sValue == null || curSValue.compareTo(aggResult.sValue) > 0)) {
            aggResult.sValue = curSValue;
        }
    }

    private Boolean getBooleanValue(Row row) {
        if (aggregation == Aggregation.MIN || aggregation == Aggregation.MAX) {
            return row.getBool(BOOL_POS);
        } else {
            return null; //NOSONAR, null is used for further comparison
        }
    }

    private JsonNode getJsonValue(Row row) {
        if (aggregation == Aggregation.MIN || aggregation == Aggregation.MAX) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                return mapper.readTree(row.getString(JSON_POS));
            } catch (IOException ex) {
                ex.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    private String getStringValue(Row row) {
        if (aggregation == Aggregation.MIN || aggregation == Aggregation.MAX) {
            return row.getString(STR_POS);
        } else {
            return null;
        }
    }

    private Long getLongValue(Row row) {
        if (aggregation == Aggregation.MIN || aggregation == Aggregation.MAX
                || aggregation == Aggregation.SUM || aggregation == Aggregation.AVG) {
            return row.getLong(LONG_POS);
        } else {
            return null;
        }
    }

    private Double getDoubleValue(Row row) {
        if (aggregation == Aggregation.MIN || aggregation == Aggregation.MAX
                || aggregation == Aggregation.SUM || aggregation == Aggregation.AVG) {
            return row.getDouble(DOUBLE_POS);
        } else {
            return null;
        }
    }

    private Optional<TsKvEntry> processAggregationResult(AggregationResult aggResult) {
        Optional<TsKvEntry> result;
        if (aggResult.dataType == null) {
            result = Optional.empty();
        } else if (aggregation == Aggregation.COUNT) {
            result = Optional.of(new BasicTsKvEntry(ts, new LongDataEntry(key, aggResult.count)));
        } else if (aggregation == Aggregation.AVG || aggregation == Aggregation.SUM) {
            result = processAvgOrSumResult(aggResult);
        } else if (aggregation == Aggregation.MIN || aggregation == Aggregation.MAX) {
            result = processMinOrMaxResult(aggResult);
        } else {
            result = Optional.empty();
        }
        if (!result.isPresent()) {
            log.trace("[{}][{}][{}] Aggregated data is empty.", key, ts, aggregation);
        }
        return result;
    }

    private Optional<TsKvEntry> processAvgOrSumResult(AggregationResult aggResult) {
        if (aggResult.count == 0 || (aggResult.dataType == DataType.DOUBLE && aggResult.dValue == null) || (aggResult.dataType == DataType.LONG && aggResult.lValue == null)) {
            return Optional.empty();
        } else if (aggResult.dataType == DataType.DOUBLE) {
            return Optional.of(new BasicTsKvEntry(ts, new DoubleDataEntry(key, aggregation == Aggregation.SUM ? aggResult.dValue : (aggResult.dValue / aggResult.count))));
        } else if (aggResult.dataType == DataType.LONG) {
            return Optional.of(new BasicTsKvEntry(ts, new LongDataEntry(key, aggregation == Aggregation.SUM ? aggResult.lValue : (aggResult.lValue / aggResult.count))));
        }
        return Optional.empty();
    }

    private Optional<TsKvEntry> processMinOrMaxResult(AggregationResult aggResult) {
        if (aggResult.dataType == DataType.DOUBLE) {
            return Optional.of(new BasicTsKvEntry(ts, new DoubleDataEntry(key, aggResult.dValue)));
        } else if (aggResult.dataType == DataType.LONG) {
            return Optional.of(new BasicTsKvEntry(ts, new LongDataEntry(key, aggResult.lValue)));
        } else if (aggResult.dataType == DataType.STRING) {
            return Optional.of(new BasicTsKvEntry(ts, new StringDataEntry(key, aggResult.sValue)));
        } else {
            return Optional.of(new BasicTsKvEntry(ts, new BooleanDataEntry(key, aggResult.bValue)));
        }
    }

    private class AggregationResult {
        DataType dataType = null;
        Boolean bValue = null;
        String sValue = null;
        Double dValue = null;
        Long lValue = null;
        long count = 0;
    }
}
