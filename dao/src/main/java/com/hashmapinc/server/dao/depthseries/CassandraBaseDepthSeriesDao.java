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

import com.datastax.driver.core.*;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Function;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.hashmapinc.server.common.data.DeviceDataSet;
import com.hashmapinc.server.common.data.id.EntityId;
import com.hashmapinc.server.common.data.kv.*;
import com.hashmapinc.server.common.msg.exception.TempusRuntimeException;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.nosql.CassandraAbstractAsyncDao;
import com.hashmapinc.server.dao.timeseries.SimpleListenableFuture;
import com.hashmapinc.server.dao.util.NoSqlDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.stream.Collectors;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;

@Component
@Slf4j
@NoSqlDao
public class CassandraBaseDepthSeriesDao extends CassandraAbstractAsyncDao implements DepthSeriesDao {

    private static final Double MIN_AGGREGATION_STEP_MS = 0.1;
    public static final String INSERT_INTO = "INSERT INTO ";
    public static final String SELECT = "SELECT ";
    private static final String EQUAL_PLACEHOLDER = " = ? ";
    public static final String GENERATED_QUERY_FOR_ENTITY_TYPE_AND_ENTITY_ID = "Generated query [{}] for entityType {} and entityId {}";

    @Autowired
    private Environment environment;


    private PreparedStatement partitionInsertStmt;
    private PreparedStatement partitionInsertTtlStmt;
    private PreparedStatement[] latestInsertStmts;
    private PreparedStatement[] saveStmts;
    private PreparedStatement[] saveTtlStmts;
    private PreparedStatement[] fetchStmts;
    private PreparedStatement findLatestStmt;
    private PreparedStatement findAllLatestStmt;

    private boolean isInstall() {
        return environment.acceptsProfiles("install");
    }

    @PostConstruct
    public void init() {
        super.startExecutor();
        if (!isInstall()) {
            getFetchStmt(DepthAggregation.NONE);
        }
    }

    @PreDestroy
    public void stop() {
        super.stopExecutor();
    }

    @Override
    public ListenableFuture<List<DsKvEntry>> findAllAsync(EntityId entityId, List<DsKvQuery> queries) {
        List<ListenableFuture<List<DsKvEntry>>> futures = queries.stream().map(query -> findAllAsync(entityId, query)).collect(Collectors.toList());
        return Futures.transform(Futures.allAsList(futures),(@Nullable List<List<DsKvEntry>> results)->{
                if (results == null || results.isEmpty()) {
                    return null;
                }
                return results.stream()
                        .flatMap(List::stream)
                        .collect(Collectors.toList());

        }, readResultsProcessingExecutor);
    }



    private ListenableFuture<List<DsKvEntry>> findAllAsync(EntityId entityId, DsKvQuery query) {
        if (query.getDepthAggregation() == DepthAggregation.NONE) {
            return findAllAsyncWithLimit(entityId, query);
        } else {
            Double step = Math.max(query.getInterval(), MIN_AGGREGATION_STEP_MS);
            Double stepDs = query.getStartDs();
            List<ListenableFuture<Optional<DsKvEntry>>> futures = new ArrayList<>();
            while (stepDs < query.getEndDs()) {
                Double startDs = stepDs;
                Double endDs = stepDs + step;
                DsKvQuery subQuery = new BaseDsKvQuery(query.getKey(), startDs, endDs, step, 1, query.getDepthAggregation());
                futures.add(findAndAggregateAsync(entityId, subQuery, startDs, endDs));
                stepDs = endDs;
            }
            ListenableFuture<List<Optional<DsKvEntry>>> future = Futures.allAsList(futures);
            return Futures.transform(future,(@Nullable List<Optional<DsKvEntry>> input)->{
                    if(input != null)
                        return input.stream().filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
                    else
                        return Collections.emptyList();
            }, readResultsProcessingExecutor);
        }
    }

