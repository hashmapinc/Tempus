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
package com.hashmapinc.server.dao.sql.DataModelObject;

import com.hashmapinc.server.common.data.DataModelObject.DataModelObject;
import com.hashmapinc.server.common.data.UUIDConverter;
import com.hashmapinc.server.common.data.id.DataModelObjectId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.dao.DaoUtil;
import com.hashmapinc.server.dao.model.sql.DataModelObjectEntity;
import com.hashmapinc.server.dao.datamodelobject.DataModelObjectDao;
import com.hashmapinc.server.dao.sql.JpaAbstractSearchTextDao;
import com.hashmapinc.server.dao.util.SqlDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@SqlDao
public class JpaDataModelObjectDao extends JpaAbstractSearchTextDao<DataModelObjectEntity, DataModelObject> implements DataModelObjectDao {

    @Autowired
    DataModelObjectRespository dataModelObjectRespository;

    @Override
    protected Class<DataModelObjectEntity> getEntityClass() {
        return DataModelObjectEntity.class;
    }

    @Override
    protected CrudRepository<DataModelObjectEntity, String> getCrudRepository() {
        return dataModelObjectRespository;
    }

    @Override
    public DataModelObject findById(DataModelObjectId id) {
        DataModelObjectEntity entity = dataModelObjectRespository.findOne(UUIDConverter.fromTimeUUID(id.getId()));
        return DaoUtil.getData(entity);
    }

    @Override
    public List<DataModelObject> findByTenantId(TenantId tenantId) {
        List<DataModelObjectEntity> entities = dataModelObjectRespository.findByTenantId(UUIDConverter.fromTimeUUID(tenantId.getId()));
        return DaoUtil.convertDataList(entities);
    }



}
