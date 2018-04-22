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
package com.hashmapinc.server.dao.attributes;

import com.datastax.driver.core.*;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Function;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.hashmapinc.server.common.data.DataConstants;
import com.hashmapinc.server.common.data.DeviceDataSet;
import com.hashmapinc.server.common.data.id.EntityId;
import com.hashmapinc.server.common.data.kv.AttributeKvEntry;
import com.hashmapinc.server.common.data.kv.BaseAttributeKvEntry;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.nosql.CassandraAbstractAsyncDao;
import com.hashmapinc.server.dao.timeseries.CassandraBaseTimeseriesDao;
import com.hashmapinc.server.dao.util.NoSqlDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.stream.Collectors;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;

/**
 * @author Andrew Shvayka
 */
@Component
@Slf4j
@NoSqlDao
public class CassandraBaseAttributesDao extends CassandraAbstractAsyncDao implements AttributesDao {

    private PreparedStatement saveStmt;

    @PostConstruct
    public void init() {
        super.startExecutor();
    }

    @PreDestroy
    public void stop() {
        super.stopExecutor();
    }

    @Override
    public ListenableFuture<Optional<AttributeKvEntry>> find(EntityId entityId, String attributeType, String attributeKey) {
        Select.Where select = select().from(ModelConstants.ATTRIBUTES_KV_CF)
                .where(eq(ModelConstants.ENTITY_TYPE_COLUMN, entityId.getEntityType()))
                .and(eq(ModelConstants.ENTITY_ID_COLUMN, entityId.getId()))
                .and(eq(ModelConstants.ATTRIBUTE_TYPE_COLUMN, attributeType))
                .and(eq(ModelConstants.ATTRIBUTE_KEY_COLUMN, attributeKey));
        log.trace("Generated query [{}] for entityId {} and key {}", select, entityId, attributeKey);
        return Futures.transform(executeAsyncRead(select), (Function<? super ResultSet, ? extends Optional<AttributeKvEntry>>) input ->
                        Optional.ofNullable(convertResultToAttributesKvEntry(attributeKey, input.one()))
                , readResultsProcessingExecutor);
    }

    @Override
    public ListenableFuture<List<AttributeKvEntry>> find(EntityId entityId, String attributeType, Collection<String> attributeKeys) {
        List<ListenableFuture<Optional<AttributeKvEntry>>> entries = new ArrayList<>();
        attributeKeys.forEach(attributeKey -> entries.add(find(entityId, attributeType, attributeKey)));
        return Futures.transform(Futures.allAsList(entries), (Function<List<Optional<AttributeKvEntry>>, ? extends List<AttributeKvEntry>>) input -> {
            List<AttributeKvEntry> result = new ArrayList<>();
            input.stream().filter(opt -> opt.isPresent()).forEach(opt -> result.add(opt.get()));
            return result;
        }, readResultsProcessingExecutor);
    }


    @Override
    public ListenableFuture<List<AttributeKvEntry>> findAll(EntityId entityId, String attributeType) {
        Select.Where select = select().from(ModelConstants.ATTRIBUTES_KV_CF)
                .where(eq(ModelConstants.ENTITY_TYPE_COLUMN, entityId.getEntityType()))
                .and(eq(ModelConstants.ENTITY_ID_COLUMN, entityId.getId()))
                .and(eq(ModelConstants.ATTRIBUTE_TYPE_COLUMN, attributeType));
        log.trace("Generated query [{}] for entityId {} and attributeType {}", select, entityId, attributeType);
        return Futures.transform(executeAsyncRead(select), (Function<? super ResultSet, ? extends List<AttributeKvEntry>>) input ->
                        convertResultToAttributesKvEntryList(input)
                , readResultsProcessingExecutor);
    }

    @Override
    public ListenableFuture<Void> save(EntityId entityId, String attributeType, AttributeKvEntry attribute) {
        BoundStatement stmt = getSaveStmt().bind();
        stmt.setString(0, entityId.getEntityType().name());
        stmt.setUUID(1, entityId.getId());
        stmt.setString(2, attributeType);
        stmt.setString(3, attribute.getKey());
        stmt.setLong(4, attribute.getLastUpdateTs());
        stmt.setString(5, attribute.getStrValue().orElse(null));
        Optional<Boolean> booleanValue = attribute.getBooleanValue();
        if (booleanValue.isPresent()) {
            stmt.setBool(6, booleanValue.get());
        } else {
            stmt.setToNull(6);
        }
        Optional<Long> longValue = attribute.getLongValue();
        if (longValue.isPresent()) {
            stmt.setLong(7, longValue.get());
        } else {
            stmt.setToNull(7);
        }
        Optional<Double> doubleValue = attribute.getDoubleValue();
        if (doubleValue.isPresent()) {
            stmt.setDouble(8, doubleValue.get());
        } else {
            stmt.setToNull(8);
        }
        if (attribute.getJsonValue().isPresent()) {
            stmt.setString(9, attribute.getJsonValue().get().toString());
        } else {
            stmt.setToNull(9);
        }
        log.trace("Generated save stmt [{}] for entityId {} and attributeType {} and attribute", stmt, entityId, attributeType, attribute);
        return getFuture(executeAsyncWrite(stmt), rs -> null);
    }

