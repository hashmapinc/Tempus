/**
 * Copyright © 2016-2017 The Thingsboard Authors
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
package org.thingsboard.server.dao.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Iterables;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thingsboard.server.common.data.Application;
import org.thingsboard.server.common.data.Customer;
import org.thingsboard.server.common.data.Dashboard;
import org.thingsboard.server.common.data.Tenant;
import org.thingsboard.server.common.data.computation.ComputationJob;
import org.thingsboard.server.common.data.id.*;
import org.thingsboard.server.common.data.page.TextPageData;
import org.thingsboard.server.common.data.page.TextPageLink;
import org.thingsboard.server.common.data.rule.RuleMetaData;
import org.thingsboard.server.dao.computations.ComputationJobDao;
import org.thingsboard.server.dao.customer.CustomerDao;
import org.thingsboard.server.dao.dashboard.DashboardDao;
import org.thingsboard.server.dao.entity.AbstractEntityService;
import org.thingsboard.server.dao.exception.DataValidationException;
import org.thingsboard.server.dao.exception.IncorrectParameterException;
import org.thingsboard.server.dao.rule.RuleDao;
import org.thingsboard.server.dao.service.DataValidator;
import org.thingsboard.server.dao.tenant.TenantDao;

import java.util.*;
import java.util.stream.Collectors;

import static org.thingsboard.server.dao.model.ModelConstants.NULL_DEVICE_TYPE;
import static org.thingsboard.server.dao.model.ModelConstants.NULL_UUID;
import static org.thingsboard.server.dao.service.Validator.validateId;
import static org.thingsboard.server.dao.service.Validator.validatePageLink;

@Service
@Slf4j
public class ApplicationServiceImpl extends AbstractEntityService implements ApplicationService{

    @Autowired
    private ApplicationDao applicationDao;

    @Autowired
    private TenantDao tenantDao;

    @Autowired
    private CustomerDao customerDao;

    @Autowired
    private DashboardDao dashboardDao;

    @Autowired
    private RuleDao ruleDao;

    @Autowired
    private ComputationJobDao computationJobDao;

    @Override
    public Application saveApplication(Application application) {
        log.trace("Executing saveApplication [{}]", application);
        applicationValidator.validate(application);
        return applicationDao.save(application);
    }

    @Override
    public Application findApplicationById(ApplicationId applicationId) {
        log.trace("Executing findApplicationById [{}]", applicationId);
        validateId(applicationId, "Incorrect applicationId " + applicationId);
        return applicationDao.findById(applicationId.getId());
    }

    @Override
    public void deleteApplication(ApplicationId applicationId) {
        log.trace("Executing deleteApplication [{}]", applicationId);
        validateId(applicationId, "Incorrect applicationId " + applicationId);

        applicationDao.removeById(applicationId.getId());
    }


    @Override
    public TextPageData<Application> findApplicationsByTenantId(TenantId tenantId, TextPageLink pageLink) {
        log.trace("Executing findApplicationsByTenantId, tenantId [{}], pageLink [{}]", tenantId, pageLink);
        validateId(tenantId, "Incorrect tenantId " + tenantId);
        validatePageLink(pageLink, "Incorrect page link " + pageLink);
        List<Application> applications = applicationDao.findApplicationsByTenantId(tenantId.getId(), pageLink);
        return new TextPageData<>(applications, pageLink);
    }

    @Override
    public List<Application> findApplicationsByDeviceType(TenantId tenantId, String deviceType){
        log.trace("Executing findApplicationsByDeviceType,  tenantId [{}], device Type [{}]", tenantId, deviceType);
        validateId(tenantId, "Incorrect tenantId " + tenantId);
        return applicationDao.findApplicationByDeviceType(tenantId.getId(), deviceType);
    }

    @Override
    public List<String> findApplicationByRuleId(TenantId tenantId, RuleId ruleId) {
        log.trace("Executing findApplicationByRuleId,  tenantId [{}], ruleId [{}]", tenantId, ruleId);
        validateId(tenantId, "Incorrect tenantId " + tenantId);
        validateId(ruleId, "Incorrect ruleId " + ruleId);
        return applicationDao.findApplicationByRuleId(tenantId.getId(), ruleId.getId()).stream().map(Application::getName).collect(Collectors.toList());
    }

    @Override
    public List<String> findApplicationByDashboardId(TenantId tenantId, DashboardId dashboardId){
        log.trace("Executing findApplicationByDashboardId,  tenantId [{}], dashboardId [{}]", tenantId, dashboardId);
        validateId(tenantId, "Incorrect tenantId " + tenantId);
        validateId(dashboardId, "Incorrect dashboardId " + dashboardId);
        return applicationDao.findApplicationsByDashboardId(tenantId.getId(), dashboardId.getId()).stream().map(Application::getName).collect(Collectors.toList());
    }

    @Override
    public Application assignApplicationToCustomer(ApplicationId applicationId, CustomerId customerId) {
        Application application = findApplicationById(applicationId);
        application.setCustomerId(customerId);
        return saveApplication(application);
    }

    @Override
    public Application unassignApplicationFromCustomer(ApplicationId applicationId) {
        Application application = findApplicationById(applicationId);
        application.setCustomerId(null);
        return saveApplication(application);
    }

    @Override
    public Application assignDashboardToApplication(ApplicationId applicationId, DashboardId dashboardId, String dashboardType) {
        Application application = findApplicationById(applicationId);
        if(dashboardType.equals("mini")) {
            application.setMiniDashboardId(dashboardId);
        } else if(dashboardType.equals("main")) {
            application.setDashboardId(dashboardId);
        } else {
            throw new IncorrectParameterException("Incorrect dashboard type: "+dashboardType);
        }

        return saveApplication(application);
    }

    @Override
    public Application unassignDashboardFromApplication(ApplicationId applicationId, String dashboardType) {
        Application application = findApplicationById(applicationId);
        if(dashboardType.equals("mini")) {
            application.setMiniDashboardId(null);
        } else if(dashboardType.equals("main")) {
            application.setDashboardId(null);
        } else {
            throw new IncorrectParameterException("Incorrect dashboard type: "+dashboardType);
        }

        return saveApplication(application);
    }


    @Override
    public Application unassignRulesToApplication(ApplicationId applicationId, Set<RuleId> ruleIdSet) {
        Application application = findApplicationById(applicationId);
        application.getRules().removeAll(ruleIdSet);
        for(RuleId ruleId: application.getRules()) {
            RuleMetaData ruleMetaData = ruleDao.findById(ruleId);
            application.setDeviceTypes(getDeviceTypesfromFiltersJson(ruleMetaData.getFilters()));
        }
        return saveApplication(application);
    }

    @Override
    public Application assignRulesToApplication(ApplicationId applicationId, Set<RuleId> ruleIdSet) {
        Application application = findApplicationById(applicationId);
        for(RuleId ruleId: ruleIdSet) {
            RuleMetaData ruleMetaData = ruleDao.findById(ruleId);
            application.addDeviceTypes(getDeviceTypesfromFiltersJson(ruleMetaData.getFilters()));
        }
        if(application.getRules().contains(new RuleId(NULL_UUID))) {
            application.getRules().remove(new RuleId(NULL_UUID));
            application.getDeviceTypes().remove(NULL_DEVICE_TYPE);
        }
        application.addRules(ruleIdSet);
        return saveApplication(application);
    }

    @Override
    public Application assignComputationJobsToApplication(ApplicationId applicationId, Set<ComputationJobId> computationJobIds) {
        Application application = findApplicationById(applicationId);
        if(application.getComputationJobIdSet().contains(new ComputationJobId(NULL_UUID))) {
            application.getComputationJobIdSet().remove(new ComputationJobId(NULL_UUID));
        }
        application.addComputationJobs(computationJobIds);
        return saveApplication(application);
    }

    @Override
    public Application unassignComputationJobsToApplication(ApplicationId applicationId, Set<ComputationJobId> computationJobIds) {
        Application application = findApplicationById(applicationId);
        application.getComputationJobIdSet().removeAll(computationJobIds);
        return saveApplication(application);
    }

    @Override
    public List<String> findApplicationByComputationJobId(TenantId tenantId, ComputationJobId computationJobId) {
        log.trace("Executing findApplicationByComputationJobId,  tenantId [{}], computationJobId [{}]", tenantId, computationJobId);
        validateId(tenantId, "Incorrect tenantId " + tenantId);
        validateId(computationJobId, "Incorrect computationJobId " + computationJobId);
        return applicationDao.findApplicationByComputationJobId(tenantId.getId(), computationJobId.getId()).stream().map(Application::getName).collect(Collectors.toList());
    }

    @Override
    public void updateApplicationOnComputationJobDelete(ComputationJobId computationJobId, TenantId tenantId) {
        log.trace("Executing updateApplicationOnComputationJobDelete,  tenantId [{}], computationJobId [{}]", tenantId, computationJobId);
        List<Application> applications = applicationDao.findApplicationByComputationJobId(tenantId.getId(), computationJobId.getId());
        if(!applications.isEmpty()) {
            for(Application a: applications) {
                a.getComputationJobIdSet().remove(computationJobId);
                saveApplication(a);
            }
        }
    }

    @Override
    public void updateApplicationOnComputationDelete(ComputationId computationId, TenantId tenantId) {
        log.trace("Executing updateApplicationOnComputationDelete,  tenantId [{}], computationId [{}]", tenantId, computationId);
        List<ComputationJob> computationJobs = computationJobDao.findByComputationId(computationId);
        if(computationJobs !=null && !computationJobs.isEmpty()) {
            for (ComputationJob computationJob: computationJobs) {
                updateApplicationOnComputationJobDelete(computationJob.getId(), tenantId);
            }
        }
    }

    private Set<String> getDeviceTypesfromFiltersJson(JsonNode filters){
        Set<String> deviceTypes = new HashSet<>();
        Iterator<JsonNode> configurations = filters.elements();
        while (configurations.hasNext()) {
            JsonNode configuration = configurations.next();
            for(JsonNode jsonNode: configuration.findValues("deviceTypes")) {
                java.util.List<String> name = jsonNode.findValues("name").stream().map(JsonNode::asText).collect(Collectors.toList());
                deviceTypes.addAll(name);
            }
        }
        return deviceTypes;
    }

    @Override
    public void updateApplicationOnRuleDelete(RuleId ruleId, TenantId tenantId) {
        log.trace("Executing updateApplicationOnRuleDelete,  tenantId [{}], ruleId [{}]", tenantId, ruleId);
        List<Application> applications = applicationDao.findApplicationByRuleId(tenantId.getId(), ruleId.getId());
        if(!applications.isEmpty()) {
            for(Application a: applications) {
                a.getRules().remove(ruleId);
                saveApplication(a);
            }
        }
    }

    @Override
    public void updateApplicationOnDashboardDelete(DashboardId dashboardId, TenantId tenantId) {
        log.trace("Executing updateApplicationOnDashboardDelete,  tenantId [{}], dashboardId [{}]", tenantId, dashboardId);
        List<Application> applications = applicationDao.findApplicationsByDashboardId(tenantId.getId(), dashboardId.getId());
        if(!applications.isEmpty()) {
            for(Application a: applications) {
                if(a.getMiniDashboardId().equals(dashboardId)) {
                    a.setMiniDashboardId(null);
                } else {
                    a.setDashboardId(null);
                }
                saveApplication(a);
            }
        }
    }


    private DataValidator<Application> applicationValidator =
            new DataValidator<Application>() {

                @Override
                protected void validateCreate(Application application) {
                    applicationDao.findApplicationByTenantIdAndName(application.getTenantId().getId(), application.getName()).ifPresent(
                            d -> {
                                throw new DataValidationException("Application with such name already exists!");
                            }
                    );
                }

                @Override
                protected void validateUpdate(Application application) {
                    applicationDao.findApplicationByTenantIdAndName(application.getTenantId().getId(), application.getName()).ifPresent(
                            d -> {
                                if (!d.getUuidId().equals(application.getUuidId())) {
                                    throw new DataValidationException("Application with such name already exists!");
                                }
                            }
                    );
                }

                @Override
                protected void validateDataImpl(Application application) {
                    Boolean isValid = true;
                    if (StringUtils.isEmpty(application.getName())) {
                        throw new DataValidationException("Application name should be specified!");
                    }
                    if (application.getTenantId() == null) {
                        throw new DataValidationException("Application should be assigned to tenant!");
                    } else {
                        Tenant tenant = tenantDao.findById(application.getTenantId().getId());
                        if (tenant == null) {
                            throw new DataValidationException("Application is referencing to non-existent tenant!");
                        }
                    }
                    if (application.getCustomerId() == null) {
                        application.setCustomerId(new CustomerId(NULL_UUID));
                    } else if (!application.getCustomerId().getId().equals(NULL_UUID)) {
                        Customer customer = customerDao.findById(application.getCustomerId().getId());
                        if (customer == null) {
                            throw new DataValidationException("Can't assign application to non-existent customer!");
                        }
                        if (!customer.getTenantId().getId().equals(application.getTenantId().getId())) {
                            throw new DataValidationException("Can't assign application to customer from different tenant!");
                        }
                    }

                    if(application.getDashboardId() == null) {
                        application.setDashboardId(new DashboardId(NULL_UUID));
                    } else if(!application.getDashboardId().getId().equals(NULL_UUID)) {
                        Dashboard dashboard = dashboardDao.findById(application.getDashboardId().getId());
                        if(dashboard == null) {
                            isValid =false;
                        }
                    }

                    if(application.getRules() == null || application.getRules().isEmpty()) {
                        application.setRules(new HashSet<>(Arrays.asList(new RuleId(NULL_UUID))));
                        isValid = false;
                    } else if(application.getRules().size() > 1 || !Iterables.getOnlyElement(application.getRules()).getId().equals(NULL_UUID)) {
                        for(RuleId ruleId: application.getRules()) {
                            RuleMetaData ruleMetaData = ruleDao.findById(ruleId);
                            if(ruleMetaData == null) {
                                throw new DataValidationException("Application is referencing to non-existent rule!");
                            }
                        }
                    } else {
                        isValid = false;
                    }

                    if(application.getComputationJobIdSet() == null || application.getComputationJobIdSet().isEmpty()) {
                        application.setComputationJobIdSet(new HashSet<>(Arrays.asList(new ComputationJobId(NULL_UUID))));
                        isValid = false;
                    } else if(application.getComputationJobIdSet().size() > 1 || !Iterables.getOnlyElement(application.getComputationJobIdSet()).getId().equals(NULL_UUID)) {
                        for(ComputationJobId computationJobId: application.getComputationJobIdSet()) {
                            ComputationJob computationJob = computationJobDao.findById(computationJobId);
                            if(computationJob == null) {
                                throw new DataValidationException("Application is referencing to non-existent computation!");
                            }
                        }
                    } else {
                        isValid = false;
                    }

                    if(application.getDeviceTypes() == null || application.getDeviceTypes().isEmpty()) {
                        application.setDeviceTypes(new HashSet<>(Arrays.asList(NULL_DEVICE_TYPE)));
                    }

                    if(application.getMiniDashboardId() == null) {
                        isValid = false;
                        application.setMiniDashboardId(new DashboardId(NULL_UUID));
                    } else if(!application.getMiniDashboardId().getId().equals(NULL_UUID)) {
                        Dashboard miniDashboard = dashboardDao.findById(application.getMiniDashboardId().getId());
                        if(miniDashboard == null) {
                            throw new DataValidationException("Application is referencing to non-existent dashboard!");
                        }
                    } else {
                        isValid = false;
                    }
                    application.setIsValid(isValid);
                }
            };
}
