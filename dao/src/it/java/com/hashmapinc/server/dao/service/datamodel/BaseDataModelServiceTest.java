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
package com.hashmapinc.server.dao.service.datamodel;

import com.datastax.driver.core.utils.UUIDs;
import com.hashmapinc.server.common.data.datamodel.DataModel;
import com.hashmapinc.server.common.data.Tenant;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.dao.exception.DataValidationException;
import com.hashmapinc.server.dao.service.AbstractServiceTest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public abstract class BaseDataModelServiceTest extends AbstractServiceTest {
    private TenantId tenantId;

    @Before
    public void before() {
        Tenant tenant = new Tenant();
        tenant.setTitle("My tenant");
        Tenant savedTenant = tenantService.saveTenant(tenant);
        Assert.assertNotNull(savedTenant);
        tenantId = savedTenant.getId();
    }

    @After
    public void after() {
        tenantService.deleteTenant(tenantId);
    }

    @Test(expected = DataValidationException.class)
    public void testSaveDataModelWithEmptyName() {
        DataModel dataModel = new DataModel();
        dataModel.setLastUpdatedTs(System.currentTimeMillis());
        dataModel.setTenantId(tenantId);

        dataModelService.saveDataModel(dataModel);
    }


    @Test(expected = DataValidationException.class)
    public void testSaveDataModelWithInvalidTenant() {
        DataModel dataModel = new DataModel();
        dataModel.setName("Data model 1");
        dataModel.setLastUpdatedTs(System.currentTimeMillis());
        dataModel.setTenantId(new TenantId(UUIDs.timeBased()));

        dataModelService.saveDataModel(dataModel);
    }

    @Test(expected = DataValidationException.class)
    public void testSaveDataModelWithoutLastUpdatedTs() {
        DataModel dataModel = new DataModel();
        dataModel.setName("Data model 1");
        dataModel.setTenantId(tenantId);
        dataModel.setTenantId(new TenantId(UUIDs.timeBased()));

        dataModelService.saveDataModel(dataModel);
    }


    @Test(expected = DataValidationException.class)
    public void testSaveDataModeleWithEmptyTenant() {
        DataModel dataModel = new DataModel();
        dataModel.setName("Data model 1");
        dataModel.setLastUpdatedTs(System.currentTimeMillis());

        dataModelService.saveDataModel(dataModel);
    }

    @Test(expected = DataValidationException.class)
    public void testSaveDataModeleWithSameName() {
        DataModel dataModel = new DataModel();
        dataModel.setName("Data model 1");
        dataModel.setLastUpdatedTs(System.currentTimeMillis());
        dataModel.setTenantId(tenantId);

        DataModel savedDataModel1 = dataModelService.saveDataModel(dataModel);
        Assert.assertNotNull(savedDataModel1.getId());

        DataModel sameNamedataModel= new DataModel();
        sameNamedataModel.setName("Data model 1");
        sameNamedataModel.setLastUpdatedTs(System.currentTimeMillis());
        sameNamedataModel.setTenantId(tenantId);

        dataModelService.saveDataModel(sameNamedataModel);
    }

    @Test
    public void testDeleteById() {
        DataModel savedDataModel = createDataModel("Data model 1",tenantId);
        Assert.assertNotNull(savedDataModel.getId());

        dataModelService.deleteById(savedDataModel.getId());
        Assert.assertNull(dataModelService.findById(savedDataModel.getId()));
    }

    @Test
    public void testDeleteDataModelsByTenantId() {
        DataModel savedDataModel1 = createDataModel("data-model-1",tenantId);
        DataModel savedDataModel2 = createDataModel("data-model-2",tenantId);
        Assert.assertNotNull(savedDataModel1.getId());
        Assert.assertNotNull(savedDataModel2.getId());

        dataModelService.deleteDataModelsByTenantId(tenantId);
        List<DataModel> dataModels =  dataModelService.findByTenantId(tenantId);
        Assert.assertEquals(0,dataModels.size());
    }

    private DataModel createDataModel(String name ,TenantId tenantId) {
        DataModel dataModel = new DataModel();
        dataModel.setName(name);
        dataModel.setLastUpdatedTs(System.currentTimeMillis());
        dataModel.setTenantId(tenantId);
        return dataModelService.saveDataModel(dataModel);
    }

}
