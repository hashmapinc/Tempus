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

package com.hashmapinc.server.dao.sql.TagMetaData;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.hashmapinc.server.common.data.TagMetaDataQuality;
import com.hashmapinc.server.common.data.UUIDConverter;
import com.hashmapinc.server.common.data.id.EntityId;
import com.hashmapinc.server.dao.DaoUtil;
import com.hashmapinc.server.dao.TagMetaData.TagMetaDataQualityDao;
import com.hashmapinc.server.dao.model.sql.TagMetaDataQualityCompositeKey;
import com.hashmapinc.server.dao.model.sql.TagMetaDataQualityEntity;
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
public class JpaTagMetaDataQualityDao extends JpaAbstractDaoListeningExecutorService implements TagMetaDataQualityDao {

    @Value("${sql.ts_inserts_executor_type}")
    private String insertExecutorType;

    @Value("${sql.ts_inserts_fixed_thread_pool_size}")
    private int insertFixedThreadPoolSize;

    private ListeningExecutorService insertService;

    @Autowired
    private TagMetaDataQualityRepository tagMetaDataQualityRepository;

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
    public ListenableFuture<Void> save(TagMetaDataQuality tagMetaDataQuality) {
        UUID id = UUID.fromString(tagMetaDataQuality.getEntityId());
        tagMetaDataQuality.setEntityId(UUIDConverter.fromTimeUUID(id));
        TagMetaDataQualityEntity tagMetaDataQualityEntity = new TagMetaDataQualityEntity(tagMetaDataQuality);
        return insertService.submit(() -> {
            tagMetaDataQualityRepository.save(tagMetaDataQualityEntity);
            return null;
        });
    }

    @Override
    public ListenableFuture<TagMetaDataQuality> getByEntityIdAndKey(EntityId entityId, String key) {
        TagMetaDataQualityCompositeKey tagMetaDataQualityCompositeKey = new TagMetaDataQualityCompositeKey(entityId.getEntityType(),
                UUIDConverter.fromTimeUUID(entityId.getId()), key);
        TagMetaDataQualityEntity tagMetaDataQualityEntity = tagMetaDataQualityRepository.findOne(tagMetaDataQualityCompositeKey);
        TagMetaDataQuality tagMetaDataQuality = new TagMetaDataQuality();
        if(tagMetaDataQualityEntity != null){
            tagMetaDataQuality = tagMetaDataQualityEntity.toData();
        }
        return Futures.immediateFuture(tagMetaDataQuality);
    }

    @Override
    public ListenableFuture<List<TagMetaDataQuality>> getAllByEntityId(EntityId entityId) {
        return Futures.immediateFuture(
                DaoUtil.convertDataList(Lists.newArrayList(
                        tagMetaDataQualityRepository.findAllByEntityId(UUIDConverter.fromTimeUUID(entityId.getId())))));
    }
}
