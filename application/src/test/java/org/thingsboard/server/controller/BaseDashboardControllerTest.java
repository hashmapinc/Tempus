/**
 * Copyright © 2016-2018 Hashmap, Inc
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
package org.thingsboard.server.controller;

import static org.hamcrest.Matchers.containsString;
import static org.thingsboard.server.dao.model.ModelConstants.NULL_UUID;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import java.io.IOException;
import java.util.*;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import com.datastax.driver.core.utils.UUIDs;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.thingsboard.server.common.data.*;
import org.thingsboard.server.common.data.computation.ComputationJob;
import org.thingsboard.server.common.data.computation.Computations;
import org.thingsboard.server.common.data.id.ComputationId;
import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.page.TextPageData;
import org.thingsboard.server.common.data.page.TextPageLink;

import org.thingsboard.server.common.data.plugin.PluginMetaData;
import org.thingsboard.server.common.data.rule.RuleMetaData;

import org.thingsboard.server.common.data.page.TimePageData;
import org.thingsboard.server.common.data.page.TimePageLink;

import org.thingsboard.server.common.data.security.Authority;
import org.thingsboard.server.dao.computations.ComputationsService;
import org.thingsboard.server.dao.model.ModelConstants;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import org.thingsboard.server.extensions.core.plugin.telemetry.TelemetryStoragePlugin;

public abstract class BaseDashboardControllerTest extends AbstractControllerTest {
    
    private IdComparator<DashboardInfo> idComparator = new IdComparator<>();
    private static final ObjectMapper mapper = new ObjectMapper();
    private Tenant savedTenant;
    private User tenantAdmin;
    private PluginMetaData tenantPlugin;

    @Autowired
    ComputationsService computationsService;
    
    @Before
    public void beforeTest() throws Exception {
        loginSysAdmin();
        
        Tenant tenant = new Tenant();
        tenant.setTitle("My tenant");
        savedTenant = doPost("/api/tenant", tenant, Tenant.class);
        Assert.assertNotNull(savedTenant);
        
        tenantAdmin = new User();
        tenantAdmin.setAuthority(Authority.TENANT_ADMIN);
        tenantAdmin.setTenantId(savedTenant.getId());
        tenantAdmin.setEmail("tenant2@thingsboard.org");
        tenantAdmin.setFirstName("Joe");
        tenantAdmin.setLastName("Downs");
        
        tenantAdmin = createUserAndLogin(tenantAdmin, "testPassword1");

        tenantPlugin = new PluginMetaData();
        tenantPlugin.setName("My plugin");
        tenantPlugin.setApiToken("myplugin");
        tenantPlugin.setConfiguration(mapper.readTree("{}"));
        tenantPlugin.setClazz(TelemetryStoragePlugin.class.getName());
        tenantPlugin = doPost("/api/plugin", tenantPlugin, PluginMetaData.class);
    }
    
    @After
    public void afterTest() throws Exception {
        loginSysAdmin();
        
        doDelete("/api/tenant/"+savedTenant.getId().getId().toString())
        .andExpect(status().isOk());
    }
    
    @Test
    public void testSaveDashboard() throws Exception {
        Dashboard dashboard = new Dashboard();
        dashboard.setTitle("My dashboard");
        Dashboard savedDashboard = doPost("/api/dashboard", dashboard, Dashboard.class);
        
        Assert.assertNotNull(savedDashboard);
        Assert.assertNotNull(savedDashboard.getId());
        Assert.assertTrue(savedDashboard.getCreatedTime() > 0);
        Assert.assertEquals(savedTenant.getId(), savedDashboard.getTenantId());
        Assert.assertEquals(dashboard.getTitle(), savedDashboard.getTitle());
        
        savedDashboard.setTitle("My new dashboard");
        doPost("/api/dashboard", savedDashboard, Dashboard.class);
        
        Dashboard foundDashboard = doGet("/api/dashboard/" + savedDashboard.getId().getId().toString(), Dashboard.class);
        Assert.assertEquals(foundDashboard.getTitle(), savedDashboard.getTitle());
    }
    
    @Test
    public void testFindDashboardById() throws Exception {
        Dashboard dashboard = new Dashboard();
        dashboard.setTitle("My dashboard");
        Dashboard savedDashboard = doPost("/api/dashboard", dashboard, Dashboard.class);
        Dashboard foundDashboard = doGet("/api/dashboard/" + savedDashboard.getId().getId().toString(), Dashboard.class);
        Assert.assertNotNull(foundDashboard);
        Assert.assertEquals(savedDashboard, foundDashboard);
    }

    @Test
    public void testDeleteDashboardAndUpdateApplication() throws Exception {
        Dashboard dashboard = new Dashboard();
        dashboard.setTitle("My dashboard");
        Dashboard savedDashboard = doPost("/api/dashboard", dashboard, Dashboard.class);

        Application application = new Application();
        application.setName("My Application");
        application.setDeviceTypes(mapper.readTree("{\"deviceTypes\":[{\"name\":\"DT1\"}]}"));
        application.setAdditionalInfo(mapper.readTree("{\n" +
                "\" additionalInfo\": {\n" +
                "\"description\": \"string\"\n" +
                "}\n" +
                "}"));

        Application savedApplication = doPost("/api/application", application, Application.class);


        Computations savedComputations = saveComputation();
        ComputationJob computationJob1 = new ComputationJob();
        computationJob1.setName("Computation Job 1");
        computationJob1.setJobId("0123");
        ComputationJob savedComputationJob1 = doPost("/api/computations/"+savedComputations.getId().getId().toString()+"/jobs", computationJob1, ComputationJob.class);
        ApplicationFieldsWrapper applicationComputationJosWrapper = new ApplicationFieldsWrapper();
        applicationComputationJosWrapper.setApplicationId(savedApplication.getId().getId().toString());
        applicationComputationJosWrapper.setFields(new HashSet<>(Arrays.asList(savedComputationJob1.getId().toString())));
        doPostWithDifferentResponse("/api/app/assignComputationJobs", applicationComputationJosWrapper, Application.class);


        RuleMetaData rule = createRuleMetaData(tenantPlugin);
        RuleMetaData savedRule = doPost("/api/rule", rule, RuleMetaData.class);
        RuleMetaData foundRule = doGet("/api/rule/" + savedRule.getId().getId().toString(), RuleMetaData.class);
        Assert.assertNotNull(foundRule);
        ApplicationFieldsWrapper applicationRulesWrapper = new ApplicationFieldsWrapper();
        applicationRulesWrapper.setApplicationId(savedApplication.getId().getId().toString());
        applicationRulesWrapper.setFields(new HashSet<>(Arrays.asList(savedRule.getId().getId().toString())));
        doPostWithDifferentResponse("/api/app/assignRules", applicationRulesWrapper, Application.class);

        String dashboardType = "mini";
        Application assignedApplication = doPost("/api/dashboard/"+dashboardType+"/"+savedDashboard.getId().getId().toString()
                +"/application/"+savedApplication.getId().getId().toString(), Application.class);
        Assert.assertEquals(savedDashboard.getId(), assignedApplication.getMiniDashboardId());
        Assert.assertTrue(assignedApplication.getIsValid());

        doDelete("/api/dashboard/"+savedDashboard.getId().getId().toString())
                .andExpect(status().isOk());

        doGet("/api/dashboard/"+savedDashboard.getId().getId().toString())
                .andExpect(status().isNotFound());

        Thread.sleep(10000);

        Application foundApplication = doGet("/api/application/" + savedApplication.getId().getId().toString(), Application.class);
        Assert.assertFalse(foundApplication.getIsValid());
    }

    @Test
    public void testDeleteDashboard() throws Exception {
        Dashboard dashboard = new Dashboard();
        dashboard.setTitle("My dashboard");
        Dashboard savedDashboard = doPost("/api/dashboard", dashboard, Dashboard.class);
        
        doDelete("/api/dashboard/"+savedDashboard.getId().getId().toString())
        .andExpect(status().isOk());

        doGet("/api/dashboard/"+savedDashboard.getId().getId().toString())
        .andExpect(status().isNotFound());
    }
    
    @Test
    public void testSaveDashboardWithEmptyTitle() throws Exception {
        Dashboard dashboard = new Dashboard();
        doPost("/api/dashboard", dashboard)
        .andExpect(status().isBadRequest())
        .andExpect(statusReason(containsString("Dashboard title should be specified")));
    }
    
    @Test
    public void testAssignUnassignDashboardToCustomer() throws Exception {
        Dashboard dashboard = new Dashboard();
        dashboard.setTitle("My dashboard");
        Dashboard savedDashboard = doPost("/api/dashboard", dashboard, Dashboard.class);
        
        Customer customer = new Customer();
        customer.setTitle("My customer");
        Customer savedCustomer = doPost("/api/customer", customer, Customer.class);
        
        Dashboard assignedDashboard = doPost("/api/customer/" + savedCustomer.getId().getId().toString() 
                + "/dashboard/" + savedDashboard.getId().getId().toString(), Dashboard.class);

        Assert.assertTrue(assignedDashboard.getAssignedCustomers().contains(savedCustomer.toShortCustomerInfo()));

        Dashboard foundDashboard = doGet("/api/dashboard/" + savedDashboard.getId().getId().toString(), Dashboard.class);
        Assert.assertTrue(foundDashboard.getAssignedCustomers().contains(savedCustomer.toShortCustomerInfo()));

        Dashboard unassignedDashboard = 
                doDelete("/api/customer/"+savedCustomer.getId().getId().toString()+"/dashboard/" + savedDashboard.getId().getId().toString(), Dashboard.class);

        Assert.assertTrue(unassignedDashboard.getAssignedCustomers() == null || unassignedDashboard.getAssignedCustomers().isEmpty());

        foundDashboard = doGet("/api/dashboard/" + savedDashboard.getId().getId().toString(), Dashboard.class);

        Assert.assertTrue(foundDashboard.getAssignedCustomers() == null || foundDashboard.getAssignedCustomers().isEmpty());
    }
    
    @Test
    public void testAssignDashboardToNonExistentCustomer() throws Exception {
        Dashboard dashboard = new Dashboard();
        dashboard.setTitle("My dashboard");
        Dashboard savedDashboard = doPost("/api/dashboard", dashboard, Dashboard.class);
        
        doPost("/api/customer/" + UUIDs.timeBased().toString()
                + "/dashboard/" + savedDashboard.getId().getId().toString())
        .andExpect(status().isNotFound());
    }
    
    @Test
    public void testAssignDashboardToCustomerFromDifferentTenant() throws Exception {
        loginSysAdmin();
        
        Tenant tenant2 = new Tenant();
        tenant2.setTitle("Different tenant");
        Tenant savedTenant2 = doPost("/api/tenant", tenant2, Tenant.class);
        Assert.assertNotNull(savedTenant2);

        User tenantAdmin2 = new User();
        tenantAdmin2.setAuthority(Authority.TENANT_ADMIN);
        tenantAdmin2.setTenantId(savedTenant2.getId());
        tenantAdmin2.setEmail("tenant3@thingsboard.org");
        tenantAdmin2.setFirstName("Joe");
        tenantAdmin2.setLastName("Downs");
        
        tenantAdmin2 = createUserAndLogin(tenantAdmin2, "testPassword1");
        
        Customer customer = new Customer();
        customer.setTitle("Different customer");
        Customer savedCustomer = doPost("/api/customer", customer, Customer.class);

        login(tenantAdmin.getEmail(), "testPassword1");
        
        Dashboard dashboard = new Dashboard();
        dashboard.setTitle("My dashboard");
        Dashboard savedDashboard = doPost("/api/dashboard", dashboard, Dashboard.class);
        
        doPost("/api/customer/" + savedCustomer.getId().getId().toString()
                + "/dashboard/" + savedDashboard.getId().getId().toString())
        .andExpect(status().isForbidden());
        
        loginSysAdmin();
        
        doDelete("/api/tenant/"+savedTenant2.getId().getId().toString())
        .andExpect(status().isOk());
    }

    @Test
    public void testFindTenantDashboards() throws Exception {
        List<DashboardInfo> dashboards = new ArrayList<>();
        for (int i=0;i<173;i++) {
            Dashboard dashboard = new Dashboard();
            dashboard.setTitle("Dashboard"+i);
            dashboards.add(new DashboardInfo(doPost("/api/dashboard", dashboard, Dashboard.class)));
        }
        List<DashboardInfo> loadedDashboards = new ArrayList<>();
        TextPageLink pageLink = new TextPageLink(24);
        TextPageData<DashboardInfo> pageData = null;
        do {
            pageData = doGetTypedWithPageLink("/api/tenant/dashboards?", 
                    new TypeReference<TextPageData<DashboardInfo>>(){}, pageLink);
            loadedDashboards.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());
        
        Collections.sort(dashboards, idComparator);
        Collections.sort(loadedDashboards, idComparator);
        
        Assert.assertEquals(dashboards, loadedDashboards);
    }
    
    @Test
    public void testFindTenantDashboardsByTitle() throws Exception {
        String title1 = "Dashboard title 1";
        List<DashboardInfo> dashboardsTitle1 = new ArrayList<>();
        for (int i=0;i<134;i++) {
            Dashboard dashboard = new Dashboard();
            String suffix = RandomStringUtils.randomAlphanumeric((int)(Math.random()*15));
            String title = title1+suffix;
            title = i % 2 == 0 ? title.toLowerCase() : title.toUpperCase();
            dashboard.setTitle(title);
            dashboardsTitle1.add(new DashboardInfo(doPost("/api/dashboard", dashboard, Dashboard.class)));
        }
        String title2 = "Dashboard title 2";
        List<DashboardInfo> dashboardsTitle2 = new ArrayList<>();
        for (int i=0;i<112;i++) {
            Dashboard dashboard = new Dashboard();
            String suffix = RandomStringUtils.randomAlphanumeric((int)(Math.random()*15));
            String title = title2+suffix;
            title = i % 2 == 0 ? title.toLowerCase() : title.toUpperCase();
            dashboard.setTitle(title);
            dashboardsTitle2.add(new DashboardInfo(doPost("/api/dashboard", dashboard, Dashboard.class)));
        }
        
        List<DashboardInfo> loadedDashboardsTitle1 = new ArrayList<>();
        TextPageLink pageLink = new TextPageLink(15, title1);
        TextPageData<DashboardInfo> pageData = null;
        do {
            pageData = doGetTypedWithPageLink("/api/tenant/dashboards?", 
                    new TypeReference<TextPageData<DashboardInfo>>(){}, pageLink);
            loadedDashboardsTitle1.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());
        
        Collections.sort(dashboardsTitle1, idComparator);
        Collections.sort(loadedDashboardsTitle1, idComparator);
        
        Assert.assertEquals(dashboardsTitle1, loadedDashboardsTitle1);
        
        List<DashboardInfo> loadedDashboardsTitle2 = new ArrayList<>();
        pageLink = new TextPageLink(4, title2);
        do {
            pageData = doGetTypedWithPageLink("/api/tenant/dashboards?", 
                    new TypeReference<TextPageData<DashboardInfo>>(){}, pageLink);
            loadedDashboardsTitle2.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());

        Collections.sort(dashboardsTitle2, idComparator);
        Collections.sort(loadedDashboardsTitle2, idComparator);
        
        Assert.assertEquals(dashboardsTitle2, loadedDashboardsTitle2);
        
        for (DashboardInfo dashboard : loadedDashboardsTitle1) {
            doDelete("/api/dashboard/"+dashboard.getId().getId().toString())
            .andExpect(status().isOk());
        }
        
        pageLink = new TextPageLink(4, title1);
        pageData = doGetTypedWithPageLink("/api/tenant/dashboards?", 
                new TypeReference<TextPageData<DashboardInfo>>(){}, pageLink);
        Assert.assertFalse(pageData.hasNext());
        Assert.assertEquals(0, pageData.getData().size());
        
        for (DashboardInfo dashboard : loadedDashboardsTitle2) {
            doDelete("/api/dashboard/"+dashboard.getId().getId().toString())
            .andExpect(status().isOk());
        }
        
        pageLink = new TextPageLink(4, title2);
        pageData = doGetTypedWithPageLink("/api/tenant/dashboards?", 
                new TypeReference<TextPageData<DashboardInfo>>(){}, pageLink);
        Assert.assertFalse(pageData.hasNext());
        Assert.assertEquals(0, pageData.getData().size());
    }
    
    @Test
    public void testFindCustomerDashboards() throws Exception {
        Customer customer = new Customer();
        customer.setTitle("Test customer");
        customer = doPost("/api/customer", customer, Customer.class);
        CustomerId customerId = customer.getId();
        
        List<DashboardInfo> dashboards = new ArrayList<>();
        for (int i=0;i<173;i++) {
            Dashboard dashboard = new Dashboard();
            dashboard.setTitle("Dashboard"+i);
            dashboard = doPost("/api/dashboard", dashboard, Dashboard.class);
            dashboards.add(new DashboardInfo(doPost("/api/customer/" + customerId.getId().toString()
                            + "/dashboard/" + dashboard.getId().getId().toString(), Dashboard.class)));
        }
        
        List<DashboardInfo> loadedDashboards = new ArrayList<>();
        TimePageLink pageLink = new TimePageLink(21);
        TimePageData<DashboardInfo> pageData = null;
        do {
            pageData = doGetTypedWithTimePageLink("/api/customer/" + customerId.getId().toString() + "/dashboards?",
                    new TypeReference<TimePageData<DashboardInfo>>(){}, pageLink);
            loadedDashboards.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());
        
        Collections.sort(dashboards, idComparator);
        Collections.sort(loadedDashboards, idComparator);
        
        Assert.assertEquals(dashboards, loadedDashboards);
    }

    private Computations saveComputation() {
        Computations computations = new Computations();
        computations.setName("Computation");
        computations.setId(new ComputationId(UUIDs.timeBased()));
        computations.setJarPath("/Some/Jar/path");
        computations.setTenantId(savedTenant.getId());
        computations.setJarName("SomeJar");
        computations.setMainClass("MainClass");
        //computations.setJsonDescriptor();
        computations.setArgsformat("argsFormat");
        computations.setArgsType("ArgsType");
        return computationsService.save(computations);
    }

    public static RuleMetaData createRuleMetaData(PluginMetaData plugin) throws IOException {
        RuleMetaData rule = new RuleMetaData();
        rule.setName("My Rule");
        rule.setPluginToken(plugin.getApiToken());
        rule.setFilters(mapper.readTree("[{\"clazz\":\"org.thingsboard.server.extensions.core.filter.MsgTypeFilter\", " +
                "\"name\":\"TelemetryFilter\", " +
                "\"configuration\": {\"messageTypes\":[\"POST_TELEMETRY\",\"POST_ATTRIBUTES\",\"GET_ATTRIBUTES\"]}}]"));
        rule.setAction(mapper.readTree("{\"clazz\":\"org.thingsboard.server.extensions.core.action.telemetry.TelemetryPluginAction\", \"name\":\"TelemetryMsgConverterAction\", " +
                "\"configuration\":{\"timeUnit\":\"DAYS\", \"ttlValue\":1}}"));
        return rule;
    }

}
