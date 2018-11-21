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

import com.hashmapinc.server.common.data.datamodel.DataModel;
import com.hashmapinc.server.dao.DaoUtil;
import com.hashmapinc.server.dao.datamodel.DataModelDao;
import com.hashmapinc.server.dao.model.sql.DataModelEntity;
import com.hashmapinc.server.dao.sql.JpaAbstractSearchTextDao;
import com.hashmapinc.server.dao.util.SqlDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.hashmapinc.server.common.data.UUIDConverter.fromTimeUUID;

@Component
public class JpaDataModelDao extends JpaAbstractSearchTextDao<DataModelEntity, DataModel> implements DataModelDao {


    @Autowired
    private DataModelRepository dataModelRepository;

    @Override
    public Optional<DataModel> findDataModelByTenantIdAndName(UUID tenantId, String name) {
        DataModel dataModel = DaoUtil.getData(dataModelRepository.findByTenantIdAndName(fromTimeUUID(tenantId), name));
        return Optional.ofNullable(dataModel);
    }

    @Override
    public List<DataModel> findByTenantId(UUID tenantId) {
        return DaoUtil.convertDataList(dataModelRepository.findByTenantId(fromTimeUUID(tenantId)));
    }

    @Override
    protected Class<DataModelEntity> getEntityClass() {
        return DataModelEntity.class;
    }

    @Override
    protected CrudRepository<DataModelEntity, String> getCrudRepository() {
        return dataModelRepository;
    }
}
