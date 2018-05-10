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
import com.hashmapinc.server.common.data.kv.*;
import com.hashmapinc.server.common.data.kv.DataType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import com.hashmapinc.server.common.data.id.EntityId;
import com.hashmapinc.server.common.data.kv.*;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.nosql.CassandraAbstractAsyncDao;
import com.hashmapinc.server.dao.util.NoSqlDao;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.hashmapinc.server.dao.model.ModelConstants.*;


@Component
@Slf4j
@NoSqlDao
public class CassandraBaseTimeseriesDao extends CassandraAbstractAsyncDao implements TimeseriesDao {

    private static final int MIN_AGGREGATION_STEP_MS = 1000;
    public static final String INSERT_INTO = "INSERT INTO ";
    public static final String GENERATED_QUERY_FOR_ENTITY_TYPE_AND_ENTITY_ID = "Generated query [{}] for entityType {} and entityId {}";
    public static final String SELECT_PREFIX = "SELECT ";
    public static final String EQUALS_PARAM = " = ? ";

    @Autowired
    private Environment environment;

    @Value("${cassandra.query.ts_key_value_partitioning}")
    private String partitioning;

    private TsPartitionDate tsFormat;

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
            getFetchStmt(Aggregation.NONE);
            log.debug("HMDC partioning value " + partitioning );
            Optional<TsPartitionDate> partition = TsPartitionDate.parse(partitioning);
            if (partition.isPresent()) {
                tsFormat = partition.get();
            } else {
                log.warn("Incorrect configuration of partitioning {}", partitioning);
                throw new RuntimeException("Failed to parse partitioning property: " + partitioning + "!");
            }
        }
    }

    @PreDestroy
    public void stop() {
        super.stopExecutor();
    }

    @Override
    public ListenableFuture<List<TsKvEntry>> findAllAsync(EntityId entityId, List<TsKvQuery> queries) {
        List<ListenableFuture<List<TsKvEntry>>> futures = queries.stream().map(query -> findAllAsync(entityId, query)).collect(Collectors.toList());
        return Futures.transform(Futures.allAsList(futures), new Function<List<List<TsKvEntry>>, List<TsKvEntry>>() {
            @Nullable
            @Override
            public List<TsKvEntry> apply(@Nullable List<List<TsKvEntry>> results) {
                if (results == null || results.isEmpty()) {
                    return null;
                }
                return results.stream()
                        .flatMap(List::stream)
                        .collect(Collectors.toList());
            }
        }, readResultsProcessingExecutor);
    }


    private ListenableFuture<List<TsKvEntry>> findAllAsync(EntityId entityId, TsKvQuery query) {
        if (query.getAggregation() == Aggregation.NONE) {
            return findAllAsyncWithLimit(entityId, query);
        } else {
            long step = Math.max(query.getInterval(), MIN_AGGREGATION_STEP_MS);
            long stepTs = query.getStartTs();
            List<ListenableFuture<Optional<TsKvEntry>>> futures = new ArrayList<>();
            while (stepTs < query.getEndTs()) {
                long startTs = stepTs;
                long endTs = stepTs + step;
                TsKvQuery subQuery = new BaseTsKvQuery(query.getKey(), startTs, endTs, step, 1, query.getAggregation());
                futures.add(findAndAggregateAsync(entityId, subQuery, toPartitionTs(startTs), toPartitionTs(endTs)));
                stepTs = endTs;
            }
            ListenableFuture<List<Optional<TsKvEntry>>> future = Futures.allAsList(futures);
            return Futures.transform(future, new Function<List<Optional<TsKvEntry>>, List<TsKvEntry>>() {
                @Nullable
                @Override
                public List<TsKvEntry> apply(@Nullable List<Optional<TsKvEntry>> input) {
                    return input == null ? Collections.emptyList() : input.stream().filter(v -> v.isPresent()).map(v -> v.get()).collect(Collectors.toList());
                }
            }, readResultsProcessingExecutor);
        }
    }

    private ListenableFuture<List<TsKvEntry>> findAllAsyncWithLimit(EntityId entityId, TsKvQuery query) {
        long minPartition = toPartitionTs(query.getStartTs());
        long maxPartition = toPartitionTs(query.getEndTs());

        ResultSetFuture partitionsFuture = fetchPartitions(entityId, query.getKey(), minPartition, maxPartition);

        final SimpleListenableFuture<List<TsKvEntry>> resultFuture = new SimpleListenableFuture<>();
        final ListenableFuture<List<Long>> partitionsListFuture = Futures.transform(partitionsFuture, getPartitionsArrayFunction(), readResultsProcessingExecutor);

        Futures.addCallback(partitionsListFuture, new FutureCallback<List<Long>>() {
            @Override
            public void onSuccess(@Nullable List<Long> partitions) {
                TsKvQueryCursor cursor = new TsKvQueryCursor(entityId.getEntityType().name(), entityId.getId(), query, partitions);
                findAllAsyncSequentiallyWithLimit(cursor, resultFuture);
            }

            @Override
            public void onFailure(Throwable t) {
                log.error("[{}][{}] Failed to fetch partitions for interval {}-{}", entityId.getEntityType().name(), entityId.getId(), minPartition, maxPartition, t);
            }
        }, readResultsProcessingExecutor);

        return resultFuture;
    }

    @Override
    public DeviceDataSet findAllBetweenTimeStamp(EntityId entityId, Long startTs, Long endTs) {
        Set<String> keys = findAllKeysForEntity(entityId);
        long minPartition = toPartitionTs(startTs);
        long maxPartition = toPartitionTs(endTs);
        Set<Long> partitions = fetchPartitionsSet(entityId, keys, minPartition, maxPartition);

        Select.Where select = QueryBuilder.select(TS_COLUMN, KEY_COLUMN, BOOLEAN_VALUE_COLUMN, DOUBLE_VALUE_COLUMN, JSON_VALUE_COLUMN, LONG_VALUE_COLUMN, STRING_VALUE_COLUMN)
                .from(TS_KV_CF)
                .where(eq(ENTITY_TYPE_COLUMN, entityId.getEntityType().name())).and(eq(ModelConstants.ENTITY_ID_COLUMN, entityId.getId()));
        select.and(QueryBuilder.in(KEY_COLUMN, keys.toArray(new String[keys.size()])));
        select.and(QueryBuilder.in(PARTITION_COLUMN, partitions.toArray(new Long[partitions.size()])));
        select.and(QueryBuilder.gte(TS_COLUMN, startTs));
        select.and(QueryBuilder.lte(TS_COLUMN, endTs));
        List<Row> rows = executeRead(select).all();

        List<String> headerColumns = new ArrayList<>();
        headerColumns.add(TS_COLUMN);
        Map<String, Map<String, String>> tableRowsGroupedByTS = new HashMap<>();
        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            String ts = row.get(TS_COLUMN, Long.class).toString();
            String key = row.getString(KEY_COLUMN);

            String value = getNonNullValueForTsKey(row);
            if (!headerColumns.contains(key)) {
                headerColumns.add(key);
            }
            if (tableRowsGroupedByTS.containsKey(ts)) {
                Map<String, String> attributeVsValue = tableRowsGroupedByTS.get(ts);
                attributeVsValue.put(key, value);
                tableRowsGroupedByTS.put(ts, attributeVsValue);
            } else {
                Map<String, String> attributeVsValue = new HashMap<>();
                attributeVsValue.put(key, value);
                tableRowsGroupedByTS.put(ts, attributeVsValue);
            }
        }
        return new DeviceDataSet(tableRowsGroupedByTS, headerColumns, TS_COLUMN);
    }


    private String getNonNullValueForTsKey(Row row) {
        if(row.get(BOOLEAN_VALUE_COLUMN, Boolean.class) != null) {
            return row.get(BOOLEAN_VALUE_COLUMN, Boolean.class).toString();
        } else if(row.get(DOUBLE_VALUE_COLUMN, Double.class) != null) {
            return row.get(DOUBLE_VALUE_COLUMN, Double.class).toString();
        } else if(row.get(JSON_VALUE_COLUMN, JsonNode.class) != null) {
            return row.get(JSON_VALUE_COLUMN, JsonNode.class).toString();
        } else if(row.get(LONG_VALUE_COLUMN, Long.class) !=null) {
            return row.get(LONG_VALUE_COLUMN, Long.class).toString();
        } else if(row.get(STRING_VALUE_COLUMN, String.class) != null) {
            return row.get(STRING_VALUE_COLUMN, String.class);
        } else {
            return "";
        }
    }

    private Set<String> findAllKeysForEntity(EntityId entityId) {
        Select.Where select = QueryBuilder.select(KEY_COLUMN)
                .from(TS_KV_LATEST_CF)
                .where(eq(ENTITY_TYPE_COLUMN, entityId.getEntityType().name())).and(eq(ModelConstants.ENTITY_ID_COLUMN, entityId.getId()));
        return executeRead(select).all().stream().map(row -> row.getString(KEY_COLUMN)).collect(Collectors.toSet());
    }


    private long toPartitionTs(long ts) {
        LocalDateTime time = LocalDateTime.ofInstant(Instant.ofEpochMilli(ts), ZoneOffset.UTC);
        return tsFormat.truncatedTo(time).toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    private void findAllAsyncSequentiallyWithLimit(final TsKvQueryCursor cursor, final SimpleListenableFuture<List<TsKvEntry>> resultFuture) {
        if (cursor.isFull() || !cursor.hasNextPartition()) {
            resultFuture.set(cursor.getData());
        } else {
            PreparedStatement proto = getFetchStmt(Aggregation.NONE);
            BoundStatement stmt = proto.bind();
            stmt.setString(0, cursor.getEntityType());
            stmt.setUUID(1, cursor.getEntityId());
            stmt.setString(2, cursor.getKey());
            stmt.setLong(3, cursor.getNextPartition());
            stmt.setLong(4, cursor.getStartTs());
            stmt.setLong(5, cursor.getEndTs());
            stmt.setInt(6, cursor.getCurrentLimit());

            Futures.addCallback(executeAsyncRead(stmt), new FutureCallback<ResultSet>() {
                @Override
                public void onSuccess(@Nullable ResultSet result) {
                    cursor.addData(convertResultToTsKvEntryList(result == null ? Collections.emptyList() : result.all()));
                    findAllAsyncSequentiallyWithLimit(cursor, resultFuture);
                }

                @Override
                public void onFailure(Throwable t) {
                    log.error("[{}][{}] Failed to fetch data for query {}-{}", stmt, t);
                }
            }, readResultsProcessingExecutor);
        }
    }

    private ListenableFuture<Optional<TsKvEntry>> findAndAggregateAsync(EntityId entityId, TsKvQuery query, long minPartition, long maxPartition) {
        final Aggregation aggregation = query.getAggregation();
        final String key = query.getKey();
        final long startTs = query.getStartTs();
        final long endTs = query.getEndTs();
        final long ts = startTs + (endTs - startTs) / 2;

        ResultSetFuture partitionsFuture = fetchPartitions(entityId, key, minPartition, maxPartition);

        ListenableFuture<List<Long>> partitionsListFuture = Futures.transform(partitionsFuture, getPartitionsArrayFunction(), readResultsProcessingExecutor);

        ListenableFuture<List<ResultSet>> aggregationChunks = Futures.transform(partitionsListFuture,
                getFetchChunksAsyncFunction(entityId, key, aggregation, startTs, endTs), readResultsProcessingExecutor);

        return Futures.transform(aggregationChunks, new AggregatePartitionsFunction(aggregation, key, ts), readResultsProcessingExecutor);
    }

    private Function<ResultSet, List<Long>> getPartitionsArrayFunction() {
        return rows -> rows.all().stream()
                .map(row -> row.getLong(ModelConstants.PARTITION_COLUMN)).collect(Collectors.toList());
    }

    private AsyncFunction<List<Long>, List<ResultSet>> getFetchChunksAsyncFunction(EntityId entityId, String key, Aggregation aggregation, long startTs, long endTs) {
        return partitions -> {
            try {
                PreparedStatement proto = getFetchStmt(aggregation);
                List<ResultSetFuture> futures = new ArrayList<>(partitions.size());
                for (Long partition : partitions) {
                    log.trace("Fetching data for partition [{}] for entityType {} and entityId {}", partition, entityId.getEntityType(), entityId.getId());
                    BoundStatement stmt = proto.bind();
                    stmt.setString(0, entityId.getEntityType().name());
                    stmt.setUUID(1, entityId.getId());
                    stmt.setString(2, key);
                    stmt.setLong(3, partition);
                    stmt.setLong(4, startTs);
                    stmt.setLong(5, endTs);
                    log.debug(GENERATED_QUERY_FOR_ENTITY_TYPE_AND_ENTITY_ID, stmt, entityId.getEntityType(), entityId.getId());
                    futures.add(executeAsyncRead(stmt));
                }
                return Futures.allAsList(futures);
            } catch (Throwable e) {
                log.error("Failed to fetch data", e);
                throw e;
            }
        };
    }

    @Override
    public ListenableFuture<TsKvEntry> findLatest(EntityId entityId, String key) {
        BoundStatement stmt = getFindLatestStmt().bind();
        stmt.setString(0, entityId.getEntityType().name());
        stmt.setUUID(1, entityId.getId());
        stmt.setString(2, key);
        log.debug(GENERATED_QUERY_FOR_ENTITY_TYPE_AND_ENTITY_ID, stmt, entityId.getEntityType(), entityId.getId());
        return getFuture(executeAsyncRead(stmt), rs -> convertResultToTsKvEntry(key, rs.one()));
    }

    @Override
    public ListenableFuture<List<TsKvEntry>> findAllLatest(EntityId entityId) {
        BoundStatement stmt = getFindAllLatestStmt().bind();
        stmt.setString(0, entityId.getEntityType().name());
        stmt.setUUID(1, entityId.getId());
        log.debug(GENERATED_QUERY_FOR_ENTITY_TYPE_AND_ENTITY_ID, stmt, entityId.getEntityType(), entityId.getId());
        return getFuture(executeAsyncRead(stmt), rs -> convertResultToTsKvEntryList(rs.all()));
    }

    @Override
    public ListenableFuture<Void> save(EntityId entityId, TsKvEntry tsKvEntry, long ttl) {
        long partition = toPartitionTs(tsKvEntry.getTs());
        log.debug("HMDC Partition value " + partition);
        DataType type = tsKvEntry.getDataType();
        BoundStatement stmt = (ttl == 0 ? getSaveStmt(type) : getSaveTtlStmt(type)).bind();
        stmt.setString(0, entityId.getEntityType().name())
                .setUUID(1, entityId.getId())
                .setString(2, tsKvEntry.getKey())
                .setLong(3, partition)
                .setLong(4, tsKvEntry.getTs());
        addValue(tsKvEntry, stmt, 5);
        if (ttl > 0) {
            stmt.setInt(6, (int) ttl);
        }
        return getFuture(executeAsyncWrite(stmt), rs -> null);
    }

    @Override
    public ListenableFuture<Void> savePartition(EntityId entityId, long tsKvEntryTs, String key, long ttl) {
        long partition = toPartitionTs(tsKvEntryTs);
        log.debug("Saving partition {} for the entity [{}-{}] and key {}", partition, entityId.getEntityType(), entityId.getId(), key);
        BoundStatement stmt = (ttl == 0 ? getPartitionInsertStmt() : getPartitionInsertTtlStmt()).bind();
        stmt = stmt.setString(0, entityId.getEntityType().name())
                .setUUID(1, entityId.getId())
                .setLong(2, partition)
                .setString(3, key);
        if (ttl > 0) {
            stmt.setInt(4, (int) ttl);
        }
        return getFuture(executeAsyncWrite(stmt), rs -> null);
    }

    @Override
    public ListenableFuture<Void> saveLatest(EntityId entityId, TsKvEntry tsKvEntry) {
        DataType type = tsKvEntry.getDataType();
        BoundStatement stmt = getLatestStmt(type).bind()
                .setString(0, entityId.getEntityType().name())
                .setUUID(1, entityId.getId())
                .setString(2, tsKvEntry.getKey())
                .setLong(3, tsKvEntry.getTs());
        addValue(tsKvEntry, stmt, 4);
        return getFuture(executeAsyncWrite(stmt), rs -> null);
    }

    private List<TsKvEntry> convertResultToTsKvEntryList(List<Row> rows) {
        List<TsKvEntry> entries = new ArrayList<>(rows.size());
        if (!rows.isEmpty()) {
            rows.forEach(row -> entries.add(convertResultToTsKvEntry(row)));
        }
        return entries;
    }

    private TsKvEntry convertResultToTsKvEntry(String key, Row row) {
        if (row != null) {
            long ts = row.getLong(TS_COLUMN);
            return new BasicTsKvEntry(ts, toKvEntry(row, key));
        } else {
            return new BasicTsKvEntry(System.currentTimeMillis(), new StringDataEntry(key, null));
        }
    }

    private TsKvEntry convertResultToTsKvEntry(Row row) {
        String key = row.getString(KEY_COLUMN);
        long ts = row.getLong(TS_COLUMN);
        return new BasicTsKvEntry(ts, toKvEntry(row, key));
    }

    public static KvEntry toKvEntry(Row row, String key) {
        KvEntry kvEntry = null;
        String strV = row.get(STRING_VALUE_COLUMN, String.class);
        if (strV != null) {
            kvEntry = new StringDataEntry(key, strV);
        } else {
            Long longV = row.get(LONG_VALUE_COLUMN, Long.class);
            if (longV != null) {
                kvEntry = new LongDataEntry(key, longV);
            } else {
                Double doubleV = row.get(DOUBLE_VALUE_COLUMN, Double.class);
                if (doubleV != null) {
                    kvEntry = new DoubleDataEntry(key, doubleV);
                } else {
                    Boolean boolV = row.get(BOOLEAN_VALUE_COLUMN, Boolean.class);
                    if (boolV != null) {
                        kvEntry = new BooleanDataEntry(key, boolV);
                    } else {
                        JsonNode jsonV = row.get(JSON_VALUE_COLUMN, JsonNode.class);
                        if (jsonV != null) {
                            kvEntry = new JsonDataEntry(key, jsonV);
                        } else {
                            log.warn("All values in key-value row are nullable ");
                        }
                    }
                }
            }
        }
        return kvEntry;
    }

    /**
     * Select existing partitions from the table
     * <code>{@link ModelConstants#TS_KV_PARTITIONS_CF}</code> for the given entity
     */
    private ResultSetFuture fetchPartitions(EntityId entityId, String key, long minPartition, long maxPartition) {
        Select.Where select = QueryBuilder.select(ModelConstants.PARTITION_COLUMN).from(ModelConstants.TS_KV_PARTITIONS_CF).where(eq(ENTITY_TYPE_COLUMN, entityId.getEntityType().name()))
                .and(eq(ModelConstants.ENTITY_ID_COLUMN, entityId.getId())).and(eq(KEY_COLUMN, key));
        select.and(QueryBuilder.gte(ModelConstants.PARTITION_COLUMN, minPartition));
        select.and(QueryBuilder.lte(ModelConstants.PARTITION_COLUMN, maxPartition));
        return executeAsyncRead(select);
    }

    private Set<Long> fetchPartitionsSet(EntityId entityId, Set<String> keys, long minPartition, long maxPartition) {
        Select.Where select = QueryBuilder.select(ModelConstants.PARTITION_COLUMN).from(ModelConstants.TS_KV_PARTITIONS_CF).where(eq(ENTITY_TYPE_COLUMN, entityId.getEntityType().name()))
                .and(eq(ModelConstants.ENTITY_ID_COLUMN, entityId.getId())).and(QueryBuilder.in(KEY_COLUMN, keys.toArray(new String[keys.size()])));
        select.and(QueryBuilder.gte(ModelConstants.PARTITION_COLUMN, minPartition));
        select.and(QueryBuilder.lte(ModelConstants.PARTITION_COLUMN, maxPartition));
        return executeRead(select).all().stream().map(row -> row.getLong(PARTITION_COLUMN)).collect(Collectors.toSet());
    }

    private PreparedStatement getSaveStmt(DataType dataType) {
        if (saveStmts == null) {
            saveStmts = new PreparedStatement[DataType.values().length];
            for (DataType type : DataType.values()) {
                saveStmts[type.ordinal()] = getSession().prepare(INSERT_INTO + ModelConstants.TS_KV_CF +
                        "(" + ModelConstants.ENTITY_TYPE_COLUMN +
                        "," + ModelConstants.ENTITY_ID_COLUMN +
                        "," + KEY_COLUMN +
                        "," + ModelConstants.PARTITION_COLUMN +
                        "," + TS_COLUMN +
                        "," + getColumnName(type) + ")" +
                        " VALUES(?, ?, ?, ?, ?, ?)");
            }
        }
        return saveStmts[dataType.ordinal()];
    }

    private PreparedStatement getSaveTtlStmt(DataType dataType) {
        if (saveTtlStmts == null) {
            saveTtlStmts = new PreparedStatement[DataType.values().length];
            for (DataType type : DataType.values()) {
                saveTtlStmts[type.ordinal()] = getSession().prepare(INSERT_INTO + ModelConstants.TS_KV_CF +
                        "(" + ModelConstants.ENTITY_TYPE_COLUMN +
                        "," + ModelConstants.ENTITY_ID_COLUMN +
                        "," + KEY_COLUMN +
                        "," + ModelConstants.PARTITION_COLUMN +
                        "," + TS_COLUMN +
                        "," + getColumnName(type) + ")" +
                        " VALUES(?, ?, ?, ?, ?, ?) USING TTL ?");
            }
        }
        return saveTtlStmts[dataType.ordinal()];
    }

    private PreparedStatement getFetchStmt(Aggregation aggType) {
        if (fetchStmts == null) {
            fetchStmts = new PreparedStatement[Aggregation.values().length];
            for (Aggregation type : Aggregation.values()) {
                if (type == Aggregation.SUM && fetchStmts[Aggregation.AVG.ordinal()] != null) {
                    fetchStmts[type.ordinal()] = fetchStmts[Aggregation.AVG.ordinal()];
                } else if (type == Aggregation.AVG && fetchStmts[Aggregation.SUM.ordinal()] != null) {
                    fetchStmts[type.ordinal()] = fetchStmts[Aggregation.SUM.ordinal()];
                } else {
                    fetchStmts[type.ordinal()] = getSession().prepare(SELECT_PREFIX +
                            String.join(", ", ModelConstants.getFetchColumnNames(type)) + " FROM " + ModelConstants.TS_KV_CF
                            + " WHERE " + ModelConstants.ENTITY_TYPE_COLUMN + EQUALS_PARAM
                            + "AND " + ModelConstants.ENTITY_ID_COLUMN + EQUALS_PARAM
                            + "AND " + ModelConstants.KEY_COLUMN + EQUALS_PARAM
                            + "AND " + ModelConstants.PARTITION_COLUMN + EQUALS_PARAM
                            + "AND " + ModelConstants.TS_COLUMN + " > ? "
                            + "AND " + ModelConstants.TS_COLUMN + " <= ?"
                            + (type == Aggregation.NONE ? " ORDER BY " + ModelConstants.TS_COLUMN + " DESC LIMIT ?" : ""));
                }
            }
        }
        return fetchStmts[aggType.ordinal()];
    }

    private PreparedStatement getLatestStmt(DataType dataType) {
        if (latestInsertStmts == null) {
            latestInsertStmts = new PreparedStatement[DataType.values().length];
            for (DataType type : DataType.values()) {
                latestInsertStmts[type.ordinal()] = getSession().prepare(INSERT_INTO + ModelConstants.TS_KV_LATEST_CF +
                        "(" + ModelConstants.ENTITY_TYPE_COLUMN +
                        "," + ModelConstants.ENTITY_ID_COLUMN +
                        "," + KEY_COLUMN +
                        "," + TS_COLUMN +
                        "," + getColumnName(type) + ")" +
                        " VALUES(?, ?, ?, ?, ?)");
            }
        }
        return latestInsertStmts[dataType.ordinal()];
    }


    private PreparedStatement getPartitionInsertStmt() {
        if (partitionInsertStmt == null) {
            partitionInsertStmt = getSession().prepare(INSERT_INTO + ModelConstants.TS_KV_PARTITIONS_CF +
                    "(" + ModelConstants.ENTITY_TYPE_COLUMN +
                    "," + ModelConstants.ENTITY_ID_COLUMN +
                    "," + ModelConstants.PARTITION_COLUMN +
                    "," + KEY_COLUMN + ")" +
                    " VALUES(?, ?, ?, ?)");
        }
        return partitionInsertStmt;
    }

    private PreparedStatement getPartitionInsertTtlStmt() {
        if (partitionInsertTtlStmt == null) {
            partitionInsertTtlStmt = getSession().prepare(INSERT_INTO + ModelConstants.TS_KV_PARTITIONS_CF +
                    "(" + ModelConstants.ENTITY_TYPE_COLUMN +
                    "," + ModelConstants.ENTITY_ID_COLUMN +
                    "," + ModelConstants.PARTITION_COLUMN +
                    "," + KEY_COLUMN + ")" +
                    " VALUES(?, ?, ?, ?) USING TTL ?");
        }
        return partitionInsertTtlStmt;
    }


    private PreparedStatement getFindLatestStmt() {
        if (findLatestStmt == null) {
            findLatestStmt = getSession().prepare(SELECT_PREFIX +
                    ModelConstants.KEY_COLUMN + "," +
                    ModelConstants.TS_COLUMN + "," +
                    ModelConstants.STRING_VALUE_COLUMN + "," +
                    ModelConstants.BOOLEAN_VALUE_COLUMN + "," +
                    ModelConstants.LONG_VALUE_COLUMN + "," +
                    ModelConstants.DOUBLE_VALUE_COLUMN + "," +
                    ModelConstants.JSON_VALUE_COLUMN + " " +
                    "FROM " + ModelConstants.TS_KV_LATEST_CF + " " +
                    "WHERE " + ModelConstants.ENTITY_TYPE_COLUMN + EQUALS_PARAM +
                    "AND " + ModelConstants.ENTITY_ID_COLUMN + EQUALS_PARAM +
                    "AND " + ModelConstants.KEY_COLUMN + EQUALS_PARAM);
        }
        return findLatestStmt;
    }

    private PreparedStatement getFindAllLatestStmt() {
        if (findAllLatestStmt == null) {
            findAllLatestStmt = getSession().prepare(SELECT_PREFIX +
                    ModelConstants.KEY_COLUMN + "," +
                    ModelConstants.TS_COLUMN + "," +
                    ModelConstants.STRING_VALUE_COLUMN + "," +
                    ModelConstants.BOOLEAN_VALUE_COLUMN + "," +
                    ModelConstants.LONG_VALUE_COLUMN + "," +
                    ModelConstants.DOUBLE_VALUE_COLUMN + "," +
                    ModelConstants.JSON_VALUE_COLUMN + " " +
                    "FROM " + ModelConstants.TS_KV_LATEST_CF + " " +
                    "WHERE " + ModelConstants.ENTITY_TYPE_COLUMN + EQUALS_PARAM +
                    "AND " + ModelConstants.ENTITY_ID_COLUMN + EQUALS_PARAM);
        }
        return findAllLatestStmt;
    }

    private static String getColumnName(DataType type) {
        switch (type) {
            case BOOLEAN:
                return BOOLEAN_VALUE_COLUMN;
            case STRING:
                return STRING_VALUE_COLUMN;
            case LONG:
                return LONG_VALUE_COLUMN;
            case DOUBLE:
                return DOUBLE_VALUE_COLUMN;
            case JSON:
                return JSON_VALUE_COLUMN;
            default:
                throw new RuntimeException("Not implemented!");
        }
    }

    private static void addValue(KvEntry kvEntry, BoundStatement stmt, int column) {
        switch (kvEntry.getDataType()) {
            case BOOLEAN:
                Optional<Boolean> booleanValue = kvEntry.getBooleanValue();
                if (booleanValue.isPresent()) {
                    stmt.setBool(column, booleanValue.get().booleanValue());
                }
                break;
            case STRING:
                Optional<String> stringValue = kvEntry.getStrValue();
                if (stringValue.isPresent()) {
                    stmt.setString(column, stringValue.get());
                }
                break;
            case LONG:
                Optional<Long> longValue = kvEntry.getLongValue();
                if (longValue.isPresent()) {
                    stmt.setLong(column, longValue.get().longValue());
                }
                break;
            case DOUBLE:
                Optional<Double> doubleValue = kvEntry.getDoubleValue();
                if (doubleValue.isPresent()) {
                    stmt.setDouble(column, doubleValue.get().doubleValue());
                }
                break;
            case JSON:
                stmt.setString(column, kvEntry.getJsonValue().get().toString());
                break;
        }
    }

}
