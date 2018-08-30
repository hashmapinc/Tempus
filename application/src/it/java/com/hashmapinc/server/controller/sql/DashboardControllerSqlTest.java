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
package com.hashmapinc.server.controller.sql;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hashmapinc.server.common.data.AssetLandingDashboardInfo;
import com.hashmapinc.server.common.data.Dashboard;
import com.hashmapinc.server.common.data.DashboardType;
import com.hashmapinc.server.common.data.datamodel.AttributeDefinition;
import com.hashmapinc.server.common.data.datamodel.DataModel;
import com.hashmapinc.server.common.data.datamodel.DataModelObject;
import com.hashmapinc.server.controller.BaseDashboardControllerTest;
import com.hashmapinc.server.dao.service.DaoSqlTest;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Valerii Sosliuk on 6/28/2017.
 */
@DaoSqlTest
public class DashboardControllerSqlTest extends BaseDashboardControllerTest {
    @Test
    public void testFindDashboardByDataModelObj() throws Exception {
        Dashboard dashboard = new Dashboard();
        dashboard.setTitle("My dashboard");
        dashboard.setType(DashboardType.ASSET_LANDING_PAGE);

        DataModel dataModel = createDataModel();
        DataModelObject dataModelObject = createDataModelObject(dataModel);

        AssetLandingDashboardInfo ald = new AssetLandingDashboardInfo();
        ald.setDataModelId(dataModel.getId());
        ald.setDataModelObjectId(dataModelObject.getId());

        dashboard.setAssetLandingDashboardInfo(ald);

        Dashboard savedDashboard = doPost("/api/dashboard", dashboard, Dashboard.class);
        Assert.assertNotNull(savedDashboard);

        List<Dashboard> dashboards = doGetTyped("/api/asset/dashboard/data-model-object/" + dataModelObject.getId().toString(), new TypeReference<List<Dashboard>>(){});
        Assert.assertEquals(1, dashboards.size());
    }

    private DataModel createDataModel() throws Exception{
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
        return savedDataModel;
    }

    private DataModelObject createDataModelObject(DataModel dataModel) throws Exception{
        DataModelObject dataModelObject = new DataModelObject();
        dataModelObject.setName("Well2");

        AttributeDefinition ad = new AttributeDefinition();
        ad.setValueType("STRING");
        ad.setName("attr name2");
        List<AttributeDefinition> attributeDefinitions = new ArrayList<>();
        attributeDefinitions.add(ad);
        dataModelObject.setAttributeDefinitions(attributeDefinitions);

        DataModelObject savedDataModelObj = doPost("/api/data-model/" + dataModel.getId().toString() + "/objects", dataModelObject, DataModelObject.class);
        Assert.assertNotNull(savedDataModelObj);
        Assert.assertEquals(dataModel.getId(), savedDataModelObj.getDataModelId());
        return savedDataModelObj;
    }
}
