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
import com.hashmapinc.server.common.data.CustomerGroup;
import com.hashmapinc.server.common.data.User;
import com.hashmapinc.server.common.data.UserPermission;
import com.hashmapinc.server.common.data.asset.Asset;
import com.hashmapinc.server.common.data.datamodel.AttributeDefinition;
import com.hashmapinc.server.common.data.datamodel.DataModel;
import com.hashmapinc.server.common.data.datamodel.DataModelObject;
import com.hashmapinc.server.common.data.id.*;
import com.hashmapinc.server.common.data.security.Authority;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class BaseAuthControllerTest extends AbstractControllerTest {

    @Test
    public void testGetUser() throws Exception {
        
        doGet("/api/auth/user")
        .andExpect(status().isUnauthorized());
        
        loginSysAdmin();
        doGet("/api/auth/user")
        .andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.authority", Matchers.is(Authority.SYS_ADMIN.name())))
        .andExpect(jsonPath("$.email",is(SYS_ADMIN_EMAIL)));
        
        loginTenantAdmin();
        doGet("/api/auth/user")
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.authority",is(Authority.TENANT_ADMIN.name())))
        .andExpect(jsonPath("$.email",is(TENANT_ADMIN_EMAIL)));
        
        loginCustomerUser();
        doGet("/api/auth/user")
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.authority",is(Authority.CUSTOMER_USER.name())))
        .andExpect(jsonPath("$.email",is(CUSTOMER_USER_EMAIL)));
    }
    
    @Test
    public void testLoginLogout() throws Exception {
        loginSysAdmin();
        doGet("/api/auth/user")
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.authority",is(Authority.SYS_ADMIN.name())))
        .andExpect(jsonPath("$.email",is(SYS_ADMIN_EMAIL)));

        logout();
        doGet("/api/auth/user")
        .andExpect(status().isUnauthorized());
    }

    @Test
    public void testRefreshToken() throws Exception {
        loginSysAdmin();
        doGet("/api/auth/user")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authority",is(Authority.SYS_ADMIN.name())))
                .andExpect(jsonPath("$.email",is(SYS_ADMIN_EMAIL)));

        refreshToken();
        doGet("/api/auth/user")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authority",is(Authority.SYS_ADMIN.name())))
                .andExpect(jsonPath("$.email",is(SYS_ADMIN_EMAIL)));
    }

    @Test
    public void testPolicyForAsset() throws Exception {
        CustomerId customerId = getCustomerIdOfCustomerUser();

        loginTenantAdmin();
        DataModel dataModel = createDataModel();
        DataModelObject dataModelObject = createDataModelObject(dataModel);
        Asset asset = createAsset(dataModelObject.getId(), customerId);
        String policy = String.format("CUSTOMER_USER:ASSET?%s=%s&%s=%s:READ",
                UserPermission.ResourceAttribute.ID, asset.getId().getId().toString(),
                UserPermission.ResourceAttribute.DATA_MODEL_ID, dataModelObject.getId().getId().toString());

        List<String> policies = Collections.singletonList(policy);

        CustomerGroup savedCustomerGroup = createGroupWithPolicies(policies, customerId);
        String getPolicyUrl = "/api/customer/group/" + savedCustomerGroup.getId().getId().toString() + "/policy";

        final Map<String, Map<String, String>> displayablePolicies = doGetTyped(getPolicyUrl, new TypeReference<Map<String, Map<String, String>>>() {
        });

        Assert.assertArrayEquals(policies.toArray(), displayablePolicies.keySet().toArray());
        Assert.assertEquals(displayablePolicies.get(policy).get(UserPermission.ResourceAttribute.ID.toString()), asset.getName());
        Assert.assertEquals(displayablePolicies.get(policy).get(UserPermission.ResourceAttribute.DATA_MODEL_ID.toString()), dataModelObject.getName());
        logout();


        UserId customerUserId = getCustomerUserId();
        assignUserToGroup(customerUserId, savedCustomerGroup);

        loginCustomerUser();
        Asset assetByCustomer1 = doGet("/api/asset/"+asset.getId().getId().toString(), Asset.class);
        Assert.assertEquals(asset.getId().getId(), assetByCustomer1.getId().getId());
        assetByCustomer1.setName("UpdatedAsset");
        doPost("/api/asset", assetByCustomer1).andExpect(status().is4xxClientError());
        logout();


        String policyNew = String.format("CUSTOMER_USER:ASSET?%s=%s&%s=%s:UPDATE",
                UserPermission.ResourceAttribute.ID, asset.getId().getId().toString(),
                UserPermission.ResourceAttribute.DATA_MODEL_ID, dataModelObject.getId().getId().toString());
        List<String> policiesNew = Collections.singletonList(policyNew);
        updateGroupWithPolicies(policiesNew, savedCustomerGroup);


        loginCustomerUser();
        assetByCustomer1.setName("UpdatedAssetName");
        Asset assetUpdated = doPost("/api/asset", assetByCustomer1, Asset.class);
        Assert.assertEquals(assetByCustomer1.getName(), assetUpdated.getName());
        logout();

        loginTenantAdmin();
        deleteGroup(savedCustomerGroup.getId());
        deleteAsset(asset.getId());
        deleteDataModelObject(dataModelObject.getId());
        logout();
    }

    private CustomerId getCustomerIdOfCustomerUser() throws Exception {
        loginCustomerUser();
        User tenantUser = doGet("/api/auth/user", User.class);
        CustomerId customerId = tenantUser.getCustomerId();
        logout();
        return customerId;
    }

    private void assignUserToGroup(UserId customerUserId, CustomerGroup savedCustomerGroup) throws Exception {
        loginTenantAdmin();
        doPost("/api/customer/group/"+savedCustomerGroup.getId().getId().toString()+"/users", Collections.singletonList(customerUserId.getId().toString()))
                .andExpect(status().isOk());
        logout();
    }

    private UserId getCustomerUserId() throws Exception {
        loginCustomerUser();
        User user = doGet("/api/auth/user", User.class);
        logout();
        return user.getId();
    }

    private CustomerGroup createGroupWithPolicies(List<String> policies, CustomerId customerId) throws Exception {
        CustomerGroup customerGroup = new CustomerGroup();
        customerGroup.setTitle("My Customer Group");
        customerGroup.setTenantId(tenantId);
        customerGroup.setCustomerId(customerId);
        customerGroup.setPolicies(policies);
        return doPost("/api/customer/group", customerGroup, CustomerGroup.class);
    }

    private CustomerGroup updateGroupWithPolicies(List<String> policies, CustomerGroup customerGroup) throws Exception {
        loginTenantAdmin();
        customerGroup.setPolicies(policies);
        CustomerGroup customerGroupReturned = doPost("/api/customer/group", customerGroup, CustomerGroup.class);
        logout();
        return customerGroupReturned;
    }

    private void deleteGroup(CustomerGroupId customerGroupId) throws Exception {
        doDelete("/api/customer/group/"+customerGroupId.getId().toString())
                .andExpect(status().isOk());
    }


    private DataModel createDataModel() throws Exception{
        DataModel dataModel = new DataModel();
        dataModel.setName("Default Drilling Data Model1");
        dataModel.setLastUpdatedTs(System.currentTimeMillis());
        return doPost("/api/data-model", dataModel, DataModel.class);
    }

    private DataModelObject createDataModelObject(DataModel dataModel) throws Exception{
        DataModelObject dataModelObject = new DataModelObject();
        dataModelObject.setName("Well");

        AttributeDefinition ad = new AttributeDefinition();
        ad.setValueType("STRING");
        ad.setName("attr name2");
        List<AttributeDefinition> attributeDefinitions = new ArrayList<>();
        attributeDefinitions.add(ad);
        dataModelObject.setAttributeDefinitions(attributeDefinitions);

        return doPost("/api/data-model/" + dataModel.getId().toString() + "/objects", dataModelObject, DataModelObject.class);
    }

    private void deleteDataModelObject(DataModelObjectId dataModelObjectId) throws Exception {
        doDelete("/api/data-model/objects/"+dataModelObjectId.getId().toString())
                .andExpect(status().isOk());
    }

    public Asset createAsset(DataModelObjectId dataModelObjectId, CustomerId customerId) throws Exception {
        Asset asset = new Asset();
        asset.setName("My asset");
        asset.setType("default");
        asset.setDataModelObjectId(dataModelObjectId);
        asset.setTenantId(tenantId);
        asset.setCustomerId(customerId);
        return doPost("/api/asset", asset, Asset.class);
    }

    private void deleteAsset(AssetId assetId) throws Exception {
        doDelete("/api/asset/"+assetId.getId().toString())
                .andExpect(status().isOk());
    }
}
