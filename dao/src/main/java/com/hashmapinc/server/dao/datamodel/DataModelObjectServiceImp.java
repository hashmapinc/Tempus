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

import com.hashmapinc.server.common.data.DataModelObject.DataModelObject;
import com.hashmapinc.server.common.data.Tenant;
import com.hashmapinc.server.common.data.id.DataModelObjectId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.dao.exception.DataValidationException;
import com.hashmapinc.server.dao.service.DataValidator;
import com.hashmapinc.server.dao.tenant.TenantDao;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.hashmapinc.server.dao.service.Validator.validateId;

@Service
@Slf4j
public class DataModelObjectServiceImp implements DataModelObjectService {

    public static final String INCORRECT_TENANT_ID = "Incorrect tenantId ";
    public static final String INCORRECT_DATA_MODEL_OBJECT_ID = "Incorrect dataModelObjectId ";

    @Autowired
    DataModelObjectDao dataModelObjectDao;

    @Autowired
    TenantDao tenantDao;

    @Override
    public DataModelObject save(DataModelObject dataModelObject) {
        log.trace("Executing save for DataModel Object {}", dataModelObject);
        dataModelObjectDataValidator.validate(dataModelObject);
        return dataModelObjectDao.save(dataModelObject);
    }

    @Override
    public DataModelObject findById(DataModelObjectId dataModelObjectId) {
        validateId(dataModelObjectId, INCORRECT_DATA_MODEL_OBJECT_ID + dataModelObjectId);
        return dataModelObjectDao.findById(dataModelObjectId);
    }

    @Override
    public List<DataModelObject> findByTenantId(TenantId tenantId) {
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        return dataModelObjectDao.findByTenantId(tenantId);
    }

    @Override
    public boolean deleteById(DataModelObjectId dataModelObjectId) {
        validateId(dataModelObjectId, INCORRECT_DATA_MODEL_OBJECT_ID + dataModelObjectId);
        return dataModelObjectDao.removeById(dataModelObjectId.getId());
    }

    private DataValidator<DataModelObject> dataModelObjectDataValidator =
            new DataValidator<DataModelObject>() {
                @Override
                protected void validateDataImpl(DataModelObject dataModelObject) {
                    if (StringUtils.isEmpty(dataModelObject.getName())) {
                        throw new DataValidationException("Data Model object name should be specified!");
                    }
                    if (dataModelObject.getTenantId() == null || dataModelObject.getDataModelId() == null) {
                        throw new DataValidationException("Data Model object should be assigned to tenant and a data model!");
                    } else {
                        Tenant tenant = tenantDao.findById(dataModelObject.getTenantId().getId());
                        if (tenant == null) {
                            throw new DataValidationException("Data Model object is referencing to non-existent tenant!");
                        }
                    }
                }
            };
}