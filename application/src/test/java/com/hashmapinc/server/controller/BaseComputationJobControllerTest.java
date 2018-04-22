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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hashmapinc.server.common.data.*;
import com.hashmapinc.server.common.data.computation.ComputationJob;
import com.hashmapinc.server.common.data.computation.Computations;
import com.hashmapinc.server.common.data.id.ComputationId;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.hashmapinc.server.common.data.*;
import com.hashmapinc.server.common.data.plugin.PluginMetaData;
import com.hashmapinc.server.common.data.rule.RuleMetaData;
import com.hashmapinc.server.common.data.security.Authority;
import com.hashmapinc.server.dao.computations.ComputationsService;
import com.hashmapinc.server.extensions.core.plugin.telemetry.TelemetryStoragePlugin;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BaseComputationJobControllerTest extends AbstractControllerTest {
    private IdComparator<Application> idComparator = new IdComparator<>();

    private Tenant savedTenant;
    private Computations savedComputations;
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
        savedComputations = computationsService.save(computations);
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
    public void testDeleteComputationJobAndUpdateApplication() throws Exception {
        ComputationJob computationJob = new ComputationJob();
        computationJob.setName("Computation Job");
        computationJob.setJobId("0123");

        ComputationJob savedComputationJob = doPost("/api/computations/"+savedComputations.getId().getId().toString()+"/jobs", computationJob, ComputationJob.class);
        Assert.assertEquals(computationJob.getName(), savedComputationJob.getName());

        Application application = new Application();
        application.setName("My Application");
        application.setAdditionalInfo(mapper.readTree("{\n" +
                "\" additionalInfo\": {\n" +
                "\"description\": \"string\"\n" +
                "}\n" +
                "}"));

        application.setDeviceTypes(mapper.readTree("{\"deviceTypes\":[{\"name\":\"DT1\"}]}"));
        Application savedApplication = doPost("/api/application", application, Application.class);

        Dashboard dashboard = new Dashboard();
        dashboard.setTitle("My Dashboard");
        Dashboard savedDashboard = doPost("/api/dashboard", dashboard, Dashboard.class);
        String dashboardType = "mini";
        doPost("/api/dashboard/"+dashboardType+"/"+savedDashboard.getId().getId().toString()
                +"/application/"+savedApplication.getId().getId().toString(), Application.class);


        RuleMetaData rule = createRuleMetaData(tenantPlugin);
        RuleMetaData savedRule = doPost("/api/rule", rule, RuleMetaData.class);
        RuleMetaData foundRule = doGet("/api/rule/" + savedRule.getId().getId().toString(), RuleMetaData.class);
        Assert.assertNotNull(foundRule);
        ApplicationFieldsWrapper applicationRulesWrapper = new ApplicationFieldsWrapper();
        applicationRulesWrapper.setApplicationId(savedApplication.getId().getId().toString());
        applicationRulesWrapper.setFields(new HashSet<>(Arrays.asList(savedRule.getId().getId().toString())));
        doPostWithDifferentResponse("/api/app/assignRules", applicationRulesWrapper, Application.class);


        ApplicationFieldsWrapper applicationComputationJosWrapper = new ApplicationFieldsWrapper();
        applicationComputationJosWrapper.setApplicationId(savedApplication.getId().getId().toString());
        applicationComputationJosWrapper.setFields(new HashSet<>(Arrays.asList(savedComputationJob.getId().getId().toString())));

        Application assignedApplication = doPostWithDifferentResponse("/api/app/assignComputationJobs", applicationComputationJosWrapper, Application.class);

        Assert.assertEquals(new HashSet<>(Arrays.asList(savedComputationJob.getId())), assignedApplication.getComputationJobIdSet());
        Assert.assertTrue(assignedApplication.getIsValid());


        doDelete("/api/computations/jobs/"+savedComputationJob.getId().getId().toString()).andExpect(status().isOk());
        doGet("/api/computations/"+savedComputations.getId().getId().toString()+"/jobs/"+savedComputationJob.getId().getId().toString()).andExpect(status().isNotFound());
        Thread.sleep(10000);

        Application foundApplication = doGet("/api/application/" + savedApplication.getId().getId().toString(), Application.class);
        Assert.assertFalse(foundApplication.getIsValid());

    }

    public static RuleMetaData createRuleMetaData(PluginMetaData plugin) throws IOException {
        RuleMetaData rule = new RuleMetaData();
        rule.setName("My Rule");
        rule.setPluginToken(plugin.getApiToken());
        rule.setFilters(mapper.readTree("[{\"clazz\":\"com.hashmapinc.server.extensions.core.filter.MsgTypeFilter\", " +
                "\"name\":\"TelemetryFilter\", " +
                "\"configuration\": {\"messageTypes\":[\"POST_TELEMETRY\",\"POST_ATTRIBUTES\",\"GET_ATTRIBUTES\"]}}]"));
        rule.setAction(mapper.readTree("{\"clazz\":\"com.hashmapinc.server.extensions.core.action.telemetry.TelemetryPluginAction\", \"name\":\"TelemetryMsgConverterAction\", " +
                "\"configuration\":{\"timeUnit\":\"DAYS\", \"ttlValue\":1}}"));
        return rule;
    }
}
