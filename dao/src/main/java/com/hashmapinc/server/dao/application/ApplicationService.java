/**
 * Copyright © 2017-2018 Hashmap, Inc
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
package com.hashmapinc.server.dao.application;

import com.hashmapinc.server.common.data.id.*;
import com.hashmapinc.server.common.data.page.TextPageLink;
import com.hashmapinc.server.common.data.Application;
import com.hashmapinc.server.common.data.id.*;
import com.hashmapinc.server.common.data.page.TextPageData;

import java.util.List;
import java.util.Set;

public interface ApplicationService {

    Application saveApplication(Application application);

    Application findApplicationById(ApplicationId applicationId);

    void deleteApplication(ApplicationId applicationId);

    TextPageData<Application> findApplicationsByTenantId(TenantId tenantId, TextPageLink pageLink);

    List<Application> findApplicationsByDeviceType(TenantId tenantId, String deviceType);

    Set<String> findApplicationByRuleIds(TenantId tenantId, Set<RuleId> ruleIds);

    List<String> findApplicationByDashboardId(TenantId tenantId, DashboardId dashboardId);

    Application assignApplicationToCustomer(ApplicationId applicationId, CustomerId customerId);

    Application unassignApplicationFromCustomer(ApplicationId applicationId);

    Application assignDashboardToApplication(ApplicationId applicationId, DashboardId dashboardId, String dashboardType);

    Application unassignDashboardFromApplication(ApplicationId applicationId, String dashboardType);

    Application assignRulesToApplication(ApplicationId applicationId, Set<RuleId> ruleIdList);

    Application unassignRulesToApplication(ApplicationId applicationId, Set<RuleId> ruleIdList);

    Application assignComputationJobsToApplication(ApplicationId applicationId, Set<ComputationJobId> computationJobIds);

    Application unassignComputationJobsToApplication(ApplicationId applicationId, Set<ComputationJobId> computationJobIds);

    List<String> findApplicationByComputationJobId(TenantId tenantId, ComputationJobId computationJobId);

    void updateApplicationOnComputationJobDelete(ComputationJobId computationJobId, TenantId tenantId);

    void updateApplicationOnComputationDelete(ComputationId computationId, TenantId tenantId);

    void updateApplicationOnRuleDelete(RuleId ruleId, TenantId tenantId);

    void updateApplicationOnDashboardDelete(DashboardId dashboardIdId, TenantId tenantId);

    void activateApplicationById(ApplicationId applicationId);

    void suspendApplicationById(ApplicationId applicationId);

}
