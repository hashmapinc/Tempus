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

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.hashmapinc.server.common.data.TagMetaData;
import com.hashmapinc.server.common.data.id.EntityId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class BaseTagMetaDataService implements TagMetaDataService {

    @Autowired
    TagMetaDataDao tagMetaDataDao;

    @Override
    public ListenableFuture<List<Void>> saveTagMetaData(TagMetaData tagMetaData) {
        List<ListenableFuture<Void>> futures = Lists.newArrayListWithExpectedSize(1);
        futures.add(tagMetaDataDao.save(tagMetaData));
        return Futures.allAsList(futures);
    }

    @Override
    public ListenableFuture<TagMetaData> getTagMetaDataByEntityIdAndKey(EntityId entityId, String key) {
        return tagMetaDataDao.getByEntityIdAndKey(entityId, key);
    }

    @Override
    public ListenableFuture<List<TagMetaData>> getAllTagMetaDataByEntityId(EntityId entityId) {
        return tagMetaDataDao.getAllByEntityId(entityId);
    }

}
