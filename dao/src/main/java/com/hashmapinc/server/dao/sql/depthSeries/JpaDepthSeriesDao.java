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
package com.hashmapinc.server.dao.sql.depthSeries;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.hashmapinc.server.common.data.DeviceDataSet;
import com.hashmapinc.server.common.data.id.EntityId;
import com.hashmapinc.server.common.data.kv.*;
import com.hashmapinc.server.dao.DaoUtil;
import com.hashmapinc.server.dao.model.sql.DsKvEntity;
import com.hashmapinc.server.dao.model.sql.DsKvLatestEntity;
import com.hashmapinc.server.dao.sql.JpaAbstractDaoListeningExecutorService;
import com.hashmapinc.server.dao.util.SqlDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import com.hashmapinc.server.common.data.UUIDConverter;
import com.hashmapinc.server.common.data.kv.*;
import com.hashmapinc.server.dao.model.sql.*;
import com.hashmapinc.server.dao.depthSeries.DepthSeriesDao;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.hashmapinc.server.common.data.UUIDConverter.fromTimeUUID;
import static com.hashmapinc.server.dao.model.ModelConstants.DS_COLUMN;


@Component
@Slf4j
@SqlDao
public class JpaDepthSeriesDao extends JpaAbstractDaoListeningExecutorService implements DepthSeriesDao {

    @Autowired
    private DsKvRepository dsKvRepository;

    @Autowired
    private DsKvLatestRepository dsKvLatestRepository;

    @Override
    public ListenableFuture<List<DsKvEntry>> findAllAsync(EntityId entityId, List<DsKvQuery> queries) {
        List<ListenableFuture<List<DsKvEntry>>> futures = queries
                .stream()
                .map(query -> findAllAsync(entityId, query))
                .collect(Collectors.toList());
        return Futures.transform(Futures.allAsList(futures), new Function<List<List<DsKvEntry>>, List<DsKvEntry>>() {
            @Nullable
            @Override
            public List<DsKvEntry> apply(@Nullable List<List<DsKvEntry>> results) {
                if (results == null || results.isEmpty()) {
                    return null;
                }
                return results.stream()
                        .flatMap(List::stream)
                        .collect(Collectors.toList());
            }
        }, service);
    }

