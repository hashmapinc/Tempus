package com.hashmapinc.server.dao.model.sql;

import com.hashmapinc.server.common.data.AssetLandingDashboard;
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
public class AssetLandingDashboardEntity implements ToData<AssetLandingDashboard> {

    @Id
    @Column(name = ModelConstants.ASSET_LANDING_DASHBOARD_ID)
    private String dashboardId;

    @Column(name = ModelConstants.ASSET_LANDING_DATA_MODEL_ID)
    private String dataModelId;

    @Column(name = ModelConstants.ASSET_LANDING_DATAMODEL_OBJECT_ID)
    private String dataModelObjectId;

    public AssetLandingDashboardEntity(AssetLandingDashboard assetLandingDashboard){
        if (assetLandingDashboard.getDashboardId() != null) {
            this.dashboardId = UUIDConverter.fromTimeUUID(assetLandingDashboard.getDashboardId().getId());
        }
        if (assetLandingDashboard.getDataModelId() != null) {
            this.dataModelId = UUIDConverter.fromTimeUUID(assetLandingDashboard.getDataModelId().getId());
        }
        if (assetLandingDashboard.getDataModelObjectId() != null) {
            this.dataModelObjectId = UUIDConverter.fromTimeUUID(assetLandingDashboard.getDataModelObjectId().getId());
        }
    }

    @Override
    public AssetLandingDashboard toData() {
        AssetLandingDashboard landingDashboard = new AssetLandingDashboard(new DashboardId(UUIDConverter.fromString(this.getDashboardId())));
        if (dataModelId != null)
            landingDashboard.setDataModelId(new DataModelId(UUIDConverter.fromString(this.getDataModelId())));
        if (dataModelObjectId != null)
            landingDashboard.setDataModelObjectId(new DataModelObjectId(UUIDConverter.fromString(this.getDataModelObjectId())));
        return landingDashboard;
    }
}
