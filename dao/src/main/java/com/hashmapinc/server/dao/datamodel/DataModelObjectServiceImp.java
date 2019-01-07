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

import com.hashmapinc.server.common.data.Dashboard;
import com.hashmapinc.server.common.data.DashboardType;
import com.hashmapinc.server.common.data.asset.Asset;
import com.hashmapinc.server.common.data.datamodel.AttributeDefinition;
import com.hashmapinc.server.common.data.datamodel.DataModel;
import com.hashmapinc.server.common.data.datamodel.DataModelObject;
import com.hashmapinc.server.common.data.id.DataModelId;
import com.hashmapinc.server.common.data.id.DataModelObjectId;
import com.hashmapinc.server.common.data.kv.DataType;
import com.hashmapinc.server.dao.asset.AssetDao;
import com.hashmapinc.server.dao.dashboard.DashboardService;
import com.hashmapinc.server.dao.exception.DataValidationException;
import com.hashmapinc.server.dao.service.DataValidator;
import com.hashmapinc.server.dao.service.Validator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.hashmapinc.server.dao.service.Validator.validateId;
import static com.hashmapinc.server.dao.service.Validator.validateString;

@Service
@Slf4j
public class DataModelObjectServiceImp implements DataModelObjectService {

    public static final String INCORRECT_DATA_MODEL_ID = "Incorrect dataModelId ";
    public static final String INCORRECT_DATA_MODEL_OBJECT_ID = "Incorrect dataModelObjectId ";
    public static final String INCORRECT_DATA_MODEL_OBJECT_NAME = "Incorrect dataModelObjectName ";

    @Autowired
    DataModelObjectDao dataModelObjectDao;

    @Autowired
    DataModelDao dataModelDao;

    @Autowired
    AttributeDefinitionDao attributeDefinitionDao;

    @Autowired
    AssetDao assetDao;

    @Autowired
    DashboardService dashboardService;

