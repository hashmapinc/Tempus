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
import com.hashmapinc.server.common.data.computation.ComputationJob;
import com.hashmapinc.server.common.data.computation.Computations;
import com.hashmapinc.server.common.data.id.ComputationId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.hashmapinc.server.common.data.plugin.PluginMetaData;
import com.hashmapinc.server.common.data.rule.RuleMetaData;
import com.hashmapinc.server.dao.computations.ComputationsService;
import com.hashmapinc.server.extensions.core.plugin.telemetry.TelemetryStoragePlugin;

import java.io.IOException;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BaseComputationsControllerTest extends AbstractControllerTest {


    private Computations savedComputations;

    private PluginMetaData sysPlugin;
    private PluginMetaData tenantPlugin;
    private static final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    ComputationsService computationsService;


    @Before
    public void beforeTest() throws Exception {
        loginTenantAdmin();


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
        computations.setArgsformat("argsFormat");
        computations.setArgsType("ArgsType");
        savedComputations = computationsService.save(computations);
    }


    @Test
    public void testDeleteComputation() throws Exception {
        ComputationJob computationJob = new ComputationJob();
        computationJob.setName("Computation Job");
        computationJob.setJobId("0123");

        ComputationJob savedComputationJob = doPost("/api/computations/"+savedComputations.getId().getId().toString()+"/jobs", computationJob, ComputationJob.class);
        Assert.assertEquals(computationJob.getName(), savedComputationJob.getName());

        doDelete("/api/computations/"+savedComputations.getId().getId().toString()).andExpect(status().isOk());
        doGet("/api/computations/"+savedComputations.getId().getId().toString()).andExpect(status().isNotFound());
    }

}
