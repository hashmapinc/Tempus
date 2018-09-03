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
package com.hashmapinc.server.dao.service.sql;

import com.datastax.driver.core.utils.UUIDs;
import com.hashmapinc.server.common.data.asset.Asset;
import com.hashmapinc.server.common.data.datamodel.DataModel;
import com.hashmapinc.server.common.data.id.DataModelObjectId;
import com.hashmapinc.server.dao.service.BaseAssetServiceTest;
import com.hashmapinc.server.dao.service.DaoSqlTest;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

@DaoSqlTest
public class AssetServiceSqlTest extends BaseAssetServiceTest {
    @Test
    public void testFindAssetsByDataModelObjectId() {
        Asset asset = new Asset();
        asset.setTenantId(tenantId);
        DataModelObjectId dataModelObjectId = new DataModelObjectId(UUIDs.timeBased());
        asset.setDataModelObjectId(dataModelObjectId);
        asset.setName("My asset");
        asset.setType("default");

        Asset asset2 = new Asset();
        asset2.setTenantId(tenantId);
        asset2.setDataModelObjectId(dataModelObjectId);
        asset2.setName("My asset2");
        asset2.setType("default");

        Asset savedAsset = assetService.saveAsset(asset);
        Asset savedAsset2 = assetService.saveAsset(asset2);

        List<Asset> foundAsset = assetService.findAssetsByDataModelObjectId(savedAsset.getDataModelObjectId());

        Assert.assertNotNull(foundAsset);
        Assert.assertEquals(2, foundAsset.size());

        assetService.deleteAsset(savedAsset.getId());
        assetService.deleteAsset(savedAsset2.getId());
    }


}
