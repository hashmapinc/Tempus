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

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.hashmapinc.server.common.data.MetadataIngestionEntries;
import com.hashmapinc.server.common.data.Tenant;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.kv.MetaDataKvEntry;
import com.hashmapinc.server.common.data.metadata.MetadataConfigId;
import com.hashmapinc.server.dao.exception.IncorrectParameterException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.hashmapinc.server.dao.service.Validator.*;

@Service
@Slf4j
public class MetaDataIngestionServiceImpl implements MetadataIngestionService {

    @Autowired
    private MetaDataIngestionDao metaDataIngestionDao;

    private static final String INCORRECT_TENANT_ID = "Incorrect tenantId ";

    @Override
    public ListenableFuture<List<Void>> save(MetadataIngestionEntries ingestionEntries) {
        validate(ingestionEntries);

        List<ListenableFuture<Void>> futures = Lists.newArrayListWithExpectedSize(ingestionEntries.getMetaDataKvEntries().size());
        for (MetaDataKvEntry metaDataKvEntry : ingestionEntries.getMetaDataKvEntries()) {
            futures.add(metaDataIngestionDao.save(ingestionEntries.getTenantId(), ingestionEntries.getMetadataConfigId(), ingestionEntries.getMetadataSourceName(), ingestionEntries.getAttribute(), metaDataKvEntry));
        }
        return Futures.allAsList(futures);
    }

    @Override
    public ListenableFuture<List<MetaDataKvEntry>> findAll(MetadataConfigId metadataConfigId) {
        validateId(metadataConfigId, "Incorrect metadata config id " + metadataConfigId);
        return metaDataIngestionDao.findAll(metadataConfigId);
    }

    @Override
    public List<MetaDataKvEntry> findKvEntryByKeyAttributeAndTenantId(String key, TenantId tenantId) {
        log.trace("Executing findByKeyAttribute, Key : [{}], TenantId : [{}]", key, tenantId);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        return metaDataIngestionDao.findKvEntryByKeyAndTenantId(key, tenantId);
    }

    private static void validate(MetadataIngestionEntries ingestionEntries) {
        validateId(ingestionEntries.getMetadataConfigId(), "Incorrect metadata config id " + ingestionEntries.getMetadataConfigId());
        validateEntityId(ingestionEntries.getTenantId(), "Incorrect tenand id " + ingestionEntries.getTenantId());
        validateString(ingestionEntries.getMetadataSourceName(), "Incorrect data source name. Value can't be empty");
        if (ingestionEntries.getMetaDataKvEntries() != null)
            ingestionEntries.getMetaDataKvEntries().forEach(MetaDataIngestionServiceImpl::validate);
        else
            throw new IncorrectParameterException("Metadata Entry can't be null");
    }

    private static void validate(MetaDataKvEntry kvEntry) {
        if (kvEntry == null) {
            throw new IncorrectParameterException("Key value entry can't be null");
        } else {
            validateString(kvEntry.getKey(), "Incorrect kvEntry. Key can't be empty");
            validateString(kvEntry.getValue(), "Incorrect kvEntry. Value can't be empty");
            validatePositiveNumber(kvEntry.getLastUpdateTs(), "Incorrect last update ts. Ts should be positive");
        }
    }
}
