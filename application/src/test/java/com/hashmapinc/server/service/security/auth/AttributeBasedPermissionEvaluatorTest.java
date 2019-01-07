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
package com.hashmapinc.server.service.security.auth;

import com.datastax.driver.core.utils.UUIDs;
import com.hashmapinc.server.common.data.TempusResource;
import com.hashmapinc.server.common.data.asset.Asset;
import com.hashmapinc.server.common.data.id.*;
import com.hashmapinc.server.common.data.security.Authority;
import com.hashmapinc.server.dao.asset.AssetService;
import com.hashmapinc.server.dao.customergroup.CustomerGroupService;
import com.hashmapinc.server.dao.datamodel.DataModelObjectService;
import com.hashmapinc.server.dao.device.DeviceService;
import com.hashmapinc.server.service.security.model.SecurityUser;
import com.hashmapinc.server.service.security.model.UserPrincipal;
import lombok.Builder;
import lombok.Data;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;

import static com.hashmapinc.server.common.data.DataConstants.*;

@ActiveProfiles("permission-test")
@RunWith(SpringRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ContextConfiguration(classes = AttributeBasedPermissionEvaluatorTest.class, loader = SpringBootContextLoader.class)
@ComponentScan({"com.hashmapinc.server.service.security.auth"})
public class AttributeBasedPermissionEvaluatorTest {

    @Autowired
    private AttributeBasedPermissionEvaluator evaluator;

    @Autowired
    private AssetService assetService;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private DataModelObjectService dataModelObjectService;

    @Autowired
    private CustomerGroupService customerGroupService;

    private SecurityUser admin;
    private SecurityUser tenantAdmin;
    private SecurityUser customer;
    TestResource resource;

    @Before
    public void setup(){
        MockitoAnnotations.initMocks(this);

        this.admin = new SecurityUser(new UserId(UUIDs.timeBased()));
        this.admin.setEnabled(true);
        this.admin.setPermissions(Arrays.asList(SYS_ADMIN_DEFAULT_PERMISSION));
        this.admin.setUserPrincipal(new UserPrincipal(UserPrincipal.Type.USER_NAME, "admin"));
        this.admin.setAuthority(Authority.SYS_ADMIN);

        this.tenantAdmin = new SecurityUser(new UserId(UUIDs.timeBased()));
        this.tenantAdmin.setEnabled(true);
        this.tenantAdmin.setPermissions(Arrays.asList(TENANT_ADMIN_DEFAULT_PERMISSION));
        this.tenantAdmin.setTenantId(new TenantId(UUIDs.timeBased()));
        this.tenantAdmin.setUserPrincipal(new UserPrincipal(UserPrincipal.Type.USER_NAME, "tenant_admin"));
        this.tenantAdmin.setAuthority(Authority.TENANT_ADMIN);

        this.customer = new SecurityUser(new UserId(UUIDs.timeBased()));
        this.customer.setTenantId(this.tenantAdmin.getTenantId());
        this.customer.setCustomerId(new CustomerId(UUIDs.timeBased()));
        this.customer.setEnabled(true);
        this.customer.setPermissions(Arrays.asList(CUSTOMER_USER_DEFAULT_ASSET_READ_PERMISSION));
        this.customer.setUserPrincipal(new UserPrincipal(UserPrincipal.Type.USER_NAME, "customer_user"));
        this.customer.setAuthority(Authority.CUSTOMER_USER);

        this.resource = TestResource.builder()
                .id(new AssetId(UUIDs.timeBased()))
                .tenantId(new TenantId(tenantAdmin.getTenantId().getId()))
                .customerId(new CustomerId(customer.getCustomerId().getId()))
                .build();
    }

    @Test
    public void testSystemAdminAccessAssets(){
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(admin, null);
        boolean hasPermission = evaluator.hasPermission(authentication, resource, "ASSET_CREATE");

        Assert.assertTrue(hasPermission);
    }

    @Test
    public void testTenantCanCreateAnAsset(){
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(tenantAdmin, null);
        boolean hasPermission = evaluator.hasPermission(authentication, resource, "ASSET_CREATE");

        Assert.assertTrue(hasPermission);
    }

    @Test
    public void testTenantCanViewAnAsset(){
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(tenantAdmin, null);
        boolean hasPermission = evaluator.hasPermission(authentication, resource, "ASSET_READ");

        Assert.assertTrue(hasPermission);
    }

    @Test
    public void testTenantCanUpdateAnAsset(){
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(tenantAdmin, null);
        boolean hasPermission = evaluator.hasPermission(authentication, resource, "ASSET_UPDATE");

        Assert.assertTrue(hasPermission);
    }

    @Test
    public void testTenantCanDeleteAnAsset(){
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(tenantAdmin, null);
        boolean hasPermission = evaluator.hasPermission(authentication, resource, "ASSET_DELETE");

        Assert.assertTrue(hasPermission);
    }

    @Test
    public void testTenantCanNotAccessResourceOfAnotherTenant(){
        TestResource otherTenantResource = TestResource.builder()
                .id(new AssetId(UUIDs.timeBased()))
                .tenantId(new TenantId(UUIDs.timeBased()))
                .customerId(new CustomerId(UUIDs.timeBased()))
                .build();

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(tenantAdmin, null);
        boolean hasPermission = evaluator.hasPermission(authentication, otherTenantResource, "ASSET_DELETE");

        Assert.assertFalse(hasPermission);
    }

    @Test
    public void testCustomerShouldBeAbleToReadAsset(){
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(customer, null);
        boolean hasPermission = evaluator.hasPermission(authentication, resource,"ASSET_READ");

        Assert.assertTrue(hasPermission);
    }

    @Test
    public void testCustomerShouldNotBeAbleToDeleteAsset(){
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(customer, null);
        boolean hasPermission = evaluator.hasPermission(authentication, resource,"ASSET_DELETE");

        Assert.assertFalse(hasPermission);
    }

    @Test
    public void testCustomerShouldBeAbleToReadAssetOfOtherCustomer(){
        TestResource otherCustomerResource = TestResource.builder()
                .id(new AssetId(UUIDs.timeBased()))
                .tenantId(new TenantId(tenantAdmin.getTenantId().getId()))
                .customerId(new CustomerId(UUIDs.timeBased()))
                .build();

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(customer, null);
        boolean hasPermission = evaluator.hasPermission(authentication, otherCustomerResource,"ASSET_READ");

        Assert.assertFalse(hasPermission);
    }

    @Test
    public void testPermissionWhenDomainObjectIsNotAvailableForEvaluation(){
        AssetId assetId = new AssetId(UUIDs.timeBased());
        Asset mockAsset = new Asset(assetId);
        mockAsset.setName("Test Asset");
        mockAsset.setTenantId(this.tenantAdmin.getTenantId());
        Mockito.when(assetService.findAssetById(assetId))
                .thenReturn(mockAsset);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(this.tenantAdmin, null);

        boolean hasPermission = evaluator.hasPermission(authentication, assetId.getId().toString(), "ASSET","ASSET_DELETE");

        Assert.assertTrue(hasPermission);
    }

    @Data
    @Builder
    public static class TestResource implements TempusResource {
        private EntityId id;
        private TenantId tenantId;
        private CustomerId customerId;
        private String type;
    }
}

@Profile("permission-test")
@Configuration
class PermissionEvaluatorTestConfiguration {

    @Bean
    @Primary
    public DeviceService deviceServiceImpl() {
        return Mockito.mock(DeviceService.class);
    }

    @Bean
    @Primary
    public AssetService baseAssetService() {
        return Mockito.mock(AssetService.class);
    }

    @Bean
    @Primary
    public DataModelObjectService dataModelObjectService () { return Mockito.mock(DataModelObjectService.class); }

    @Bean
    @Primary
    public CustomerGroupService customerGroupService () {return Mockito.mock(CustomerGroupService.class); }
}