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
package com.hashmapinc.server.dao.model.sql;

import com.hashmapinc.server.common.data.AssetLandingDashboardInfo;
import com.hashmapinc.server.common.data.UUIDConverter;
import com.hashmapinc.server.common.data.id.DashboardId;
import com.hashmapinc.server.common.data.id.DataModelId;
import com.hashmapinc.server.common.data.id.DataModelObjectId;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.model.ToData;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@EqualsAndHashCode
@Entity
@Table(name = ModelConstants.ASSET_LANDING_COLUMN_FAMILY_NAME)
public class AssetLandingDashboardEntity implements ToData<AssetLandingDashboardInfo> {

    @Id
    @Column(name = ModelConstants.ASSET_LANDING_DASHBOARD_ID)
    private String dashboardId;

    @Column(name = ModelConstants.ASSET_LANDING_DATA_MODEL_ID)
    private String dataModelId;

    @Column(name = ModelConstants.ASSET_LANDING_DATAMODEL_OBJECT_ID)
    private String dataModelObjectId;

    public AssetLandingDashboardEntity() {

    }

    public AssetLandingDashboardEntity(AssetLandingDashboardInfo assetLandingDashboardInfo){
        if (assetLandingDashboardInfo.getDashboardId() != null) {
            this.dashboardId = UUIDConverter.fromTimeUUID(assetLandingDashboardInfo.getDashboardId().getId());
        }
        if (assetLandingDashboardInfo.getDataModelId() != null) {
            this.dataModelId = UUIDConverter.fromTimeUUID(assetLandingDashboardInfo.getDataModelId().getId());
        }
        if (assetLandingDashboardInfo.getDataModelObjectId() != null) {
            this.dataModelObjectId = UUIDConverter.fromTimeUUID(assetLandingDashboardInfo.getDataModelObjectId().getId());
        }
    }

    @Override
    public AssetLandingDashboardInfo toData() {
        AssetLandingDashboardInfo landingDashboard = new AssetLandingDashboardInfo(new DashboardId(UUIDConverter.fromString(this.getDashboardId())));
        if (dataModelId != null)
            landingDashboard.setDataModelId(new DataModelId(UUIDConverter.fromString(this.getDataModelId())));
        if (dataModelObjectId != null)
            landingDashboard.setDataModelObjectId(new DataModelObjectId(UUIDConverter.fromString(this.getDataModelObjectId())));
        return landingDashboard;
    }
}
