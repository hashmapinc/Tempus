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
import com.hashmapinc.server.common.data.TagMetaDataQuality;
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
public class CassandraBaseTagMetaDataQualityDao extends CassandraAbstractAsyncDao implements TagMetaDataQualityDao {

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
    public ListenableFuture<Void> save(TagMetaDataQuality tagMetaDataQuality) {
        log.debug("Saving tag MetaData [{}]", tagMetaDataQuality);
        BoundStatement stmt = getLatestStmt().bind()
                .setString(0, tagMetaDataQuality.getEntityType().name())
                .setUUID(1, UUID.fromString(tagMetaDataQuality.getEntityId()))
                .setString(2, tagMetaDataQuality.getKey())
                .setString(3, tagMetaDataQuality.getUnit())
                .setDouble(4, tagMetaDataQuality.getAvgFrequency())
                .setDouble(5, tagMetaDataQuality.getMinFrequency())
                .setDouble(6, tagMetaDataQuality.getMaxFrequency())
                .setDouble(7, tagMetaDataQuality.getMeanFrequency())
                .setDouble(8, tagMetaDataQuality.getMedianFrequency())
                .setString(9, tagMetaDataQuality.getSource());
        return getFuture(executeAsyncWrite(stmt), rs -> null);
    }

    private PreparedStatement getLatestStmt() {
        if (latestInsertStmts == null) {
            String strStatement = INSERT_INTO + ModelConstants.TAG_METADATA_QUALITY_COLUMN_FAMILY_NAME +
                    "(" + ModelConstants.ENTITY_TYPE_COLUMN +
                    "," + ModelConstants.ENTITY_ID_COLUMN +
                    "," + KEY_COLUMN +
                    "," + TAG_METADATA_QUALITY_UNIT +
                    "," + TAG_METADATA_QUALITY_AVG_FREQUENCY +
                    "," + TAG_METADATA_QUALITY_MIN_FREQUENCY +
                    "," + TAG_METADATA_QUALITY_MAX_FREQUENCY +
                    "," + TAG_METADATA_QUALITY_MEAN_FREQUENCY +
                    "," + TAG_METADATA_QUALITY_MEDIAN_FREQUENCY +
                    "," + TAG_METADATA_QUALITY_SOURCE + ")" +
                    " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            Session session = getSession();
            latestInsertStmts = session.prepare(strStatement);

        }
        return latestInsertStmts;
    }

    @Override
    public ListenableFuture<TagMetaDataQuality> getByEntityIdAndKey(EntityId entityId, String key) {
        BoundStatement stmt = createLatestStmt().bind();
        stmt.setUUID(0, entityId.getId());
        stmt.setString(1, key);
        log.debug(GENERATED_QUERY_FOR_ENTITY_TYPE_AND_ENTITY_ID, stmt, entityId.getEntityType(), entityId.getId());
        return getFuture(executeAsyncRead(stmt), rs -> convertToTagMetaDataEntity(entityId, key, rs.one()));
    }

    private PreparedStatement createLatestStmt() {
        if (findLatestStmt == null) {
            findLatestStmt = getSession().prepare(SELECT_PREFIX + "* " +
                    "FROM " + ModelConstants.TAG_METADATA_QUALITY_COLUMN_FAMILY_NAME + " " +
                    "WHERE " + ModelConstants.ENTITY_ID_COLUMN + EQUALS_PARAM +
                    "AND " + ModelConstants.KEY_COLUMN + EQUALS_PARAM);
        }
        return findLatestStmt;
    }

    @Override
    public ListenableFuture<List<TagMetaDataQuality>> getAllByEntityId(EntityId entityId) {
        BoundStatement stmt = createLatestAllStmt().bind();
        stmt.setUUID(0, entityId.getId());
        log.debug(GENERATED_QUERY_FOR_ENTITY_TYPE_AND_ENTITY_ID, stmt, entityId.getEntityType(), entityId.getId());
        return getFuture(executeAsyncRead(stmt), rs -> convertToTagMetaDataEntities(entityId, rs.all()));
    }

    private List<TagMetaDataQuality> convertToTagMetaDataEntities(EntityId entityId, List<Row> rs){
        if(!rs.isEmpty()){
            List<TagMetaDataQuality> tagMetaDataQualityList = new ArrayList<>();
            for(Row row : rs) {
                TagMetaDataQuality tagMetaDataQuality = convertToTagMetaDataEntity(entityId, row.getString(KEY_COLUMN), row);
                tagMetaDataQualityList.add(tagMetaDataQuality);
            }
            return tagMetaDataQualityList;
        }
        return null;
    }

    private TagMetaDataQuality convertToTagMetaDataEntity(EntityId entityId, String key, Row row){
        if(row != null){
            TagMetaDataQuality tagMetaDataQuality = new TagMetaDataQuality();
            tagMetaDataQuality.setEntityId(entityId.getId().toString());
            tagMetaDataQuality.setKey(key);
            tagMetaDataQuality.setUnit(row.getString(TAG_METADATA_QUALITY_UNIT));
            tagMetaDataQuality.setAvgFrequency(row.getDouble(TAG_METADATA_QUALITY_AVG_FREQUENCY));
            tagMetaDataQuality.setMinFrequency(row.getDouble(TAG_METADATA_QUALITY_MIN_FREQUENCY));
            tagMetaDataQuality.setMaxFrequency(row.getDouble(TAG_METADATA_QUALITY_MAX_FREQUENCY));
            tagMetaDataQuality.setMeanFrequency(row.getDouble(TAG_METADATA_QUALITY_MEAN_FREQUENCY));
            tagMetaDataQuality.setMedianFrequency(row.getDouble(TAG_METADATA_QUALITY_MEDIAN_FREQUENCY));
            tagMetaDataQuality.setSource(row.getString(TAG_METADATA_QUALITY_SOURCE));
            return tagMetaDataQuality;
        }
        return null;
    }

    private PreparedStatement createLatestAllStmt() {
        if (findLatestStmt == null) {
            findLatestStmt = getSession().prepare(SELECT_PREFIX + "* " +
                    "FROM " + ModelConstants.TAG_METADATA_QUALITY_COLUMN_FAMILY_NAME + " " +
                    "WHERE " + ModelConstants.ENTITY_ID_COLUMN + EQUALS_PARAM);
        }
        return findLatestStmt;
    }

}
