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

package com.hashmapinc.server.dao.TagMetaData;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.google.common.util.concurrent.ListenableFuture;
import com.hashmapinc.server.common.data.TagMetaData;
import com.hashmapinc.server.common.data.id.EntityId;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.nosql.CassandraAbstractAsyncDao;
import com.hashmapinc.server.dao.timeseries.TsPartitionDate;
import com.hashmapinc.server.dao.util.NoSqlDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.hashmapinc.server.dao.model.ModelConstants.*;

@Component
@Slf4j
@NoSqlDao
public class CassandraBaseTagMetaDataDao  extends CassandraAbstractAsyncDao implements TagMetaDataDao {

    public static final String INSERT_INTO = "INSERT INTO ";
    public static final String GENERATED_QUERY_FOR_ENTITY_TYPE_AND_ENTITY_ID = "Generated query [{}] for entityType {} and entityId {}";
    public static final String SELECT_PREFIX = "SELECT ";
    public static final String EQUALS_PARAM = " = ? ";

    @Autowired
    private Environment environment;

    @Value("${cassandra.query.ts_key_value_partitioning}")
    private String partitioning;

    private TsPartitionDate tsFormat;

    private PreparedStatement latestInsertStmts;
    private PreparedStatement findLatestStmt;

    private boolean isInstall() {
        return environment.acceptsProfiles("install");
    }

    @PostConstruct
    public void init() {
        super.startExecutor();
        if (!isInstall()) {
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
    public ListenableFuture<Void> save(TagMetaData tagMetaData) {
        log.info("Saving tag MetaData [{}]", tagMetaData );
        BoundStatement stmt = getLatestStmt().bind()
                .setString(0, tagMetaData.getEntityType().name())
                .setUUID(1, UUID.fromString(tagMetaData.getEntityId()))
                .setString(2, tagMetaData.getKey())
                .setString(3, tagMetaData.getUnit())
                .setDouble(4, tagMetaData.getAvgFrequency())
                .setDouble(5, tagMetaData.getMinFrequency())
                .setDouble(6, tagMetaData.getMaxFrequency())
                .setDouble(7, tagMetaData.getMeanFrequency())
                .setDouble(8, tagMetaData.getMedianFrequency())
                .setString(9, tagMetaData.getSource());
        log.info("Statement created [{}] ", stmt );
        return getFuture(executeAsyncWrite(stmt), rs -> null);
    }

    private PreparedStatement getLatestStmt() {
        if (latestInsertStmts == null) {
            log.info("latestInsertStmts is null");
            String strStatement = INSERT_INTO + ModelConstants.TAG_METADATA_COLUMN_FAMILY_NAME +
                    "(" + ModelConstants.ENTITY_TYPE_COLUMN +
                    "," + ModelConstants.ENTITY_ID_COLUMN +
                    "," + KEY_COLUMN +
                    "," + TAG_METADATA_UNIT +
                    "," + TAG_METADATA_AVG_FREQUENCY +
                    "," + TAG_METADATA_MIN_FREQUENCY +
                    "," + TAG_METADATA_MAX_FREQUENCY +
                    "," + TAG_METADATA_MEAN_FREQUENCY +
                    "," + TAG_METADATA_MEDIAN_FREQUENCY +
                    "," + TAG_METADATA_SOURCE + ")" +
                    " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            log.info("strStatement " + strStatement);
            Session session = getSession();
            latestInsertStmts = session.prepare(strStatement);

        }
        log.info("Latest Insert Statemet " + latestInsertStmts);
        return latestInsertStmts;
    }

    @Override
    public ListenableFuture<TagMetaData> getByEntityIdAndKey(EntityId entityId, String key) {
        BoundStatement stmt = createLatestStmt().bind();
        stmt.setUUID(0, entityId.getId());
        stmt.setString(1, key);
        log.debug(GENERATED_QUERY_FOR_ENTITY_TYPE_AND_ENTITY_ID, stmt, entityId.getEntityType(), entityId.getId());
        return getFuture(executeAsyncRead(stmt), rs -> convertToTagMetaDataEntity(entityId, key, rs.one()));
    }

    private PreparedStatement createLatestStmt() {
        if (findLatestStmt == null) {
            findLatestStmt = getSession().prepare(SELECT_PREFIX + "* " +
                    "FROM " + ModelConstants.TAG_METADATA_COLUMN_FAMILY_NAME + " " +
                    "WHERE " + ModelConstants.ENTITY_ID_COLUMN + EQUALS_PARAM +
                    "AND " + ModelConstants.KEY_COLUMN + EQUALS_PARAM);
        }
        return findLatestStmt;
    }

    @Override
    public ListenableFuture<List<TagMetaData>> getAllByEntityId(EntityId entityId) {
        BoundStatement stmt = createLatestAllStmt().bind();
        stmt.setUUID(0, entityId.getId());
        log.debug(GENERATED_QUERY_FOR_ENTITY_TYPE_AND_ENTITY_ID, stmt, entityId.getEntityType(), entityId.getId());
        return getFuture(executeAsyncRead(stmt), rs -> convertToTagMetaDataEntities(entityId, rs.all()));
    }

    private List<TagMetaData> convertToTagMetaDataEntities(EntityId entityId, List<Row> rs){
        if(!rs.isEmpty()){
            List<TagMetaData> tagMetaDataList = new ArrayList<>();
            for(Row row : rs) {
                TagMetaData tagMetaData = convertToTagMetaDataEntity(entityId, row.getString(KEY_COLUMN), row);
                tagMetaDataList.add(tagMetaData);
            }
            return tagMetaDataList;
        }
        return null;
    }

    private TagMetaData convertToTagMetaDataEntity(EntityId entityId, String key, Row row){
        if(row != null){
            TagMetaData tagMetaData = new TagMetaData();
            tagMetaData.setEntityId(entityId.getId().toString());
            tagMetaData.setKey(key);
            tagMetaData.setUnit(row.getString(TAG_METADATA_UNIT));
            tagMetaData.setAvgFrequency(row.getDouble(TAG_METADATA_AVG_FREQUENCY));
            tagMetaData.setMinFrequency(row.getDouble(TAG_METADATA_MIN_FREQUENCY));
            tagMetaData.setMaxFrequency(row.getDouble(TAG_METADATA_MAX_FREQUENCY));
            tagMetaData.setMeanFrequency(row.getDouble(TAG_METADATA_MEAN_FREQUENCY));
            tagMetaData.setMedianFrequency(row.getDouble(TAG_METADATA_MEDIAN_FREQUENCY));
            tagMetaData.setSource(row.getString(TAG_METADATA_SOURCE));
            return tagMetaData;
        }
        return null;
    }

    private PreparedStatement createLatestAllStmt() {
        if (findLatestStmt == null) {
            findLatestStmt = getSession().prepare(SELECT_PREFIX + "* " +
                    "FROM " + ModelConstants.TAG_METADATA_COLUMN_FAMILY_NAME + " " +
                    "WHERE " + ModelConstants.ENTITY_ID_COLUMN + EQUALS_PARAM);
        }
        return findLatestStmt;
    }

}
