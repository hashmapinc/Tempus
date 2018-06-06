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
package com.hashmapinc.server.dao.sql.ModelObjects;

import com.hashmapinc.server.common.data.ModelObject;
import com.hashmapinc.server.common.data.UUIDConverter;
import com.hashmapinc.server.common.data.id.ModelObjectId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.dao.DaoUtil;
import com.hashmapinc.server.dao.model.sql.ModelObjectEntity;
import com.hashmapinc.server.dao.modelobjects.ModelObjectDao;
import com.hashmapinc.server.dao.sql.JpaAbstractSearchTextDao;
import com.hashmapinc.server.dao.util.SqlDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@SqlDao
public class JpaModelObjectDao extends JpaAbstractSearchTextDao<ModelObjectEntity, ModelObject> implements ModelObjectDao {

    @Autowired
    ModelObjectRespository modelObjectRespository;

    @Override
    protected Class<ModelObjectEntity> getEntityClass() {
        return ModelObjectEntity.class;
    }

    @Override
    protected CrudRepository<ModelObjectEntity, String> getCrudRepository() {
        return modelObjectRespository;
    }

    @Override
    public ModelObject findById(ModelObjectId id) {
        ModelObjectEntity entity = modelObjectRespository.findOne(UUIDConverter.fromTimeUUID(id.getId()));
        return DaoUtil.getData(entity);
    }

    @Override
    public List<ModelObject> findByTenantId(TenantId tenantId) {
        List<ModelObjectEntity> entities = modelObjectRespository.findByTenantId(UUIDConverter.fromTimeUUID(tenantId.getId()));
        return DaoUtil.convertDataList(entities);
    }



}
