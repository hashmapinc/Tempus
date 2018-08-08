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
package com.hashmapinc.server.dao.model.nosql;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.hashmapinc.server.common.data.AssetLandingDashboard;
import com.hashmapinc.server.common.data.id.DashboardId;
import com.hashmapinc.server.common.data.id.DataModelId;
import com.hashmapinc.server.common.data.id.DataModelObjectId;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.model.ToData;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Data
@EqualsAndHashCode
@Table(name = ModelConstants.ASSET_LANDING_COLUMN_FAMILY_NAME)
public class AssetLandingDashboardEntity implements ToData<AssetLandingDashboard>{

    @PartitionKey(value = 0)
    @Column(name = ModelConstants.ASSET_LANDING_DASHBOARD_ID)
    private UUID dashboardId;

    @Column(name = ModelConstants.ASSET_LANDING_DATA_MODEL_ID)
    private UUID dataModelId;

    @Column(name = ModelConstants.ASSET_LANDING_DATAMODEL_OBJECT_ID)
    private UUID dataModelObjectId;

    public AssetLandingDashboardEntity(AssetLandingDashboard assetLandingDashboard){
        if (assetLandingDashboard.getDashboardId() != null) {
            this.dashboardId = assetLandingDashboard.getDashboardId().getId();
        }
        if (assetLandingDashboard.getDataModelId() != null) {
            this.dataModelId = assetLandingDashboard.getDataModelId().getId();
        }
        if (assetLandingDashboard.getDataModelObjectId() != null) {
            this.dataModelObjectId = assetLandingDashboard.getDataModelObjectId().getId();
        }
    }

    @Override
    public AssetLandingDashboard toData() {
        AssetLandingDashboard landingDashboard = new AssetLandingDashboard(new DashboardId(this.getDashboardId()));
        if (dataModelId != null)
            landingDashboard.setDataModelId(new DataModelId(this.getDataModelId()));
        if (dataModelObjectId != null)
            landingDashboard.setDataModelObjectId(new DataModelObjectId(this.getDataModelObjectId()));
        return landingDashboard;
    }
}
