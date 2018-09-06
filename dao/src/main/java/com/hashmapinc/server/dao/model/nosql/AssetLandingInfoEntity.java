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
import com.hashmapinc.server.common.data.AssetLandingInfo;
import com.hashmapinc.server.common.data.id.DashboardId;
import com.hashmapinc.server.common.data.id.DataModelId;
import com.hashmapinc.server.common.data.id.DataModelObjectId;
import com.hashmapinc.server.dao.model.BaseEntity;
import com.hashmapinc.server.dao.model.ModelConstants;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@EqualsAndHashCode
@NoArgsConstructor
@Table(name = ModelConstants.ASSET_LANDING_COLUMN_FAMILY_NAME)
public class AssetLandingInfoEntity implements BaseEntity<AssetLandingInfo> {

    @PartitionKey
    @Column(name = ModelConstants.ID_PROPERTY)
    private UUID dashboardId;

    @Column(name = ModelConstants.ASSET_LANDING_DATA_MODEL_ID)
    private UUID dataModelId;

    @Column(name = ModelConstants.ASSET_LANDING_DATAMODEL_OBJECT_ID)
    private UUID dataModelObjectId;

    public AssetLandingInfoEntity(AssetLandingInfo assetLandingInfo){
        if (assetLandingInfo.getDashboardId() != null) {
            this.dashboardId = assetLandingInfo.getDashboardId().getId();
        }
        if (assetLandingInfo.getDataModelId() != null) {
            this.dataModelId = assetLandingInfo.getDataModelId().getId();
        }
        if (assetLandingInfo.getDataModelObjectId() != null) {
            this.dataModelObjectId = assetLandingInfo.getDataModelObjectId().getId();
        }
    }

    @Override
    public UUID getId() {
        return dashboardId;
    }

    @Override
    public void setId(UUID id) {
        dashboardId = id;
    }

    @Override
    public AssetLandingInfo toData() {
        AssetLandingInfo landingDashboard = new AssetLandingInfo(new DashboardId(this.getDashboardId()));
        if (dataModelId != null)
            landingDashboard.setDataModelId(new DataModelId(this.getDataModelId()));
        if (dataModelObjectId != null)
            landingDashboard.setDataModelObjectId(new DataModelObjectId(this.getDataModelObjectId()));
        return landingDashboard;
    }
}

