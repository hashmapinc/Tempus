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
package com.hashmapinc.server.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hashmapinc.server.common.data.datamodel.AttributeDefinition;
import com.hashmapinc.server.common.data.datamodel.DataModel;
import com.hashmapinc.server.common.data.datamodel.DataModelObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BaseDataModelControllerTest extends AbstractControllerTest {

    private DataModel defaultDataModel;
    private DataModelObject defaultDataModelObj;

    @Before
    public void beforeTest() throws Exception {
        loginTenantAdmin();
        defaultDataModel = createDataModel();
        defaultDataModelObj = createDataModelObject(defaultDataModel);
    }

    @Test
    public void testFetchDataModelById() throws Exception {
        DataModel fetchedDataModel = doGet("/api/data-model/" + defaultDataModel.getId().toString(), DataModel.class);
        Assert.assertEquals(defaultDataModel.getName(), fetchedDataModel.getName());
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

    @Test
    public void testRemoveDataModelById() throws Exception {

        DataModel fetchedDataModel1 = doGet("/api/data-model/" + defaultDataModel.getId().toString(), DataModel.class);
        Assert.assertEquals(defaultDataModel.getName(), fetchedDataModel1.getName());

        doDelete("/api/data-model/" +defaultDataModel.getId().getId().toString())
                .andExpect(status().isOk());

        doGet("/api/data-model/" + defaultDataModel.getId().toString()).
                andExpect(status().isNotFound());
    }

}
