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
package com.hashmapinc.server.controller;

import com.google.common.collect.Iterables;
import com.hashmapinc.server.common.data.Application;
import com.hashmapinc.server.common.data.computation.ComputationJob;
import com.hashmapinc.server.common.data.id.*;
import com.hashmapinc.server.common.data.page.TextPageData;
import com.hashmapinc.server.common.data.page.TextPageLink;
import com.hashmapinc.server.common.data.plugin.ComponentLifecycleEvent;
import com.hashmapinc.server.common.data.plugin.ComponentLifecycleState;
import com.hashmapinc.server.dao.exception.IncorrectParameterException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.hashmapinc.server.common.data.ApplicationFieldsWrapper;
import com.hashmapinc.server.common.data.id.*;
import com.hashmapinc.server.common.data.rule.RuleMetaData;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.exception.TempusException;

import java.util.*;
import java.util.stream.Collectors;

import static com.hashmapinc.server.dao.model.ModelConstants.NULL_UUID;

@RestController
@RequestMapping("/api")
@Slf4j
public class ApplicationController extends BaseController {

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/application", method = RequestMethod.POST)
    @ResponseBody
    public Application saveApplication(@RequestBody Application application) throws TempusException {
        try{
            application.setTenantId(getCurrentUser().getTenantId());
            return checkNotNull(applicationService.saveApplication(application));
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/application/{applicationId}", method = RequestMethod.GET)
    @ResponseBody
    public Application getApplicationById(@PathVariable("applicationId") String strApplicationId) throws TempusException {
        checkParameter("applicationId", strApplicationId);
        try {
            ApplicationId applicationId = new ApplicationId(toUUID(strApplicationId));
            return checkApplicationId(applicationId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/application/{applicationId}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteApplication(@PathVariable("applicationId") String strApplicationId) throws TempusException {
        checkParameter("applicationId", strApplicationId);
        try {
            ApplicationId applicationId = new ApplicationId(toUUID(strApplicationId));
            Application application = checkApplicationId(applicationId);
            applicationService.deleteApplication(applicationId);
            cleanupApplicationRelatedEntities(application);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    private void cleanupApplicationRelatedEntities(Application application) throws TempusException {
        for(RuleId ruleId: application.getRules()) {
            if(!ruleId.isNullUid()) {
                ruleService.deleteRuleById(ruleId);
                actorService.onRuleStateChange(application.getTenantId(), ruleId, ComponentLifecycleEvent.DELETED);
            }
        }

        if(!application.getDashboardId().isNullUid()) {
            dashboardService.deleteDashboard(application.getDashboardId());
        }

        if(!application.getMiniDashboardId().isNullUid()) {
            dashboardService.deleteDashboard(application.getMiniDashboardId());
        }

        for(ComputationJobId computationJobId: application.getComputationJobIdSet()) {
            if(!computationJobId.isNullUid()) {
                ComputationJob computationJob = checkComputationJob(computationJobService.findComputationJobById(computationJobId));
                computationJobService.deleteComputationJobById(computationJobId);
                actorService.onComputationJobStateChange(computationJob.getTenantId(), computationJob.getComputationId(), computationJob.getId(), ComponentLifecycleEvent.DELETED);
            }
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/tenant/applications", params = {"limit"}, method = RequestMethod.GET)
    @ResponseBody
    public TextPageData<Application> getTenantApplications(
            @RequestParam int limit,
            @RequestParam(required = false) String textSearch,
            @RequestParam(required = false) String idOffset,
            @RequestParam(required = false) String textOffset) throws TempusException {
        try {
            TenantId tenantId = getCurrentUser().getTenantId();
            TextPageLink pageLink = createPageLink(limit, textSearch, idOffset, textOffset);
            return checkNotNull(applicationService.findApplicationsByTenantId(tenantId, pageLink));
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/applications/{deviceType}", method = RequestMethod.GET)
    @ResponseBody
    public List<Application> getDeviceTypeApplications(@PathVariable("deviceType") String deviceType)  throws TempusException {
        checkParameter("deviceType", deviceType);
        try {
            TenantId tenantId = getCurrentUser().getTenantId();
            return checkNotNull(applicationService.findApplicationsByDeviceType(tenantId, deviceType));
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/applications/rules/{strRuleIds}", method = RequestMethod.GET)
    @ResponseBody
    public Set<String> findApplicationsByruleIds(@PathVariable String[] strRuleIds) throws TempusException {
        checkArrayParameter("ruleIds", strRuleIds);
        Set<RuleId> ruleIds = Arrays.stream(strRuleIds).map(r -> new RuleId(toUUID(r))).collect(Collectors.toSet());
        TenantId tenantId = getCurrentUser().getTenantId();
        return applicationService.findApplicationByRuleIds(tenantId, ruleIds);
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/applications/dashboard/{dashboardId}", method = RequestMethod.GET)
    @ResponseBody
    public List<String> findApplicationsByDashboardId(@PathVariable("dashboardId") String strDashboardId) throws TempusException {
        checkParameter("dashboardId", strDashboardId);
        DashboardId dashboardId = new DashboardId(toUUID(strDashboardId));
        TenantId tenantId = getCurrentUser().getTenantId();
        return applicationService.findApplicationByDashboardId(tenantId, dashboardId);
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/customer/{customerId}/application/{applicationId}", method = RequestMethod.POST)
    @ResponseBody
    public Application assignApplicationToCustomer(@PathVariable("customerId") String strCustomerId,
                                         @PathVariable("applicationId") String strApplicationId) throws TempusException {
        checkParameter("customerId", strCustomerId);
        checkParameter("applicationId", strApplicationId);
        try {
            CustomerId customerId = new CustomerId(toUUID(strCustomerId));
            checkCustomerId(customerId);

            ApplicationId applicationId = new ApplicationId(toUUID(strApplicationId));
            checkApplicationId(applicationId);

            return checkNotNull(applicationService.assignApplicationToCustomer(applicationId, customerId));
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/customer/application/{applicationId}", method = RequestMethod.DELETE)
    @ResponseBody
    public Application unassignApplicationFromCustomer(@PathVariable("applicationId") String strApplicationId) throws TempusException {
        checkParameter("applicationId", strApplicationId);
        try {
            ApplicationId applicationId = new ApplicationId(toUUID(strApplicationId));
            Application application = checkApplicationId(applicationId);
            if (application.getCustomerId() == null || application.getCustomerId().getId().equals(ModelConstants.NULL_UUID)) {
                throw new IncorrectParameterException("Application isn't assigned to any customer!");
            }
            return checkNotNull(applicationService.unassignApplicationFromCustomer(applicationId));
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/dashboard/{dashboardType}/{dashboardId}/application/{applicationId}", method = RequestMethod.POST)
    @ResponseBody
    public Application assignDashboardToApplication(
            @PathVariable("dashboardType") String dashboardType,
            @PathVariable("dashboardId") String strDashboardId,
            @PathVariable("applicationId") String strApplicationId) throws TempusException {

        checkParameter("dashboardType", dashboardType);
        checkParameter("dashboardId", strDashboardId);
        checkParameter("applicationId", strApplicationId);
        try {
            DashboardId dashboardId =  new DashboardId(toUUID(strDashboardId));
            checkDashboardId(dashboardId);

            ApplicationId applicationId = new ApplicationId(toUUID(strApplicationId));
            checkApplicationId(applicationId);

            return checkNotNull(applicationService.assignDashboardToApplication(applicationId, dashboardId, dashboardType));
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/dashboard/{dashboardType}/application/{applicationId}", method = RequestMethod.DELETE)
    @ResponseBody
    public Application unassignDashboardFromApplication(@PathVariable("dashboardType") String dashboardType, @PathVariable("applicationId") String strApplicationId) throws TempusException {
        checkParameter("applicationId", strApplicationId);
        checkParameter("dashboardType", dashboardType);
        try {
            ApplicationId applicationId = new ApplicationId(toUUID(strApplicationId));
            Application application = checkApplicationId(applicationId);
            if(dashboardType.equals("mini")) {
                if (application.getMiniDashboardId() == null || application.getMiniDashboardId().getId().equals(ModelConstants.NULL_UUID)) {
                    throw new IncorrectParameterException("No mini dashboard assigned to an application!");
                }
                return checkNotNull(applicationService.unassignDashboardFromApplication(applicationId, dashboardType));
            } else if(dashboardType.equals("main")) {
                if (application.getDashboardId() == null || application.getDashboardId().getId().equals(ModelConstants.NULL_UUID)) {
                    throw new IncorrectParameterException("No dashboard assigned to an application!");
                }
                return checkNotNull(applicationService.unassignDashboardFromApplication(applicationId, dashboardType));
            } else {
                throw new IncorrectParameterException("Incorrect Dashboard Type for an application");
            }

        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/app/assignRules", method = RequestMethod.POST,consumes = "application/json")
    public Application assignRulesToApplication(@RequestBody ApplicationFieldsWrapper applicationFieldsWrapper) throws TempusException {
        checkParameter("applicationId", applicationFieldsWrapper.getApplicationId());
        try {
            ApplicationId applicationId = new ApplicationId(toUUID(applicationFieldsWrapper.getApplicationId()));
            checkApplicationId(applicationId);
            Set<RuleId> ruleIds = applicationFieldsWrapper.getFields().stream().map(r -> new RuleId(toUUID(r))).collect(Collectors.toSet());
            Application application  = checkNotNull(applicationService.assignRulesToApplication(applicationId, ruleIds));
            if(application.getState().equals(ComponentLifecycleState.ACTIVE)) {
                for(RuleId ruleId: ruleIds){
                    RuleMetaData rule = checkRule(ruleService.findRuleById(ruleId));
                    ruleService.activateRuleById(ruleId);
                    actorService.onRuleStateChange(rule.getTenantId(), rule.getId(), ComponentLifecycleEvent.ACTIVATED);
                }
            }
            return application;
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/app/unassignRules", method = RequestMethod.POST,consumes = "application/json")
    public Application unassignRulesToApplication(@RequestBody ApplicationFieldsWrapper applicationFieldsWrapper) throws TempusException {
        checkParameter("applicationId", applicationFieldsWrapper.getApplicationId());
        try {
            ApplicationId applicationId = new ApplicationId(toUUID(applicationFieldsWrapper.getApplicationId()));
            checkApplicationId(applicationId);
            Set<RuleId> ruleIds = applicationFieldsWrapper.getFields().stream().map(r -> new RuleId(toUUID(r))).collect(Collectors.toSet());
            return checkNotNull(applicationService.unassignRulesToApplication(applicationId, ruleIds));
        } catch (Exception e) {
            throw handleException(e);
        }
    }


    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/app/assignComputationJobs", method = RequestMethod.POST,consumes = "application/json")
    public Application assignComputationsJobToApplication(@RequestBody ApplicationFieldsWrapper applicationFieldsWrapper) throws TempusException {
        checkParameter("applicationId", applicationFieldsWrapper.getApplicationId());
        try {
            ApplicationId applicationId = new ApplicationId(toUUID(applicationFieldsWrapper.getApplicationId()));
            checkApplicationId(applicationId);
            Set<ComputationJobId> computationJobIds = applicationFieldsWrapper.getFields().stream().map(r -> new ComputationJobId(toUUID(r))).collect(Collectors.toSet());
            Application application =  checkNotNull(applicationService.assignComputationJobsToApplication(applicationId, computationJobIds));
            if(application.getState().equals(ComponentLifecycleState.ACTIVE)) {
                for(ComputationJobId computationJobId: computationJobIds) {
                    ComputationJob computationJob = checkComputationJob(computationJobService.findComputationJobById(computationJobId));
                    computationJobService.activateComputationJobById(computationJobId);
                    actorService.onComputationJobStateChange(computationJob.getTenantId(), computationJob.getComputationId(), computationJob.getId(), ComponentLifecycleEvent.ACTIVATED);
                }
            }
            return application;
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/app/unassignComputationJobs", method = RequestMethod.POST,consumes = "application/json")
    public Application unassignComputationsJobToApplication(@RequestBody ApplicationFieldsWrapper applicationFieldsWrapper) throws TempusException {
        checkParameter("applicationId", applicationFieldsWrapper.getApplicationId());
        try {
            ApplicationId applicationId = new ApplicationId(toUUID(applicationFieldsWrapper.getApplicationId()));
            checkApplicationId(applicationId);
            Set<ComputationJobId> computationJobIds  = applicationFieldsWrapper.getFields().stream().map(c -> new ComputationJobId(toUUID(c))).collect(Collectors.toSet());
            return checkNotNull(applicationService.unassignComputationJobsToApplication(applicationId, computationJobIds));
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/application/{applicationId}/activate", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void activateApplicationById(@PathVariable("applicationId") String strApplicationId) throws TempusException {
        checkParameter("applicationId", strApplicationId);
        try {
            ApplicationId applicationId = new ApplicationId(toUUID(strApplicationId));
            Application application =  checkApplicationId(applicationId);
            activateApplication(application);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    private void activateApplication(Application application) throws TempusException {
        Map<RuleId, RuleMetaData> activatedRules = new HashMap<>();
        Map<ComputationJobId, ComputationJob> activatedComputations = new HashMap<>();
        try {
            if(application.getRules().size() > 1 || !Iterables.getOnlyElement(application.getRules()).getId().equals(NULL_UUID)) {
                for(RuleId ruleId: application.getRules()) {
                    RuleMetaData rule = checkRule(ruleService.findRuleById(ruleId));
                    if(!rule.getState().equals(ComponentLifecycleState.ACTIVE)) {
                        ruleService.activateRuleById(ruleId);
                        actorService.onRuleStateChange(rule.getTenantId(), rule.getId(), ComponentLifecycleEvent.ACTIVATED);
                        activatedRules.put(ruleId, rule);
                    }
                }
            }

            if(application.getComputationJobIdSet().size() >1 || !Iterables.getOnlyElement(application.getComputationJobIdSet()).getId().equals(NULL_UUID)) {
                for(ComputationJobId computationJobId: application.getComputationJobIdSet()) {
                    ComputationJob computationJob = checkComputationJob(computationJobService.findComputationJobById(computationJobId));
                    if(!computationJob.getState().equals(ComponentLifecycleState.ACTIVE)) {
                        computationJobService.activateComputationJobById(computationJobId);
                        actorService.onComputationJobStateChange(computationJob.getTenantId(), computationJob.getComputationId(), computationJob.getId(), ComponentLifecycleEvent.ACTIVATED);
                        activatedComputations.put(computationJob.getId(), computationJob);
                    }
                }
            }

            applicationService.activateApplicationById(application.getId());
        } catch (Exception e) {
            if(!activatedRules.isEmpty()) {
                for(Map.Entry<RuleId, RuleMetaData> entry: activatedRules.entrySet()){
                    ruleService.suspendRuleById(entry.getKey());
                    actorService.onRuleStateChange(entry.getValue().getTenantId(), entry.getKey(), ComponentLifecycleEvent.SUSPENDED);
                }
            }

            if(!activatedComputations.isEmpty()) {
                for(Map.Entry<ComputationJobId, ComputationJob> entry: activatedComputations.entrySet()){
                    computationJobService.suspendComputationJobById(entry.getKey());
                    actorService.onComputationJobStateChange(entry.getValue().getTenantId(), entry.getValue().getComputationId(), entry.getValue().getId(), ComponentLifecycleEvent.SUSPENDED);
                }
            }
            throw e;
        }
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
    @RequestMapping(value = "/application/{applicationId}/suspend", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void suspendApplicationById(@PathVariable("applicationId") String strApplicationId) throws TempusException {
        checkParameter("applicationId", strApplicationId);
        try {
            ApplicationId applicationId = new ApplicationId(toUUID(strApplicationId));
            Application application =  checkApplicationId(applicationId);
            suspendRules(application.getRules());
            suspendComputations(application.getComputationJobIdSet());
            applicationService.suspendApplicationById(applicationId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    private void suspendComputations(Set<ComputationJobId> computationJobIds) throws TempusException {
        if(computationJobIds.size() > 1 || !Iterables.getOnlyElement(computationJobIds).getId().equals(NULL_UUID)) {
            for(ComputationJobId computationJobId: computationJobIds) {
                ComputationJob computationJob = checkComputationJob(computationJobService.findComputationJobById(computationJobId));
                computationJobService.suspendComputationJobById(computationJobId);
                actorService.onComputationJobStateChange(computationJob.getTenantId(), computationJob.getComputationId(), computationJob.getId(), ComponentLifecycleEvent.SUSPENDED);
            }
        }
    }

    private void suspendRules(Set<RuleId> ruleIds) throws TempusException {
        if(ruleIds.size() > 1 || !Iterables.getOnlyElement(ruleIds).getId().equals(NULL_UUID)) {
            for(RuleId ruleId: ruleIds) {
                RuleMetaData rule = checkRule(ruleService.findRuleById(ruleId));
                ruleService.suspendRuleById(ruleId);
                actorService.onRuleStateChange(rule.getTenantId(), rule.getId(), ComponentLifecycleEvent.SUSPENDED);
            }
        }
    }

}
