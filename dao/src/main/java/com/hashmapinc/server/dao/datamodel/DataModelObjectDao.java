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

import com.hashmapinc.server.common.data.DataModelObject.DataModelObject;
import com.hashmapinc.server.common.data.id.DataModelObjectId;
import com.hashmapinc.server.common.data.id.TenantId;

import java.util.List;
import java.util.UUID;

public interface DataModelObjectDao {
    DataModelObject findById(DataModelObjectId id);
    DataModelObject save(DataModelObject dataModelObject);
    List<DataModelObject> findByTenantId(TenantId tenantId);
    boolean removeById(UUID id);
}