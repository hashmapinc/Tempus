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
package com.hashmapinc.server.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hashmapinc.server.common.data.datamodel.AttributeDefinition;
import com.hashmapinc.server.common.data.datamodel.DataModel;
import com.hashmapinc.server.common.data.Tenant;
import com.hashmapinc.server.common.data.User;
import com.hashmapinc.server.common.data.datamodel.DataModelObject;
import com.hashmapinc.server.common.data.security.Authority;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BaseDataModelControllerTest extends AbstractControllerTest {

    private Tenant savedTenant;
    private User tenantAdmin;
    private DataModel defaultDataModel;
    private DataModelObject defaultDataModelObj;

    @Before
    public void beforeTest() throws Exception {
        loginSysAdmin();

        Tenant tenant = new Tenant();
        tenant.setTitle("My tenant");
        savedTenant = doPost("/api/tenant", tenant, Tenant.class);
        Assert.assertNotNull(savedTenant);

        tenantAdmin = new User();
        tenantAdmin.setAuthority(Authority.TENANT_ADMIN);
        tenantAdmin.setTenantId(savedTenant.getId());
        tenantAdmin.setEmail("tenant2@tempus.org");
        tenantAdmin.setFirstName("Joe");
        tenantAdmin.setLastName("Downs");

        tenantAdmin = createUserAndLogin(tenantAdmin, "testPassword1");

        createDataModel();
        createDataModelObject();
    }

    @After
    public void afterTest() throws Exception {
        loginSysAdmin();

        doDelete("/api/tenant/"+savedTenant.getId().getId().toString())
                .andExpect(status().isOk());
    }


    @Test
    public void testSaveDataModel() throws Exception {
        DataModel dataModel = new DataModel();
        dataModel.setName("Drilling Data Model1");
        dataModel.setLastUpdatedTs(System.currentTimeMillis());

        DataModel savedDataModel = doPost("/api/data-model", dataModel, DataModel.class);

        Assert.assertNotNull(savedDataModel);
        Assert.assertNotNull(savedDataModel.getId());
        Assert.assertTrue(savedDataModel.getCreatedTime() > 0);
        Assert.assertEquals(savedTenant.getId(), savedDataModel.getTenantId());
        Assert.assertEquals(dataModel.getName(), savedDataModel.getName());
        Assert.assertTrue(savedDataModel.getLastUpdatedTs() > 0);
    }

    @Test
    public void testFetchDataModelById() throws Exception {
        DataModel dataModel = new DataModel();
        dataModel.setName("Drilling Data Model for fetch by id");
        dataModel.setLastUpdatedTs(System.currentTimeMillis());
        DataModel savedDataModel = doPost("/api/data-model", dataModel, DataModel.class);

        DataModel fetchedDataModel = doGet("/api/data-model/" + savedDataModel.getId().toString(), DataModel.class);
        Assert.assertEquals(savedDataModel.getName(), fetchedDataModel.getName());
    }

    @Test
    public void testFetchDataModelByTenantId() throws Exception {
        DataModel dataModel = new DataModel();
        dataModel.setName("Drilling Data Model for fetch by tenant id");
        dataModel.setLastUpdatedTs(System.currentTimeMillis());
        DataModel savedDataModel = doPost("/api/data-model", dataModel, DataModel.class);
        List<DataModel> fetchedDataModels = doGetTyped("/api/data-model", new TypeReference<List<DataModel>>(){});
        Assert.assertEquals(2, fetchedDataModels.size());
    }

    @Test
    public void testSaveDataModelObject() throws Exception {
        DataModelObject dataModelObject = new DataModelObject();
        dataModelObject.setName("Well");

        AttributeDefinition ad = new AttributeDefinition();
        ad.setValueType("STRING");
        ad.setName("attr name");
        List<AttributeDefinition> attributeDefinitions = new ArrayList<>();
        attributeDefinitions.add(ad);
        dataModelObject.setAttributeDefinitions(attributeDefinitions);

        DataModelObject savedDataModelObj = doPost("/api/data-model/" + defaultDataModel.getId().toString() + "/objects", dataModelObject, DataModelObject.class);
        Assert.assertNotNull(savedDataModelObj);
        Assert.assertEquals(defaultDataModel.getId(), savedDataModelObj.getDataModelId());
    }

    @Test
    public void testFetchDataModelObjectsByModel() throws Exception{

        List<DataModelObject> foundDataModelObjs = doGetTyped("/api/data-model/" + defaultDataModel.getId().toString() + "/objects", new TypeReference<List<DataModelObject>>() {});
        Assert.assertNotNull(foundDataModelObjs);
        Assert.assertEquals(1, foundDataModelObjs.size());
    }

    @Test
    public void testFetchDataModelObjectById() throws Exception{

        DataModelObject foundDataModelObj = doGet("/api/data-model/objects/" + defaultDataModelObj.getId().toString(), DataModelObject.class);
        Assert.assertNotNull(foundDataModelObj);
        Assert.assertEquals(defaultDataModelObj.getName(), foundDataModelObj.getName());
    }

    private void createDataModel() throws Exception{
        DataModel dataModel = new DataModel();
        dataModel.setName("Default Drilling Data Model1");
        dataModel.setLastUpdatedTs(System.currentTimeMillis());

        DataModel savedDataModel = doPost("/api/data-model", dataModel, DataModel.class);

        Assert.assertNotNull(savedDataModel);
        Assert.assertNotNull(savedDataModel.getId());
        Assert.assertTrue(savedDataModel.getCreatedTime() > 0);
        Assert.assertEquals(savedTenant.getId(), savedDataModel.getTenantId());
        Assert.assertEquals(dataModel.getName(), savedDataModel.getName());
        Assert.assertTrue(savedDataModel.getLastUpdatedTs() > 0);
        defaultDataModel = savedDataModel;
    }

    private void createDataModelObject() throws Exception{
        DataModelObject dataModelObject = new DataModelObject();
        dataModelObject.setName("Well2");

        AttributeDefinition ad = new AttributeDefinition();
        ad.setValueType("STRING");
        ad.setName("attr name2");
        List<AttributeDefinition> attributeDefinitions = new ArrayList<>();
        attributeDefinitions.add(ad);
        dataModelObject.setAttributeDefinitions(attributeDefinitions);

        DataModelObject savedDataModelObj = doPost("/api/data-model/" + defaultDataModel.getId().toString() + "/objects", dataModelObject, DataModelObject.class);
        Assert.assertNotNull(savedDataModelObj);
        Assert.assertEquals(defaultDataModel.getId(), savedDataModelObj.getDataModelId());
        defaultDataModelObj = savedDataModelObj;
    }
}
