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

import com.hashmapinc.server.common.data.datamodel.AttributeDefinition;
import com.hashmapinc.server.common.data.datamodel.DataModel;
import com.hashmapinc.server.common.data.datamodel.DataModelObject;
import com.hashmapinc.server.common.data.id.DataModelId;
import com.hashmapinc.server.common.data.id.DataModelObjectId;
import com.hashmapinc.server.common.data.kv.DataType;
import com.hashmapinc.server.dao.exception.DataValidationException;
import com.hashmapinc.server.dao.service.DataValidator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.hashmapinc.server.dao.service.Validator.validateId;

@Service
@Slf4j
public class DataModelObjectServiceImp implements DataModelObjectService {

    public static final String INCORRECT_DATA_MODEL_ID = "Incorrect dataModelId ";
    public static final String INCORRECT_DATA_MODEL_OBJECT_ID = "Incorrect dataModelObjectId ";

    @Autowired
    DataModelObjectDao dataModelObjectDao;

    @Autowired
    DataModelDao dataModelDao;

    @Autowired
    AttributeDefinitionDao attributeDefinitionDao;

    @Override
    public DataModelObject save(DataModelObject dataModelObject) {
        log.trace("Executing save for DataModel Object {}", dataModelObject);
        dataModelObjectDataValidator.validate(dataModelObject);
        DataModelObject savedDataModelObj = dataModelObjectDao.save(dataModelObject);

        List<AttributeDefinition> savedAttributeDefs = dataModelObject.getAttributeDefinitions().stream().map(attributeDefinition -> {
            attributeDefinition.setDataModelObjectId(savedDataModelObj.getId());
            validateAttributeDefinition(attributeDefinition);
            return attributeDefinitionDao.save(attributeDefinition);
        }).collect(Collectors.toList());

        savedDataModelObj.setAttributeDefinitions(savedAttributeDefs);
        return savedDataModelObj;
    }

    @Override
    public DataModelObject findById(DataModelObjectId dataModelObjectId) {
        validateId(dataModelObjectId, INCORRECT_DATA_MODEL_OBJECT_ID + dataModelObjectId);
        DataModelObject foundDataModelObj = dataModelObjectDao.findById(dataModelObjectId);
        if(foundDataModelObj != null){
            List<AttributeDefinition> attributeDefinitions = attributeDefinitionDao.findByDataModelObjectId(foundDataModelObj.getId());
            foundDataModelObj.setAttributeDefinitions(attributeDefinitions);
        }
        return foundDataModelObj;
    }

    @Override
    public List<DataModelObject> findByDataModelId(DataModelId dataModelId) {
        validateId(dataModelId, INCORRECT_DATA_MODEL_ID + dataModelId);
        List<DataModelObject> dataModelObjects = dataModelObjectDao.findByDataModelId(dataModelId);
        dataModelObjects.stream().forEach(dataModelObject -> {
            if(dataModelObject != null){
                List<AttributeDefinition> attributeDefinitions = attributeDefinitionDao.findByDataModelObjectId(dataModelObject.getId());
                dataModelObject.setAttributeDefinitions(attributeDefinitions);
            }
        });
        return dataModelObjects;
    }

    private DataValidator<DataModelObject> dataModelObjectDataValidator =
            new DataValidator<DataModelObject>() {
                @Override
                protected void validateDataImpl(DataModelObject dataModelObject) {
                    if (StringUtils.isEmpty(dataModelObject.getName())) {
                        throw new DataValidationException("Data Model object name should be specified!");
                    }else if (dataModelObject.getDataModelId() == null) {
                        throw new DataValidationException("Data Model object should be assigned to a data model!");
                    } else {
                        DataModel dataModel = dataModelDao.findById(dataModelObject.getDataModelId().getId());
                        if(dataModel == null) {
                            throw new DataValidationException("Data Model object is referencing to non-existent data model!");
                        }
                    }
                }
            };

    private void validateAttributeDefinition(AttributeDefinition attributeDefinition) {
        if (StringUtils.isEmpty(attributeDefinition.getName())) {
            throw new DataValidationException("Attribute name should be specified!");
        } else if(StringUtils.isEmpty(attributeDefinition.getValueType()) || !EnumUtils.isValidEnum(DataType.class, attributeDefinition.getValueType())) {
            throw new DataValidationException("A Valid attribute value type should be specified!");
        }
    }
}
