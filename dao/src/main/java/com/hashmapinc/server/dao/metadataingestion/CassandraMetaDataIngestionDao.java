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
package com.hashmapinc.server.dao.metadataingestion;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.querybuilder.Select;
import com.google.common.base.Function;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.kv.MetaDataKvEntry;
import com.hashmapinc.server.common.data.kv.StringDataEntry;
import com.hashmapinc.server.common.data.metadata.MetadataConfigId;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.nosql.CassandraAbstractAsyncDao;
import com.hashmapinc.server.dao.util.NoSqlDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;

@Component
@Slf4j
@NoSqlDao
public class CassandraMetaDataIngestionDao extends CassandraAbstractAsyncDao implements MetaDataIngestionDao{

    private PreparedStatement saveStmt;

    @Override
    public ListenableFuture<Void> save(TenantId tenantId, MetadataConfigId metadataConfigId, String dataSourceName, MetaDataKvEntry metaDataKvEntry) {
        BoundStatement stmt = getSaveStmt().bind();
        stmt.setUUID(0, tenantId.getId());
        stmt.setUUID(1, metadataConfigId.getId());
        stmt.setString(2, dataSourceName);
        stmt.setString(3, metaDataKvEntry.getKey());
        stmt.setString(4, metaDataKvEntry.getValue());
        stmt.setLong(5, metaDataKvEntry.getLastUpdateTs());
        return getFuture(executeAsyncWrite(stmt), rs -> null);
    }

    @Override
    public ListenableFuture<List<MetaDataKvEntry>> findAll(MetadataConfigId metadataConfigId) {
        Select.Where select = select().from(ModelConstants.METADATA_ENTRIES_VIEW_BY_CONFIG)
                .where(eq(ModelConstants.METADATA_CONFIG_ID, metadataConfigId.getId()));
        log.trace("Generated query [{}] for metadataConfigId {}", select, metadataConfigId);
        return Futures.transform(executeAsyncRead(select), (Function<? super ResultSet, ? extends List<MetaDataKvEntry>>) this::convertResultToMetadataKvEntryList
                , readResultsProcessingExecutor);
    }

    private PreparedStatement getSaveStmt() {
        if (saveStmt == null) {
            saveStmt = getSession().prepare("INSERT INTO " + ModelConstants.METADATA_ENTRIES_TABLE +
                    "(" + ModelConstants.TENANT_ID_PROPERTY +
                    "," + ModelConstants.METADATA_CONFIG_ID +
                    "," + ModelConstants.METADATA_DATASOURCE_NAME_COLUMN +
                    "," + ModelConstants.METADATA_INGESTION_KEY_COLUMN +
                    "," + ModelConstants.METADATA_INGESTION_VALUE_COLUMN +
                    "," + ModelConstants.LAST_UPDATE_TS_COLUMN +
                    ")" +
                    " VALUES(?, ?, ?, ?, ?, ?)");
        }
        return saveStmt;
    }

    private List<MetaDataKvEntry> convertResultToMetadataKvEntryList(ResultSet resultSet) {
        List<Row> rows = resultSet.all();
        List<MetaDataKvEntry> entries = new ArrayList<>(rows.size());
        rows.forEach(row -> {
            String key = row.getString(ModelConstants.METADATA_INGESTION_KEY_COLUMN);
            String value = row.getString(ModelConstants.METADATA_INGESTION_VALUE_COLUMN);
            long lastUpdateTs = row.get(ModelConstants.LAST_UPDATE_TS_COLUMN, Long.class);
            entries.add(new MetaDataKvEntry(new StringDataEntry(key, value), lastUpdateTs));
        });
        return entries;
    }
}
