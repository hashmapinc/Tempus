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
package com.hashmapinc.server.controller;

import com.hashmapinc.server.common.data.*;
import com.hashmapinc.server.common.data.audit.ActionType;
import com.hashmapinc.server.common.data.id.CustomerId;
import com.hashmapinc.server.common.data.id.DashboardId;
import com.hashmapinc.server.common.data.id.DataModelObjectId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.page.TextPageData;
import com.hashmapinc.server.common.data.page.TextPageLink;
import com.hashmapinc.server.common.data.page.TimePageData;
import com.hashmapinc.server.common.data.page.TimePageLink;
import com.hashmapinc.server.common.data.security.Authority;
import com.hashmapinc.server.dao.exception.DataValidationException;
import com.hashmapinc.server.exception.TempusException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api")
public class DashboardController extends BaseController {

    public static final String DASHBOARD_ID = "dashboardId";
    public static final String CUSTOMER_ID = "customerId";

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'CUSTOMER_USER')")
    @GetMapping(value = "/dashboard/serverTime")
    @ResponseBody
    public long getServerTime() {
        return System.currentTimeMillis();
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'CUSTOMER_USER')")
    @GetMapping(value = "/dashboard/info/{dashboardId}")
    @ResponseBody
    public DashboardInfo getDashboardInfoById(@PathVariable(DASHBOARD_ID) String strDashboardId) throws TempusException {
        checkParameter(DASHBOARD_ID, strDashboardId);
        try {
            DashboardId dashboardId = new DashboardId(toUUID(strDashboardId));
            return checkDashboardInfoId(dashboardId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @GetMapping(value = "/dashboard/{dashboardId}")
    @ResponseBody
    public Dashboard getDashboardById(@PathVariable(DASHBOARD_ID) String strDashboardId) throws TempusException {
        checkParameter(DASHBOARD_ID, strDashboardId);
        try {
            DashboardId dashboardId = new DashboardId(toUUID(strDashboardId));
            return checkDashboardId(dashboardId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN' , 'CUSTOMER_USER')")
    @PostMapping(value = "/dashboard")
    @ResponseBody
    public Dashboard saveDashboard(@RequestBody Dashboard dashboard) throws TempusException {
        try {
            User user = getCurrentUser();
            dashboard.setTenantId(user.getTenantId());

            Dashboard savedDashboard = checkNotNull(dashboardService.saveDashboard(dashboard));

            if((user).getAuthority().compareTo(Authority.CUSTOMER_USER) == 0) {
                savedDashboard = autoAssignedUserToDashboard(savedDashboard.getId(),user.getCustomerId());
            }

            logEntityAction(savedDashboard.getId(), savedDashboard,
                            null,
                            dashboard.getId() == null ? ActionType.ADDED : ActionType.UPDATED, null);

            return savedDashboard;
        } catch (Exception e) {
            logEntityAction(emptyId(EntityType.DASHBOARD), dashboard,
                            null, dashboard.getId() == null ? ActionType.ADDED : ActionType.UPDATED, e);

            throw handleException(e);
        }
    }

    private Dashboard autoAssignedUserToDashboard(DashboardId dashboardId,CustomerId customerId)throws TempusException {
        Dashboard savedDashboard;
        savedDashboard = checkNotNull(dashboardService.assignDashboardToCustomer(dashboardId, customerId));
        return savedDashboard;
    }


    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN')")
    @DeleteMapping(value = "/dashboard/{dashboardId}")
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteDashboard(@PathVariable(DASHBOARD_ID) String strDashboardId) throws TempusException {
        checkParameter(DASHBOARD_ID, strDashboardId);
        try {
            DashboardId dashboardId = new DashboardId(toUUID(strDashboardId));
            Dashboard dashboard = checkDashboardId(dashboardId);
            dashboardService.deleteDashboard(dashboardId);

            logEntityAction(dashboardId, dashboard,
                            null,
                            ActionType.DELETED, null, strDashboardId);

        } catch (Exception e) {

            logEntityAction(emptyId(EntityType.DASHBOARD),
                            null,
                            null,
                            ActionType.DELETED, e, strDashboardId);

            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @PostMapping(value = "/customer/{customerId}/dashboard/{dashboardId}")
    @ResponseBody
    public Dashboard assignDashboardToCustomer(@PathVariable(CUSTOMER_ID) String strCustomerId,
                                               @PathVariable(DASHBOARD_ID) String strDashboardId) throws TempusException {
        checkParameter(CUSTOMER_ID, strCustomerId);
        checkParameter(DASHBOARD_ID, strDashboardId);
        try {
            CustomerId customerId = new CustomerId(toUUID(strCustomerId));
            Customer customer = checkCustomerId(customerId);

            DashboardId dashboardId = new DashboardId(toUUID(strDashboardId));
            checkDashboardId(dashboardId);

            Dashboard savedDashboard = checkNotNull(dashboardService.assignDashboardToCustomer(dashboardId, customerId));

            logEntityAction(dashboardId, savedDashboard,
                            customerId,
                            ActionType.ASSIGNED_TO_CUSTOMER, null, strDashboardId, strCustomerId, customer.getName());


            return savedDashboard;
        } catch (Exception e) {

            logEntityAction(emptyId(EntityType.DASHBOARD), null,
                            null,
                            ActionType.ASSIGNED_TO_CUSTOMER, e, strDashboardId, strCustomerId);

            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN','CUSTOMER_USER')")
    @DeleteMapping(value = "/customer/{customerId}/dashboard/{dashboardId}")
    @ResponseBody
    public Dashboard unassignDashboardFromCustomer(@PathVariable(CUSTOMER_ID) String strCustomerId,
                                                   @PathVariable(DASHBOARD_ID) String strDashboardId) throws TempusException {
        checkParameter(CUSTOMER_ID, strCustomerId);
        checkParameter(DASHBOARD_ID, strDashboardId);
        try {
            CustomerId customerId = new CustomerId(toUUID(strCustomerId));
            Customer customer = checkCustomerId(customerId);
            DashboardId dashboardId = new DashboardId(toUUID(strDashboardId));
            Dashboard dashboard = checkDashboardId(dashboardId);

            Dashboard savedDashboard = checkNotNull(dashboardService.unassignDashboardFromCustomer(dashboardId, customerId));

            logEntityAction(dashboardId, dashboard,
                            customerId,
                            ActionType.UNASSIGNED_FROM_CUSTOMER, null, strDashboardId, customer.getId().toString(), customer.getName());

            return savedDashboard;
        } catch (Exception e) {

            logEntityAction(emptyId(EntityType.DASHBOARD), null,
                            null,
                            ActionType.UNASSIGNED_FROM_CUSTOMER, e, strDashboardId);

            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN')")
    @PostMapping(value = "/dashboard/{dashboardId}/customers")
    @ResponseBody
    public Dashboard updateDashboardCustomers(@PathVariable(DASHBOARD_ID) String strDashboardId,
                                              @RequestBody String[] strCustomerIds) throws TempusException {
        checkParameter(DASHBOARD_ID, strDashboardId);
        try {
            DashboardId dashboardId = new DashboardId(toUUID(strDashboardId));
            Dashboard dashboard = checkDashboardId(dashboardId);

            Set<CustomerId> customerIds = getCustomerIds(strCustomerIds);

            Set<CustomerId> addedCustomerIds = getNewlyAddedCustomerIds(dashboard, customerIds);

            Set<CustomerId> removedCustomerIds = getRemovedCustomerIds(dashboard, customerIds);

            if (addedCustomerIds.isEmpty() && removedCustomerIds.isEmpty()) {
                return dashboard;
            } else {
                Dashboard savedDashboard = null;
                for (CustomerId customerId : addedCustomerIds) {
                    savedDashboard = checkNotNull(dashboardService.assignDashboardToCustomer(dashboardId, customerId));
                    ShortCustomerInfo customerInfo = savedDashboard.getAssignedCustomerInfo(customerId);
                    logEntityAction(dashboardId, savedDashboard,
                                    customerId,
                                    ActionType.ASSIGNED_TO_CUSTOMER, null, strDashboardId, customerId.toString(), customerInfo.getTitle());
                }
                for (CustomerId customerId : removedCustomerIds) {
                    ShortCustomerInfo customerInfo = dashboard.getAssignedCustomerInfo(customerId);
                    savedDashboard = checkNotNull(dashboardService.unassignDashboardFromCustomer(dashboardId, customerId));
                    logEntityAction(dashboardId, dashboard,
                                    customerId,
                                    ActionType.UNASSIGNED_FROM_CUSTOMER, null, strDashboardId, customerId.toString(), customerInfo.getTitle());

                }
                return savedDashboard;
            }
        } catch (Exception e) {

            logEntityAction(emptyId(EntityType.DASHBOARD), null,
                            null,
                            ActionType.ASSIGNED_TO_CUSTOMER, e, strDashboardId);

            throw handleException(e);
        }
    }

    private Set<CustomerId> getRemovedCustomerIds(Dashboard dashboard, Set<CustomerId> customerIds) {
        Set<CustomerId> removedCustomerIds = new HashSet<>();
        Set<ShortCustomerInfo> assignedCustomers = dashboard.getAssignedCustomers();
        if (assignedCustomers != null) {
            for (ShortCustomerInfo customerInfo : assignedCustomers) {
                if (!customerIds.contains(customerInfo.getCustomerId())) {
                    removedCustomerIds.add(customerInfo.getCustomerId());
                }
            }
        }
        return removedCustomerIds;
    }

    private Set<CustomerId> getNewlyAddedCustomerIds(Dashboard dashboard, Set<CustomerId> customerIds) {
        Set<CustomerId> addedCustomerIds = new HashSet<>();

        for (CustomerId customerId : customerIds) {
            if (!dashboard.isAssignedToCustomer(customerId)) {
                addedCustomerIds.add(customerId);
            }
        }
        return addedCustomerIds;
    }

    private Set<CustomerId> getCustomerIds(@RequestBody String[] strCustomerIds) {
        Set<CustomerId> customerIds = new HashSet<>();
        if (strCustomerIds != null) {
            for (String strCustomerId : strCustomerIds) {
                customerIds.add(new CustomerId(toUUID(strCustomerId)));
            }
        }
        return customerIds;
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @PostMapping(value = "/dashboard/{dashboardId}/customers/add")
    @ResponseBody
    public Dashboard addDashboardCustomers(@PathVariable(DASHBOARD_ID) String strDashboardId,
                                           @RequestBody String[] strCustomerIds) throws TempusException {
        checkParameter(DASHBOARD_ID, strDashboardId);
        try {
            DashboardId dashboardId = new DashboardId(toUUID(strDashboardId));
            Dashboard dashboard = checkDashboardId(dashboardId);

            Set<CustomerId> customerIds = new HashSet<>();
            if (strCustomerIds != null) {
                for (String strCustomerId : strCustomerIds) {
                    CustomerId customerId = new CustomerId(toUUID(strCustomerId));
                    if (!dashboard.isAssignedToCustomer(customerId)) {
                        customerIds.add(customerId);
                    }
                }
            }

            if (customerIds.isEmpty()) {
                return dashboard;
            } else {
                Dashboard savedDashboard = null;
                for (CustomerId customerId : customerIds) {
                    savedDashboard = checkNotNull(dashboardService.assignDashboardToCustomer(dashboardId, customerId));
                    ShortCustomerInfo customerInfo = savedDashboard.getAssignedCustomerInfo(customerId);
                    logEntityAction(dashboardId, savedDashboard,
                                    customerId,
                                    ActionType.ASSIGNED_TO_CUSTOMER, null, strDashboardId, customerId.toString(), customerInfo.getTitle());
                }
                return savedDashboard;
            }
        } catch (Exception e) {

            logEntityAction(emptyId(EntityType.DASHBOARD), null,
                            null,
                            ActionType.ASSIGNED_TO_CUSTOMER, e, strDashboardId);

            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN')")
    @PostMapping(value = "/dashboard/{dashboardId}/customers/remove")
    @ResponseBody
    public Dashboard removeDashboardCustomers(@PathVariable(DASHBOARD_ID) String strDashboardId,
                                              @RequestBody String[] strCustomerIds) throws TempusException {
        checkParameter(DASHBOARD_ID, strDashboardId);
        try {
            DashboardId dashboardId = new DashboardId(toUUID(strDashboardId));
            Dashboard dashboard = checkDashboardId(dashboardId);

            Set<CustomerId> customerIds = new HashSet<>();
            if (strCustomerIds != null) {
                for (String strCustomerId : strCustomerIds) {
                    CustomerId customerId = new CustomerId(toUUID(strCustomerId));
                    if (dashboard.isAssignedToCustomer(customerId)) {
                        customerIds.add(customerId);
                    }
                }
            }

            if (customerIds.isEmpty()) {
                return dashboard;
            } else {
                Dashboard savedDashboard = null;
                for (CustomerId customerId : customerIds) {
                    ShortCustomerInfo customerInfo = dashboard.getAssignedCustomerInfo(customerId);
                    savedDashboard = checkNotNull(dashboardService.unassignDashboardFromCustomer(dashboardId, customerId));
                    logEntityAction(dashboardId, dashboard,
                                    customerId,
                                    ActionType.UNASSIGNED_FROM_CUSTOMER, null, strDashboardId, customerId.toString(), customerInfo.getTitle());

                }
                return savedDashboard;
            }
        } catch (Exception e) {

            logEntityAction(emptyId(EntityType.DASHBOARD), null,
                            null,
                            ActionType.UNASSIGNED_FROM_CUSTOMER, e, strDashboardId);

            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @PostMapping(value = "/customer/public/dashboard/{dashboardId}")
    @ResponseBody
    public Dashboard assignDashboardToPublicCustomer(@PathVariable(DASHBOARD_ID) String strDashboardId) throws TempusException {
        checkParameter(DASHBOARD_ID, strDashboardId);
        try {
            DashboardId dashboardId = new DashboardId(toUUID(strDashboardId));
            Dashboard dashboard = checkDashboardId(dashboardId);
            Customer publicCustomer = customerService.findOrCreatePublicCustomer(dashboard.getTenantId());
            Dashboard savedDashboard = checkNotNull(dashboardService.assignDashboardToCustomer(dashboardId, publicCustomer.getId()));

            logEntityAction(dashboardId, savedDashboard,
                            publicCustomer.getId(),
                            ActionType.ASSIGNED_TO_CUSTOMER, null, strDashboardId, publicCustomer.getId().toString(), publicCustomer.getName());

            return savedDashboard;
        } catch (Exception e) {

            logEntityAction(emptyId(EntityType.DASHBOARD), null,
                            null,
                            ActionType.ASSIGNED_TO_CUSTOMER, e, strDashboardId);

            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @DeleteMapping(value = "/customer/public/dashboard/{dashboardId}")
    @ResponseBody
    public Dashboard unassignDashboardFromPublicCustomer(@PathVariable(DASHBOARD_ID) String strDashboardId) throws TempusException {
        checkParameter(DASHBOARD_ID, strDashboardId);
        try {
            DashboardId dashboardId = new DashboardId(toUUID(strDashboardId));
            Dashboard dashboard = checkDashboardId(dashboardId);
            Customer publicCustomer = customerService.findOrCreatePublicCustomer(dashboard.getTenantId());

            Dashboard savedDashboard = checkNotNull(dashboardService.unassignDashboardFromCustomer(dashboardId, publicCustomer.getId()));

            logEntityAction(dashboardId, dashboard,
                            publicCustomer.getId(),
                            ActionType.UNASSIGNED_FROM_CUSTOMER, null, strDashboardId, publicCustomer.getId().toString(), publicCustomer.getName());

            return savedDashboard;
        } catch (Exception e) {

            logEntityAction(emptyId(EntityType.DASHBOARD), null,
                            null,
                            ActionType.UNASSIGNED_FROM_CUSTOMER, e, strDashboardId);

            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('SYS_ADMIN')")
    @GetMapping(value = "/tenant/{tenantId}/dashboards", params = { "limit" })
    @ResponseBody
    public TextPageData<DashboardInfo> getTenantDashboards(
            @PathVariable("tenantId") String strTenantId,
            @RequestParam int limit,
            @RequestParam(required = false) String textSearch,
            @RequestParam(required = false) String idOffset,
            @RequestParam(required = false) String textOffset) throws TempusException {
        try {
            TenantId tenantId = new TenantId(toUUID(strTenantId));
            checkTenantId(tenantId);
            TextPageLink pageLink = createPageLink(limit, textSearch, idOffset, textOffset);
            return checkNotNull(dashboardService.findDashboardsByTenantId(tenantId, pageLink));
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @GetMapping(value = "/tenant/dashboards", params = { "limit" })
    @ResponseBody
    public TextPageData<DashboardInfo> getTenantDashboards(
            @RequestParam int limit,
            @RequestParam(required = false) String textSearch,
            @RequestParam(required = false) String idOffset,
            @RequestParam(required = false) String textOffset) throws TempusException {
        try {
            TenantId tenantId = getCurrentUser().getTenantId();
            TextPageLink pageLink = createPageLink(limit, textSearch, idOffset, textOffset);
            return checkNotNull(dashboardService.findDashboardsByTenantId(tenantId, pageLink));
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @GetMapping(value = "/customer/{customerId}/dashboards", params = { "limit" })
    @ResponseBody
    public TimePageData<DashboardInfo> getCustomerDashboards(
            @PathVariable(CUSTOMER_ID) String strCustomerId,
            @RequestParam int limit,
            @RequestParam(required = false) Long startTime,
            @RequestParam(required = false) Long endTime,
            @RequestParam(required = false, defaultValue = "false") boolean ascOrder,
            @RequestParam(required = false) String offset) throws TempusException {
        checkParameter(CUSTOMER_ID, strCustomerId);
        try {
            TenantId tenantId = getCurrentUser().getTenantId();
            CustomerId customerId = new CustomerId(toUUID(strCustomerId));
            checkCustomerId(customerId);
            TimePageLink pageLink = createPageLink(limit, startTime, endTime, ascOrder, offset);
            return checkNotNull(dashboardService.findDashboardsByTenantIdAndCustomerId(tenantId, customerId, pageLink).get());
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN','CUSTOMER_USER')")
    @PostMapping(value = "/asset/dashboard/")
    @ResponseBody
    public RequestAssetDashboard saveAssetLandingDashboard(@RequestBody RequestAssetDashboard requestAssetDashboard) throws TempusException {
        try {
            AssetLandingDashboardInfo assetLandingDashboardInfo = requestAssetDashboard.getAssetLandingDashboardInfo();
            Dashboard dashboard = requestAssetDashboard.getDashboard();

            List<AssetLandingDashboardInfo> assetLandingDashboardInfos;
            assetLandingDashboardInfos = assetLandingDashboardService.findByDataModelObjectId(assetLandingDashboardInfo.getDataModelObjectId());

            if(assetLandingDashboardInfos.isEmpty()) {

                User user = getCurrentUser();
                dashboard.setTenantId(user.getTenantId());

                Dashboard savedDashboard = checkNotNull(dashboardService.saveDashboard(dashboard));

                if((user).getAuthority().compareTo(Authority.CUSTOMER_USER) == 0) {
                    savedDashboard = autoAssignedUserToDashboard(savedDashboard.getId(),user.getCustomerId());
                }

                assetLandingDashboardInfo.setDashboardId(savedDashboard.getId());

                AssetLandingDashboardInfo savedAssetLandingDashboardInfo = checkNotNull(assetLandingDashboardService.save(assetLandingDashboardInfo));

                return new RequestAssetDashboard(savedDashboard,savedAssetLandingDashboardInfo);
            }else {
                throw new DataValidationException("Asset landing page is already created for dataModelObject");
            }
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN','CUSTOMER_USER')")
    @GetMapping(value = "/asset/dashboard/{dashboardId}")
    @ResponseBody
    public AssetLandingDashboardInfo findAssetDashboardByDashboardId(@PathVariable(DASHBOARD_ID) String strDashboardId) throws TempusException {
        try {
            DashboardId dashboardId = new DashboardId(toUUID(strDashboardId));
            checkDashboardId(dashboardId);
            return checkNotNull(assetLandingDashboardService.findByDashboardId(dashboardId));
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN','CUSTOMER_USER')")
    @DeleteMapping(value = "/asset/dashboard/{dashboardId}")
    @ResponseBody
    public void deleteAssetDashboardByDashboardId(@PathVariable(DASHBOARD_ID) String strDashboardId) throws TempusException {
        try {
            DashboardId dashboardId = new DashboardId(toUUID(strDashboardId));
            checkDashboardId(dashboardId);
            assetLandingDashboardService.removeByDashboardId(dashboardId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN','CUSTOMER_USER')")
    @GetMapping(value = "/asset/dashboard/data-model-object/{dataModelObjectId}")
    @ResponseBody
    public List<AssetLandingDashboardInfo> findAssetLandingDashboardByDataModelObj(@PathVariable("dataModelObjectId") String strDataModelObjectId) throws TempusException {
        try {
            DataModelObjectId dataModelObjectId = new DataModelObjectId(toUUID(strDataModelObjectId));
            return assetLandingDashboardService.findByDataModelObjectId(dataModelObjectId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }
}
