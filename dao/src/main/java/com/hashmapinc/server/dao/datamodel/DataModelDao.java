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
package com.hashmapinc.server.dao.datamodel;

import com.hashmapinc.server.common.data.datamodel.DataModel;
import com.hashmapinc.server.dao.Dao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DataModelDao extends Dao<DataModel> {

    /**
     * Save or update DataModel object
     *
     * @param dataModel the DataModel object
     * @return saved DataModel object
     */
    DataModel save(DataModel dataModel);


    /**
     * Find data models by tenantId and data model name.
     *
     * @param tenantId the tenantId
     * @param name the DataModel name
     * @return the optional DataModel object
     */
    Optional<DataModel> findDataModelByTenantIdAndName(UUID tenantId, String name);

    /**
     * Find data models by tenantId.
     *
     * @param tenantId the tenantId
     * @return the list of DataModel objects
     */
    List<DataModel> findByTenantId(UUID tenantId);

    /**
     * Delete the data model by dataModelId
     *
     * @param dataModelId the dataModelId
     * @return boolean
     */
    boolean removeById(UUID dataModelId);
}
