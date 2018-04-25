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

import com.datastax.driver.core.utils.UUIDs;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hashmapinc.server.common.data.*;
import com.hashmapinc.server.common.data.computation.ComputationJob;
import com.hashmapinc.server.common.data.computation.Computations;
import com.hashmapinc.server.common.data.id.ComputationId;
import com.hashmapinc.server.common.data.id.ComputationJobId;
import com.hashmapinc.server.common.data.id.RuleId;
import com.hashmapinc.server.common.data.page.TextPageData;
import com.hashmapinc.server.common.data.page.TextPageLink;
import com.hashmapinc.server.common.data.plugin.ComponentLifecycleState;
import com.hashmapinc.server.common.data.rule.RuleMetaData;
import com.hashmapinc.server.common.data.security.Authority;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.extensions.core.plugin.telemetry.TelemetryStoragePlugin;
import org.apache.commons.lang.RandomStringUtils;
import org.json.JSONObject;
import org.junit.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.hashmapinc.server.common.data.*;
import com.hashmapinc.server.common.data.id.*;
import com.hashmapinc.server.common.data.plugin.PluginMetaData;
import com.hashmapinc.server.dao.computations.ComputationsService;

import java.util.*;

import static org.hamcrest.CoreMatchers.containsString;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BaseApplicationControllerTest extends AbstractControllerTest {
    private IdComparator<Application> idComparator = new IdComparator<>();

    private Tenant savedTenant;
    private User tenantAdmin;

    private PluginMetaData sysPlugin;
    private PluginMetaData tenantPlugin;
    private static final ObjectMapper mapper = new ObjectMapper();

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
        tenantAdmin.setEmail("tenant2@tempus.org");
        tenantAdmin.setFirstName("Joe");
        tenantAdmin.setLastName("Downs");

        if(ldapEnabled) {
            createLDAPEntry(tenantAdmin.getEmail(), "testPassword1");
        }
        tenantAdmin = createUserAndLogin(tenantAdmin, "testPassword1");


        sysPlugin = new PluginMetaData();
        sysPlugin.setName("Sys plugin");
        sysPlugin.setApiToken("sysplugin");
        sysPlugin.setConfiguration(mapper.readTree("{}"));
        sysPlugin.setClazz(TelemetryStoragePlugin.class.getName());
        sysPlugin = doPost("/api/plugin", sysPlugin, PluginMetaData.class);

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
        if(ldapEnabled) {
            deleteLDAPEntry(tenantAdmin.getEmail());
        }
        doDelete("/api/tenant/"+savedTenant.getId().getId().toString())
                .andExpect(status().isOk());
    }


   @Test
    public void testSaveApplication() throws Exception {
        Application application = new Application();
        JsonNode deviceTypes1 = mapper.readTree("{\"deviceTypes\":[{\"name\":\"DT1\"}]}");
        application.setDeviceTypes(deviceTypes1);
        application.setName("My Application");
        application.setAdditionalInfo(mapper.readTree("{\n" +
                "\" additionalInfo\": {\n" +
                "\"description\": \"string\"\n" +
                "}\n" +
                "}"));

        Application savedApplication = doPost("/api/application", application, Application.class);

        Assert.assertNotNull(savedApplication);
        Assert.assertNotNull(savedApplication.getId());
        Assert.assertTrue(savedApplication.getCreatedTime() > 0);
        Assert.assertEquals(savedTenant.getId(), savedApplication.getTenantId());
        Assert.assertNotNull(savedApplication.getCustomerId());
        Assert.assertEquals(ModelConstants.NULL_UUID, savedApplication.getCustomerId().getId());
        Assert.assertEquals(application.getName(), savedApplication.getName());
    }

    @Test
    public void testSaveApplicationWithEmptyName() throws Exception {
        Application application = new Application();
        JsonNode deviceTypes1 = mapper.readTree("{\"configuration\":{\"deviceTypes\":[{\"name\":\"DT1\"}]}}");
        application.setDeviceTypes(deviceTypes1);
        doPost("/api/application", application)
                .andExpect(status().isBadRequest())
                .andExpect(statusReason(containsString("Application name should be specified")));
    }

    @Test
    public void testFindApplicationById() throws Exception {
        Application application = new Application();
        JsonNode deviceTypes1 = mapper.readTree("{\"deviceTypes\":[{\"name\":\"DT1\"}]}");
        application.setDeviceTypes(deviceTypes1);
        application.setName("My App");

        Application savedApplication = doPost("/api/application", application, Application.class);
        Application foundApplication = doGet("/api/application/" + savedApplication.getId().getId().toString(), Application.class);
        Assert.assertNotNull(foundApplication);
        Assert.assertEquals(savedApplication, foundApplication);
    }

    @Test
    public void testDeleteApplication() throws Exception {
        Application application = new Application();
        application.setName("My application");
        JsonNode deviceTypes1 = mapper.readTree("{\"deviceTypes\":[{\"name\":\"DT1\"}]}");
        application.setDeviceTypes(deviceTypes1);
        Application savedApplicaiton = doPost("/api/application", application, Application.class);

        doDelete("/api/application/"+savedApplicaiton.getId().getId().toString())
                .andExpect(status().isOk());

        doGet("/api/application/"+savedApplicaiton.getId().getId().toString())
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteApplicationAndRelatedEntities() throws Exception {
        Application application = new Application();
        application.setName("My application");
        JsonNode deviceTypes1 = mapper.readTree("{\"deviceTypes\":[{\"name\":\"DT1\"}]}");
        application.setDeviceTypes(deviceTypes1);
        Application savedApplication = doPost("/api/application", application, Application.class);

        RuleMetaData rule1 = new RuleMetaData();
        rule1.setName("My Rule1");
        rule1.setPluginToken(tenantPlugin.getApiToken());
        rule1.setFilters(mapper.readTree("[{\"configuration\":{\"deviceTypes\":[{\"name\":\"Motor\"},{\"name\":\"Pump\"}]},\"clazz\":\"com.hashmapinc.server.extensions.core.filter.DeviceTypeFilter\",\"name\":\"jetinder\"},{\"configuration\":{\"deviceTypes\":[{\"name\":\"Well\"},{}]},\"clazz\":\"com.hashmapinc.server.extensions.core.filter.DeviceTypeFilter\",\"name\":\"F2\"},{\"configuration\":{\"methodNames\":[{\"name\":\"sdsdsdsdsdsdsd\"}]},\"clazz\":\"com.hashmapinc.server.extensions.core.filter.MethodNameFilter\",\"name\":\"sdsdsdsdsdsdsdsdsd\"}]"));
        rule1.setAction(mapper.readTree("{\"clazz\":\"com.hashmapinc.server.extensions.core.action.telemetry.TelemetryPluginAction\", \"name\":\"TelemetryMsgConverterAction\", \"configuration\":{\"timeUnit\":\"DAYS\", \"ttlValue\":1}}"));
        RuleMetaData savedRule1 = doPost("/api/rule", rule1, RuleMetaData.class);

        doGet("/api/rule/"+savedRule1.getId().getId().toString()).andExpect(status().isOk());

        ApplicationFieldsWrapper applicationRulesWrapper = new ApplicationFieldsWrapper();
        applicationRulesWrapper.setApplicationId(savedApplication.getId().getId().toString());
        applicationRulesWrapper.setFields(new HashSet<>(Arrays.asList(savedRule1.getId().getId().toString())));
        doPostWithDifferentResponse("/api/app/assignRules", applicationRulesWrapper, Application.class);


        Dashboard dashboard = new Dashboard();
        dashboard.setTitle("My Dashboard");
        Dashboard savedDashboard = doPost("/api/dashboard", dashboard, Dashboard.class);
        doPost("/api/dashboard/main/"+savedDashboard.getId().getId().toString()
                +"/application/"+savedApplication.getId().getId().toString(), Application.class);
        doGet("/api/dashboard/"+savedDashboard.getId().getId().toString()).andExpect(status().isOk());


        doDelete("/api/application/"+savedApplication.getId().getId().toString())
                .andExpect(status().isOk());

        doGet("/api/application/"+savedApplication.getId().getId().toString())
                .andExpect(status().isNotFound());

        doGet("/api/rule/"+savedRule1.getId().getId().toString()).andExpect(status().isNotFound());
        doGet("/api/dashboard/"+savedDashboard.getId().getId().toString()).andExpect(status().isNotFound());
    }

    @Test
    public void testAssignUnassignApplicationToCustomer() throws Exception {
        Application application = new Application();
        application.setName("My application");
        application.setDeviceTypes(mapper.readTree("{\"deviceTypes\":[{\"name\":\"DT1\"}]}"));
        Application savedApplication = doPost("/api/application", application, Application.class);

        Customer customer = new Customer();
        customer.setTitle("My customer");
        Customer savedCustomer = doPost("/api/customer", customer, Customer.class);

        Application assignedApplication = doPost("/api/customer/" + savedCustomer.getId().getId().toString()
                + "/application/" + savedApplication.getId().getId().toString(), Application.class);
        Assert.assertEquals(savedCustomer.getId(), assignedApplication.getCustomerId());

        Application foundApplication = doGet("/api/application/" + savedApplication.getId().getId().toString(), Application.class);
        Assert.assertEquals(savedCustomer.getId(), foundApplication.getCustomerId());

        Application unassignedApplication =
                doDelete("/api/customer/application/" + savedApplication.getId().getId().toString(), Application.class);
        Assert.assertEquals(ModelConstants.NULL_UUID, unassignedApplication.getCustomerId().getId());

        foundApplication = doGet("/api/application/" + savedApplication.getId().getId().toString(), Application.class);
        Assert.assertEquals(ModelConstants.NULL_UUID, foundApplication.getCustomerId().getId());
    }

    @Test
    public void testAssignApplicationToNonExistentCustomer() throws Exception {
        Application application = new Application();
        application.setName("My application");
        JsonNode deviceTypes1 = mapper.readTree("{\"deviceTypes\":[{\"name\":\"DT1\"}]}");
        application.setDeviceTypes(deviceTypes1);

        Application savedApplication = doPost("/api/application", application, Application.class);

        doPost("/api/customer/" + UUIDs.timeBased().toString()
                + "/application/" + savedApplication.getId().getId().toString())
                .andExpect(status().isNotFound());
    }

    @Test
    public void testAssignApplicationToCustomerFromDifferentTenant() throws Exception {
        loginSysAdmin();

        Tenant tenant2 = new Tenant();
        tenant2.setTitle("Different tenant");
        Tenant savedTenant2 = doPost("/api/tenant", tenant2, Tenant.class);
        Assert.assertNotNull(savedTenant2);

        User tenantAdmin2 = new User();
        tenantAdmin2.setAuthority(Authority.TENANT_ADMIN);
        tenantAdmin2.setTenantId(savedTenant2.getId());
        tenantAdmin2.setEmail("sometenant@tempus.org");
        tenantAdmin2.setFirstName("Joe");
        tenantAdmin2.setLastName("Downs");

        if(ldapEnabled) {
            createLDAPEntry(tenantAdmin2.getEmail(), "testPassword1");
        }
        createUserAndLogin(tenantAdmin2, "testPassword1");

        Customer customer = new Customer();
        customer.setTitle("Different customer");
        Customer savedCustomer = doPost("/api/customer", customer, Customer.class);

        login(tenantAdmin.getEmail(), "testPassword1");

        Application application = new Application();
        application.setName("My application");
        JsonNode deviceTypes1 = mapper.readTree("{\"deviceTypes\":[{\"name\":\"DT1\"}]}");
        application.setDeviceTypes(deviceTypes1);

        Application savedApplication = doPost("/api/application", application, Application.class);

        doPost("/api/customer/" + savedCustomer.getId().getId().toString()
                + "/application/" + savedApplication.getId().getId().toString())
                .andExpect(status().isForbidden());

        loginSysAdmin();

        if(ldapEnabled) {
            deleteLDAPEntry(tenantAdmin2.getEmail());
        }
        doDelete("/api/tenant/"+savedTenant2.getId().getId().toString())
                .andExpect(status().isOk());
    }

    @Test
    public void testFindTenantApplications() throws Exception {
        List<Application> applications = new ArrayList<>();
        for (int i=0;i<178;i++) {
            Application application = new Application();
            JsonNode deviceTypes1 = mapper.readTree("{\"deviceTypes\":[{\"name\":\"DT1\"}]}");
            application.setDeviceTypes(deviceTypes1);
            application.setName("Application"+i);

            applications.add(doPost("/api/application", application, Application.class));
        }
        List<Application> loadedApplications = new ArrayList<>();
        TextPageLink pageLink = new TextPageLink(23);
        TextPageData<Application> pageData = null;
        do {
            pageData = doGetTypedWithPageLink("/api/tenant/applications?",
                    new TypeReference<TextPageData<Application>>(){}, pageLink);
            loadedApplications.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());

        Collections.sort(applications, idComparator);
        Collections.sort(loadedApplications, idComparator);

        Assert.assertEquals(applications, loadedApplications);
    }

    @Test
    public void testGetDeviceTypeApplications() throws Exception {
        Application application1 = new Application();
        application1.setName("application1");
        JsonNode deviceTypes1 = mapper.readTree("{\"deviceTypes\":[{\"name\":\"DT1\"},{\"name\":\"DT2\"}]}");
        application1.setDeviceTypes(deviceTypes1);
        doPost("/api/application", application1, Application.class);

        Application application2 = new Application();
        application2.setName("application2");
        JsonNode deviceTypes2 = mapper.readTree("{\"deviceTypes\":[{\"name\":\"DT2\"},{\"name\":\"DT3\"}]}");
        application2.setDeviceTypes(deviceTypes2);
        doPost("/api/application", application2, Application.class);

        Application application3 = new Application();
        application3.setName("application3");
        JsonNode deviceTypes3 = mapper.readTree("{\"deviceTypes\":[{\"name\":\"DT3\"},{\"name\":\"DT4\"}]}");
        application3.setDeviceTypes(deviceTypes3);
        doPost("/api/application", application3, Application.class);

        Application application4 = new Application();
        application4.setDeviceTypes(mapper.readTree("{\"deviceTypes\":[{\"name\":\"DT1\"}]}"));
        application4.setName("application4");
        doPost("/api/application", application4, Application.class);

        List<Application> foundApplications = doGetTyped("/api/applications/DT2" , new TypeReference<List<Application>>(){});

        Assert.assertEquals(2, foundApplications.size());
        /*Assert.assertTrue(foundApplications.get(0).getDeviceTypes().contains("DT2"));
        Assert.assertTrue(foundApplications.get(1).getDeviceTypes().contains("DT2"));*/

    }

    @Test
    public void testFindTenantApplicationsByName() throws Exception {
        String title1 = "Application title 1";
        List<Application> applicationsTitle1 = new ArrayList<>();
        for (int i=0;i<143;i++) {
            Application application = new Application();
            JsonNode deviceTypes1 = mapper.readTree("{\"deviceTypes\":[{\"name\":\"DT1\"}]}");
            application.setDeviceTypes(deviceTypes1);
            String suffix = RandomStringUtils.randomAlphanumeric(15);
            String name = title1+suffix;
            name = i % 2 == 0 ? name.toLowerCase() : name.toUpperCase();
            application.setName(name);
            applicationsTitle1.add(doPost("/api/application", application, Application.class));
        }
        String title2 = "Application title 2";
        List<Application> applicationsTitle2 = new ArrayList<>();
        for (int i=0;i<75;i++) {
            Application application = new Application();
            JsonNode deviceTypes1 = mapper.readTree("{\"deviceTypes\":[{\"name\":\"DT1\"}]}");
            application.setDeviceTypes(deviceTypes1);
            String suffix = RandomStringUtils.randomAlphanumeric(15);
            String name = title2+suffix;
            name = i % 2 == 0 ? name.toLowerCase() : name.toUpperCase();
            application.setName(name);
            applicationsTitle2.add(doPost("/api/application", application, Application.class));
        }

        List<Application> loadedApplicationsTitle1 = new ArrayList<>();
        TextPageLink pageLink = new TextPageLink(15, title1);
        TextPageData<Application> pageData = null;
        do {
            pageData = doGetTypedWithPageLink("/api/tenant/applications?",
                    new TypeReference<TextPageData<Application>>(){}, pageLink);
            loadedApplicationsTitle1.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());

        Collections.sort(applicationsTitle1, idComparator);
        Collections.sort(loadedApplicationsTitle1, idComparator);

        Assert.assertEquals(applicationsTitle1, loadedApplicationsTitle1);

        List<Application> loadedApplicationsTitle2 = new ArrayList<>();
        pageLink = new TextPageLink(4, title2);
        do {
            pageData = doGetTypedWithPageLink("/api/tenant/applications?",
                    new TypeReference<TextPageData<Application>>(){}, pageLink);
            loadedApplicationsTitle2.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());

        Collections.sort(applicationsTitle2, idComparator);
        Collections.sort(loadedApplicationsTitle2, idComparator);

        Assert.assertEquals(applicationsTitle2, loadedApplicationsTitle2);

        for (Application application : loadedApplicationsTitle1) {
            doDelete("/api/application/"+application.getId().getId().toString())
                    .andExpect(status().isOk());
        }

        pageLink = new TextPageLink(4, title1);
        pageData = doGetTypedWithPageLink("/api/tenant/applications?",
                new TypeReference<TextPageData<Application>>(){}, pageLink);
        Assert.assertFalse(pageData.hasNext());
        Assert.assertEquals(0, pageData.getData().size());

        for (Application application : loadedApplicationsTitle2) {
            doDelete("/api/application/"+application.getId().getId().toString())
                    .andExpect(status().isOk());
        }

        pageLink = new TextPageLink(4, title2);
        pageData = doGetTypedWithPageLink("/api/tenant/applications?",
                new TypeReference<TextPageData<Application>>(){}, pageLink);
        Assert.assertFalse(pageData.hasNext());
        Assert.assertEquals(0, pageData.getData().size());
    }

    @Test
    public void testAssignUnAssignDashboardToApplication() throws Exception {
        Application application = new Application();
        JsonNode deviceTypes1 = mapper.readTree("{\"deviceTypes\":[{\"name\":\"DT1\"}]}");
        application.setDeviceTypes(deviceTypes1);
        application.setName("My application");
        Application savedApplication = doPost("/api/application", application, Application.class);

        Dashboard dashboard = new Dashboard();
        dashboard.setTitle("My Dashboard");
        Dashboard savedDashboard = doPost("/api/dashboard", dashboard, Dashboard.class);

        String dashboardType = "main";

        Application assignedApplication = doPost("/api/dashboard/"+dashboardType+"/"+savedDashboard.getId().getId().toString()
                +"/application/"+savedApplication.getId().getId().toString(), Application.class);

        Assert.assertEquals(savedDashboard.getId(), assignedApplication.getDashboardId());

        Application foundApplication = doGet("/api/application/" + savedApplication.getId().getId().toString(), Application.class);
        Assert.assertEquals(savedDashboard.getId(), foundApplication.getDashboardId());

        Application unassignedApplication =
                doDelete("/api/dashboard/"+dashboardType + "/application/" + savedApplication.getId().getId().toString(), Application.class);
        Assert.assertEquals(ModelConstants.NULL_UUID, unassignedApplication.getDashboardId().getId());

        foundApplication = doGet("/api/application/" + savedApplication.getId().getId().toString(), Application.class);
        Assert.assertEquals(ModelConstants.NULL_UUID, foundApplication.getDashboardId().getId());
    }

    @Test
    public void testAssignUnAssignMiniDashboardToApplication() throws Exception {
        Application application = new Application();
        application.setName("My application");
        JsonNode deviceTypes1 = mapper.readTree("{\"deviceTypes\":[{\"name\":\"DT1\"}]}");
        application.setDeviceTypes(deviceTypes1);
        Application savedApplication = doPost("/api/application", application, Application.class);

        Dashboard dashboard = new Dashboard();
        dashboard.setTitle("My Dashboard");
        Dashboard savedDashboard = doPost("/api/dashboard", dashboard, Dashboard.class);

        String dashboardType = "mini";

        Application assignedApplication = doPost("/api/dashboard/"+dashboardType+"/"+savedDashboard.getId().getId().toString()
                +"/application/"+savedApplication.getId().getId().toString(), Application.class);

        Assert.assertEquals(savedDashboard.getId(), assignedApplication.getMiniDashboardId());

        Application foundApplication = doGet("/api/application/" + savedApplication.getId().getId().toString(), Application.class);
        Assert.assertEquals(savedDashboard.getId(), foundApplication.getMiniDashboardId());

        Application unassignedApplication =
                doDelete("/api/dashboard/"+dashboardType + "/application/" + savedApplication.getId().getId().toString(), Application.class);
        Assert.assertEquals(ModelConstants.NULL_UUID, unassignedApplication.getMiniDashboardId().getId());

        foundApplication = doGet("/api/application/" + savedApplication.getId().getId().toString(), Application.class);
        Assert.assertEquals(ModelConstants.NULL_UUID, foundApplication.getMiniDashboardId().getId());
    }

    @Test
    public void testAssignRulesToApplication() throws Exception{
        Application application = new Application();
        JsonNode deviceTypes1 = mapper.readTree("{\"deviceTypes\":[{\"name\":\"DT1\"}]}");
        application.setDeviceTypes(deviceTypes1);
        application.setName("My application");
        Application savedApplication = doPost("/api/application", application, Application.class);

        Assert.assertEquals(new HashSet<>(Arrays.asList(new RuleId(ModelConstants.NULL_UUID))), savedApplication.getRules());
        Assert.assertEquals(1,savedApplication.getDeviceTypes().size());

        RuleMetaData rule1 = new RuleMetaData();
        rule1.setName("My Rule1");
        rule1.setPluginToken(tenantPlugin.getApiToken());
        rule1.setFilters(mapper.readTree("[{\"configuration\":{\"deviceTypes\":[{\"name\":\"Motor\"},{\"name\":\"Pump\"}]},\"clazz\":\"com.hashmapinc.server.extensions.core.filter.DeviceTypeFilter\",\"name\":\"jetinder\"},{\"configuration\":{\"deviceTypes\":[{\"name\":\"Well\"},{}]},\"clazz\":\"com.hashmapinc.server.extensions.core.filter.DeviceTypeFilter\",\"name\":\"F2\"},{\"configuration\":{\"methodNames\":[{\"name\":\"sdsdsdsdsdsdsd\"}]},\"clazz\":\"com.hashmapinc.server.extensions.core.filter.MethodNameFilter\",\"name\":\"sdsdsdsdsdsdsdsdsd\"}]"));
        rule1.setAction(mapper.readTree("{\"clazz\":\"com.hashmapinc.server.extensions.core.action.telemetry.TelemetryPluginAction\", \"name\":\"TelemetryMsgConverterAction\", \"configuration\":{\"timeUnit\":\"DAYS\", \"ttlValue\":1}}"));
        RuleMetaData savedRule1 = doPost("/api/rule", rule1, RuleMetaData.class);

        RuleMetaData rule2 = new RuleMetaData();
        rule2.setName("My Rule2");
        rule2.setPluginToken(tenantPlugin.getApiToken());
        rule2.setFilters(mapper.readTree("[{\"clazz\":\"com.hashmapinc.server.extensions.core.filter.MsgTypeFilter\", " +
                "\"name\":\"TelemetryFilter\", " +
                "\"configuration\": {\"messageTypes\":[\"POST_TELEMETRY\",\"POST_ATTRIBUTES\",\"GET_ATTRIBUTES\"]}}]"));
        rule2.setAction(mapper.readTree("{\"clazz\":\"com.hashmapinc.server.extensions.core.action.telemetry.TelemetryPluginAction\", \"name\":\"TelemetryMsgConverterAction\", \"configuration\":{\"timeUnit\":\"DAYS\", \"ttlValue\":1}}"));
        RuleMetaData savedRule2 = doPost("/api/rule", rule2, RuleMetaData.class);

        ApplicationFieldsWrapper applicationRulesWrapper = new ApplicationFieldsWrapper();
        applicationRulesWrapper.setApplicationId(savedApplication.getId().getId().toString());
        applicationRulesWrapper.setFields(new HashSet<>(Arrays.asList(savedRule1.getId().getId().toString(), savedRule2.getId().getId().toString())));

        Application assignedApplication = doPostWithDifferentResponse("/api/app/assignRules", applicationRulesWrapper, Application.class);
        Assert.assertEquals(new HashSet<>(Arrays.asList(savedRule1.getId(), savedRule2.getId())), assignedApplication.getRules());

        Application foundApplication = doGet("/api/application/" + savedApplication.getId().getId().toString(), Application.class);
        Assert.assertEquals(new HashSet<>(Arrays.asList(savedRule1.getId(), savedRule2.getId())), foundApplication.getRules());
//        Assert.assertEquals(3,foundApplication.getDeviceTypes().size());
  //      Assert.assertTrue(foundApplication.getDeviceTypes().containsAll(new HashSet<>(Arrays.asList("Motor", "Pump", "Well"))));
    }

    @Test
    public void testUnassignRulesToApplication() throws Exception{
        Application application = new Application();
        JsonNode deviceTypes1 = mapper.readTree("{\"deviceTypes\":[{\"name\":\"DT1\"}]}");
        application.setDeviceTypes(deviceTypes1);
        application.setName("My application");
        Application savedApplication = doPost("/api/application", application, Application.class);

        RuleMetaData rule1 = new RuleMetaData();
        rule1.setName("My Rule1");
        rule1.setPluginToken(tenantPlugin.getApiToken());
        rule1.setFilters(mapper.readTree("[{\"configuration\": {\"deviceTypes\": [{\"name\": \"Motor\"}, {\"name\": \"Pump\"}]},\"clazz\": \"com.hashmapinc.server.extensions.core.filter.DeviceTypeFilter\",\"name\": \"jetinder\"\n" +
                "}, {\"configuration\": {\"methodNames\": [{\"name\": \"sdsdsdsdsdsdsd\"}]},\"clazz\": \"com.hashmapinc.server.extensions.core.filter.MethodNameFilter\",\"name\": \"sdsdsdsdsdsdsdsdsd\"}]"));
        rule1.setAction(mapper.readTree("{\"clazz\":\"com.hashmapinc.server.extensions.core.action.telemetry.TelemetryPluginAction\", \"name\":\"TelemetryMsgConverterAction\", \"configuration\":{\"timeUnit\":\"DAYS\", \"ttlValue\":1}}"));
        RuleMetaData savedRule1 = doPost("/api/rule", rule1, RuleMetaData.class);

        RuleMetaData rule2 = new RuleMetaData();
        rule2.setName("My Rule2");
        rule2.setPluginToken(tenantPlugin.getApiToken());
        rule2.setFilters(mapper.readTree("[{\"configuration\": {\"deviceTypes\": [{\"name\": \"Well\"}]},\"clazz\":\"com.hashmapinc.server.extensions.core.filter.DeviceTypeFilter\",\"name\": \"jetinder\"}, {\"configuration\": {\"methodNames\": [{\"name\": \"sdsdsdsdsdsdsd\"}]},\"clazz\":\"com.hashmapinc.server.extensions.core.filter.MethodNameFilter\",\"name\":\"sdsdsdsdsdsdsdsdsd\"}]"));
        rule2.setAction(mapper.readTree("{\"clazz\":\"com.hashmapinc.server.extensions.core.action.telemetry.TelemetryPluginAction\", \"name\":\"TelemetryMsgConverterAction\", \"configuration\":{\"timeUnit\":\"DAYS\", \"ttlValue\":1}}"));
        rule2.setAction(mapper.readTree("{\"clazz\":\"com.hashmapinc.server.extensions.core.action.telemetry.TelemetryPluginAction\", \"name\":\"TelemetryMsgConverterAction\", \"configuration\":{\"timeUnit\":\"DAYS\", \"ttlValue\":1}}"));
        RuleMetaData savedRule2 = doPost("/api/rule", rule2, RuleMetaData.class);

        ApplicationFieldsWrapper applicationRulesWrapper = new ApplicationFieldsWrapper();
        applicationRulesWrapper.setApplicationId(savedApplication.getId().getId().toString());
        applicationRulesWrapper.setFields(new HashSet<>(Arrays.asList(savedRule1.getId().getId().toString(), savedRule2.getId().getId().toString())));

        Application assignedApplication = doPostWithDifferentResponse("/api/app/assignRules", applicationRulesWrapper, Application.class);
        Assert.assertEquals(new HashSet<>(Arrays.asList(savedRule1.getId(), savedRule2.getId())), assignedApplication.getRules());

        Application foundApplication = doGet("/api/application/" + savedApplication.getId().getId().toString(), Application.class);
        Assert.assertEquals(new HashSet<>(Arrays.asList(savedRule1.getId(), savedRule2.getId())), foundApplication.getRules());

        ApplicationFieldsWrapper newApplicationRulesWrapper = new ApplicationFieldsWrapper();
        newApplicationRulesWrapper.setApplicationId(savedApplication.getId().getId().toString());
        newApplicationRulesWrapper.setFields(new HashSet<>(Arrays.asList(savedRule2.getId().getId().toString())));
        doPostWithDifferentResponse("/api/app/unassignRules", newApplicationRulesWrapper, Application.class);

        Application foundUnassignedApplication = doGet("/api/application/" + savedApplication.getId().getId().toString(), Application.class);
        Assert.assertEquals(new HashSet<>(Arrays.asList(savedRule1.getId())), foundUnassignedApplication.getRules());
    }

    @Test
    public void findApplicationsByDashboardId() throws Exception {
        Application application = new Application();
        application.setName("My application");
        JsonNode deviceTypes = mapper.readTree("{\"deviceTypes\":[{\"name\":\"DT1\"}]}");
        application.setDeviceTypes(deviceTypes);
        Application savedApplication = doPost("/api/application", application, Application.class);

        Application application1 = new Application();
        JsonNode deviceTypes1 = mapper.readTree("{\"deviceTypes\":[{\"name\":\"DT1\"}]}");
        application1.setDeviceTypes(deviceTypes1);
        application1.setName("My application 1");
        Application savedApplication1 = doPost("/api/application", application1, Application.class);

        Dashboard dashboard = new Dashboard();
        dashboard.setTitle("My Dashboard");
        Dashboard savedDashboard = doPost("/api/dashboard", dashboard, Dashboard.class);

        Dashboard dashboard1 = new Dashboard();
        dashboard1.setTitle("My Dashboard 1");
        Dashboard savedDashboard1 = doPost("/api/dashboard", dashboard1, Dashboard.class);

        Dashboard dashboard2 = new Dashboard();
        dashboard2.setTitle("My Dashboard 2");
        Dashboard savedDashboard2 = doPost("/api/dashboard", dashboard2, Dashboard.class);


        doPost("/api/dashboard/main/"+savedDashboard.getId().getId().toString() +"/application/"+savedApplication.getId().getId().toString(), Application.class);
        doPost("/api/dashboard/mini/"+savedDashboard1.getId().getId().toString() +"/application/"+savedApplication.getId().getId().toString(), Application.class);

        List<String> foundApplications1 = doGetTyped("/api/applications/dashboard/"+savedDashboard.getId().getId().toString() , new TypeReference<List<String>>(){});
        Assert.assertEquals(1, foundApplications1.size());
        Assert.assertTrue(foundApplications1.containsAll(Arrays.asList("My application")));

        List<String> foundApplications2 = doGetTyped("/api/applications/dashboard/"+savedDashboard1.getId().getId().toString() , new TypeReference<List<String>>(){});
        Assert.assertEquals(1, foundApplications2.size());
        Assert.assertTrue(foundApplications2.containsAll(Arrays.asList("My application")));

        List<String> foundApplications3 = doGetTyped("/api/applications/dashboard/"+savedDashboard2.getId().getId().toString() , new TypeReference<List<String>>(){});
        Assert.assertEquals(0, foundApplications3.size());
    }

    @Test
    public void findApplicationsByruleId() throws Exception {
        Application application = new Application();
        JsonNode deviceTypes = mapper.readTree("{\"deviceTypes\":[{\"name\":\"DT1\"}]}");
        application.setDeviceTypes(deviceTypes);
        application.setName("My application");
        Application savedApplication = doPost("/api/application", application, Application.class);

        Application application1 = new Application();
        JsonNode deviceTypes1 = mapper.readTree("{\"deviceTypes\":[{\"name\":\"DT1\"}]}");
        application1.setDeviceTypes(deviceTypes1);
        application1.setName("My application 1");
        Application savedApplication1 = doPost("/api/application", application1, Application.class);

        Application application2 = new Application();
        JsonNode deviceTypes2 = mapper.readTree("{\"deviceTypes\":[{\"name\":\"DT1\"}]}");
        application2.setDeviceTypes(deviceTypes2);
        application2.setName("My application 2");
        Application savedApplication2 = doPost("/api/application", application2, Application.class);


        RuleMetaData rule1 = new RuleMetaData();
        rule1.setName("My Rule1");
        rule1.setPluginToken(tenantPlugin.getApiToken());
        rule1.setFilters(mapper.readTree("[{\"clazz\":\"com.hashmapinc.server.extensions.core.filter.MsgTypeFilter\", " +
                "\"name\":\"TelemetryFilter\", " +
                "\"configuration\": {\"messageTypes\":[\"POST_TELEMETRY\",\"POST_ATTRIBUTES\",\"GET_ATTRIBUTES\"]}}]"));
        rule1.setAction(mapper.readTree("{\"clazz\":\"com.hashmapinc.server.extensions.core.action.telemetry.TelemetryPluginAction\", \"name\":\"TelemetryMsgConverterAction\", \"configuration\":{\"timeUnit\":\"DAYS\", \"ttlValue\":1}}"));
        RuleMetaData savedRule1 = doPost("/api/rule", rule1, RuleMetaData.class);

        RuleMetaData rule2 = new RuleMetaData();
        rule2.setName("My Rule2");
        rule2.setPluginToken(tenantPlugin.getApiToken());
        rule2.setFilters(mapper.readTree("[{\"clazz\":\"com.hashmapinc.server.extensions.core.filter.MsgTypeFilter\", " +
                "\"name\":\"TelemetryFilter\", " +
                "\"configuration\": {\"messageTypes\":[\"POST_TELEMETRY\",\"POST_ATTRIBUTES\",\"GET_ATTRIBUTES\"]}}]"));
        rule2.setAction(mapper.readTree("{\"clazz\":\"com.hashmapinc.server.extensions.core.action.telemetry.TelemetryPluginAction\", \"name\":\"TelemetryMsgConverterAction\", \"configuration\":{\"timeUnit\":\"DAYS\", \"ttlValue\":1}}"));
        RuleMetaData savedRule2 = doPost("/api/rule", rule2, RuleMetaData.class);
        JSONObject obj = new JSONObject(rule2.getFilters());

        RuleMetaData rule3 = new RuleMetaData();
        rule3.setName("My Rule3");
        rule3.setPluginToken(tenantPlugin.getApiToken());
        rule3.setFilters(mapper.readTree("[{\"clazz\":\"com.hashmapinc.server.extensions.core.filter.MsgTypeFilter\", " +
                "\"name\":\"TelemetryFilter\", " +
                "\"configuration\": {\"messageTypes\":[\"POST_TELEMETRY\",\"POST_ATTRIBUTES\",\"GET_ATTRIBUTES\"]}}]"));
        rule3.setAction(mapper.readTree("{\"clazz\":\"com.hashmapinc.server.extensions.core.action.telemetry.TelemetryPluginAction\", \"name\":\"TelemetryMsgConverterAction\", \"configuration\":{\"timeUnit\":\"DAYS\", \"ttlValue\":1}}"));
        RuleMetaData savedRule3 = doPost("/api/rule", rule3, RuleMetaData.class);

        ApplicationFieldsWrapper applicationRulesWrapper = new ApplicationFieldsWrapper();
        applicationRulesWrapper.setApplicationId(savedApplication.getId().getId().toString());
        applicationRulesWrapper.setFields(new HashSet<>(Arrays.asList(savedRule1.getId().getId().toString(), savedRule2.getId().getId().toString())));
        doPostWithDifferentResponse("/api/app/assignRules", applicationRulesWrapper, Application.class);

        ApplicationFieldsWrapper applicationRulesWrapper1 = new ApplicationFieldsWrapper();
        applicationRulesWrapper1.setApplicationId(savedApplication1.getId().getId().toString());
        applicationRulesWrapper1.setFields(new HashSet<>(Arrays.asList(savedRule1.getId().getId().toString(), savedRule3.getId().getId().toString())));
        doPostWithDifferentResponse("/api/app/assignRules", applicationRulesWrapper1, Application.class);


        ApplicationFieldsWrapper applicationRulesWrapper2 = new ApplicationFieldsWrapper();
        applicationRulesWrapper2.setApplicationId(savedApplication2.getId().getId().toString());
        applicationRulesWrapper2.setFields(new HashSet<>(Arrays.asList(savedRule2.getId().getId().toString(), savedRule3.getId().getId().toString())));
        doPostWithDifferentResponse("/api/app/assignRules", applicationRulesWrapper2, Application.class);

        Set<String> foundApplications1 = doGetTyped("/api/applications/rules/"+savedRule1.getId().getId().toString() , new TypeReference<Set<String>>(){});
        Assert.assertEquals(2, foundApplications1.size());

        Set<String> foundApplications2 = doGetTyped("/api/applications/rules/"+savedRule1.getId().getId().toString()+","+savedRule3.getId().getId().toString() , new TypeReference<Set<String>>(){});
        Assert.assertEquals(3, foundApplications2.size());
    }

    @Test
    public void testUnAssignUnAssignComputationJobsToApplication() throws Exception{
        Application application = new Application();
        application.setDeviceTypes(mapper.readTree("{\"deviceTypes\":[{\"name\":\"DT1\"}]}"));
        application.setName("My application");
        Application savedApplication = doPost("/api/application", application, Application.class);

        Assert.assertEquals(new HashSet<>(Arrays.asList(new ComputationJobId(ModelConstants.NULL_UUID))), savedApplication.getComputationJobIdSet());


        Computations savedComputations = saveComputation();

        ComputationJob computationJob1 = new ComputationJob();
        computationJob1.setName("Computation Job 1");
        ComputationJob savedComputationJob1 = doPost("/api/computations/"+savedComputations.getId().getId().toString()+"/jobs", computationJob1, ComputationJob.class);


        ComputationJob computationJob2 = new ComputationJob();
        computationJob2.setName("Computation Job 2");
        ComputationJob savedComputationJob2 = doPost("/api/computations/"+savedComputations.getId().getId().toString()+"/jobs", computationJob2, ComputationJob.class);


        ApplicationFieldsWrapper applicationComputationJosWrapper = new ApplicationFieldsWrapper();
        applicationComputationJosWrapper.setApplicationId(savedApplication.getId().getId().toString());
        applicationComputationJosWrapper.setFields(new HashSet<>(Arrays.asList(savedComputationJob1.getId().toString(), savedComputationJob2.getId().toString())));

        Application assignedApplication = doPostWithDifferentResponse("/api/app/assignComputationJobs", applicationComputationJosWrapper, Application.class);

        Application foundAssignedApplication = doGet("/api/application/" + savedApplication.getId().getId().toString(), Application.class);
        Assert.assertEquals(new HashSet<>(Arrays.asList(savedComputationJob1.getId(), savedComputationJob2.getId())), foundAssignedApplication.getComputationJobIdSet());


        ApplicationFieldsWrapper applicationComputationJosWrapper1 = new ApplicationFieldsWrapper();
        applicationComputationJosWrapper1.setApplicationId(savedApplication.getId().getId().toString());
        applicationComputationJosWrapper1.setFields(new HashSet<>(Arrays.asList(savedComputationJob2.getId().toString())));
        Application unAssignedApplication = doPostWithDifferentResponse("/api/app/unassignComputationJobs", applicationComputationJosWrapper1, Application.class);

        Application foundUnApplication = doGet("/api/application/" + savedApplication.getId().getId().toString(), Application.class);
        Assert.assertEquals(new HashSet<>(Arrays.asList(savedComputationJob1.getId())), foundUnApplication.getComputationJobIdSet());
    }

    @Test
    public void testActivateApplicationFailure() throws Exception {
        Application application = new Application();
        application.setDeviceTypes(mapper.readTree("{\"deviceTypes\":[{\"name\":\"DT1\"}]}"));
        application.setName("My application");
        Application savedApplication = doPost("/api/application", application, Application.class);

        Computations savedComputations = saveComputation();
        ComputationJob computationJob1 = new ComputationJob();
        computationJob1.setName("Computation Job 1");
        ComputationJob savedComputationJob1 = doPost("/api/computations/"+savedComputations.getId().getId().toString()+"/jobs", computationJob1, ComputationJob.class);

        ApplicationFieldsWrapper applicationComputationJosWrapper = new ApplicationFieldsWrapper();
        applicationComputationJosWrapper.setApplicationId(savedApplication.getId().getId().toString());
        applicationComputationJosWrapper.setFields(new HashSet<>(Arrays.asList(savedComputationJob1.getId().toString())));

        doPostWithDifferentResponse("/api/app/assignComputationJobs", applicationComputationJosWrapper, Application.class);


        RuleMetaData rule1 = new RuleMetaData();
        rule1.setName("My Rule1");
        rule1.setPluginToken(tenantPlugin.getApiToken());
        rule1.setFilters(mapper.readTree("[{\"clazz\":\"com.hashmapinc.server.extensions.core.filter.MsgTypeFilter\", " +
                "\"name\":\"TelemetryFilter\", " +
                "\"configuration\": {\"messageTypes\":[\"POST_TELEMETRY\",\"POST_ATTRIBUTES\",\"GET_ATTRIBUTES\"]}}]"));
        rule1.setAction(mapper.readTree("{\"clazz\":\"com.hashmapinc.server.extensions.core.action.telemetry.TelemetryPluginAction\", \"name\":\"TelemetryMsgConverterAction\", \"configuration\":{\"timeUnit\":\"DAYS\", \"ttlValue\":1}}"));
        RuleMetaData savedRule1 = doPost("/api/rule", rule1, RuleMetaData.class);


        ApplicationFieldsWrapper applicationRulesWrapper = new ApplicationFieldsWrapper();
        applicationRulesWrapper.setApplicationId(savedApplication.getId().getId().toString());
        applicationRulesWrapper.setFields(new HashSet<>(Arrays.asList(savedRule1.getId().getId().toString())));
        doPostWithDifferentResponse("/api/app/assignRules", applicationRulesWrapper, Application.class);


        Assert.assertEquals(ComponentLifecycleState.SUSPENDED, savedApplication.getState());
        Assert.assertEquals(ComponentLifecycleState.SUSPENDED, savedRule1.getState());
        Assert.assertEquals(ComponentLifecycleState.SUSPENDED, savedComputationJob1.getState());

        doPost("/api/application/"+savedApplication.getId().getId().toString() +"/activate").andExpect(status().isBadRequest());

        Application foundApplication = doGet("/api/application/" + savedApplication.getId().getId().toString(), Application.class);
        Assert.assertEquals(ComponentLifecycleState.SUSPENDED, foundApplication.getState());

        RuleMetaData foundRuleMetaData = doGet("/api/rule/"+savedRule1.getId().getId().toString(), RuleMetaData.class);
        Assert.assertEquals(ComponentLifecycleState.SUSPENDED ,foundRuleMetaData.getState());

        ComputationJob foundComputationJob = doGet("/api/computations/"+savedComputations.getId().getId().toString()+"/jobs/"+ savedComputationJob1.getId(), ComputationJob.class);
        Assert.assertEquals(ComponentLifecycleState.SUSPENDED, foundComputationJob.getState());
    }


    @Test
    public void testActivateApplicationSuccess() throws Exception {
        Application application = new Application();
        application.setDeviceTypes(mapper.readTree("{\"deviceTypes\":[{\"name\":\"DT1\"}]}"));
        application.setName("My application");
        Application savedApplication = doPost("/api/application", application, Application.class);

        Computations savedComputations = saveComputation();
        ComputationJob computationJob1 = new ComputationJob();
        computationJob1.setName("Computation Job 1");
        computationJob1.setArgParameters(mapper.readTree("{\n" +
                "\t\"port\": 8998,\n" +
                "\t\"host\": \"spark - master\",\n" +
                "\t\"window\": 1,\n" +
                "\t\"kafkaUrl\": \"kafka: 9092\",\n" +
                "\t\"kafkaTopic\": \"water - tank - level - data\",\n" +
                "\t\"actionPath\": \"batches\",\n" +
                "\t\"mqttUrl\": \"tb: 1883\",\n" +
                "\t\"highWaterMark\": \"70\",\n" +
                "\t\"gatewayAccessToken\": \"GATEWAY_ACCESS_TOKEN\"\n" +
                "}"));
        ComputationJob savedComputationJob1 = doPost("/api/computations/"+savedComputations.getId().getId().toString()+"/jobs", computationJob1, ComputationJob.class);

        ApplicationFieldsWrapper applicationComputationJosWrapper = new ApplicationFieldsWrapper();
        applicationComputationJosWrapper.setApplicationId(savedApplication.getId().getId().toString());
        applicationComputationJosWrapper.setFields(new HashSet<>(Arrays.asList(savedComputationJob1.getId().toString())));

        doPostWithDifferentResponse("/api/app/assignComputationJobs", applicationComputationJosWrapper, Application.class);


        RuleMetaData rule1 = new RuleMetaData();
        rule1.setName("My Rule1");
        rule1.setPluginToken(tenantPlugin.getApiToken());
        rule1.setFilters(mapper.readTree("[{\"clazz\":\"com.hashmapinc.server.extensions.core.filter.MsgTypeFilter\", " +
                "\"name\":\"TelemetryFilter\", " +
                "\"configuration\": {\"messageTypes\":[\"POST_TELEMETRY\",\"POST_ATTRIBUTES\",\"GET_ATTRIBUTES\"]}}]"));
        rule1.setAction(mapper.readTree("{\"clazz\":\"com.hashmapinc.server.extensions.core.action.telemetry.TelemetryPluginAction\", \"name\":\"TelemetryMsgConverterAction\", \"configuration\":{\"timeUnit\":\"DAYS\", \"ttlValue\":1}}"));
        RuleMetaData savedRule1 = doPost("/api/rule", rule1, RuleMetaData.class);


        ApplicationFieldsWrapper applicationRulesWrapper = new ApplicationFieldsWrapper();
        applicationRulesWrapper.setApplicationId(savedApplication.getId().getId().toString());
        applicationRulesWrapper.setFields(new HashSet<>(Arrays.asList(savedRule1.getId().getId().toString())));
        doPostWithDifferentResponse("/api/app/assignRules", applicationRulesWrapper, Application.class);


        Assert.assertEquals(ComponentLifecycleState.SUSPENDED, savedApplication.getState());
        Assert.assertEquals(ComponentLifecycleState.SUSPENDED, savedRule1.getState());
        Assert.assertEquals(ComponentLifecycleState.SUSPENDED, savedComputationJob1.getState());

        doPost("/api/plugin/" + tenantPlugin.getId().getId().toString() + "/activate").andExpect(status().isOk());
        doPost("/api/application/"+savedApplication.getId().getId().toString() +"/activate").andExpect(status().isOk());

        Application foundApplication = doGet("/api/application/" + savedApplication.getId().getId().toString(), Application.class);
        Assert.assertEquals(ComponentLifecycleState.ACTIVE, foundApplication.getState());

        RuleMetaData foundRuleMetaData = doGet("/api/rule/"+savedRule1.getId().getId().toString(), RuleMetaData.class);
        Assert.assertEquals(ComponentLifecycleState.ACTIVE ,foundRuleMetaData.getState());

        ComputationJob foundComputationJob = doGet("/api/computations/"+savedComputations.getId().getId().toString()+"/jobs/"+ savedComputationJob1.getId(), ComputationJob.class);
        Assert.assertEquals(ComponentLifecycleState.ACTIVE, foundComputationJob.getState());

        doPost("/api/application/"+savedApplication.getId().getId().toString() +"/suspend").andExpect(status().isOk());
        Assert.assertEquals(ComponentLifecycleState.SUSPENDED, doGet("/api/application/" + savedApplication.getId().getId().toString(), Application.class).getState());
        Assert.assertEquals(ComponentLifecycleState.SUSPENDED ,doGet("/api/rule/"+savedRule1.getId().getId().toString(), RuleMetaData.class).getState());
        Assert.assertEquals(ComponentLifecycleState.SUSPENDED, doGet("/api/computations/"+savedComputations.getId().getId().toString()+"/jobs/"+ savedComputationJob1.getId(), ComputationJob.class).getState());
    }

    @Test
    public void testActivateSuspendApplicationWithoutRulesAndComputationSuccess() throws Exception {
        Application application = new Application();
        application.setDeviceTypes(mapper.readTree("{\"deviceTypes\":[{\"name\":\"DT1\"}]}"));
        application.setName("My application");
        Application savedApplication = doPost("/api/application", application, Application.class);
        Assert.assertEquals(ComponentLifecycleState.SUSPENDED, savedApplication.getState());
        doPost("/api/application/"+savedApplication.getId().getId().toString() +"/activate").andExpect(status().isOk());

        Application foundApplication = doGet("/api/application/" + savedApplication.getId().getId().toString(), Application.class);
        Assert.assertEquals(ComponentLifecycleState.ACTIVE, foundApplication.getState());

        doPost("/api/application/"+savedApplication.getId().getId().toString() +"/suspend").andExpect(status().isOk());
        Assert.assertEquals(ComponentLifecycleState.SUSPENDED,  doGet("/api/application/" + savedApplication.getId().getId().toString(), Application.class).getState());
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


}
