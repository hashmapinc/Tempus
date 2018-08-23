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
package com.hashmapinc.server.dao.sql.datamodel;

import com.hashmapinc.server.dao.model.sql.DataModelObjectEntity;
import com.hashmapinc.server.dao.util.SqlDao;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

@SqlDao
public interface DataModelObjectRespository extends CrudRepository<DataModelObjectEntity, String> {
    List<DataModelObjectEntity> findByDataModelId(@Param("dataModelId") String dataModelId);
    DataModelObjectEntity findByDataModelIdAndName(@Param("dataModelId") String dataModelId, @Param("name") String name);
}
