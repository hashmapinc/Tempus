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
package com.hashmapinc.server.dao.service;

import com.hashmapinc.server.common.data.TempusGatewayConfiguration;
import com.hashmapinc.server.common.data.Tenant;
import com.hashmapinc.server.common.data.id.TenantId;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

public abstract class BaseTempusGatewayConfigurationServiceTest extends AbstractServiceTest {
    private TenantId tenantId;

    @Before
    public void before() {
        Tenant tenant = new Tenant();
        tenant.setTitle("My tenant");
        Tenant savedTenant = tenantService.saveTenant(tenant);
        Assert.assertNotNull(savedTenant);
        tenantId = savedTenant.getId();
    }

    @After
    public void after() {
        tenantService.deleteTenant(tenantId);
    }

    @Test
    public void testSaveAndUpdateTempusGatewayConfiguration() {
        final TempusGatewayConfiguration saveTempusGatewayConfiguration = createTempusGatewayConfiguration();

        saveTempusGatewayConfiguration.setGatewayToken("gateway-config-token-new");
        final TempusGatewayConfiguration updatedTempusGatewayConfiguration = tempusGatewayConfigurationService.saveTempusGatewayConfiguration(saveTempusGatewayConfiguration);
        Assert.assertNotNull(updatedTempusGatewayConfiguration);
        Assert.assertEquals(saveTempusGatewayConfiguration.getGatewayToken(), updatedTempusGatewayConfiguration.getGatewayToken());

        tempusGatewayConfigurationService.deleteTempusGatewayConfiguration(saveTempusGatewayConfiguration.getId());
    }

    @Test
    public void testFindTempusGatewayConfigurationByIdAndByTenantId(){
        final TempusGatewayConfiguration saveTempusGatewayConfiguration = createTempusGatewayConfiguration();
        final TempusGatewayConfiguration tempusGatewayConfigurationById = tempusGatewayConfigurationService.findTempusGatewayConfigurationById(saveTempusGatewayConfiguration.getId());
        Assert.assertNotNull(tempusGatewayConfigurationById);
        Assert.assertEquals(saveTempusGatewayConfiguration.getId(), tempusGatewayConfigurationById.getId());
        final Optional<TempusGatewayConfiguration> tempusGatewayConfigurationByTenantId =
                tempusGatewayConfigurationService.findTempusGatewayConfigurationByTenantId(saveTempusGatewayConfiguration.getTenantId());
        Assert.assertTrue(tempusGatewayConfigurationByTenantId.isPresent());
        Assert.assertEquals(saveTempusGatewayConfiguration.getId(), tempusGatewayConfigurationByTenantId.get().getId());

        tempusGatewayConfigurationService.deleteTempusGatewayConfiguration(saveTempusGatewayConfiguration.getId());
    }

    private TempusGatewayConfiguration createTempusGatewayConfiguration() {
        TempusGatewayConfiguration tempusGatewayConfiguration = new TempusGatewayConfiguration();
        tempusGatewayConfiguration.setReplicas(1);
        tempusGatewayConfiguration.setGatewayToken("token");
        tempusGatewayConfiguration.setTenantId(tenantId);
        final TempusGatewayConfiguration saveTempusGatewayConfiguration = tempusGatewayConfigurationService.saveTempusGatewayConfiguration(tempusGatewayConfiguration);

        Assert.assertNotNull(saveTempusGatewayConfiguration);
        Assert.assertNotNull(saveTempusGatewayConfiguration.getId());
        Assert.assertTrue(saveTempusGatewayConfiguration.getCreatedTime() > 0);
        Assert.assertEquals(tempusGatewayConfiguration.getTenantId(), saveTempusGatewayConfiguration.getTenantId());
        Assert.assertEquals(tempusGatewayConfiguration.getGatewayToken(), saveTempusGatewayConfiguration.getGatewayToken());
        return saveTempusGatewayConfiguration;
    }
}
