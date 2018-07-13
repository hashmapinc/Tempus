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

package com.hashmapinc.server.dao.sql.tagmetadata;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.hashmapinc.server.common.data.TagMetaData;
import com.hashmapinc.server.common.data.UUIDConverter;
import com.hashmapinc.server.common.data.id.EntityId;
import com.hashmapinc.server.dao.DaoUtil;
import com.hashmapinc.server.dao.tagmetadata.TagMetaDataDao;
import com.hashmapinc.server.dao.model.sql.TagMetaDataCompositeKey;
import com.hashmapinc.server.dao.model.sql.TagMetaDataEntity;
import com.hashmapinc.server.dao.sql.JpaAbstractDaoListeningExecutorService;
import com.hashmapinc.server.dao.timeseries.TsInsertExecutorType;
import com.hashmapinc.server.dao.util.SqlDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;

@Component
@Slf4j
@SqlDao
public class JpaTagMetaDataDao extends JpaAbstractDaoListeningExecutorService implements TagMetaDataDao {

    @Value("${sql.ts_inserts_executor_type}")
    private String insertExecutorType;

    @Value("${sql.ts_inserts_fixed_thread_pool_size}")
    private int insertFixedThreadPoolSize;

    private ListeningExecutorService insertService;

    @Autowired
    private TagMetaDataRepository tagMetaDataRepository;

    @PostConstruct
    public void init() {
        Optional<TsInsertExecutorType> executorTypeOptional = TsInsertExecutorType.parse(insertExecutorType);
        TsInsertExecutorType executorType;
        if (executorTypeOptional.isPresent()) {
            executorType = executorTypeOptional.get();
        } else {
            executorType = TsInsertExecutorType.FIXED;
        }
        switch (executorType) {
            case SINGLE:
                insertService = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());
                break;
            case FIXED:
                int poolSize = insertFixedThreadPoolSize;
                if (poolSize <= 0) {
                    poolSize = 10;
                }
                insertService = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(poolSize));
                break;
            case CACHED:
                insertService = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
                break;
        }
    }



    @Override
    public ListenableFuture<Void> save(TagMetaData tagMetaData) {
        UUID id = UUID.fromString(tagMetaData.getEntityId());
        tagMetaData.setEntityId(UUIDConverter.fromTimeUUID(id));
        TagMetaDataEntity tagMetaDataEntity = new TagMetaDataEntity(tagMetaData);
        return insertService.submit(() -> {
            tagMetaDataRepository.save(tagMetaDataEntity);
            return null;
        });
    }

    @Override
    public ListenableFuture<TagMetaData> getByEntityIdAndKey(EntityId entityId, String key) {
        TagMetaDataCompositeKey tagMetaDataCompositeKey = new TagMetaDataCompositeKey(entityId.getEntityType(),
                UUIDConverter.fromTimeUUID(entityId.getId()), key);
        TagMetaDataEntity tagMetaDataEntity = tagMetaDataRepository.findOne(tagMetaDataCompositeKey);
        TagMetaData tagMetaData = new TagMetaData();
        if(tagMetaDataEntity != null){
            tagMetaData = tagMetaDataEntity.toData();
        }
        return Futures.immediateFuture(tagMetaData);
    }

    @Override
    public ListenableFuture<List<TagMetaData>> getAllByEntityId(EntityId entityId) {
        return Futures.immediateFuture(
                DaoUtil.convertDataList(Lists.newArrayList(
                        tagMetaDataRepository.findAllByEntityIdAndEntityType(UUIDConverter.fromTimeUUID(entityId.getId()), entityId.getEntityType()))));
    }
}
