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
package com.hashmapinc.server.dao.depthSeries;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.hashmapinc.server.common.data.DeviceDataSet;
import com.hashmapinc.server.common.data.id.EntityId;
import com.hashmapinc.server.dao.exception.IncorrectParameterException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.hashmapinc.server.common.data.kv.DsKvEntry;
import com.hashmapinc.server.common.data.kv.DsKvQuery;
import com.hashmapinc.server.dao.service.Validator;

import java.util.Collection;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;


@Service
@Slf4j
public class BaseDepthSeriesService implements DepthSeriesService {

    public static final int INSERTS_PER_ENTRY = 3;

    @Autowired
    private DepthSeriesDao depthSeriesDao;

    @Override
    public ListenableFuture<List<DsKvEntry>> findAll(EntityId entityId, List<DsKvQuery> queries) {
        validate(entityId);
        queries.forEach(query -> validate(query));
        return depthSeriesDao.findAllAsync(entityId, queries);
    }

    @Override
    public ListenableFuture<List<DsKvEntry>> findLatest(EntityId entityId, Collection<String> keys) {
        validate(entityId);
        List<ListenableFuture<DsKvEntry>> futures = Lists.newArrayListWithExpectedSize(keys.size());
        keys.forEach(key -> Validator.validateString(key, "Incorrect key " + key));
        keys.forEach(key -> futures.add(depthSeriesDao.findLatest(entityId, key)));
        return Futures.allAsList(futures);
    }

    @Override
    public ListenableFuture<List<DsKvEntry>> findAllLatest(EntityId entityId) {
        validate(entityId);
        return depthSeriesDao.findAllLatest(entityId);
    }

    @Override
    public ListenableFuture<List<Void>> save(EntityId entityId, DsKvEntry dsKvEntry) {
        validate(entityId);
        if (dsKvEntry == null) {
            throw new IncorrectParameterException("Key value entry can't be null");
        }
        List<ListenableFuture<Void>> futures = Lists.newArrayListWithExpectedSize(INSERTS_PER_ENTRY);
        saveAndRegisterFutures(futures, entityId, dsKvEntry, 0L);
        return Futures.allAsList(futures);
    }

    @Override
    public ListenableFuture<List<Void>> save(EntityId entityId, List<DsKvEntry> dsKvEntries, long ttl) {
        List<ListenableFuture<Void>> futures = Lists.newArrayListWithExpectedSize(dsKvEntries.size() * INSERTS_PER_ENTRY);
        for (DsKvEntry dsKvEntry : dsKvEntries) {
            if (dsKvEntry == null) {
                throw new IncorrectParameterException("Key value entry can't be null");
            }
            saveAndRegisterFutures(futures, entityId, dsKvEntry, ttl);
        }
        return Futures.allAsList(futures);
    }

    @Override
    public DeviceDataSet findAllBetweenDepths(EntityId entityId, Double startDs, Double endDs) {
        validate(entityId);
        return depthSeriesDao.findAllBetweenDepths(entityId, startDs, endDs);
    }

    private void saveAndRegisterFutures(List<ListenableFuture<Void>> futures, EntityId entityId, DsKvEntry dsKvEntry, long ttl) {
        futures.add(depthSeriesDao.savePartition(entityId, dsKvEntry.getDs(), dsKvEntry.getKey(), ttl));
        futures.add(depthSeriesDao.saveLatest(entityId, dsKvEntry));
        futures.add(depthSeriesDao.save(entityId, dsKvEntry, ttl));
    }

    private static void validate(EntityId entityId) {
        Validator.validateEntityId(entityId, "Incorrect entityId " + entityId);
    }

    private static void validate(DsKvQuery query) {
        if (query == null) {
            throw new IncorrectParameterException("TsKvQuery can't be null");
        } else if (isBlank(query.getKey())) {
            throw new IncorrectParameterException("Incorrect TsKvQuery. Key can't be empty");
        } else if (query.getDepthAggregation() == null) {
            throw new IncorrectParameterException("Incorrect TsKvQuery. Aggregation can't be empty");
        }
    }
}