    private ListenableFuture<List<DsKvEntry>> findAllAsync(EntityId entityId, DsKvQuery query) {
        if (query.getDepthAggregation() == DepthAggregation.NONE) {
            return findAllAsyncWithLimit(entityId, query);
        } else {
            Double stepDs = query.getStartDs();
            List<ListenableFuture<Optional<DsKvEntry>>> futures = new ArrayList<>();
            while (stepDs < query.getEndDs()) {
                Double startDs = stepDs;
                Double endDs = stepDs + query.getInterval();
                Double ds = startDs + (endDs - startDs) / 2;
                futures.add(findAndAggregateAsync(entityId, query.getKey(), startDs, endDs, ds, query.getDepthAggregation()));
                stepDs = endDs;
            }
            ListenableFuture<List<Optional<DsKvEntry>>> future = Futures.allAsList(futures);
            return Futures.transform(future, new Function<List<Optional<DsKvEntry>>, List<DsKvEntry>>() {
                @Nullable
                @Override
                public List<DsKvEntry> apply(@Nullable List<Optional<DsKvEntry>> results) {
                    if (results == null || results.isEmpty()) {
                        return null;
                    }
                    return results.stream()
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toList());
                }
            }, service);
        }
    }

    private ListenableFuture<Optional<DsKvEntry>> findAndAggregateAsync(EntityId entityId, String key, Double startDs, Double endDs, Double ds, DepthAggregation depthAggregation) {
        CompletableFuture<DsKvEntity> entity;
        String entityIdStr = fromTimeUUID(entityId.getId());
        switch (depthAggregation) {
            case AVG:
                entity = dsKvRepository.findAvg(
                        entityIdStr,
                        entityId.getEntityType(),
                        key,
                        startDs,
                        endDs);

                break;
            case MAX:
                entity = dsKvRepository.findMax(
                        entityIdStr,
                        entityId.getEntityType(),
                        key,
                        startDs,
                        endDs);

                break;
            case MIN:
                entity = dsKvRepository.findMin(
                        entityIdStr,
                        entityId.getEntityType(),
                        key,
                        startDs,
                        endDs);

                break;
            case SUM:
                entity = dsKvRepository.findSum(
                        entityIdStr,
                        entityId.getEntityType(),
                        key,
                        startDs,
                        endDs);

                break;
            case COUNT:
                entity = dsKvRepository.findCount(
                        entityIdStr,
                        entityId.getEntityType(),
                        key,
                        startDs,
                        endDs);

                break;
            default:
                throw new IllegalArgumentException("Not supported depthAggregation type: " + depthAggregation);
        }

        SettableFuture<DsKvEntity> listenableFuture = SettableFuture.create();
        entity.whenComplete((dsKvEntity, throwable) -> {
            if (throwable != null) {
                listenableFuture.setException(throwable);
            } else {
                listenableFuture.set(dsKvEntity);
            }
        });
        return Futures.transform(listenableFuture, new Function<DsKvEntity, Optional<DsKvEntry>>() {
            @Nullable
            @Override
            public Optional<DsKvEntry> apply(@Nullable DsKvEntity entity) {
                if (entity != null && entity.isNotEmpty()) {
                    entity.setEntityId(entityIdStr);
                    entity.setEntityType(entityId.getEntityType());
                    entity.setKey(key);
                    entity.setDs(ds);
                    return Optional.of(DaoUtil.getData(entity));
                } else {
                    return Optional.empty();
                }
            }
        });
    }

    private ListenableFuture<List<DsKvEntry>> findAllAsyncWithLimit(EntityId entityId, DsKvQuery query) {
        return Futures.immediateFuture(
                DaoUtil.convertDataList(
                        dsKvRepository.findAllWithLimit(
                                fromTimeUUID(entityId.getId()),
                                entityId.getEntityType(),
                                query.getKey(),
                                query.getStartDs(),
                                query.getEndDs(),
                                new PageRequest(0, query.getLimit()))));
    }

    @Override
    public ListenableFuture<DsKvEntry> findLatest(EntityId entityId, String key) {
        List<DsKvLatestEntity> entries = dsKvLatestRepository.findAllByEntityTypeAndEntityIdAndKey(
                entityId.getEntityType(),
                UUIDConverter.fromTimeUUID(entityId.getId()),key);
        DsKvEntry result;
        if (entries.get(0) != null) {
            result = DaoUtil.getData(entries.get(0));
        } else {
            result = new BasicDsKvEntry(0.0, new StringDataEntry(key, null));
        }
        return Futures.immediateFuture(result);
    }

    @Override
    public ListenableFuture<List<DsKvEntry>> findAllLatest(EntityId entityId) {
        return Futures.immediateFuture(
                DaoUtil.convertDataList(Lists.newArrayList(
                        dsKvLatestRepository.findAllByEntityTypeAndEntityId(
                                entityId.getEntityType(),
                                UUIDConverter.fromTimeUUID(entityId.getId())))));
    }

    @Override
    public ListenableFuture<Void> save(EntityId entityId, DsKvEntry dsKvEntry, long ttl) {
        DsKvEntity entity = new DsKvEntity();
        entity.setEntityType(entityId.getEntityType());
        entity.setEntityId(fromTimeUUID(entityId.getId()));
        entity.setDs(dsKvEntry.getDs());
        entity.setKey(dsKvEntry.getKey());
        entity.setStrValue(dsKvEntry.getStrValue().orElse(null));
        entity.setDoubleValue(dsKvEntry.getDoubleValue().orElse(null));
        entity.setLongValue(dsKvEntry.getLongValue().orElse(null));
        entity.setBooleanValue(dsKvEntry.getBooleanValue().orElse(null));
        entity.setJsonValue(dsKvEntry.getJsonValue().orElse(null));
        return service.submit(() -> {
            dsKvRepository.save(entity);
            return null;
        });
    }

    @Override
    public ListenableFuture<Void> savePartition(EntityId entityId, Double dsKvEntryTs, String key, long ttl) {
        return service.submit(() -> null);
    }

    @Override
    public ListenableFuture<Void> saveLatest(EntityId entityId, DsKvEntry dsKvEntry) {
        DsKvLatestEntity latestEntity = new DsKvLatestEntity();
        latestEntity.setEntityType(entityId.getEntityType());
        latestEntity.setEntityId(fromTimeUUID(entityId.getId()));
        latestEntity.setDs(dsKvEntry.getDs());
        latestEntity.setKey(dsKvEntry.getKey());
        latestEntity.setStrValue(dsKvEntry.getStrValue().orElse(null));
        latestEntity.setDoubleValue(dsKvEntry.getDoubleValue().orElse(null));
        latestEntity.setLongValue(dsKvEntry.getLongValue().orElse(null));
        latestEntity.setBooleanValue(dsKvEntry.getBooleanValue().orElse(null));
        latestEntity.setJsonValue(dsKvEntry.getJsonValue().orElse(null));
        return service.submit(() -> {
            dsKvLatestRepository.save(latestEntity);
            return null;
        });
    }

    @Override
    public DeviceDataSet findAllBetweenDepths(EntityId entityId, Double startDs, Double endDs) {
        List<Object[]> results = dsKvRepository.findSelected(fromTimeUUID(entityId.getId()), entityId.getEntityType(), startDs, endDs);
        List<String> headerColumns = new ArrayList<>();
        headerColumns.add(DS_COLUMN);
        Map<String, Map<String, String>> tableRowsGroupedByDS = new HashMap<>();
        for (int i = 0; i < results.size(); i++) {

            Object[] row = results.get(i);
            String ds = row[0].toString();
            String key = row[1].toString();
            String value = getFirstNonEmptyValue(row, 2, row.length - 1);
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

        return new DeviceDataSet(tableRowsGroupedByDS, headerColumns, DS_COLUMN);
    }

    private String getFirstNonEmptyValue(Object[] array, int s, int e) {
        for(int i = s; i <= e; i ++) {
            if(array[i] !=null && array[i] != "") return  array[i].toString();
        }
        return "";
    }
}