    @Override
    public DataModelObject save(DataModelObject dataModelObject) {
        log.trace("Executing save for DataModel Object {}", dataModelObject);
        dataModelObjectDataValidator.validate(dataModelObject);
        DataModelObject savedDataModelObj = dataModelObjectDao.save(dataModelObject);

        removeAttributeDefinitions(savedDataModelObj);
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

    @Override
    public List<DataModelObject> findByDataModelIdAndType(DataModelId dataModelId, String type) {
        validateId(dataModelId, INCORRECT_DATA_MODEL_ID + dataModelId);
        List<DataModelObject> dataModelObjects = dataModelObjectDao.findByDataModelIdAndType(dataModelId, type);
        dataModelObjects.stream().forEach(dataModelObject -> {
            if(dataModelObject != null){
                List<AttributeDefinition> attributeDefinitions = attributeDefinitionDao.findByDataModelObjectId(dataModelObject.getId());
                dataModelObject.setAttributeDefinitions(attributeDefinitions);
            }
        });
        return dataModelObjects;
    }

    @Override
    public Set<DataModelObjectId> getAllParentDataModelIdsOf(DataModelObjectId dataModelObjectId) {
        Set<DataModelObjectId> parents = new HashSet<>();
        DataModelObject dataModelObject = findById(dataModelObjectId);
        if(dataModelObject != null) {
            DataModelObjectId parentId = dataModelObject.getParentId();
            while (parentId != null) {
                parents.add(parentId);
                parentId = findById(parentId).getParentId();
            }
        }
        return parents;
    }

    @Override
    public void removeById(DataModelObjectId dataModelObjectId) {
        validateId(dataModelObjectId, INCORRECT_DATA_MODEL_OBJECT_ID + dataModelObjectId);
        DataModelObject dataModelObject = dataModelObjectDao.findById(dataModelObjectId);
        if(dataModelObject != null) {
            List<Asset> assets = assetDao.findAssetsByDataModelObjectId(dataModelObject.getId().getId());
            if (!assets.isEmpty())
                throw new DataValidationException("Cannot delete dataModelObject because one or more assets are associated with it");
            else {
                removeAttributeDefinitions(dataModelObject);
                removeAssetLandingDashboard(dataModelObjectId);
                dataModelObjectDao.removeById(dataModelObjectId.getId());
            }
        }
    }

    private void removeAssetLandingDashboard(DataModelObjectId dataModelObjectId) {
        List<Dashboard> dashboards = dashboardService.findDashboardByDataModelObjectId(dataModelObjectId);
        dashboards.forEach(dashboard -> {
            if(dashboard.getType() == DashboardType.ASSET_LANDING_PAGE)
                dashboardService.deleteDashboard(dashboard.getId());
        });
    }

    private void removeAttributeDefinitions(DataModelObject dataModelObject) {
        DataModelObjectId dataModelObjectId = dataModelObject.getId();
        List<AttributeDefinition> attributeDefinitions = attributeDefinitionDao.findByDataModelObjectId(dataModelObjectId);
        attributeDefinitions.forEach(attributeDefinition -> {
            if(attributeDefinition != null){
                attributeDefinitionDao.removeByNameAndDataModelObjectId(attributeDefinition.getName(),dataModelObjectId);
            }
        });
    }

    @Override
    public List<DataModelObject> findByName(String name) {
        log.trace("Executing findByName for DataModel Object name {}", name);
        validateString(name, INCORRECT_DATA_MODEL_OBJECT_NAME + name);
        List<DataModelObject> dataModelObjects = dataModelObjectDao.findByName(name);
        dataModelObjects.forEach(dataModelObject -> {
            if(dataModelObject != null){
                List<AttributeDefinition> attributeDefinitions = attributeDefinitionDao.findByDataModelObjectId(dataModelObject.getId());
                dataModelObject.setAttributeDefinitions(attributeDefinitions);
            }
        });
        return dataModelObjects;
    }

    @Override
    public void deleteDataModelObjectsByDataModelId(DataModelId dataModelId) {
        log.trace("Executing deleteDataModelObjectsByDataModelId, dataModelId [{}]",dataModelId);
        Validator.validateId(dataModelId, INCORRECT_DATA_MODEL_ID + dataModelId);
        removeEntities(dataModelId);
    }

    @Override
    public String findKeyAttributeByDataModelObjectId(DataModelObjectId dataModelObjectId) {
        log.trace("Executing findKeyAttributeByDataModelObjectId, dataModelObjectId : [{}]", dataModelObjectId);
        validateId(dataModelObjectId, INCORRECT_DATA_MODEL_OBJECT_ID + dataModelObjectId);
        AttributeDefinition attributeDefinition = attributeDefinitionDao.findKeyAttributeDefinitionByDataModelObjectId(dataModelObjectId);
        return attributeDefinition.getName();
    }

    private void removeEntities(DataModelId dataModelId) {
        List<DataModelObject> dataModelObjects = findByDataModelId(dataModelId);
        dataModelObjects.forEach(dataModelObject -> {
            if(dataModelObject != null){
                removeById(dataModelObject.getId());
            }
        });
    }

    private DataValidator<DataModelObject> dataModelObjectDataValidator =
            new DataValidator<DataModelObject>() {
                @Override
                protected void validateDataImpl(DataModelObject dataModelObject) {
                    if (StringUtils.isEmpty(dataModelObject.getName())) {
                        throw new DataValidationException("Data Model object name should be specified!");
                    } else if (dataModelObject.getDataModelId() == null) {
                        throw new DataValidationException("Data Model object should be assigned to a data model!");
                    } else {
                        DataModel dataModel = dataModelDao.findById(dataModelObject.getDataModelId().getId());
                        if(dataModel == null) {
                            throw new DataValidationException("Data Model object is referencing to non-existent data model!");
                        }
                    }
                }

                @Override
                protected void validateCreate(DataModelObject dataModelObject) {
                        DataModelObject foundDataModelObject = dataModelObjectDao.findByDataModeIdAndName(dataModelObject);
                        if(foundDataModelObject != null) {
                            throw new DataValidationException("DataModelObject is already created for name");
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
