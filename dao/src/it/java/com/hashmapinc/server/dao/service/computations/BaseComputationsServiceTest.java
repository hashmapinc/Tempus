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
package com.hashmapinc.server.dao.service.computations;

import com.datastax.driver.core.utils.UUIDs;
import com.hashmapinc.server.common.data.computation.Computations;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.dao.service.AbstractServiceTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

@Slf4j
public abstract class BaseComputationsServiceTest extends AbstractServiceTest {

    @Test
    public void saveComputation() throws Exception {
        Computations computation = computationsService.save(generateComputation(null));
        Assert.assertNotNull(computation.getId());
        Computations newComputation = computationsService.save(computation);
        Assert.assertEquals(computation.getName(), newComputation.getName());
    }

    @Test
    public void findComputationById() throws Exception {
        Computations expected = computationsService.save(generateComputation(null));
        Assert.assertNotNull(expected.getId());
        Computations found = computationsService.findById(expected.getId());
        Assert.assertEquals(expected.getName(), found.getName());
    }

    @Test
    public void findComputationByName() throws Exception {
        Computations expected = computationsService.save(generateComputation(null));
        Assert.assertNotNull(expected.getName());
        Computations found = computationsService.findByName(expected.getName());
        Assert.assertEquals(expected.getName(), found.getName());
    }

    @Test
    public void findTenantComputations() throws Exception {
        TenantId tenantId = new TenantId(UUIDs.timeBased());
        computationsService.save(generateComputation(null));
        computationsService.save(generateComputation(null));
        computationsService.save(generateComputation(tenantId));
        computationsService.save(generateComputation(tenantId));
        computationsService.save(generateComputation(tenantId));
        List<Computations> found = computationsService.findAllTenantComputationsByTenantId(tenantId);
        Assert.assertEquals(3, found.size());
    }

    @Test
    public void deleteComputationById() throws Exception {
        Computations expected = computationsService.save(generateComputation(null));
        Assert.assertNotNull(expected.getId());
        computationsService.deleteById(expected.getId());
        Computations found = computationsService.findById(expected.getId());
        Assert.assertNull(found);
    }

}