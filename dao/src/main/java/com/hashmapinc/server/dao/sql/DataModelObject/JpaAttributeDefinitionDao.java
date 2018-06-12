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

import com.hashmapinc.server.common.data.DataModelObject.AttributeDefinition;
import com.hashmapinc.server.common.data.UUIDConverter;
import com.hashmapinc.server.common.data.id.DataModelObjectId;
import com.hashmapinc.server.dao.DaoUtil;
import com.hashmapinc.server.dao.datamodelobject.AttributeDefinitionDao;
import com.hashmapinc.server.dao.model.sql.AttributeDefinitionEntity;
import com.hashmapinc.server.dao.sql.JpaAbstractDao;
import com.hashmapinc.server.dao.util.SqlDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@SqlDao
public class JpaAttributeDefinitionDao extends JpaAbstractDao<AttributeDefinitionEntity, AttributeDefinition> implements AttributeDefinitionDao{

    @Autowired
    AttributeDefinitionRepository attributeDefinitionRepository;

    @Override
    protected Class<AttributeDefinitionEntity> getEntityClass() {
        return AttributeDefinitionEntity.class;
    }

    @Override
    protected CrudRepository<AttributeDefinitionEntity, String> getCrudRepository() {
        return attributeDefinitionRepository;
    }

    @Override
    public List<AttributeDefinition> findByDataModelObjectId(DataModelObjectId dataModelObjectId) {
        List<AttributeDefinitionEntity> entities = attributeDefinitionRepository.findByDataModelObjectId(UUIDConverter.fromTimeUUID(dataModelObjectId.getId()));
        return DaoUtil.convertDataList(entities);
    }

    @Override
    public boolean deleteById(UUID id) {
        return super.removeById(id);
    }
}
