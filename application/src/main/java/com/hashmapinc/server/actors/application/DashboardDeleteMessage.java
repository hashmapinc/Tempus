package com.hashmapinc.server.actors.application;

import lombok.Getter;
import com.hashmapinc.server.common.data.id.DashboardId;

public class DashboardDeleteMessage {
    @Getter
    private final DashboardId dashboardId;

    public DashboardDeleteMessage(DashboardId dashboardId) {
        this.dashboardId = dashboardId;
    }
}
