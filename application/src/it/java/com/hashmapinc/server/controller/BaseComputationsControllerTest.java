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

import com.datastax.driver.core.utils.UUIDs;
import com.hashmapinc.server.common.data.computation.ComputationJob;
import com.hashmapinc.server.common.data.computation.ComputationType;
import com.hashmapinc.server.common.data.computation.Computations;
import com.hashmapinc.server.common.data.computation.SparkComputationMetadata;
import com.hashmapinc.server.common.data.id.ComputationId;
import com.hashmapinc.server.dao.computations.ComputationsService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BaseComputationsControllerTest extends AbstractControllerTest {


    private Computations savedComputations;

    @Autowired
    ComputationsService computationsService;


    @Before
    public void beforeTest() throws Exception {
        loginTenantAdmin();

        Computations computations = new Computations();
        ComputationId computationId = new ComputationId(UUIDs.timeBased());
        computations.setName("Computation");
        computations.setId(computationId);
        computations.setTenantId(savedTenant.getId());
        computations.setType(ComputationType.SPARK);

        SparkComputationMetadata md = new SparkComputationMetadata();
        md.setId(computationId);
        md.setJarPath("/Some/Jar/path");
        md.setMainClass("MainClass");
        md.setArgsType("ArgsType");
        md.setArgsformat("argsFormat");
        md.setJarName("SomeJar");

        computations.setComputationMetadata(md);
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
