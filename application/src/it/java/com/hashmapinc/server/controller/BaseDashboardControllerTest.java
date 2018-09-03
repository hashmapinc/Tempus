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

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import com.datastax.driver.core.utils.UUIDs;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hashmapinc.server.common.data.*;
import com.hashmapinc.server.common.data.datamodel.AttributeDefinition;
import com.hashmapinc.server.common.data.datamodel.DataModel;
import com.hashmapinc.server.common.data.datamodel.DataModelObject;
import com.hashmapinc.server.common.data.page.TextPageLink;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.hashmapinc.server.common.data.computation.Computations;
import com.hashmapinc.server.common.data.id.ComputationId;
import com.hashmapinc.server.common.data.id.CustomerId;
import com.hashmapinc.server.common.data.page.TextPageData;

import com.hashmapinc.server.common.data.plugin.PluginMetaData;
import com.hashmapinc.server.common.data.rule.RuleMetaData;

import com.hashmapinc.server.common.data.page.TimePageData;
import com.hashmapinc.server.common.data.page.TimePageLink;

import com.hashmapinc.server.common.data.security.Authority;
import com.hashmapinc.server.dao.computations.ComputationsService;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hashmapinc.server.extensions.core.plugin.telemetry.TelemetryStoragePlugin;

public abstract class BaseDashboardControllerTest extends AbstractControllerTest {
    
    private IdComparator<DashboardInfo> idComparator = new IdComparator<>();
    private static final ObjectMapper mapper = new ObjectMapper();
    private PluginMetaData tenantPlugin;

    @Autowired
    ComputationsService computationsService;
    
