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
package com.hashmapinc.server.service.security.auth.rules;

import com.datastax.driver.core.utils.UUIDs;
import com.hashmapinc.server.common.data.id.AssetId;
import com.hashmapinc.server.common.data.id.CustomerId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.id.UserId;
import com.hashmapinc.server.common.data.security.Authority;
import com.hashmapinc.server.service.security.model.SecurityUser;
import com.hashmapinc.server.service.security.model.UserPrincipal;
import lombok.Builder;
import lombok.Data;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ContextConfiguration(classes = AttributeBasedPermissionEvaluatorTest.class, loader = SpringBootContextLoader.class)
@ComponentScan({"com.hashmapinc.server.service.security.auth.rules"})
public class AttributeBasedPermissionEvaluatorTest {

    @Autowired
    private AttributeBasedPermissionEvaluator evaluator;

    private SecurityUser admin;
    private SecurityUser tenantAdmin;
    private SecurityUser customer;

    @Before
    public void setup(){
        this.admin = new SecurityUser(new UserId(UUIDs.timeBased()));
        this.admin.setEnabled(true);
        this.admin.setUserPrincipal(new UserPrincipal(UserPrincipal.Type.USER_NAME, "admin"));
        this.admin.setAuthority(Authority.SYS_ADMIN);

        this.tenantAdmin = new SecurityUser(new UserId(UUIDs.timeBased()));
        this.tenantAdmin.setEnabled(true);
        this.tenantAdmin.setUserPrincipal(new UserPrincipal(UserPrincipal.Type.USER_NAME, "tenant_admin"));
        this.tenantAdmin.setAuthority(Authority.TENANT_ADMIN);

        this.customer = new SecurityUser(new UserId(UUIDs.timeBased()));
        this.customer.setTenantId(this.tenantAdmin.getTenantId());
        this.customer.setEnabled(true);
        this.customer.setUserPrincipal(new UserPrincipal(UserPrincipal.Type.USER_NAME, "customer_user"));
        this.customer.setAuthority(Authority.CUSTOMER_USER);
    }

    @Test
    public void testSystemAdminAccessAssets(){
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(admin, null);
        boolean hasPermission = evaluator.hasPermission(authentication, TestResource.builder().build(), "ASSET_CREATE");

        Assert.assertTrue(hasPermission);
    }

    @Test
    public void testTenantCanCreateAnAsset(){
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(tenantAdmin, null);
        boolean hasPermission = evaluator.hasPermission(authentication, TestResource.builder().tenantId(new TenantId(tenantAdmin.getId().getId())).build(), "ASSET_CREATE");

        Assert.assertTrue(hasPermission);
    }

    @Test
    public void testTenantCanViewAnAsset(){
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(tenantAdmin, null);
        boolean hasPermission = evaluator.hasPermission(authentication, TestResource.builder().tenantId(new TenantId(tenantAdmin.getId().getId())).build(), "ASSET_READ");

        Assert.assertTrue(hasPermission);
    }

    @Test
    public void testTenantCanUpdateAnAsset(){
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(tenantAdmin, null);
        boolean hasPermission = evaluator.hasPermission(authentication, TestResource.builder().tenantId(new TenantId(tenantAdmin.getId().getId())).build(), "ASSET_UPDATE");

        Assert.assertTrue(hasPermission);
    }

    @Test
    public void testTenantCanDeleteAnAsset(){
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(tenantAdmin, null);
        boolean hasPermission = evaluator.hasPermission(authentication, TestResource.builder().tenantId(new TenantId(tenantAdmin.getId().getId())).build(), "ASSET_DELETE");

        Assert.assertTrue(hasPermission);
    }

    @Test
    public void testTenantCanNotAccessResourceOfAnotherTenant(){
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(tenantAdmin, null);
        boolean hasPermission = evaluator.hasPermission(authentication, TestResource.builder().tenantId(new TenantId(UUIDs.timeBased())).build(), "ASSET_DELETE");

        Assert.assertFalse(hasPermission);
    }

    @Test
    public void testCustomerShouldNotBeAbleToDeleteAsset(){
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(customer, null);
        boolean hasPermission = evaluator.hasPermission(authentication,
                TestResource.builder().tenantId(new TenantId(UUIDs.timeBased())).build(),
                "ASSET_DELETE");

        Assert.assertFalse(hasPermission);
    }

    @Data
    @Builder
    public static class TestResource{
        private AssetId id;
        private TenantId tenantId;
        private CustomerId customerId;
    }
}
