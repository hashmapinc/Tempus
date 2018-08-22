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
import com.hashmapinc.server.common.data.AssetLandingDashboardInfo;
import com.hashmapinc.server.common.data.id.DashboardId;
import com.hashmapinc.server.common.data.id.DataModelId;
import com.hashmapinc.server.common.data.id.DataModelObjectId;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public abstract class BaseAssetLandingDashboardInfoServiceTest extends AbstractServiceTest {

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

        AssetLandingDashboardInfo assetLandingDashboardInfo = new AssetLandingDashboardInfo(dashboardId1);
        assetLandingDashboardInfo.setDataModelId(dataModelId);
        assetLandingDashboardInfo.setDataModelObjectId(dataModelObjectId);

        AssetLandingDashboardInfo saved = assetLandingDashboardService.save(assetLandingDashboardInfo);
        Assert.assertNotNull(saved);

        AssetLandingDashboardInfo assetLandingDashboardInfo2 = new AssetLandingDashboardInfo(dashboardId2);
        assetLandingDashboardInfo2.setDataModelId(dataModelId);
        assetLandingDashboardInfo2.setDataModelObjectId(dataModelObjectId);

        AssetLandingDashboardInfo saved2 = assetLandingDashboardService.save(assetLandingDashboardInfo2);
        Assert.assertNotNull(saved2);

    }

    @After
    public void After() {
        assetLandingDashboardService.removeByDashboardId(dashboardId1);
        assetLandingDashboardService.removeByDashboardId(dashboardId2);
    }

    @Test
    public void testFetchByDataModelObjectId() {
        List<AssetLandingDashboardInfo> assetLanding = assetLandingDashboardService.findByDataModelObjectId(dataModelObjectId);
        Assert.assertEquals(2, assetLanding.size());
    }

    @Test
    public void testFetchByDashboardId() {
        AssetLandingDashboardInfo assetLandingDashboardInfo = assetLandingDashboardService.findByDashboardId(dashboardId1);
        Assert.assertNotNull(assetLandingDashboardInfo);
        Assert.assertEquals(assetLandingDashboardInfo.getDataModelId(), dataModelId);
        Assert.assertEquals(assetLandingDashboardInfo.getDataModelObjectId(), dataModelObjectId);
    }

}