    @Before
    public void beforeTest() throws Exception {
        loginTenantAdmin();

        tenantPlugin = new PluginMetaData();
        tenantPlugin.setName("My plugin");
        tenantPlugin.setApiToken("myplugin");
        tenantPlugin.setConfiguration(mapper.readTree("{}"));
        tenantPlugin.setClazz(TelemetryStoragePlugin.class.getName());
        tenantPlugin = doPost("/api/plugin", tenantPlugin, PluginMetaData.class);

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
    public void testSaveDashboardByCustomer() throws Exception {

        loginCustomerUser();

        Dashboard dashboard = new Dashboard();
        dashboard.setTitle("My dashboard");
        Dashboard savedDashboard = doPost("/api/dashboard", dashboard, Dashboard.class);

        Assert.assertNotNull(savedDashboard);
        Assert.assertNotNull(savedDashboard.getId());
        Assert.assertTrue(savedDashboard.getCreatedTime() > 0);
        Assert.assertEquals(savedCustomer.getTenantId() , savedDashboard.getTenantId());
        Assert.assertEquals(dashboard.getTitle(), savedDashboard.getTitle());

        Assert.assertEquals(savedDashboard.getAssignedCustomerInfo(savedCustomer.getId()).getCustomerId(),savedCustomer.getId());

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
        tenantAdmin2.setEmail("tenant3@tempus.org");
        tenantAdmin2.setFirstName("Joe");
        tenantAdmin2.setLastName("Downs");

        stubUser(tenantAdmin2, "testPassword1");
        tenantAdmin2 = createUserAndLogin(tenantAdmin2, "testPassword1");
        
        Customer customer = new Customer();
        customer.setTitle("Different customer");
        Customer savedCustomer = doPost("/api/customer", customer, Customer.class);

        loginTenantAdmin();
        
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

    @Test
    public void testAssetLandingDashboardSave() throws Exception {
        Dashboard dashboard = new Dashboard();
        dashboard.setTitle("My dashboard");
        Dashboard savedDashboard = doPost("/api/dashboard", dashboard, Dashboard.class);

        Assert.assertNotNull(savedDashboard);
        Assert.assertNotNull(savedDashboard.getId());

        DataModel dataModel = createDataModel();
        DataModelObject dataModelObject = createDataModelObject(dataModel);

        AssetLandingDashboard ald = new AssetLandingDashboard(savedDashboard.getId());
        ald.setDataModelId(dataModel.getId());
        ald.setDataModelObjectId(dataModelObject.getId());

        AssetLandingDashboard savedAld = doPost("/api/asset-landing-dashboard/", ald, AssetLandingDashboard.class);
        Assert.assertNotNull(savedAld);

        AssetLandingDashboard foundAld = doGet("/api/asset-landing-dashboard/" + savedAld.getDashboardId().getId().toString(), AssetLandingDashboard.class);
        Assert.assertEquals(foundAld.getDashboardId(), savedAld.getDashboardId());
    }

    @Test
    public void testAssetLandingDashboardDelete() throws Exception {
        Dashboard dashboard = new Dashboard();
        dashboard.setTitle("My dashboard");
        Dashboard savedDashboard = doPost("/api/dashboard", dashboard, Dashboard.class);

        Assert.assertNotNull(savedDashboard);
        Assert.assertNotNull(savedDashboard.getId());

        DataModel dataModel = createDataModel();
        DataModelObject dataModelObject = createDataModelObject(dataModel);

        AssetLandingDashboard ald = new AssetLandingDashboard(savedDashboard.getId());
        ald.setDataModelId(dataModel.getId());
        ald.setDataModelObjectId(dataModelObject.getId());

        AssetLandingDashboard savedAld = doPost("/api/asset-landing-dashboard/", ald, AssetLandingDashboard.class);
        Assert.assertNotNull(savedAld);

        doDelete("/api/asset-landing-dashboard/"+savedAld.getDashboardId().getId().toString())
                .andExpect(status().isOk());
    }

    @Test
    public void testFindAssetLandingDashboardByDataModelObj() throws Exception {
        Dashboard dashboard = new Dashboard();
        dashboard.setTitle("My dashboard");
        Dashboard savedDashboard = doPost("/api/dashboard", dashboard, Dashboard.class);

        Assert.assertNotNull(savedDashboard);
        Assert.assertNotNull(savedDashboard.getId());

        Dashboard dashboard2 = new Dashboard();
        dashboard2.setTitle("My dashboard2");
        Dashboard savedDashboard2 = doPost("/api/dashboard", dashboard2, Dashboard.class);

        Assert.assertNotNull(savedDashboard2);
        Assert.assertNotNull(savedDashboard2.getId());

        DataModel dataModel = createDataModel();
        DataModelObject dataModelObject = createDataModelObject(dataModel);

        AssetLandingDashboard ald = new AssetLandingDashboard(savedDashboard.getId());
        ald.setDataModelId(dataModel.getId());
        ald.setDataModelObjectId(dataModelObject.getId());

        AssetLandingDashboard ald2 = new AssetLandingDashboard(savedDashboard2.getId());
        ald2.setDataModelId(dataModel.getId());
        ald2.setDataModelObjectId(dataModelObject.getId());

        AssetLandingDashboard savedAld = doPost("/api/asset-landing-dashboard/", ald, AssetLandingDashboard.class);
        Assert.assertNotNull(savedAld);

        AssetLandingDashboard savedAld2 = doPost("/api/asset-landing-dashboard/", ald2, AssetLandingDashboard.class);
        Assert.assertNotNull(savedAld2);

        List<AssetLandingDashboard> list = doGetTyped("/api/asset-landing-dashboard/data-model-object/" + dataModelObject.getId().toString(),  new TypeReference<List<AssetLandingDashboard>>(){});
        Assert.assertEquals(2, list.size());
    }

    private DataModel createDataModel() throws Exception{
        DataModel dataModel = new DataModel();
        dataModel.setName("Default Drilling Data Model1");
        dataModel.setLastUpdatedTs(System.currentTimeMillis());

        DataModel savedDataModel = doPost("/api/data-model", dataModel, DataModel.class);

        Assert.assertNotNull(savedDataModel);
        Assert.assertNotNull(savedDataModel.getId());
        Assert.assertTrue(savedDataModel.getCreatedTime() > 0);
        Assert.assertEquals(savedTenant.getId(), savedDataModel.getTenantId());
        Assert.assertEquals(dataModel.getName(), savedDataModel.getName());
        Assert.assertTrue(savedDataModel.getLastUpdatedTs() > 0);
        return savedDataModel;
    }

    private DataModelObject createDataModelObject(DataModel dataModel) throws Exception{
        DataModelObject dataModelObject = new DataModelObject();
        dataModelObject.setName("Well2");

        AttributeDefinition ad = new AttributeDefinition();
        ad.setValueType("STRING");
        ad.setName("attr name2");
        List<AttributeDefinition> attributeDefinitions = new ArrayList<>();
        attributeDefinitions.add(ad);
        dataModelObject.setAttributeDefinitions(attributeDefinitions);

        DataModelObject savedDataModelObj = doPost("/api/data-model/" + dataModel.getId().toString() + "/objects", dataModelObject, DataModelObject.class);
        Assert.assertNotNull(savedDataModelObj);
        Assert.assertEquals(dataModel.getId(), savedDataModelObj.getDataModelId());
        return savedDataModelObj;
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
        rule.setFilters(mapper.readTree("[{\"clazz\":\"com.hashmapinc.server.extensions.core.filter.MsgTypeFilter\", " +
                "\"name\":\"TelemetryFilter\", " +
                "\"configuration\": {\"messageTypes\":[\"POST_TELEMETRY\",\"POST_ATTRIBUTES\",\"GET_ATTRIBUTES\"]}}]"));
        rule.setAction(mapper.readTree("{\"clazz\":\"com.hashmapinc.server.extensions.core.action.telemetry.TelemetryPluginAction\", \"name\":\"TelemetryMsgConverterAction\", " +
                "\"configuration\":{\"timeUnit\":\"DAYS\", \"ttlValue\":1, \"qualityTimeWindow\":60000, \"qualityDepthWindow\":3000}}"));
        return rule;
    }

}
