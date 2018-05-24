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

import com.hashmapinc.server.common.data.Entitlements;
import com.hashmapinc.server.common.data.Tenant;
import com.hashmapinc.server.common.data.User;
import com.hashmapinc.server.common.data.security.Authority;
import com.hashmapinc.server.common.data.security.EntitledServices;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BaseEntitlementsControllerTest extends AbstractControllerTest {

    private Tenant savedTenant;
    private User tenantAdmin;

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
        tenantAdmin = createUserAndLogin(tenantAdmin, "testPassword1");
    }

    @After
    public void afterTest() throws Exception {
        loginSysAdmin();
        doDelete("/api/tenant/"+savedTenant.getId().getId().toString())
                .andExpect(status().isOk());
    }

    @Test
    public void testSaveEntitlements() throws Exception {
        User user = new User();
        user.setAuthority(Authority.API_USER);
        user.setEmail("testApiUser1@tempus.org");
        User savedUser = doPost("/api/user?activationType=mail", user, User.class);

        Entitlements entitlements = new Entitlements();
        entitlements.setUserId(savedUser.getId());
        entitlements.setEntitledServices(new HashSet<>(Arrays.asList(EntitledServices.METADATA_INGESTION)));

        Entitlements savedEntitlements = doPost("/api/entitlements", entitlements, Entitlements.class);
        Assert.assertEquals(savedUser.getId(), savedEntitlements.getUserId());
        Assert.assertEquals(new HashSet<>(Arrays.asList(EntitledServices.METADATA_INGESTION)), savedEntitlements.getEntitledServices());

    }

    @Test
    public void testRetrieveEntitlementsByUserId() throws Exception {
        User user = new User();
        user.setAuthority(Authority.API_USER);
        user.setEmail("testApiUser2@tempus.org");
        User savedUser = doPost("/api/user?activationType=mail", user, User.class);

        Entitlements entitlements = new Entitlements();
        entitlements.setUserId(savedUser.getId());
        entitlements.setEntitledServices(new HashSet<>(Arrays.asList(EntitledServices.METADATA_INGESTION)));

        Entitlements savedEntitlements = doPost("/api/entitlements", entitlements, Entitlements.class);
        Assert.assertEquals(savedUser.getId(), savedEntitlements.getUserId());
        Assert.assertEquals(new HashSet<>(Arrays.asList(EntitledServices.METADATA_INGESTION)), savedEntitlements.getEntitledServices());

    }
}
