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
import com.hashmapinc.server.common.data.kv.MetaDataKvEntry;
import com.hashmapinc.server.dao.exception.IncorrectParameterException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class MetaDataIngestionServiceImpl implements MetadataIngestionService {


    @Autowired
    private MetaDataIngestionDao metaDataIngestionDao;

    @Override
    public ListenableFuture<List<Void>> save(MetadataIngestionEntries ingestionEntries) {
        List<ListenableFuture<Void>> futures = Lists.newArrayListWithExpectedSize(ingestionEntries.getMetaDataKvEntries().size());
        for (MetaDataKvEntry metaDataKvEntry : ingestionEntries.getMetaDataKvEntries()) {
            if(metaDataKvEntry == null) {
                throw new IncorrectParameterException("Meta Data Key value entry can't be null");
            }
            futures.add(metaDataIngestionDao.save(ingestionEntries.getTenantId(), ingestionEntries.getMetadataConfigId(), ingestionEntries.getMetadataSourceName(), metaDataKvEntry));
        }
        return Futures.allAsList(futures);
    }
}
