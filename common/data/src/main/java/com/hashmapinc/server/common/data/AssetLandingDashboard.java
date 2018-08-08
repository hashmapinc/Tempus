package com.hashmapinc.server.common.data;

import com.hashmapinc.server.common.data.id.DashboardId;
import com.hashmapinc.server.common.data.id.DataModelId;
import com.hashmapinc.server.common.data.id.DataModelObjectId;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode
public class AssetLandingDashboard implements Serializable{
    private static final long serialVersionUID = 8142208716716668541L;

    private DashboardId dashboardId;
    private DataModelId dataModelId;
    private DataModelObjectId dataModelObjectId;

    public AssetLandingDashboard(DashboardId dashboardId){
        this.dashboardId = dashboardId;
    }

    public DashboardId getDashboardId() {
        return dashboardId;
    }

    public DataModelId getDataModelId() {
        return dataModelId;
    }

    public void setDataModelId(DataModelId dataModelId) {
        this.dataModelId = dataModelId;
    }

    public DataModelObjectId getDataModelObjectId() {
        return dataModelObjectId;
    }

    public void setDataModelObjectId(DataModelObjectId dataModelObjectId) {
        this.dataModelObjectId = dataModelObjectId;
    }
}
