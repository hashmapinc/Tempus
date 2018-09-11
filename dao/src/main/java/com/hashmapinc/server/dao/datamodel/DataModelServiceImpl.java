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
import com.hashmapinc.server.common.data.Tenant;
import com.hashmapinc.server.common.data.id.DataModelId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.dao.entity.AbstractEntityService;
import com.hashmapinc.server.dao.exception.DataValidationException;
import com.hashmapinc.server.dao.service.DataValidator;
import com.hashmapinc.server.dao.tenant.TenantDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.hashmapinc.server.dao.service.Validator.validateId;

@Service
@Slf4j
public class DataModelServiceImpl extends AbstractEntityService implements DataModelService {

    public static final String INCORRECT_DATA_MODEL_ID = "Incorrect dataModelId ";
    public static final String INCORRECT_TENANT_ID = "Incorrect dataModelId ";

    @Autowired
    private DataModelDao dataModelDao;

    @Autowired
    private DataModelObjectService dataModelObjectService;

    @Autowired
    private TenantDao tenantDao;

    @Override
    public DataModel saveDataModel(DataModel dataModel) {
        log.trace("Executing saveDataModel [{}]", dataModel);
        dataModelValidator.validate(dataModel);
        return dataModelDao.save(dataModel);
    }

    @Override
    public DataModel findById(DataModelId id) {
        log.trace("Executing DataModelServiceImpl.findById [{}]", id);
        validateId(id, INCORRECT_DATA_MODEL_ID + id);
        return dataModelDao.findById(id.getId());
    }

    @Override
    public List<DataModel> findByTenantId(TenantId tenantId) {
        log.trace("Executing DataModelServiceImpl.findByTenantId [{}]", tenantId);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        return dataModelDao.findByTenantId(tenantId.getId());
    }

    @Override
    public void deleteById(DataModelId dataModelId) {
        log.trace("Executing DataModelServiceImpl.deleteById [{}]", dataModelId);
        validateId(dataModelId, INCORRECT_DATA_MODEL_ID + dataModelId);
        dataModelDao.removeById(dataModelId.getId());
    }

    @Override
    public void deleteDataModelsByTenantId(TenantId tenantId) {
        log.trace("Executing DataModelServiceImpl.deleteDataModelsByTenantId [{}]", tenantId);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        removeEntities(tenantId);
    }

    private void removeEntities(TenantId tenantId) {
        List<DataModel> dataModels = dataModelDao.findByTenantId(tenantId.getId());
        dataModels.forEach(dataModel -> {
            if(dataModel != null){
                deleteById(dataModel.getId());
                dataModelObjectService.deleteDataModelObjectsByDataModelId(dataModel.getId());
            }
        });
    }

    private DataValidator<DataModel> dataModelValidator = new DataValidator<DataModel>() {
        @Override
        protected void validateCreate(DataModel dataModel) {
            dataModelDao.findDataModelByTenantIdAndName(dataModel.getTenantId().getId(), dataModel.getName()).ifPresent(
                    d -> {
                        throw new DataValidationException("Data Model with such name already exists!");
                    }
            );
        }

        @Override
        protected void validateUpdate(DataModel dataModel) {
            dataModelDao.findDataModelByTenantIdAndName(dataModel.getTenantId().getId(), dataModel.getName()).ifPresent(
                    d -> {
                        if (!d.getUuidId().equals(dataModel.getUuidId())) {
                            throw new DataValidationException("Device with such name already exists!");
                        }
                    }
            );
        }
        @Override
        protected void validateDataImpl(DataModel dataModel) {
            if(StringUtils.isEmpty(dataModel.getName())) {
                throw new DataValidationException("Data Model name should be specified");
            }

            if (dataModel.getTenantId() == null) {
                throw new DataValidationException("Data Model should be assigned to tenant!");
            } else {
                Tenant tenant = tenantDao.findById(dataModel.getTenantId().getId());
                if (tenant == null) {
                    throw new DataValidationException("Data Model is referencing to non-existent tenant!");
                }
            }

            if(dataModel.getLastUpdatedTs() == null) {
                throw new DataValidationException("Data Model last updated time should be specified!");
            }
        }

    };
}