    private ListenableFuture<List<DsKvEntry>> findAllAsyncWithLimit(EntityId entityId, DsKvQuery query) {
        Double minPartition = query.getStartDs();
        Double maxPartition = query.getEndDs();

        ResultSetFuture partitionsFuture = fetchPartitions(entityId, query.getKey(), minPartition, maxPartition);

        final SimpleListenableFuture<List<DsKvEntry>> resultFuture = new SimpleListenableFuture<>();
        final ListenableFuture<List<Double>> partitionsListFuture = Futures.transform(partitionsFuture, getPartitionsArrayFunction(), readResultsProcessingExecutor);

        Futures.addCallback(partitionsListFuture, new FutureCallback<List<Double>>() {
            @Override
            public void onSuccess(@Nullable List<Double> partitions) {
                DsKvQueryCursor cursor = new DsKvQueryCursor(entityId.getEntityType().name(), entityId.getId(), query, partitions);
                findAllAsyncSequentiallyWithLimit(cursor, resultFuture);
            }

            @Override
            public void onFailure(Throwable t) {
                log.error("[{}][{}] Failed to fetch partitions for interval {}-{}", entityId.getEntityType().name(), entityId.getId(), minPartition, maxPartition, t);
            }
        }, readResultsProcessingExecutor);

        return resultFuture;
    }

    private void findAllAsyncSequentiallyWithLimit(final DsKvQueryCursor cursor, final SimpleListenableFuture<List<DsKvEntry>> resultFuture) {
        if (cursor.isFull() || !cursor.hasNextPartition()) {
            resultFuture.set(cursor.getData());
        } else {
            PreparedStatement proto = getFetchStmt(DepthAggregation.NONE);
            BoundStatement stmt = proto.bind();
            stmt.setString(0, cursor.getEntityType());
            stmt.setUUID(1, cursor.getEntityId());
            stmt.setString(2, cursor.getKey());
            stmt.setDouble(3, cursor.getNextPartition());
            stmt.setDouble(4, cursor.getStartDs());
            stmt.setDouble(5, cursor.getEndDs());
            stmt.setInt(6, cursor.getCurrentLimit());

            Futures.addCallback(executeAsyncRead(stmt), new FutureCallback<ResultSet>() {
                @Override
                public void onSuccess(@Nullable ResultSet result) {
                    cursor.addData(convertResultToDsKvEntryList(result == null ? Collections.emptyList() : result.all()));
                    findAllAsyncSequentiallyWithLimit(cursor, resultFuture);
                }

                @Override
                public void onFailure(Throwable t) {
                    log.error("[{}][{}] Failed to fetch data for query {}-{}", stmt, t);
                }
            }, readResultsProcessingExecutor);
        }
    }

    private ListenableFuture<Optional<DsKvEntry>> findAndAggregateAsync(EntityId entityId, DsKvQuery query, Double minPartition, Double maxPartition) {
        final DepthAggregation aggregation = query.getDepthAggregation();
        final String key = query.getKey();
        final Double startDs = query.getStartDs();
        final Double endDs = query.getEndDs();
        final Double ds = startDs + (endDs - startDs) / 2;

        ResultSetFuture partitionsFuture = fetchPartitions(entityId, key, minPartition, maxPartition);

        ListenableFuture<List<Double>> partitionsListFuture = Futures.transform(partitionsFuture, getPartitionsArrayFunction(), readResultsProcessingExecutor);

        ListenableFuture<List<ResultSet>> aggregationChunks = Futures.transform(partitionsListFuture,
                getFetchChunksAsyncFunction(entityId, key, aggregation, startDs, endDs), readResultsProcessingExecutor);

        return Futures.transform(aggregationChunks, new AggregatePartitionsFunction(aggregation, key, ds), readResultsProcessingExecutor);
    }

    private Function<ResultSet, List<Double>> getPartitionsArrayFunction() {
        return rows -> rows.all().stream()
                .map(row -> row.getDouble(ModelConstants.PARTITION_COLUMN)).collect(Collectors.toList());
    }

