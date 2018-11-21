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

import com.hashmapinc.server.common.data.datamodel.AttributeDefinition;
import com.hashmapinc.server.common.data.UUIDConverter;
import com.hashmapinc.server.common.data.id.DataModelObjectId;
import com.hashmapinc.server.dao.DaoUtil;
import com.hashmapinc.server.dao.datamodel.AttributeDefinitionDao;
import com.hashmapinc.server.dao.model.sql.AttributeDefinitionCompositeKey;
import com.hashmapinc.server.dao.model.sql.AttributeDefinitionEntity;
import com.hashmapinc.server.dao.util.SqlDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class JpaAttributeDefinitionDao implements AttributeDefinitionDao{

    @Autowired
    private AttributeDefinitionRepository attributeDefinitionRepository;

    @Override
    public AttributeDefinition save(AttributeDefinition attributeDefinition) {
        AttributeDefinitionEntity attributeDefinitionEntity = new AttributeDefinitionEntity(attributeDefinition);
        AttributeDefinitionEntity retEntity = attributeDefinitionRepository.save(attributeDefinitionEntity);
        return DaoUtil.getData(retEntity);
    }

    @Override
    public AttributeDefinition findByNameAndDataModelObjectId(String name, UUID id) {
        AttributeDefinitionEntity retEntity = attributeDefinitionRepository.findByNameAndDataModelObjectId(name, UUIDConverter.fromTimeUUID(id));
        return DaoUtil.getData(retEntity);
    }

    @Override
    public List<AttributeDefinition> findByDataModelObjectId(DataModelObjectId dataModelObjectId) {
        List<AttributeDefinitionEntity> entities = attributeDefinitionRepository.findByDataModelObjectId(UUIDConverter.fromTimeUUID(dataModelObjectId.getId()));
        return DaoUtil.convertDataList(entities);
    }

    @Override
    public void removeByNameAndDataModelObjectId(String name, DataModelObjectId dataModelObjectId) {
        attributeDefinitionRepository.delete(createAttributeDefinitionCompositeKey(name, dataModelObjectId));
    }

    private AttributeDefinitionCompositeKey createAttributeDefinitionCompositeKey(String name, DataModelObjectId dataModelObjectId) {
        return new AttributeDefinitionCompositeKey(name, UUIDConverter.fromTimeUUID(dataModelObjectId.getId()));
    }
}
