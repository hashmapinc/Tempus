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
package com.hashmapinc.server.dao.service;

import com.datastax.driver.core.utils.UUIDs;
import com.hashmapinc.server.common.data.AssetLandingDashboard;
import com.hashmapinc.server.common.data.id.DashboardId;
import com.hashmapinc.server.common.data.id.DataModelId;
import com.hashmapinc.server.common.data.id.DataModelObjectId;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public abstract class BaseAssetLandingDashboardServiceTest extends AbstractServiceTest {

    DataModelObjectId dataModelObjectId;
    DataModelId dataModelId;
    DashboardId dashboardId1;
    DashboardId dashboardId2;

    @Before
    public void before() {
        dataModelId = new DataModelId(UUIDs.timeBased());
        dataModelObjectId = new DataModelObjectId(UUIDs.timeBased());

        dashboardId1 = new DashboardId(UUIDs.timeBased());
        dashboardId2 = new DashboardId(UUIDs.timeBased());

        AssetLandingDashboard assetLandingDashboard = new AssetLandingDashboard(dashboardId1);
        assetLandingDashboard.setDataModelId(dataModelId);
        assetLandingDashboard.setDataModelObjectId(dataModelObjectId);

        AssetLandingDashboard saved = assetLandingDashboardService.save(assetLandingDashboard);
        Assert.assertNotNull(saved);

        AssetLandingDashboard assetLandingDashboard2 = new AssetLandingDashboard(dashboardId2);
        assetLandingDashboard2.setDataModelId(dataModelId);
        assetLandingDashboard2.setDataModelObjectId(dataModelObjectId);

        AssetLandingDashboard saved2 = assetLandingDashboardService.save(assetLandingDashboard2);
        Assert.assertNotNull(saved2);

    }

    @After
    public void After() {
        assetLandingDashboardService.removeByDashboardId(dashboardId1);
        assetLandingDashboardService.removeByDashboardId(dashboardId2);
    }

    @Test
    public void testFetchByDataModelObjectId() {
        List<AssetLandingDashboard> assetLanding = assetLandingDashboardService.findByDataModelObjectId(dataModelObjectId);
        Assert.assertEquals(2, assetLanding.size());
    }

    @Test
    public void testFetchByDashboardId() {
        AssetLandingDashboard assetLandingDashboard = assetLandingDashboardService.findByDashboardId(dashboardId1);
        Assert.assertNotNull(assetLandingDashboard);
        Assert.assertEquals(assetLandingDashboard.getDataModelId(), dataModelId);
        Assert.assertEquals(assetLandingDashboard.getDataModelObjectId(), dataModelObjectId);
    }

}