    @Override
    public ListenableFuture<List<Void>> removeAll(EntityId entityId, String attributeType, List<String> keys) {
        List<ListenableFuture<Void>> futures = keys
                .stream()
                .map(key -> delete(entityId, attributeType, key))
                .collect(Collectors.toList());
        return Futures.allAsList(futures);
    }

    @Override
    public DeviceDataSet findAll(EntityId entityId) {
        Select.Where select = QueryBuilder.select(ModelConstants.LAST_UPDATE_TS_COLUMN, ModelConstants.ATTRIBUTE_KEY_COLUMN, ModelConstants.BOOLEAN_VALUE_COLUMN, ModelConstants.DOUBLE_VALUE_COLUMN, ModelConstants.JSON_VALUE_COLUMN, ModelConstants.LONG_VALUE_COLUMN, ModelConstants.STRING_VALUE_COLUMN)
                .from(ModelConstants.ATTRIBUTES_KV_CF)
                .where(eq(ModelConstants.ENTITY_TYPE_COLUMN, entityId.getEntityType().name())).and(eq(ModelConstants.ENTITY_ID_COLUMN, entityId.getId()));
        select.and(QueryBuilder.in(ModelConstants.ATTRIBUTE_TYPE_COLUMN, DataConstants.allScopes()));
        List<Row> rows = executeRead(select).all();
        List<String> headerColumns = new ArrayList<>();
        headerColumns.add(ModelConstants.LAST_UPDATE_TS_COLUMN);
        Map<String, Map<String, String>> tableRowsGroupedByTS = new HashMap<>();
        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            String ts = row.get(ModelConstants.LAST_UPDATE_TS_COLUMN, Long.class).toString();
            String key = row.getString(ModelConstants.ATTRIBUTE_KEY_COLUMN);

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
        return new DeviceDataSet(tableRowsGroupedByTS, headerColumns, ModelConstants.LAST_UPDATE_TS_COLUMN);
    }

    private String getNonNullValueForTsKey(Row row) {
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

    private ListenableFuture<Void> delete(EntityId entityId, String attributeType, String key) {
        Statement delete = QueryBuilder.delete().all().from(ModelConstants.ATTRIBUTES_KV_CF)
                .where(eq(ModelConstants.ENTITY_TYPE_COLUMN, entityId.getEntityType()))
                .and(eq(ModelConstants.ENTITY_ID_COLUMN, entityId.getId()))
                .and(eq(ModelConstants.ATTRIBUTE_TYPE_COLUMN, attributeType))
                .and(eq(ModelConstants.ATTRIBUTE_KEY_COLUMN, key));
        log.debug("Remove request: {}", delete.toString());
        return getFuture(getSession().executeAsync(delete), rs -> null);
    }

    private PreparedStatement getSaveStmt() {
        if (saveStmt == null) {
            saveStmt = getSession().prepare("INSERT INTO " + ModelConstants.ATTRIBUTES_KV_CF +
                    "(" + ModelConstants.ENTITY_TYPE_COLUMN +
                    "," + ModelConstants.ENTITY_ID_COLUMN +
                    "," + ModelConstants.ATTRIBUTE_TYPE_COLUMN +
                    "," + ModelConstants.ATTRIBUTE_KEY_COLUMN +
                    "," + ModelConstants.LAST_UPDATE_TS_COLUMN +
                    "," + ModelConstants.STRING_VALUE_COLUMN +
                    "," + ModelConstants.BOOLEAN_VALUE_COLUMN +
                    "," + ModelConstants.LONG_VALUE_COLUMN +
                    "," + ModelConstants.DOUBLE_VALUE_COLUMN +
                    "," + ModelConstants.JSON_VALUE_COLUMN +
                    ")" +
                    " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        }
        return saveStmt;
    }

    private AttributeKvEntry convertResultToAttributesKvEntry(String key, Row row) {
        AttributeKvEntry attributeEntry = null;
        if (row != null) {
            long lastUpdateTs = row.get(ModelConstants.LAST_UPDATE_TS_COLUMN, Long.class);
            attributeEntry = new BaseAttributeKvEntry(CassandraBaseTimeseriesDao.toKvEntry(row, key), lastUpdateTs);
        }
        return attributeEntry;
    }

    private List<AttributeKvEntry> convertResultToAttributesKvEntryList(ResultSet resultSet) {
        List<Row> rows = resultSet.all();
        List<AttributeKvEntry> entries = new ArrayList<>(rows.size());
        if (!rows.isEmpty()) {
            rows.forEach(row -> {
                String key = row.getString(ModelConstants.ATTRIBUTE_KEY_COLUMN);
                AttributeKvEntry kvEntry = convertResultToAttributesKvEntry(key, row);
                if (kvEntry != null) {
                    entries.add(kvEntry);
                }
            });
        }
        return entries;
    }
}
