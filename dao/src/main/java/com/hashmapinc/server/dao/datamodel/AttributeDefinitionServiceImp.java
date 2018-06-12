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

import com.hashmapinc.server.common.data.DataModelObject.AttributeDefinition;
import com.hashmapinc.server.common.data.DataModelObject.DataModelObject;
import com.hashmapinc.server.common.data.id.DataModelObjectId;
import com.hashmapinc.server.common.data.kv.DataType;
import com.hashmapinc.server.dao.exception.DataValidationException;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static com.hashmapinc.server.dao.service.Validator.validateId;

@Service
public class AttributeDefinitionServiceImp implements  AttributeDefinitionService{

    public static final String INCORRECT_ATTRIBUTE_DEFINITION_ID = "Incorrect attributeDefinitionId ";

    public static final String INCORRECT_DATA_MODEL_OBJECT_ID = "Incorrect dataModelObjectId ";

    @Autowired
    AttributeDefinitionDao attributeDefinitionDao;

    @Autowired
    DataModelObjectDao dataModelObjectDao;

    @Override
    public AttributeDefinition save(AttributeDefinition attributeDefinition) {
        validateAttributeDefinition(attributeDefinition);
        return attributeDefinitionDao.save(attributeDefinition);
    }

    @Override
    public AttributeDefinition findById(UUID id) {
        validateId(id, INCORRECT_ATTRIBUTE_DEFINITION_ID);
        return attributeDefinitionDao.findById(id);
    }

    @Override
    public List<AttributeDefinition> findByDataModelObjectId(DataModelObjectId dataModelObjectId) {
        validateId(dataModelObjectId, INCORRECT_ATTRIBUTE_DEFINITION_ID);
        return attributeDefinitionDao.findByDataModelObjectId(dataModelObjectId);
    }

    @Override
    public boolean deleteById(UUID id) {
        validateId(id, INCORRECT_ATTRIBUTE_DEFINITION_ID);
        return attributeDefinitionDao.deleteById(id);
    }

    private void validateAttributeDefinition(AttributeDefinition attributeDefinition) {
        if (StringUtils.isEmpty(attributeDefinition.getName())) {
            throw new DataValidationException("Attribute name should be specified!");
        } else if (StringUtils.isEmpty(attributeDefinition.getValue())) {
            throw new DataValidationException("Attribute value should be specified!");
        } else if (StringUtils.isEmpty(attributeDefinition.getValueType()) || !EnumUtils.isValidEnum(DataType.class, attributeDefinition.getValueType())) {
            throw new DataValidationException("A Valid attribute value type should be specified!");
        } else if (attributeDefinition.getDataModelObjectId() == null) {
            throw new DataValidationException("Attribute definition should be assigned to a data model object!");
        } else {
            DataModelObject dataModelObject = dataModelObjectDao.findById(attributeDefinition.getDataModelObjectId());
            if (dataModelObject == null) {
                throw new DataValidationException("Attribute definition is referencing to non-existent data model object!");
            }
        }
    }

}
