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

import com.fasterxml.jackson.core.type.TypeReference;
import com.hashmapinc.server.common.data.TempusGatewayConfiguration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

public class BaseTempusGatewayConfigurationControllerTest extends AbstractControllerTest {
    @Before
    public void beforeTest() throws Exception {
        loginTenantAdmin();
    }

    @Test
    public void testSaveAndUpdateTempusGatewayConfiguration() throws Exception {
        final TempusGatewayConfiguration saveTempusGatewayConfiguration = createTempusGatewayConfiguration();

        saveTempusGatewayConfiguration.setGatewayToken("gateway-config-token-new");
        final TempusGatewayConfiguration updatedTempusGatewayConfiguration =
                doPost("/api/configuration/tempusGateway", saveTempusGatewayConfiguration, TempusGatewayConfiguration.class);
        Assert.assertNotNull(updatedTempusGatewayConfiguration);
        Assert.assertEquals(saveTempusGatewayConfiguration.getGatewayToken(), updatedTempusGatewayConfiguration.getGatewayToken());
    }

    @Test
    public void testFindTempusGatewayConfigurationByIdAndByTenantId() throws Exception {
        final TempusGatewayConfiguration saveTempusGatewayConfiguration = createTempusGatewayConfiguration();
        final TempusGatewayConfiguration tempusGatewayConfigurationById =
                doGet("/api/configuration/tempusGateway/"+saveTempusGatewayConfiguration.getId().getId().toString(), TempusGatewayConfiguration.class);
        Assert.assertNotNull(tempusGatewayConfigurationById);
        Assert.assertEquals(saveTempusGatewayConfiguration.getId(), tempusGatewayConfigurationById.getId());
        final TempusGatewayConfiguration tempusGatewayConfigurationByTenantId =
                doGet("/api/configuration/tempusGateway", TempusGatewayConfiguration.class);
        Assert.assertNotNull(tempusGatewayConfigurationByTenantId);
        Assert.assertEquals(saveTempusGatewayConfiguration.getId(), tempusGatewayConfigurationByTenantId.getId());

    }

    private TempusGatewayConfiguration createTempusGatewayConfiguration() throws Exception {
        TempusGatewayConfiguration tempusGatewayConfiguration = new TempusGatewayConfiguration();
        tempusGatewayConfiguration.setReplicas(1);
        tempusGatewayConfiguration.setGatewayToken("token");
        tempusGatewayConfiguration.setTenantId(tenantId);
        TempusGatewayConfiguration saveTempusGatewayConfiguration =
                doPost("/api/configuration/tempusGateway", tempusGatewayConfiguration, TempusGatewayConfiguration.class);

        Assert.assertNotNull(saveTempusGatewayConfiguration);
        Assert.assertNotNull(saveTempusGatewayConfiguration.getId());
        Assert.assertTrue(saveTempusGatewayConfiguration.getCreatedTime() > 0);
        Assert.assertEquals(tempusGatewayConfiguration.getTenantId(), saveTempusGatewayConfiguration.getTenantId());
        Assert.assertEquals(tempusGatewayConfiguration.getGatewayToken(), saveTempusGatewayConfiguration.getGatewayToken());
        return saveTempusGatewayConfiguration;
    }
}
