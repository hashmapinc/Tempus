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
package com.hashmapinc.server.dao.sql.attributes;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.hashmapinc.server.dao.sql.JpaAbstractDaoListeningExecutorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.hashmapinc.server.common.data.DeviceDataSet;
import com.hashmapinc.server.common.data.UUIDConverter;
import com.hashmapinc.server.common.data.id.EntityId;
import com.hashmapinc.server.common.data.kv.AttributeKvEntry;
import com.hashmapinc.server.dao.DaoUtil;
import com.hashmapinc.server.dao.attributes.AttributesDao;
import com.hashmapinc.server.dao.model.sql.AttributeKvCompositeKey;
import com.hashmapinc.server.dao.model.sql.AttributeKvEntity;
import com.hashmapinc.server.dao.util.SqlDao;

import java.util.*;
import java.util.stream.Collectors;

import static com.hashmapinc.server.common.data.UUIDConverter.fromTimeUUID;
import static com.hashmapinc.server.dao.model.ModelConstants.LAST_UPDATE_TS_COLUMN;

@Component
@Slf4j
@SqlDao
public class JpaAttributeDao extends JpaAbstractDaoListeningExecutorService implements AttributesDao {

    @Autowired
    private AttributeKvRepository attributeKvRepository;

    @Override
    public ListenableFuture<Optional<AttributeKvEntry>> find(EntityId entityId, String attributeType, String attributeKey) {
        AttributeKvCompositeKey compositeKey =
                getAttributeKvCompositeKey(entityId, attributeType, attributeKey);
        return Futures.immediateFuture(
                Optional.ofNullable(DaoUtil.getData(attributeKvRepository.findOne(compositeKey))));
    }

    @Override
    public ListenableFuture<List<AttributeKvEntry>> find(EntityId entityId, String attributeType, Collection<String> attributeKeys) {
        List<AttributeKvCompositeKey> compositeKeys =
                attributeKeys
                        .stream()
                        .map(attributeKey ->
                                getAttributeKvCompositeKey(entityId, attributeType, attributeKey))
                        .collect(Collectors.toList());
        return Futures.immediateFuture(
                DaoUtil.convertDataList(Lists.newArrayList(attributeKvRepository.findAll(compositeKeys))));
    }

    @Override
    public ListenableFuture<List<AttributeKvEntry>> findAll(EntityId entityId, String attributeType) {
        return Futures.immediateFuture(
                DaoUtil.convertDataList(Lists.newArrayList(
                        attributeKvRepository.findAllByEntityTypeAndEntityIdAndAttributeType(
                                entityId.getEntityType(),
                                UUIDConverter.fromTimeUUID(entityId.getId()),
                                attributeType))));
    }

    @Override
    public ListenableFuture<Void> save(EntityId entityId, String attributeType, AttributeKvEntry attribute) {
        AttributeKvEntity entity = new AttributeKvEntity();
        entity.setEntityType(entityId.getEntityType());
        entity.setEntityId(fromTimeUUID(entityId.getId()));
        entity.setAttributeType(attributeType);
        entity.setAttributeKey(attribute.getKey());
        entity.setLastUpdateTs(attribute.getLastUpdateTs());
        entity.setStrValue(attribute.getStrValue().orElse(null));
        entity.setDoubleValue(attribute.getDoubleValue().orElse(null));
        entity.setLongValue(attribute.getLongValue().orElse(null));
        entity.setBooleanValue(attribute.getBooleanValue().orElse(null));
        entity.setJsonValue(attribute.getJsonValue().orElse(null));
        return service.submit(() -> {
            attributeKvRepository.save(entity);
            return null;
        });
    }

    @Override
    public ListenableFuture<List<Void>> removeAll(EntityId entityId, String attributeType, List<String> keys) {
        List<AttributeKvEntity> entitiesToDelete = keys
                .stream()
                .map(key -> {
                    AttributeKvEntity entityToDelete = new AttributeKvEntity();
                    entityToDelete.setEntityType(entityId.getEntityType());
                    entityToDelete.setEntityId(fromTimeUUID(entityId.getId()));
                    entityToDelete.setAttributeType(attributeType);
                    entityToDelete.setAttributeKey(key);
                    return entityToDelete;
                }).collect(Collectors.toList());

        return service.submit(() -> {
            attributeKvRepository.delete(entitiesToDelete);
            return null;
        });
    }

    @Override
    public DeviceDataSet findAll(EntityId entityId) {
        List<Object[]> results = attributeKvRepository.findAllByEntityTypeAndEntityId(fromTimeUUID(entityId.getId()), entityId.getEntityType());
        List<String> headerColumns = new ArrayList<>();

        headerColumns.add(LAST_UPDATE_TS_COLUMN);
        Map<String, Map<String, String>> tableRowsGroupedByTS= new HashMap<>();
        for(int i = 0; i < results.size(); i++) {
                Object[] row = results.get(i);
                String ts = row[0].toString();
                String key = row[1].toString();
                String value = getFirstNonEmptyValue(row, 2, row.length - 1);
                if(!headerColumns.contains(key)) {
                    headerColumns.add(key);
                }
                if(tableRowsGroupedByTS.containsKey(ts)) {
                    Map<String, String> attributeVsValue = tableRowsGroupedByTS.get(ts);
                    attributeVsValue.put(key, value);
                    tableRowsGroupedByTS.put(ts, attributeVsValue);
                } else {
                    Map<String, String> attributeVsValue = new HashMap<>();
                    attributeVsValue.put(key, value);
                    tableRowsGroupedByTS.put(ts, attributeVsValue);
                }
        }
        return new DeviceDataSet(tableRowsGroupedByTS, headerColumns, LAST_UPDATE_TS_COLUMN);
    }

    private String getFirstNonEmptyValue(Object[] array, int s, int e) {
        for(int i = s; i <= e; i ++) {
            if(array[i] !=null && array[i] != "") return  array[i].toString();
        }
        return "";
    }

    private AttributeKvCompositeKey getAttributeKvCompositeKey(EntityId entityId, String attributeType, String attributeKey) {
        return new AttributeKvCompositeKey(
                entityId.getEntityType(),
                fromTimeUUID(entityId.getId()),
                attributeType,
                attributeKey);
    }
}
