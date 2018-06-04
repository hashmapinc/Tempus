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
package com.hashmapinc.server.dao.datamodel;

import com.google.common.util.concurrent.ListenableFuture;
import com.hashmapinc.server.common.data.DataModel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CassandraDataModelDao implements DataModelDao {
    @Override
    public List<DataModel> find() {
        return null;
    }

    @Override
    public DataModel findById(UUID id) {
        return null;
    }

    @Override
    public ListenableFuture<DataModel> findByIdAsync(UUID id) {
        return null;
    }

    @Override
    public DataModel save(DataModel dataModel) {
        return null;
    }

    @Override
    public boolean removeById(UUID id) {
        return false;
    }

    @Override
    public Optional<DataModel> findDataModelByTenantIdAndName(UUID tenantId, String name) {
        return Optional.empty();
    }
}
