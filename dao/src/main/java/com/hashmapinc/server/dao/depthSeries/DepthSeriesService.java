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

import java.util.Collection;
import java.util.List;


public interface DepthSeriesService {

    ListenableFuture<List<DsKvEntry>> findAll(EntityId entityId, List<DsKvQuery> queries);

    ListenableFuture<List<DsKvEntry>> findLatest(EntityId entityId, Collection<String> keys);

    ListenableFuture<List<DsKvEntry>> findAllLatest(EntityId entityId);

    ListenableFuture<List<Void>> save(EntityId entityId, DsKvEntry tsKvEntry);

    ListenableFuture<List<Void>> save(EntityId entityId, List<DsKvEntry> dsKvEntry, long ttl);

    DeviceDataSet findAllBetweenDepths(EntityId entityId, Double startDs, Double endDs);
}
