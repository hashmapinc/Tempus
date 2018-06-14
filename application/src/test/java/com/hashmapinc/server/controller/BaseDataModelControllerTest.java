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
import com.hashmapinc.server.common.data.datamodel.DataModel;
import com.hashmapinc.server.common.data.Tenant;
import com.hashmapinc.server.common.data.User;
import com.hashmapinc.server.common.data.security.Authority;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BaseDataModelControllerTest extends AbstractControllerTest {

    private Tenant savedTenant;
    private User tenantAdmin;

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

        Assert.assertEquals(1, fetchedDataModels.size());
        Assert.assertEquals(savedDataModel.getName(), fetchedDataModels.get(0).getName());
    }
}
