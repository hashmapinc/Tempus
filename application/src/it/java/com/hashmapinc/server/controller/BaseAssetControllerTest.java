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
import com.fasterxml.jackson.core.type.TypeReference;
import com.hashmapinc.server.common.data.*;
import com.hashmapinc.server.common.data.asset.Asset;
import com.hashmapinc.server.common.data.datamodel.DataModel;
import com.hashmapinc.server.common.data.datamodel.DataModelObject;
import com.hashmapinc.server.common.data.id.*;
import com.hashmapinc.server.common.data.page.TextPageData;
import com.hashmapinc.server.common.data.page.TextPageLink;
import com.hashmapinc.server.common.data.security.Authority;
import com.hashmapinc.server.dao.model.ModelConstants;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class BaseAssetControllerTest extends AbstractControllerTest {

    private IdComparator<Asset> idComparator = new IdComparator<>();

    @Before
    public void beforeTest() throws Exception {
        loginTenantAdmin();
    }

    @Test
    public void testSaveAndUpdateAsset() throws Exception {
        Asset savedAsset = createAsset(null, null, "My asset");

        savedAsset.setName("My new asset");
        doPost("/api/asset", savedAsset, Asset.class);

        Asset foundAsset = doGet("/api/asset/" + savedAsset.getId().getId().toString(), Asset.class);
        Assert.assertEquals(foundAsset.getName(), savedAsset.getName());
    }

    @Test
    public void testFindAssetById() throws Exception {
        Asset savedAsset = createAsset(null, null, "My asset");
        Asset foundAsset = doGet("/api/asset/" + savedAsset.getId().getId().toString(), Asset.class);
        Assert.assertNotNull(foundAsset);
        Assert.assertEquals(savedAsset, foundAsset);
    }

    @Test
    public void testFindAssetTypesByTenantId() throws Exception {
        List<Asset> assets = new ArrayList<>();
        for (int i=0;i<3;i++) {
            Asset asset = new Asset();
            asset.setName("My asset B"+i);
            asset.setType("typeB");
            assets.add(doPost("/api/asset", asset, Asset.class));
        }
        for (int i=0;i<7;i++) {
            Asset asset = new Asset();
            asset.setName("My asset C"+i);
            asset.setType("typeC");
            assets.add(doPost("/api/asset", asset, Asset.class));
        }
        for (int i=0;i<9;i++) {
            Asset asset = new Asset();
            asset.setName("My asset A"+i);
            asset.setType("typeA");
            assets.add(doPost("/api/asset", asset, Asset.class));
        }
        List<EntitySubtype> assetTypes = doGetTyped("/api/asset/types",
                new TypeReference<List<EntitySubtype>>(){});

        Assert.assertNotNull(assetTypes);
        Assert.assertTrue(assetTypes.stream().filter(a -> a.getType().equalsIgnoreCase("typeA")).count() == 1);
        Assert.assertTrue(assetTypes.stream().filter(a -> a.getType().equalsIgnoreCase("typeB")).count() == 1);
        Assert.assertTrue(assetTypes.stream().filter(a -> a.getType().equalsIgnoreCase("typeC")).count() == 1);
    }

    @Test
    public void testDeleteAsset() throws Exception {
        Asset savedAsset = createAsset(null, null, "My asset");
        deleteAsset(savedAsset.getId());

        doGet("/api/asset/"+savedAsset.getId().getId().toString())
                .andExpect(status().isNotFound());
    }

    @Test
    public void testSaveAssetWithEmptyType() throws Exception {
        Asset asset = new Asset();
        asset.setName("My asset");
        doPost("/api/asset", asset)
                .andExpect(status().isBadRequest())
                .andExpect(statusReason(containsString("Asset type should be specified")));
    }

    @Test
    public void testSaveAssetWithEmptyName() throws Exception {
        Asset asset = new Asset();
        asset.setType("default");
        doPost("/api/asset", asset)
                .andExpect(status().isBadRequest())
                .andExpect(statusReason(containsString("Asset name should be specified")));
    }

    @Test
    public void testAssignUnassignAssetToCustomer() throws Exception {
        Asset asset = new Asset();
        asset.setName("My asset");
        asset.setType("default");
        Asset savedAsset = doPost("/api/asset", asset, Asset.class);

        Asset assignedAsset = doPost("/api/customer/" + savedCustomer.getId().getId().toString()
                + "/asset/" + savedAsset.getId().getId().toString(), Asset.class);
        Assert.assertEquals(savedCustomer.getId(), assignedAsset.getCustomerId());

        Asset foundAsset = doGet("/api/asset/" + savedAsset.getId().getId().toString(), Asset.class);
        Assert.assertEquals(savedCustomer.getId(), foundAsset.getCustomerId());

        Asset unassignedAsset =
                doDelete("/api/customer/asset/" + savedAsset.getId().getId().toString(), Asset.class);
        Assert.assertEquals(ModelConstants.NULL_UUID, unassignedAsset.getCustomerId().getId());

        foundAsset = doGet("/api/asset/" + savedAsset.getId().getId().toString(), Asset.class);
        Assert.assertEquals(ModelConstants.NULL_UUID, foundAsset.getCustomerId().getId());
    }

    @Test
    public void testAssignAssetToNonExistentCustomer() throws Exception {
        Asset savedAsset = createAsset(null, null, "My asset");
        doPost("/api/customer/" + UUIDs.timeBased().toString()
                + "/asset/" + savedAsset.getId().getId().toString())
                .andExpect(status().isNotFound());
    }

    @Test
    public void testAssignAssetToCustomerFromDifferentTenant() throws Exception {
        loginSysAdmin();

        Tenant tenant2 = new Tenant();
        tenant2.setTitle("Different tenant");
        Tenant savedTenant2 = doPost("/api/tenant", tenant2, Tenant.class);
        Assert.assertNotNull(savedTenant2);

        User tenantAdmin2 = new User();
        tenantAdmin2.setAuthority(Authority.TENANT_ADMIN);
        tenantAdmin2.setTenantId(savedTenant2.getId());
        tenantAdmin2.setEmail("tenant3@tempus.org");
        tenantAdmin2.setFirstName("Joe");
        tenantAdmin2.setLastName("Downs");
        stubUser(tenantAdmin2, "testPassword1");
        createUserAndLogin(tenantAdmin2, "testPassword1");

        Customer customer = new Customer();
        customer.setTitle("Different customer");
        Customer savedCustomer = doPost("/api/customer", customer, Customer.class);

        loginTenantAdmin();

        Asset savedAsset = createAsset(null, null, "My asset");

        doPost("/api/customer/" + savedCustomer.getId().getId().toString()
                + "/asset/" + savedAsset.getId().getId().toString())
                .andExpect(status().isForbidden());

        loginSysAdmin();

        doDelete("/api/tenant/"+savedTenant2.getId().getId().toString())
                .andExpect(status().isOk());
    }

    @Test
    public void testFindTenantAssets() throws Exception {
        List<Asset> assets = new ArrayList<>();
        for (int i=0;i<178;i++) {
            Asset asset = new Asset();
            asset.setName("Asset"+i);
            asset.setType("default");
            assets.add(doPost("/api/asset", asset, Asset.class));
        }
        List<Asset> loadedAssets = new ArrayList<>();
        TextPageLink pageLink = new TextPageLink(23);
        TextPageData<Asset> pageData = null;
        do {
            pageData = doGetTypedWithPageLink("/api/tenant/assets?",
                    new TypeReference<TextPageData<Asset>>(){}, pageLink);
            loadedAssets.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());

        Collections.sort(assets, idComparator);
        Collections.sort(loadedAssets, idComparator);

        Assert.assertEquals(assets, loadedAssets);
    }

    @Test
    public void testFindTenantAssetsByName() throws Exception {
        String title1 = "Asset title 1";
        List<Asset> assetsTitle1 = new ArrayList<>();
        for (int i=0;i<143;i++) {
            Asset asset = new Asset();
            String suffix = RandomStringUtils.randomAlphanumeric(15);
            String name = title1+suffix;
            name = i % 2 == 0 ? name.toLowerCase() : name.toUpperCase();
            asset.setName(name);
            asset.setType("default");
            assetsTitle1.add(doPost("/api/asset", asset, Asset.class));
        }
        String title2 = "Asset title 2";
        List<Asset> assetsTitle2 = new ArrayList<>();
        for (int i=0;i<75;i++) {
            Asset asset = new Asset();
            String suffix = RandomStringUtils.randomAlphanumeric(15);
            String name = title2+suffix;
            name = i % 2 == 0 ? name.toLowerCase() : name.toUpperCase();
            asset.setName(name);
            asset.setType("default");
            assetsTitle2.add(doPost("/api/asset", asset, Asset.class));
        }

        List<Asset> loadedAssetsTitle1 = new ArrayList<>();
        TextPageLink pageLink = new TextPageLink(15, title1);
        TextPageData<Asset> pageData = null;
        do {
            pageData = doGetTypedWithPageLink("/api/tenant/assets?",
                    new TypeReference<TextPageData<Asset>>(){}, pageLink);
            loadedAssetsTitle1.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());

        Collections.sort(assetsTitle1, idComparator);
        Collections.sort(loadedAssetsTitle1, idComparator);

        Assert.assertEquals(assetsTitle1, loadedAssetsTitle1);

        List<Asset> loadedAssetsTitle2 = new ArrayList<>();
        pageLink = new TextPageLink(4, title2);
        do {
            pageData = doGetTypedWithPageLink("/api/tenant/assets?",
                    new TypeReference<TextPageData<Asset>>(){}, pageLink);
            loadedAssetsTitle2.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());

        Collections.sort(assetsTitle2, idComparator);
        Collections.sort(loadedAssetsTitle2, idComparator);

        Assert.assertEquals(assetsTitle2, loadedAssetsTitle2);

        for (Asset asset : loadedAssetsTitle1) {
            doDelete("/api/asset/"+asset.getId().getId().toString())
                    .andExpect(status().isOk());
        }

        pageLink = new TextPageLink(4, title1);
        pageData = doGetTypedWithPageLink("/api/tenant/assets?",
                new TypeReference<TextPageData<Asset>>(){}, pageLink);
        Assert.assertFalse(pageData.hasNext());
        Assert.assertEquals(0, pageData.getData().size());

        for (Asset asset : loadedAssetsTitle2) {
            doDelete("/api/asset/"+asset.getId().getId().toString())
                    .andExpect(status().isOk());
        }

        pageLink = new TextPageLink(4, title2);
        pageData = doGetTypedWithPageLink("/api/tenant/assets?",
                new TypeReference<TextPageData<Asset>>(){}, pageLink);
        Assert.assertFalse(pageData.hasNext());
        Assert.assertEquals(0, pageData.getData().size());
    }

    @Test
    public void testFindTenantAssetsByType() throws Exception {
        String title1 = "Asset title 1";
        String type1 = "typeA";
        List<Asset> assetsType1 = new ArrayList<>();
        for (int i=0;i<143;i++) {
            Asset asset = new Asset();
            String suffix = RandomStringUtils.randomAlphanumeric(15);
            String name = title1+suffix;
            name = i % 2 == 0 ? name.toLowerCase() : name.toUpperCase();
            asset.setName(name);
            asset.setType(type1);
            assetsType1.add(doPost("/api/asset", asset, Asset.class));
        }
        String title2 = "Asset title 2";
        String type2 = "typeB";
        List<Asset> assetsType2 = new ArrayList<>();
        for (int i=0;i<75;i++) {
            Asset asset = new Asset();
            String suffix = RandomStringUtils.randomAlphanumeric(15);
            String name = title2+suffix;
            name = i % 2 == 0 ? name.toLowerCase() : name.toUpperCase();
            asset.setName(name);
            asset.setType(type2);
            assetsType2.add(doPost("/api/asset", asset, Asset.class));
        }

        List<Asset> loadedAssetsType1 = new ArrayList<>();
        TextPageLink pageLink = new TextPageLink(15);
        TextPageData<Asset> pageData = null;
        do {
            pageData = doGetTypedWithPageLink("/api/tenant/assets?type={type}&",
                    new TypeReference<TextPageData<Asset>>(){}, pageLink, type1);
            loadedAssetsType1.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());

        Collections.sort(assetsType1, idComparator);
        Collections.sort(loadedAssetsType1, idComparator);

        Assert.assertEquals(assetsType1, loadedAssetsType1);

        List<Asset> loadedAssetsType2 = new ArrayList<>();
        pageLink = new TextPageLink(4);
        do {
            pageData = doGetTypedWithPageLink("/api/tenant/assets?type={type}&",
                    new TypeReference<TextPageData<Asset>>(){}, pageLink, type2);
            loadedAssetsType2.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());

        Collections.sort(assetsType2, idComparator);
        Collections.sort(loadedAssetsType2, idComparator);

        Assert.assertEquals(assetsType2, loadedAssetsType2);

        for (Asset asset : loadedAssetsType1) {
            doDelete("/api/asset/"+asset.getId().getId().toString())
                    .andExpect(status().isOk());
        }

        pageLink = new TextPageLink(4);
        pageData = doGetTypedWithPageLink("/api/tenant/assets?type={type}&",
                new TypeReference<TextPageData<Asset>>(){}, pageLink, type1);
        Assert.assertFalse(pageData.hasNext());
        Assert.assertEquals(0, pageData.getData().size());

        for (Asset asset : loadedAssetsType2) {
            doDelete("/api/asset/"+asset.getId().getId().toString())
                    .andExpect(status().isOk());
        }

        pageLink = new TextPageLink(4);
        pageData = doGetTypedWithPageLink("/api/tenant/assets?type={type}&",
                new TypeReference<TextPageData<Asset>>(){}, pageLink, type2);
        Assert.assertFalse(pageData.hasNext());
        Assert.assertEquals(0, pageData.getData().size());
    }

    @Test
    public void testFindCustomerAssets() throws Exception {
        CustomerId customerId = savedCustomer.getId();

        List<Asset> assets = new ArrayList<>();
        for (int i=0;i<128;i++) {
            Asset asset = new Asset();
            asset.setName("Asset"+i);
            asset.setType("default");
            asset = doPost("/api/asset", asset, Asset.class);
            assets.add(doPost("/api/customer/" + customerId.getId().toString()
                    + "/asset/" + asset.getId().getId().toString(), Asset.class));
        }

        List<Asset> loadedAssets = new ArrayList<>();
        TextPageLink pageLink = new TextPageLink(23);
        TextPageData<Asset> pageData = null;
        do {
            pageData = doGetTypedWithPageLink("/api/customer/" + customerId.getId().toString() + "/assets?",
                    new TypeReference<TextPageData<Asset>>(){}, pageLink);
            loadedAssets.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());

        Collections.sort(assets, idComparator);
        Collections.sort(loadedAssets, idComparator);

        Assert.assertEquals(assets, loadedAssets);
    }

    @Test
    public void testFindCustomerAssetsByName() throws Exception {
        CustomerId customerId = savedCustomer.getId();

        String title1 = "Asset title 1";
        List<Asset> assetsTitle1 = new ArrayList<>();
        for (int i=0;i<125;i++) {
            Asset asset = new Asset();
            String suffix = RandomStringUtils.randomAlphanumeric(15);
            String name = title1+suffix;
            name = i % 2 == 0 ? name.toLowerCase() : name.toUpperCase();
            asset.setName(name);
            asset.setType("default");
            asset = doPost("/api/asset", asset, Asset.class);
            assetsTitle1.add(doPost("/api/customer/" + customerId.getId().toString()
                    + "/asset/" + asset.getId().getId().toString(), Asset.class));
        }
        String title2 = "Asset title 2";
        List<Asset> assetsTitle2 = new ArrayList<>();
        for (int i=0;i<143;i++) {
            Asset asset = new Asset();
            String suffix = RandomStringUtils.randomAlphanumeric(15);
            String name = title2+suffix;
            name = i % 2 == 0 ? name.toLowerCase() : name.toUpperCase();
            asset.setName(name);
            asset.setType("default");
            asset = doPost("/api/asset", asset, Asset.class);
            assetsTitle2.add(doPost("/api/customer/" + customerId.getId().toString()
                    + "/asset/" + asset.getId().getId().toString(), Asset.class));
        }

        List<Asset> loadedAssetsTitle1 = new ArrayList<>();
        TextPageLink pageLink = new TextPageLink(15, title1);
        TextPageData<Asset> pageData = null;
        do {
            pageData = doGetTypedWithPageLink("/api/customer/" + customerId.getId().toString() + "/assets?",
                    new TypeReference<TextPageData<Asset>>(){}, pageLink);
            loadedAssetsTitle1.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());

        Collections.sort(assetsTitle1, idComparator);
        Collections.sort(loadedAssetsTitle1, idComparator);

        Assert.assertEquals(assetsTitle1, loadedAssetsTitle1);

        List<Asset> loadedAssetsTitle2 = new ArrayList<>();
        pageLink = new TextPageLink(4, title2);
        do {
            pageData = doGetTypedWithPageLink("/api/customer/" + customerId.getId().toString() + "/assets?",
                    new TypeReference<TextPageData<Asset>>(){}, pageLink);
            loadedAssetsTitle2.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());

        Collections.sort(assetsTitle2, idComparator);
        Collections.sort(loadedAssetsTitle2, idComparator);

        Assert.assertEquals(assetsTitle2, loadedAssetsTitle2);

        for (Asset asset : loadedAssetsTitle1) {
            doDelete("/api/customer/asset/" + asset.getId().getId().toString())
                    .andExpect(status().isOk());
        }

        pageLink = new TextPageLink(4, title1);
        pageData = doGetTypedWithPageLink("/api/customer/" + customerId.getId().toString() + "/assets?",
                new TypeReference<TextPageData<Asset>>(){}, pageLink);
        Assert.assertFalse(pageData.hasNext());
        Assert.assertEquals(0, pageData.getData().size());

        for (Asset asset : loadedAssetsTitle2) {
            doDelete("/api/customer/asset/" + asset.getId().getId().toString())
                    .andExpect(status().isOk());
        }

        pageLink = new TextPageLink(4, title2);
        pageData = doGetTypedWithPageLink("/api/customer/" + customerId.getId().toString() + "/assets?",
                new TypeReference<TextPageData<Asset>>(){}, pageLink);
        Assert.assertFalse(pageData.hasNext());
        Assert.assertEquals(0, pageData.getData().size());
    }

    @Test
    public void testFindCustomerAssetsByType() throws Exception {
        CustomerId customerId = savedCustomer.getId();

        String title1 = "Asset title 1";
        String type1 = "typeC";
        List<Asset> assetsType1 = new ArrayList<>();
        for (int i=0;i<125;i++) {
            Asset asset = new Asset();
            String suffix = RandomStringUtils.randomAlphanumeric(15);
            String name = title1+suffix;
            name = i % 2 == 0 ? name.toLowerCase() : name.toUpperCase();
            asset.setName(name);
            asset.setType(type1);
            asset = doPost("/api/asset", asset, Asset.class);
            assetsType1.add(doPost("/api/customer/" + customerId.getId().toString()
                    + "/asset/" + asset.getId().getId().toString(), Asset.class));
        }
        String title2 = "Asset title 2";
        String type2 = "typeD";
        List<Asset> assetsType2 = new ArrayList<>();
        for (int i=0;i<143;i++) {
            Asset asset = new Asset();
            String suffix = RandomStringUtils.randomAlphanumeric(15);
            String name = title2+suffix;
            name = i % 2 == 0 ? name.toLowerCase() : name.toUpperCase();
            asset.setName(name);
            asset.setType(type2);
            asset = doPost("/api/asset", asset, Asset.class);
            assetsType2.add(doPost("/api/customer/" + customerId.getId().toString()
                    + "/asset/" + asset.getId().getId().toString(), Asset.class));
        }

        List<Asset> loadedAssetsType1 = new ArrayList<>();
        TextPageLink pageLink = new TextPageLink(15);
        TextPageData<Asset> pageData = null;
        do {
            pageData = doGetTypedWithPageLink("/api/customer/" + customerId.getId().toString() + "/assets?type={type}&",
                    new TypeReference<TextPageData<Asset>>(){}, pageLink, type1);
            loadedAssetsType1.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());

        Collections.sort(assetsType1, idComparator);
        Collections.sort(loadedAssetsType1, idComparator);

        Assert.assertEquals(assetsType1, loadedAssetsType1);

        List<Asset> loadedAssetsType2 = new ArrayList<>();
        pageLink = new TextPageLink(4);
        do {
            pageData = doGetTypedWithPageLink("/api/customer/" + customerId.getId().toString() + "/assets?type={type}&",
                    new TypeReference<TextPageData<Asset>>(){}, pageLink, type2);
            loadedAssetsType2.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());

        Collections.sort(assetsType2, idComparator);
        Collections.sort(loadedAssetsType2, idComparator);

        Assert.assertEquals(assetsType2, loadedAssetsType2);

        for (Asset asset : loadedAssetsType1) {
            doDelete("/api/customer/asset/" + asset.getId().getId().toString())
                    .andExpect(status().isOk());
        }

        pageLink = new TextPageLink(4);
        pageData = doGetTypedWithPageLink("/api/customer/" + customerId.getId().toString() + "/assets?type={type}&",
                new TypeReference<TextPageData<Asset>>(){}, pageLink, type1);
        Assert.assertFalse(pageData.hasNext());
        Assert.assertEquals(0, pageData.getData().size());

        for (Asset asset : loadedAssetsType2) {
            doDelete("/api/customer/asset/" + asset.getId().getId().toString())
                    .andExpect(status().isOk());
        }

        pageLink = new TextPageLink(4);
        pageData = doGetTypedWithPageLink("/api/customer/" + customerId.getId().toString() + "/assets?type={type}&",
                new TypeReference<TextPageData<Asset>>(){}, pageLink, type2);
        Assert.assertFalse(pageData.hasNext());
        Assert.assertEquals(0, pageData.getData().size());
    }

    @Test
    public void testPolicyForAsset() throws Exception {

        DataModel dataModel = createDataModel();
        DataModelObject dataModelObject = createDataModelObject(dataModel);
        Asset asset = createAsset(dataModelObject.getId(), customerUser.getCustomerId(), "Tenant's asset");
        String policy = String.format("CUSTOMER_USER:ASSET?%s=%s&%s=%s:READ",
                UserPermission.ResourceAttribute.ID, asset.getId().getId().toString(),
                UserPermission.ResourceAttribute.DATA_MODEL_ID, dataModelObject.getId().getId().toString());

        List<String> policies = Collections.singletonList(policy);

        CustomerGroup savedCustomerGroup = createGroupWithPolicies(policies, customerUser.getCustomerId(), "My Customer Group");
        String getPolicyUrl = "/api/customer/group/" + savedCustomerGroup.getId().getId().toString() + "/policy";

        final Map<String, Map<String, String>> displayablePolicies = doGetTyped(getPolicyUrl, new TypeReference<Map<String, Map<String, String>>>() {
        });

        Assert.assertArrayEquals(policies.toArray(), displayablePolicies.keySet().toArray());
        Assert.assertEquals(displayablePolicies.get(policy).get(UserPermission.ResourceAttribute.ID.toString()), asset.getName());
        Assert.assertEquals(displayablePolicies.get(policy).get(UserPermission.ResourceAttribute.DATA_MODEL_ID.toString()), dataModelObject.getName());


        UserId customerUserId = getCustomerUserId();
        assignUserToGroup(customerUserId, savedCustomerGroup);

        logout();
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


        logout();
        loginCustomerUser();
        assetByCustomer1.setName("UpdatedAssetName");
        Asset assetUpdated = doPost("/api/asset", assetByCustomer1, Asset.class);
        Assert.assertEquals(assetByCustomer1.getName(), assetUpdated.getName());
        logout();

        String policyNew1 = String.format("CUSTOMER_USER:ASSET?%s=%s:CREATE",
                UserPermission.ResourceAttribute.DATA_MODEL_ID, dataModelObject.getId().getId().toString());
        List<String> policiesNew1 = Collections.singletonList(policyNew1);
        updateGroupWithPolicies(policiesNew1, savedCustomerGroup);

        logout();
        loginCustomerUser();
        Asset asset1 = createAsset(dataModelObject.getId(), customerUser.getCustomerId(), "Customer's asset");
        logout();

        loginTenantAdmin();
        deleteGroup(savedCustomerGroup.getId());
        deleteAsset(asset.getId());
        deleteAsset(asset1.getId());
        deleteDataModelObject(dataModelObject.getId());
        logout();
    }

    @Test
    public void findAllAssetsByDataModeObject() throws Exception{
        DataModel dataModel = createDataModel();
        DataModelObject dataModelObject = createDataModelObject(dataModel);

        String policyNew1 = String.format("CUSTOMER_USER:ASSET?%s=%s:READ",
                UserPermission.ResourceAttribute.DATA_MODEL_ID, dataModelObject.getId().getId().toString());


        String policyNew2 = String.format("CUSTOMER_USER:ASSET?%s=%s:CREATE",
                UserPermission.ResourceAttribute.DATA_MODEL_ID, dataModelObject.getId().getId().toString());
        List<String> policies = new ArrayList<>();
        policies.add(policyNew1);
        policies.add(policyNew2);
        CustomerGroup savedCustomerGroup = createGroupWithPolicies(policies, customerUser.getCustomerId(), "My Customer Group");
        UserId customerUserId = getCustomerUserId();
        assignUserToGroup(customerUserId, savedCustomerGroup);

        logout();
        loginCustomerUser();
        for (int i = 0; i < 20; i++) {
            createAsset(dataModelObject.getId(), customerUser.getCustomerId(), "Customer's asset"+i);
        }

        TextPageLink pageLink = new TextPageLink(15);
        TextPageData<Asset> pageData  = doGetTypedWithPageLink("/api/datamodelobject/assets/"+ dataModelObject.getId().getId().toString()+"?",
                new TypeReference<TextPageData<Asset>>(){}, pageLink);
        Assert.assertEquals(15, pageData.getData().size());

        TextPageData<Asset> pageData1  = doGetTypedWithPageLink("/api/datamodelobject/assets/"+ dataModelObject.getId().getId().toString()+"?",
                new TypeReference<TextPageData<Asset>>(){}, pageData.getNextPageLink());
        Assert.assertEquals(5, pageData1.getData().size());
        logout();

        unAssignUserFromGroup(savedCustomerGroup.getId(), customerUserId);

        logout();
        loginTenantAdmin();
        int i = 0;
        List<String> policies1 = new ArrayList<>();
        while (i < 20){
            Asset asset_i = createAsset(dataModelObject.getId(), customerUser.getCustomerId(), "Tenant's asset "+ i);
            String policy_i = String.format("CUSTOMER_USER:ASSET?%s=%s&%s=%s:READ",
                    UserPermission.ResourceAttribute.ID, asset_i.getId().getId().toString(),
                    UserPermission.ResourceAttribute.DATA_MODEL_ID, dataModelObject.getId().getId().toString());
            if(i % 2 == 0)
                policies1.add(policy_i);
            i++;

        }
        CustomerGroup savedCustomerGroup1 = createGroupWithPolicies(policies1, customerUser.getCustomerId(), "My Customer Group New");

        String getPolicyUrl1 = "/api/customer/group/" + savedCustomerGroup1.getId().getId().toString() + "/policy";
        doGet(getPolicyUrl1).andExpect(status().isOk());
        assignUserToGroup(customerUserId, savedCustomerGroup1);

        logout();
        loginCustomerUser();
        TextPageLink pageLink11 = new TextPageLink(7);
        TextPageData<Asset> pageData11  = doGetTypedWithPageLink("/api/datamodelobject/assets/"+ dataModelObject.getId().getId().toString()+"?",
                new TypeReference<TextPageData<Asset>>(){}, pageLink11);
        Assert.assertEquals(7, pageData11.getData().size());

        TextPageData<Asset> pageData12  = doGetTypedWithPageLink("/api/datamodelobject/assets/"+ dataModelObject.getId().getId().toString()+"?",
                new TypeReference<TextPageData<Asset>>(){}, pageData11.getNextPageLink());
        Assert.assertEquals(3, pageData12.getData().size());
        logout();

    }

}