    private AsyncFunction<List<Double>, List<ResultSet>> getFetchChunksAsyncFunction(EntityId entityId, String key, DepthAggregation aggregation, Double startDs, Double endDs) {
        return partitions -> {
            try {
                PreparedStatement proto = getFetchStmt(aggregation);
                List<ResultSetFuture> futures = new ArrayList<>(partitions.size());
                for (Double partition : partitions) {
                    log.trace("Fetching data for partition [{}] for entityType {} and entityId {}", partition, entityId.getEntityType(), entityId.getId());
                    BoundStatement stmt = proto.bind();
                    stmt.setString(0, entityId.getEntityType().name());
                    stmt.setUUID(1, entityId.getId());
                    stmt.setString(2, key);
                    stmt.setDouble(3, partition);
                    stmt.setDouble(4, startDs);
                    stmt.setDouble(5, endDs);
                    log.debug(GENERATED_QUERY_FOR_ENTITY_TYPE_AND_ENTITY_ID, stmt, entityId.getEntityType(), entityId.getId());
                    futures.add(executeAsyncRead(stmt));
                }
                return Futures.allAsList(futures);
            } catch (TempusRuntimeException e) {
                log.error("Failed to fetch data", e);
                throw e;
            }
        };
    }

    @Override
    public ListenableFuture<DsKvEntry> findLatest(EntityId entityId, String key) {
        BoundStatement stmt = getFindLatestStmt().bind();
        stmt.setString(0, entityId.getEntityType().name());
        stmt.setUUID(1, entityId.getId());
        stmt.setString(2, key);
        log.debug(GENERATED_QUERY_FOR_ENTITY_TYPE_AND_ENTITY_ID, stmt, entityId.getEntityType(), entityId.getId());
        return getFuture(executeAsyncRead(stmt), rs -> convertResultToDsKvEntry(key, rs.one()));
    }

    @Override
    public ListenableFuture<List<DsKvEntry>> findAllLatest(EntityId entityId) {
        BoundStatement stmt = getFindAllLatestStmt().bind();
        stmt.setString(0, entityId.getEntityType().name());
        stmt.setUUID(1, entityId.getId());
        log.debug(GENERATED_QUERY_FOR_ENTITY_TYPE_AND_ENTITY_ID, stmt, entityId.getEntityType(), entityId.getId());
        return getFuture(executeAsyncRead(stmt), rs -> convertResultToDsKvEntryListForLatest(rs.all()));
    }

    @Override
    public ListenableFuture<Void> save(EntityId entityId, DsKvEntry dsKvEntry, long ttl) {
        Double partition = dsKvEntry.getDs();
        com.hashmapinc.server.common.data.kv.DataType type = dsKvEntry.getDataType();
        BoundStatement stmt = (ttl == 0 ? getSaveStmt(type) : getSaveTtlStmt(type)).bind();
        stmt.setString(0, entityId.getEntityType().name())
                .setUUID(1, entityId.getId())
                .setString(2, dsKvEntry.getKey())
                .setDouble(3, partition)
                .setDouble(4, dsKvEntry.getDs())
                .setString(6, dsKvEntry.getUnit().orElse(null))
                .setString(7, dsKvEntry.getSourceUnit().orElse(null));
        addValue(dsKvEntry, stmt, 5);
        if (ttl > 0) {
            stmt.setInt(8, (int) ttl);
        }
        return getFuture(executeAsyncWrite(stmt), rs -> null);
    }

    @Override
    public ListenableFuture<Void> savePartition(EntityId entityId, Double dsKvEntryTs, String key, long ttl) {
        Double partition = dsKvEntryTs;
        log.debug("Saving partition {} for the entity [{}-{}] and key {}", partition, entityId.getEntityType(), entityId.getId(), key);
        BoundStatement stmt = (ttl == 0 ? getPartitionInsertStmt() : getPartitionInsertTtlStmt()).bind();
        stmt = stmt.setString(0, entityId.getEntityType().name())
                .setUUID(1, entityId.getId())
                .setDouble(2, partition)
                .setString(3, key);
        if (ttl > 0) {
            stmt.setInt(4, (int) ttl);
        }
        return getFuture(executeAsyncWrite(stmt), rs -> null);
    }

