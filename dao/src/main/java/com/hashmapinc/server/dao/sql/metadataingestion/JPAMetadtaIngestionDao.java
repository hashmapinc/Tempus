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
package com.hashmapinc.server.dao.sql.metadataingestion;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.hashmapinc.server.common.data.UUIDConverter;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.kv.MetaDataKvEntry;
import com.hashmapinc.server.common.data.metadata.MetadataConfigId;
import com.hashmapinc.server.dao.metadataingestion.MetaDataIngestionDao;
import com.hashmapinc.server.dao.model.sql.MetadataIngestionEntity;
import com.hashmapinc.server.dao.sql.JpaAbstractDaoListeningExecutorService;
import com.hashmapinc.server.dao.timeseries.TsInsertExecutorType;
import com.hashmapinc.server.dao.util.SqlDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Optional;
import java.util.concurrent.Executors;

@Component
@Slf4j
@SqlDao
public class JPAMetadtaIngestionDao extends JpaAbstractDaoListeningExecutorService implements MetaDataIngestionDao {

    @Value("${metadata-ingestion.inserts_executor_type}")
    private String insertExecutorType;

    @Value("${metadata-ingestion.inserts_fixed_thread_pool_size}")
    private int insertFixedThreadPoolSize;

    @Autowired
    private MetadataIngestionEntityRepository metadataIngestionEntityRepository;

    private ListeningExecutorService insertService;

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
    public ListenableFuture<Void> save(TenantId tenantId, MetadataConfigId metadataConfigId, String dataSourceName, MetaDataKvEntry metaDataKvEntry) {
        MetadataIngestionEntity metadataIngestionEntity = new MetadataIngestionEntity();
        metadataIngestionEntity.setKey(metaDataKvEntry.getKey());
        metadataIngestionEntity.setValue(metaDataKvEntry.getValue());
        metadataIngestionEntity.setTenantId(UUIDConverter.fromTimeUUID(tenantId.getId()));
        metadataIngestionEntity.setMetadataConfigId(UUIDConverter.fromTimeUUID(metadataConfigId.getId()));
        metadataIngestionEntity.setMetadataSourceName(dataSourceName);
        metadataIngestionEntity.setLastUpdateTs(metaDataKvEntry.getLastUpdateTs());
        log.trace("Saving MetadataIngestionEntity: {}", metadataIngestionEntity);
        return insertService.submit(() -> {
            metadataIngestionEntityRepository.save(metadataIngestionEntity);
            return null;
        });
    }
}
