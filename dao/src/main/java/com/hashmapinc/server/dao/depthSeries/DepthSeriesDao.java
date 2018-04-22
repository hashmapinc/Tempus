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

import com.google.common.util.concurrent.ListenableFuture;
import com.hashmapinc.server.common.data.DeviceDataSet;
import com.hashmapinc.server.common.data.id.EntityId;
import com.hashmapinc.server.common.data.kv.DsKvEntry;
import com.hashmapinc.server.common.data.kv.DsKvQuery;

import java.util.List;

/**
 * @author Himanshu Mishra
 */
public interface DepthSeriesDao {

    ListenableFuture<List<DsKvEntry>> findAllAsync(EntityId entityId, List<DsKvQuery> queries);

    ListenableFuture<DsKvEntry> findLatest(EntityId entityId, String key);

    ListenableFuture<List<DsKvEntry>> findAllLatest(EntityId entityId);

    ListenableFuture<Void> save(EntityId entityId, DsKvEntry dsKvEntry, long ttl);

    ListenableFuture<Void> savePartition(EntityId entityId, Double dsKvEntryDs, String key, long ttl);

    ListenableFuture<Void> saveLatest(EntityId entityId, DsKvEntry DsKvEntry);

    DeviceDataSet findAllBetweenDepths(EntityId entityId, Double startDs, Double endDs);
}
