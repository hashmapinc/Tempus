package com.hashmapinc.server.common.data;

public class RequestAssetDashboard {
    Dashboard dashboard;
    AssetLandingDashboardInfo assetLandingDashboardInfo;

    public RequestAssetDashboard(Dashboard dashboard, AssetLandingDashboardInfo assetLandingDashboardInfo) {
        this.dashboard = dashboard;
        this.assetLandingDashboardInfo = assetLandingDashboardInfo;
    }

    public RequestAssetDashboard() {
    }

    public Dashboard getDashboard() {
        return dashboard;
    }

    public void setDashboard(Dashboard dashboard) {
        this.dashboard = dashboard;
    }

    public AssetLandingDashboardInfo getAssetLandingDashboardInfo() {
        return assetLandingDashboardInfo;
    }

    public void setAssetLandingDashboardInfo(AssetLandingDashboardInfo assetLandingDashboardInfo) {
        this.assetLandingDashboardInfo = assetLandingDashboardInfo;
    }
}
