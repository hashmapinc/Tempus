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
package com.hashmapinc.server.dao.dashboard;

import com.google.common.util.concurrent.ListenableFuture;
import com.hashmapinc.server.common.data.Dashboard;
import com.hashmapinc.server.common.data.DashboardInfo;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.page.TextPageData;
import com.hashmapinc.server.common.data.page.TextPageLink;
import com.hashmapinc.server.common.data.page.TimePageLink;
import com.hashmapinc.server.common.data.id.CustomerId;
import com.hashmapinc.server.common.data.id.DashboardId;
import com.hashmapinc.server.common.data.page.TimePageData;

public interface DashboardService {
    
    Dashboard findDashboardById(DashboardId dashboardId);

    ListenableFuture<Dashboard> findDashboardByIdAsync(DashboardId dashboardId);

    DashboardInfo findDashboardInfoById(DashboardId dashboardId);

    ListenableFuture<DashboardInfo> findDashboardInfoByIdAsync(DashboardId dashboardId);

    Dashboard saveDashboard(Dashboard dashboard);

    Dashboard assignDashboardToCustomer(DashboardId dashboardId, CustomerId customerId);

    Dashboard unassignDashboardFromCustomer(DashboardId dashboardId, CustomerId customerId);

    void deleteDashboard(DashboardId dashboardId);

    TextPageData<DashboardInfo> findDashboardsByTenantId(TenantId tenantId, TextPageLink pageLink);

    void deleteDashboardsByTenantId(TenantId tenantId);

    ListenableFuture<TimePageData<DashboardInfo>> findDashboardsByTenantIdAndCustomerId(TenantId tenantId, CustomerId customerId, TimePageLink pageLink);

    void unassignCustomerDashboards(CustomerId customerId);

    void updateCustomerDashboards(CustomerId customerId);

}