    @Override
    public ListenableFuture<Void> saveLatest(EntityId entityId, DsKvEntry dsKvEntry) {
        com.hashmapinc.server.common.data.kv.DataType type = dsKvEntry.getDataType();
        BoundStatement stmt = getLatestStmt(type).bind()
                .setString(0, entityId.getEntityType().name())
                .setUUID(1, entityId.getId())
                .setString(2, dsKvEntry.getKey())
                .setDouble(3, dsKvEntry.getDs())
                .setString(5, dsKvEntry.getUnit().orElse(null))
                .setString(6, dsKvEntry.getSourceUnit().orElse(null));
        addValue(dsKvEntry, stmt, 4);
        return getFuture(executeAsyncWrite(stmt), rs -> null);
    }

    @Override
    public DeviceDataSet findAllBetweenDepths(EntityId entityId, Double startDs, Double endDs) {
        Set<String> keys = findAllKeysForEntity(entityId);
        Double minPartition = startDs;
        Double maxPartition = endDs;

        Set<Double> partitions = fetchPartitionsSet(entityId, keys, minPartition, maxPartition);

        Select.Where select = QueryBuilder.select(ModelConstants.DS_COLUMN, ModelConstants.KEY_COLUMN, ModelConstants.BOOLEAN_VALUE_COLUMN, ModelConstants.DOUBLE_VALUE_COLUMN, ModelConstants.JSON_VALUE_COLUMN, ModelConstants.LONG_VALUE_COLUMN, ModelConstants.STRING_VALUE_COLUMN)
                .from(ModelConstants.DS_KV_CF)
                .where(eq(ModelConstants.ENTITY_TYPE_COLUMN, entityId.getEntityType().name())).and(eq(ModelConstants.ENTITY_ID_COLUMN, entityId.getId()));
        select.and(QueryBuilder.in(ModelConstants.KEY_COLUMN, keys.toArray(new String[keys.size()])));
        select.and(QueryBuilder.in(ModelConstants.PARTITION_COLUMN, partitions.toArray(new Double[partitions.size()])));
        select.and(QueryBuilder.gte(ModelConstants.DS_COLUMN, startDs));
        select.and(QueryBuilder.lte(ModelConstants.DS_COLUMN, endDs));
        List<Row> rows = executeRead(select).all();

        List<String> headerColumns = new ArrayList<>();
        headerColumns.add(ModelConstants.DS_COLUMN);
        Map<String, Map<String, String>> tableRowsGroupedByDS = new HashMap<>();
        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            String ds = row.get(ModelConstants.DS_COLUMN, Double.class).toString();
            String key = row.getString(ModelConstants.KEY_COLUMN);

            String value = getNonNullValueForDsKey(row);
            if (!headerColumns.contains(key)) {
                headerColumns.add(key);
            }
            if (tableRowsGroupedByDS.containsKey(ds)) {
                Map<String, String> attributeVsValue = tableRowsGroupedByDS.get(ds);
                attributeVsValue.put(key, value);
                tableRowsGroupedByDS.put(ds, attributeVsValue);
            } else {
                Map<String, String> attributeVsValue = new HashMap<>();
                attributeVsValue.put(key, value);
                tableRowsGroupedByDS.put(ds, attributeVsValue);
            }

        }
        return new DeviceDataSet(tableRowsGroupedByDS, headerColumns, ModelConstants.DS_COLUMN);
    }

    private String getNonNullValueForDsKey(Row row) {
        if(row.get(ModelConstants.BOOLEAN_VALUE_COLUMN, Boolean.class) != null) {
            return row.get(ModelConstants.BOOLEAN_VALUE_COLUMN, Boolean.class).toString();
        } else if(row.get(ModelConstants.DOUBLE_VALUE_COLUMN, Double.class) != null) {
            return row.get(ModelConstants.DOUBLE_VALUE_COLUMN, Double.class).toString();
        } else if(row.get(ModelConstants.JSON_VALUE_COLUMN, JsonNode.class) != null) {
            return row.get(ModelConstants.JSON_VALUE_COLUMN, JsonNode.class).toString();
        } else if(row.get(ModelConstants.LONG_VALUE_COLUMN, Long.class) !=null) {
            return row.get(ModelConstants.LONG_VALUE_COLUMN, Long.class).toString();
        } else if(row.get(ModelConstants.STRING_VALUE_COLUMN, String.class) != null) {
            return row.get(ModelConstants.STRING_VALUE_COLUMN, String.class);
        } else {
            return "";
        }
    }

    private Set<Double> fetchPartitionsSet(EntityId entityId, Set<String> keys, Double minPartition, Double maxPartition) {
        Select.Where select = QueryBuilder.select(ModelConstants.PARTITION_COLUMN).from(ModelConstants.DS_KV_PARTITIONS_CF).where(eq(ModelConstants.ENTITY_TYPE_COLUMN, entityId.getEntityType().name()))
                .and(eq(ModelConstants.ENTITY_ID_COLUMN, entityId.getId())).and(QueryBuilder.in(ModelConstants.KEY_COLUMN, keys.toArray(new String[keys.size()])));
        select.and(QueryBuilder.gte(ModelConstants.PARTITION_COLUMN, minPartition));
        select.and(QueryBuilder.lte(ModelConstants.PARTITION_COLUMN, maxPartition));
        return executeRead(select).all().stream().map(row -> row.getDouble(ModelConstants.PARTITION_COLUMN)).collect(Collectors.toSet());
    }


    private Set<String> findAllKeysForEntity(EntityId entityId) {
        Select.Where select = QueryBuilder.select(ModelConstants.KEY_COLUMN)
                .from(ModelConstants.DS_KV_LATEST_CF)
                .where(eq(ModelConstants.ENTITY_TYPE_COLUMN, entityId.getEntityType().name())).and(eq(ModelConstants.ENTITY_ID_COLUMN, entityId.getId()));
        return executeRead(select).all().stream().map(row -> row.getString(ModelConstants.KEY_COLUMN)).collect(Collectors.toSet());
    }

    private List<DsKvEntry> convertResultToDsKvEntryList(List<Row> rows) {
        List<DsKvEntry> entries = new ArrayList<>(rows.size());
        if (!rows.isEmpty()) {
            rows.forEach(row -> entries.add(convertResultToDsKvEntry(row)));
        }
        return entries;
    }

    private List<DsKvEntry> convertResultToDsKvEntryListForLatest(List<Row> rows) {
        List<DsKvEntry> entries = new ArrayList<>(rows.size());
        if (!rows.isEmpty()) {
            rows.forEach(row -> entries.add(convertResultToDsKvEntryForLatest(row)));
        }
        return entries;
    }

    private DsKvEntry convertResultToDsKvEntry(String key, Row row) {
        if (row != null) {
            Double ds = row.getDouble(ModelConstants.DS_COLUMN);
            return new BasicDsKvEntry(ds, toKvEntry(row, key));
        } else {
            return new BasicDsKvEntry(0.0, new StringDataEntry(key, null));
        }
    }

    private DsKvEntry convertResultToDsKvEntry(Row row) {
        String key = row.getString(ModelConstants.KEY_COLUMN);
        Double ds = row.getDouble(ModelConstants.DS_COLUMN);
        return new BasicDsKvEntry(ds, toKvEntry(row, key));
    }

    private DsKvEntry convertResultToDsKvEntryForLatest(Row row) {
        String key = row.getString(ModelConstants.KEY_COLUMN);
        Double ds = row.getDouble(ModelConstants.DS_COLUMN);
        return new BasicDsKvEntry(ds, toKvEntry(row, key));
    }

    public static KvEntry toKvEntry(Row row, String key) {
        String unit = row.get(ModelConstants.UNIT_COLUMN, String.class);
        String sourceUnit = row.get(ModelConstants.SOURCE_UNIT_COLUMN, String.class);
        if (row.get(ModelConstants.STRING_VALUE_COLUMN, String.class) != null) {
            return new StringDataEntry(key, unit, sourceUnit, row.get(ModelConstants.STRING_VALUE_COLUMN, String.class));
        }
        if (row.get(ModelConstants.LONG_VALUE_COLUMN, Long.class) != null) {
            return new LongDataEntry(key, unit, sourceUnit, row.get(ModelConstants.LONG_VALUE_COLUMN, Long.class));
        }
        if (row.get(ModelConstants.DOUBLE_VALUE_COLUMN, Double.class) != null) {
            return new DoubleDataEntry(key, unit, sourceUnit, row.get(ModelConstants.DOUBLE_VALUE_COLUMN, Double.class));
        }
        if (row.get(ModelConstants.BOOLEAN_VALUE_COLUMN, Boolean.class) != null) {
            return new BooleanDataEntry(key, unit, sourceUnit, row.get(ModelConstants.BOOLEAN_VALUE_COLUMN, Boolean.class));
        }
        if (row.get(ModelConstants.JSON_VALUE_COLUMN, JsonNode.class) != null) {
            return new JsonDataEntry(key, row.get(ModelConstants.JSON_VALUE_COLUMN, JsonNode.class));
        }
        log.warn("All values in key-value row are nullable ");
        return null;
    }

    /**
     * Select existing partitions from the table
     * <code>{@link ModelConstants#DS_KV_PARTITIONS_CF}</code> for the given entity
     */
    private ResultSetFuture fetchPartitions(EntityId entityId, String key, Double minPartition, Double maxPartition) {
        Select.Where select = QueryBuilder.select(ModelConstants.PARTITION_COLUMN).from(ModelConstants.DS_KV_PARTITIONS_CF).where(eq(ModelConstants.ENTITY_TYPE_COLUMN, entityId.getEntityType().name()))
                .and(eq(ModelConstants.ENTITY_ID_COLUMN, entityId.getId())).and(eq(ModelConstants.KEY_COLUMN, key));
        select.and(QueryBuilder.gte(ModelConstants.PARTITION_COLUMN, minPartition));
        select.and(QueryBuilder.lte(ModelConstants.PARTITION_COLUMN, maxPartition));
        return executeAsyncRead(select);
    }

    private PreparedStatement getSaveStmt(com.hashmapinc.server.common.data.kv.DataType dataType) {
        if (saveStmts == null) {
            saveStmts = new PreparedStatement[com.hashmapinc.server.common.data.kv.DataType.values().length];
            for (com.hashmapinc.server.common.data.kv.DataType type : com.hashmapinc.server.common.data.kv.DataType.values()) {
                saveStmts[type.ordinal()] = getSession().prepare(INSERT_INTO + ModelConstants.DS_KV_CF +
                        "(" + ModelConstants.ENTITY_TYPE_COLUMN +
                        "," + ModelConstants.ENTITY_ID_COLUMN +
                        "," + ModelConstants.KEY_COLUMN +
                        "," + ModelConstants.PARTITION_COLUMN +
                        "," + ModelConstants.DS_COLUMN +
                        "," + getColumnName(type) +
                        "," + ModelConstants.UNIT_COLUMN +
                        "," + ModelConstants.SOURCE_UNIT_COLUMN + ")" +
                        " VALUES(?, ?, ?, ?, ?, ?, ?, ?)");
            }
        }
        return saveStmts[dataType.ordinal()];
    }

    private PreparedStatement getSaveTtlStmt(com.hashmapinc.server.common.data.kv.DataType dataType) {
        if (saveTtlStmts == null) {
            saveTtlStmts = new PreparedStatement[com.hashmapinc.server.common.data.kv.DataType.values().length];
            for (com.hashmapinc.server.common.data.kv.DataType type : com.hashmapinc.server.common.data.kv.DataType.values()) {
                saveTtlStmts[type.ordinal()] = getSession().prepare(INSERT_INTO + ModelConstants.DS_KV_CF +
                        "(" + ModelConstants.ENTITY_TYPE_COLUMN +
                        "," + ModelConstants.ENTITY_ID_COLUMN +
                        "," + ModelConstants.KEY_COLUMN +
                        "," + ModelConstants.PARTITION_COLUMN +
                        "," + ModelConstants.DS_COLUMN +
                        "," + getColumnName(type) +
                        "," + ModelConstants.UNIT_COLUMN +
                        "," + ModelConstants.SOURCE_UNIT_COLUMN + ")" +
                        " VALUES(?, ?, ?, ?, ?, ?, ?, ?) USING TTL ?");
            }
        }
        return saveTtlStmts[dataType.ordinal()];
    }

    private PreparedStatement getFetchStmt(DepthAggregation aggType) {
        if (fetchStmts == null) {
            fetchStmts = new PreparedStatement[DepthAggregation.values().length];
            for (DepthAggregation type : DepthAggregation.values()) {
                if (type == DepthAggregation.SUM && fetchStmts[DepthAggregation.AVG.ordinal()] != null) {
                    fetchStmts[type.ordinal()] = fetchStmts[DepthAggregation.AVG.ordinal()];
                } else if (type == DepthAggregation.AVG && fetchStmts[DepthAggregation.SUM.ordinal()] != null) {
                    fetchStmts[type.ordinal()] = fetchStmts[DepthAggregation.SUM.ordinal()];
                } else {
                    fetchStmts[type.ordinal()] = getSession().prepare(SELECT +
                            String.join(", ", ModelConstants.getFetchColumnNames(type)) + " FROM " + ModelConstants.DS_KV_CF
                            + " WHERE " + ModelConstants.ENTITY_TYPE_COLUMN + EQUAL_PLACEHOLDER
                            + "AND " + ModelConstants.ENTITY_ID_COLUMN + EQUAL_PLACEHOLDER
                            + "AND " + ModelConstants.KEY_COLUMN + EQUAL_PLACEHOLDER
                            + "AND " + ModelConstants.PARTITION_COLUMN + EQUAL_PLACEHOLDER
                            + "AND " + ModelConstants.DS_COLUMN + " > ? "
                            + "AND " + ModelConstants.DS_COLUMN + " <= ?"
                            + (type == DepthAggregation.NONE ? " ORDER BY " + ModelConstants.DS_COLUMN + " DESC LIMIT ?" : ""));
                }
            }
        }
        return fetchStmts[aggType.ordinal()];
    }

    private PreparedStatement getLatestStmt(com.hashmapinc.server.common.data.kv.DataType dataType) {
        if (latestInsertStmts == null) {
            latestInsertStmts = new PreparedStatement[com.hashmapinc.server.common.data.kv.DataType.values().length];
            for (com.hashmapinc.server.common.data.kv.DataType type : com.hashmapinc.server.common.data.kv.DataType.values()) {
                latestInsertStmts[type.ordinal()] = getSession().prepare(INSERT_INTO + ModelConstants.DS_KV_LATEST_CF +
                        "(" + ModelConstants.ENTITY_TYPE_COLUMN +
                        "," + ModelConstants.ENTITY_ID_COLUMN +
                        "," + ModelConstants.KEY_COLUMN +
                        "," + ModelConstants.DS_COLUMN +
                        "," + getColumnName(type) +
                        "," + ModelConstants.UNIT_COLUMN +
                        "," + ModelConstants.SOURCE_UNIT_COLUMN + ")" +
                        " VALUES(?, ?, ?, ?, ?, ?, ?)");
            }
        }
        return latestInsertStmts[dataType.ordinal()];
    }


    private PreparedStatement getPartitionInsertStmt() {
        if (partitionInsertStmt == null) {
            partitionInsertStmt = getSession().prepare(INSERT_INTO + ModelConstants.DS_KV_PARTITIONS_CF +
                    "(" + ModelConstants.ENTITY_TYPE_COLUMN +
                    "," + ModelConstants.ENTITY_ID_COLUMN +
                    "," + ModelConstants.PARTITION_COLUMN +
                    "," + ModelConstants.KEY_COLUMN + ")" +
                    " VALUES(?, ?, ?, ?)");
        }
        return partitionInsertStmt;
    }

    private PreparedStatement getPartitionInsertTtlStmt() {
        if (partitionInsertTtlStmt == null) {
            partitionInsertTtlStmt = getSession().prepare(INSERT_INTO + ModelConstants.DS_KV_PARTITIONS_CF +
                    "(" + ModelConstants.ENTITY_TYPE_COLUMN +
                    "," + ModelConstants.ENTITY_ID_COLUMN +
                    "," + ModelConstants.PARTITION_COLUMN +
                    "," + ModelConstants.KEY_COLUMN + ")" +
                    " VALUES(?, ?, ?, ?) USING TTL ?");
        }
        return partitionInsertTtlStmt;
    }


    private PreparedStatement getFindLatestStmt() {
        if (findLatestStmt == null) {
            findLatestStmt = getSession().prepare(SELECT +
                    ModelConstants.KEY_COLUMN + "," +
                    ModelConstants.DS_COLUMN + "," +
                    ModelConstants.STRING_VALUE_COLUMN + "," +
                    ModelConstants.BOOLEAN_VALUE_COLUMN + "," +
                    ModelConstants.LONG_VALUE_COLUMN + "," +
                    ModelConstants.DOUBLE_VALUE_COLUMN + "," +
                    ModelConstants.JSON_VALUE_COLUMN + "," +
                    ModelConstants.UNIT_COLUMN + "," +
                    ModelConstants.SOURCE_UNIT_COLUMN + " " +
                    "FROM " + ModelConstants.DS_KV_LATEST_CF + " " +
                    "WHERE " + ModelConstants.ENTITY_TYPE_COLUMN + EQUAL_PLACEHOLDER +
                    "AND " + ModelConstants.ENTITY_ID_COLUMN + EQUAL_PLACEHOLDER +
                    "AND " + ModelConstants.KEY_COLUMN + EQUAL_PLACEHOLDER);
        }
        return findLatestStmt;
    }

    private PreparedStatement getFindAllLatestStmt() {
        if (findAllLatestStmt == null) {
            findAllLatestStmt = getSession().prepare(SELECT +
                    ModelConstants.KEY_COLUMN + "," +
                    ModelConstants.DS_COLUMN + "," +
                    ModelConstants.STRING_VALUE_COLUMN + "," +
                    ModelConstants.BOOLEAN_VALUE_COLUMN + "," +
                    ModelConstants.LONG_VALUE_COLUMN + "," +
                    ModelConstants.DOUBLE_VALUE_COLUMN + "," +
                    ModelConstants.JSON_VALUE_COLUMN + "," +
                    ModelConstants.UNIT_COLUMN + "," +
                    ModelConstants.SOURCE_UNIT_COLUMN + " " +
                    "FROM " + ModelConstants.DS_KV_LATEST_CF + " " +
                    "WHERE " + ModelConstants.ENTITY_TYPE_COLUMN + EQUAL_PLACEHOLDER +
                    "AND " + ModelConstants.ENTITY_ID_COLUMN + EQUAL_PLACEHOLDER);
        }
        return findAllLatestStmt;
    }

    private static String getColumnName(com.hashmapinc.server.common.data.kv.DataType type) {
        switch (type) {
            case BOOLEAN:
                return ModelConstants.BOOLEAN_VALUE_COLUMN;
            case STRING:
                return ModelConstants.STRING_VALUE_COLUMN;
            case LONG:
                return ModelConstants.LONG_VALUE_COLUMN;
            case DOUBLE:
                return ModelConstants.DOUBLE_VALUE_COLUMN;
            case JSON:
                return ModelConstants.JSON_VALUE_COLUMN;
            default:
                throw new TempusRuntimeException("Not implemented!");
        }
    }


    private static void addValue(KvEntry kvEntry, BoundStatement stmt, int column) {
        switch (kvEntry.getDataType()) {
            case BOOLEAN:
                Optional<Boolean> booleanValue = kvEntry.getBooleanValue();
                booleanValue.ifPresent(aBoolean -> stmt.setBool(column, aBoolean.booleanValue()));
                break;
            case STRING:
                Optional<String> stringValue = kvEntry.getStrValue();
                stringValue.ifPresent(s -> stmt.setString(column, s));
                break;
            case LONG:
                Optional<Long> longValue = kvEntry.getLongValue();
                longValue.ifPresent(aLong -> stmt.setLong(column, aLong.longValue()));
                break;
            case DOUBLE:
                Optional<Double> doubleValue = kvEntry.getDoubleValue();
                doubleValue.ifPresent(aDouble -> stmt.setDouble(column, aDouble.doubleValue()));
                break;
            case JSON:
                Optional<JsonNode> jsonNodeValue =  kvEntry.getJsonValue();
                jsonNodeValue.ifPresent(jsonNode -> stmt.setString(column, jsonNode.toString()));
                break;
        }
    }
}
