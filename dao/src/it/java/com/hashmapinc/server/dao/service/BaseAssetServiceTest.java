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

import com.datastax.driver.core.utils.UUIDs;
import com.hashmapinc.server.common.data.*;
import com.hashmapinc.server.common.data.asset.Asset;
import com.hashmapinc.server.common.data.id.AssetId;
import com.hashmapinc.server.common.data.id.DataModelObjectId;
import com.hashmapinc.server.common.data.page.TextPageLink;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.hashmapinc.server.common.data.id.CustomerId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.page.TextPageData;
import com.hashmapinc.server.dao.exception.DataValidationException;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public abstract class BaseAssetServiceTest extends AbstractServiceTest {

    private IdComparator<Asset> idComparator = new IdComparator<>();

    protected TenantId tenantId1;
    protected TenantId tenantId2;

    @Before
    public void before() {
        Tenant tenant = new Tenant();
        tenant.setTitle("My tenant");
        Tenant savedTenant = tenantService.saveTenant(tenant);
        Assert.assertNotNull(savedTenant);
        tenantId1 = savedTenant.getId();

        tenant.setTitle("My tenant2");
        savedTenant = tenantService.saveTenant(tenant);
        Assert.assertNotNull(savedTenant);
        tenantId2 = savedTenant.getId();
    }

    @After
    public void after() {
        tenantService.deleteTenant(tenantId1);
        tenantService.deleteTenant(tenantId2);
    }

    @Test
    public void testSaveAsset() {
        Asset savedAsset = createAsset(null, tenantId1);

        savedAsset.setName("My new asset");

        assetService.saveAsset(savedAsset);
        Asset foundAsset = assetService.findAssetById(savedAsset.getId());
        Assert.assertEquals(foundAsset.getName(), savedAsset.getName());

        deleteAsset(savedAsset.getId());
    }

    @Test(expected = DataValidationException.class)
    public void testSaveAssetWithEmptyName() {
        Asset asset = new Asset();
        asset.setTenantId(tenantId1);
        asset.setType("default");
        assetService.saveAsset(asset);
    }

    @Test(expected = DataValidationException.class)
    public void testSaveAssetWithEmptyTenant() {
        Asset asset = new Asset();
        asset.setName("My asset");
        asset.setType("default");
        assetService.saveAsset(asset);
    }

    @Test(expected = DataValidationException.class)
    public void testSaveAssetWithInvalidTenant() {
        Asset asset = new Asset();
        asset.setName("My asset");
        asset.setType("default");
        asset.setTenantId(new TenantId(UUIDs.timeBased()));
        assetService.saveAsset(asset);
    }

    @Test(expected = DataValidationException.class)
    public void testAssignAssetToNonExistentCustomer() {
        Asset asset = new Asset();
        asset.setName("My asset");
        asset.setType("default");
        asset.setTenantId(tenantId1);
        asset = assetService.saveAsset(asset);
        try {
            assetService.assignAssetToCustomer(asset.getId(), new CustomerId(UUIDs.timeBased()));
        } finally {
            assetService.deleteAsset(asset.getId());
        }
    }

    @Test(expected = DataValidationException.class)
    public void testAssignAssetToCustomerFromDifferentTenant() {
        Asset asset = new Asset();
        asset.setName("My asset");
        asset.setType("default");
        asset.setTenantId(tenantId1);
        asset = assetService.saveAsset(asset);
        Tenant tenant = new Tenant();
        tenant.setTitle("Test different tenant");
        tenant = tenantService.saveTenant(tenant);
        Customer customer = new Customer();
        customer.setTenantId(tenant.getId());
        customer.setTitle("Test different customer");
        customer = customerService.saveCustomer(customer);
        try {
            assetService.assignAssetToCustomer(asset.getId(), customer.getId());
        } finally {
            assetService.deleteAsset(asset.getId());
            tenantService.deleteTenant(tenant.getId());
        }
    }

    @Test
    public void testFindAssetById() {
        Asset savedAsset = createAsset(null, tenantId1);
        Asset foundAsset = assetService.findAssetById(savedAsset.getId());
        Assert.assertNotNull(foundAsset);
        Assert.assertEquals(savedAsset, foundAsset);
        assetService.deleteAsset(savedAsset.getId());
    }

    @Test
    public void testFindAssetTypesByTenantId() throws Exception {
        List<Asset> assets = new ArrayList<>();
        try {
            for (int i=0;i<3;i++) {
                Asset asset = new Asset();
                asset.setTenantId(tenantId1);
                asset.setName("My asset B"+i);
                asset.setType("typeB");
                assets.add(assetService.saveAsset(asset));
            }
            for (int i=0;i<7;i++) {
                Asset asset = new Asset();
                asset.setTenantId(tenantId1);
                asset.setName("My asset C"+i);
                asset.setType("typeC");
                assets.add(assetService.saveAsset(asset));
            }
            for (int i=0;i<9;i++) {
                Asset asset = new Asset();
                asset.setTenantId(tenantId1);
                asset.setName("My asset A"+i);
                asset.setType("typeA");
                assets.add(assetService.saveAsset(asset));
            }
            List<EntitySubtype> assetTypes = assetService.findAssetTypesByTenantId(tenantId1).get();
            Assert.assertNotNull(assetTypes);
            Assert.assertEquals(3, assetTypes.size());
            Assert.assertEquals("typeA", assetTypes.get(0).getType());
            Assert.assertEquals("typeB", assetTypes.get(1).getType());
            Assert.assertEquals("typeC", assetTypes.get(2).getType());
        } finally {
            assets.forEach((asset) -> { assetService.deleteAsset(asset.getId()); });
        }
    }

    @Test
    public void testDeleteAsset() {
        Asset savedAsset = createAsset(null, tenantId1);
        deleteAsset(savedAsset.getId());
        Asset foundAsset = assetService.findAssetById(savedAsset.getId());
        Assert.assertNull(foundAsset);
    }

    @Test
    public void testFindAssetsByTenantId() {
        Tenant tenant = new Tenant();
        tenant.setTitle("Test tenant");
        tenant = tenantService.saveTenant(tenant);

        TenantId tenantId = tenant.getId();

        List<Asset> assets = new ArrayList<>();
        for (int i=0;i<178;i++) {
            Asset asset = new Asset();
            asset.setTenantId(tenantId);
            asset.setName("Asset"+i);
            asset.setType("default");
            assets.add(assetService.saveAsset(asset));
        }

        List<Asset> loadedAssets = new ArrayList<>();
        TextPageLink pageLink = new TextPageLink(23);
        TextPageData<Asset> pageData = null;
        do {
            pageData = assetService.findAssetsByTenantId(tenantId, pageLink);
            loadedAssets.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());

        Collections.sort(assets, idComparator);
        Collections.sort(loadedAssets, idComparator);

        Assert.assertEquals(assets, loadedAssets);

        assetService.deleteAssetsByTenantId(tenantId);

        pageLink = new TextPageLink(33);
        pageData = assetService.findAssetsByTenantId(tenantId, pageLink);
        Assert.assertFalse(pageData.hasNext());
        Assert.assertTrue(pageData.getData().isEmpty());

        tenantService.deleteTenant(tenantId);
    }

    @Test
    public void testFindAssetsByTenantIdAndName() {
        String title1 = "Asset title 1";
        List<Asset> assetsTitle1 = new ArrayList<>();
        for (int i=0;i<143;i++) {
            Asset asset = new Asset();
            asset.setTenantId(tenantId1);
            String suffix = RandomStringUtils.randomAlphanumeric(15);
            String name = title1+suffix;
            name = i % 2 == 0 ? name.toLowerCase() : name.toUpperCase();
            asset.setName(name);
            asset.setType("default");
            assetsTitle1.add(assetService.saveAsset(asset));
        }
        String title2 = "Asset title 2";
        List<Asset> assetsTitle2 = new ArrayList<>();
        for (int i=0;i<175;i++) {
            Asset asset = new Asset();
            asset.setTenantId(tenantId1);
            String suffix = RandomStringUtils.randomAlphanumeric(15);
            String name = title2+suffix;
            name = i % 2 == 0 ? name.toLowerCase() : name.toUpperCase();
            asset.setName(name);
            asset.setType("default");
            assetsTitle2.add(assetService.saveAsset(asset));
        }

        List<Asset> loadedAssetsTitle1 = new ArrayList<>();
        TextPageLink pageLink = new TextPageLink(15, title1);
        TextPageData<Asset> pageData = null;
        do {
            pageData = assetService.findAssetsByTenantId(tenantId1, pageLink);
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
            pageData = assetService.findAssetsByTenantId(tenantId1, pageLink);
            loadedAssetsTitle2.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());

        Collections.sort(assetsTitle2, idComparator);
        Collections.sort(loadedAssetsTitle2, idComparator);

        Assert.assertEquals(assetsTitle2, loadedAssetsTitle2);

        for (Asset asset : loadedAssetsTitle1) {
            assetService.deleteAsset(asset.getId());
        }

        pageLink = new TextPageLink(4, title1);
        pageData = assetService.findAssetsByTenantId(tenantId1, pageLink);
        Assert.assertFalse(pageData.hasNext());
        Assert.assertEquals(0, pageData.getData().size());

        for (Asset asset : loadedAssetsTitle2) {
            assetService.deleteAsset(asset.getId());
        }

        pageLink = new TextPageLink(4, title2);
        pageData = assetService.findAssetsByTenantId(tenantId1, pageLink);
        Assert.assertFalse(pageData.hasNext());
        Assert.assertEquals(0, pageData.getData().size());
    }

    @Test
    public void testFindAssetsByTenantIdAndType() {
        String title1 = "Asset title 1";
        String type1 = "typeA";
        List<Asset> assetsType1 = new ArrayList<>();
        for (int i=0;i<143;i++) {
            Asset asset = new Asset();
            asset.setTenantId(tenantId1);
            String suffix = RandomStringUtils.randomAlphanumeric(15);
            String name = title1+suffix;
            name = i % 2 == 0 ? name.toLowerCase() : name.toUpperCase();
            asset.setName(name);
            asset.setType(type1);
            assetsType1.add(assetService.saveAsset(asset));
        }
        String title2 = "Asset title 2";
        String type2 = "typeB";
        List<Asset> assetsType2 = new ArrayList<>();
        for (int i=0;i<175;i++) {
            Asset asset = new Asset();
            asset.setTenantId(tenantId1);
            String suffix = RandomStringUtils.randomAlphanumeric(15);
            String name = title2+suffix;
            name = i % 2 == 0 ? name.toLowerCase() : name.toUpperCase();
            asset.setName(name);
            asset.setType(type2);
            assetsType2.add(assetService.saveAsset(asset));
        }

        List<Asset> loadedAssetsType1 = new ArrayList<>();
        TextPageLink pageLink = new TextPageLink(15);
        TextPageData<Asset> pageData = null;
        do {
            pageData = assetService.findAssetsByTenantIdAndType(tenantId1, type1, pageLink);
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
            pageData = assetService.findAssetsByTenantIdAndType(tenantId1, type2, pageLink);
            loadedAssetsType2.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());

        Collections.sort(assetsType2, idComparator);
        Collections.sort(loadedAssetsType2, idComparator);

        Assert.assertEquals(assetsType2, loadedAssetsType2);

        for (Asset asset : loadedAssetsType1) {
            assetService.deleteAsset(asset.getId());
        }

        pageLink = new TextPageLink(4);
        pageData = assetService.findAssetsByTenantIdAndType(tenantId1, type1, pageLink);
        Assert.assertFalse(pageData.hasNext());
        Assert.assertEquals(0, pageData.getData().size());

        for (Asset asset : loadedAssetsType2) {
            assetService.deleteAsset(asset.getId());
        }

        pageLink = new TextPageLink(4);
        pageData = assetService.findAssetsByTenantIdAndType(tenantId1, type2, pageLink);
        Assert.assertFalse(pageData.hasNext());
        Assert.assertEquals(0, pageData.getData().size());
    }

    @Test
    public void testFindAssetsByTenantIdAndCustomerId() {
        Tenant tenant = new Tenant();
        tenant.setTitle("Test tenant");
        tenant = tenantService.saveTenant(tenant);

        TenantId tenantId = tenant.getId();

        Customer customer = new Customer();
        customer.setTitle("Test customer");
        customer.setTenantId(tenantId);
        customer = customerService.saveCustomer(customer);
        CustomerId customerId = customer.getId();

        List<Asset> assets = new ArrayList<>();
        for (int i=0;i<278;i++) {
            Asset asset = new Asset();
            asset.setTenantId(tenantId);
            asset.setName("Asset"+i);
            asset.setType("default");
            asset = assetService.saveAsset(asset);
            assets.add(assetService.assignAssetToCustomer(asset.getId(), customerId));
        }

        List<Asset> loadedAssets = new ArrayList<>();
        TextPageLink pageLink = new TextPageLink(23);
        TextPageData<Asset> pageData = null;
        do {
            pageData = assetService.findAssetsByTenantIdAndCustomerId(tenantId, customerId, pageLink);
            loadedAssets.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());

        Collections.sort(assets, idComparator);
        Collections.sort(loadedAssets, idComparator);

        Assert.assertEquals(assets, loadedAssets);

        assetService.unassignCustomerAssets(tenantId, customerId);

        pageLink = new TextPageLink(33);
        pageData = assetService.findAssetsByTenantIdAndCustomerId(tenantId, customerId, pageLink);
        Assert.assertFalse(pageData.hasNext());
        Assert.assertTrue(pageData.getData().isEmpty());

        tenantService.deleteTenant(tenantId);
    }

    @Test
    public void testFindAssetsByTenantIdCustomerIdAndName() {

        Customer customer = new Customer();
        customer.setTitle("Test customer");
        customer.setTenantId(tenantId1);
        customer = customerService.saveCustomer(customer);
        CustomerId customerId = customer.getId();

        String title1 = "Asset title 1";
        List<Asset> assetsTitle1 = new ArrayList<>();
        for (int i=0;i<175;i++) {
            Asset asset = new Asset();
            asset.setTenantId(tenantId1);
            String suffix = RandomStringUtils.randomAlphanumeric(15);
            String name = title1+suffix;
            name = i % 2 == 0 ? name.toLowerCase() : name.toUpperCase();
            asset.setName(name);
            asset.setType("default");
            asset = assetService.saveAsset(asset);
            assetsTitle1.add(assetService.assignAssetToCustomer(asset.getId(), customerId));
        }
        String title2 = "Asset title 2";
        List<Asset> assetsTitle2 = new ArrayList<>();
        for (int i=0;i<143;i++) {
            Asset asset = new Asset();
            asset.setTenantId(tenantId1);
            String suffix = RandomStringUtils.randomAlphanumeric(15);
            String name = title2+suffix;
            name = i % 2 == 0 ? name.toLowerCase() : name.toUpperCase();
            asset.setName(name);
            asset.setType("default");
            asset = assetService.saveAsset(asset);
            assetsTitle2.add(assetService.assignAssetToCustomer(asset.getId(), customerId));
        }

        List<Asset> loadedAssetsTitle1 = new ArrayList<>();
        TextPageLink pageLink = new TextPageLink(15, title1);
        TextPageData<Asset> pageData = null;
        do {
            pageData = assetService.findAssetsByTenantIdAndCustomerId(tenantId1, customerId, pageLink);
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
            pageData = assetService.findAssetsByTenantIdAndCustomerId(tenantId1, customerId, pageLink);
            loadedAssetsTitle2.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());

        Collections.sort(assetsTitle2, idComparator);
        Collections.sort(loadedAssetsTitle2, idComparator);

        Assert.assertEquals(assetsTitle2, loadedAssetsTitle2);

        for (Asset asset : loadedAssetsTitle1) {
            assetService.deleteAsset(asset.getId());
        }

        pageLink = new TextPageLink(4, title1);
        pageData = assetService.findAssetsByTenantIdAndCustomerId(tenantId1, customerId, pageLink);
        Assert.assertFalse(pageData.hasNext());
        Assert.assertEquals(0, pageData.getData().size());

        for (Asset asset : loadedAssetsTitle2) {
            assetService.deleteAsset(asset.getId());
        }

        pageLink = new TextPageLink(4, title2);
        pageData = assetService.findAssetsByTenantIdAndCustomerId(tenantId1, customerId, pageLink);
        Assert.assertFalse(pageData.hasNext());
        Assert.assertEquals(0, pageData.getData().size());
        customerService.deleteCustomer(customerId);
    }

    @Test
    public void testFindAssetsByTenantIdCustomerIdAndType() {

        Customer customer = new Customer();
        customer.setTitle("Test customer");
        customer.setTenantId(tenantId1);
        customer = customerService.saveCustomer(customer);
        CustomerId customerId = customer.getId();

        String title1 = "Asset title 1";
        String type1 = "typeC";
        List<Asset> assetsType1 = new ArrayList<>();
        for (int i=0;i<175;i++) {
            Asset asset = new Asset();
            asset.setTenantId(tenantId1);
            String suffix = RandomStringUtils.randomAlphanumeric(15);
            String name = title1+suffix;
            name = i % 2 == 0 ? name.toLowerCase() : name.toUpperCase();
            asset.setName(name);
            asset.setType(type1);
            asset = assetService.saveAsset(asset);
            assetsType1.add(assetService.assignAssetToCustomer(asset.getId(), customerId));
        }
        String title2 = "Asset title 2";
        String type2 = "typeD";
        List<Asset> assetsType2 = new ArrayList<>();
        for (int i=0;i<143;i++) {
            Asset asset = new Asset();
            asset.setTenantId(tenantId1);
            String suffix = RandomStringUtils.randomAlphanumeric(15);
            String name = title2+suffix;
            name = i % 2 == 0 ? name.toLowerCase() : name.toUpperCase();
            asset.setName(name);
            asset.setType(type2);
            asset = assetService.saveAsset(asset);
            assetsType2.add(assetService.assignAssetToCustomer(asset.getId(), customerId));
        }

        List<Asset> loadedAssetsType1 = new ArrayList<>();
        TextPageLink pageLink = new TextPageLink(15);
        TextPageData<Asset> pageData = null;
        do {
            pageData = assetService.findAssetsByTenantIdAndCustomerIdAndType(tenantId1, customerId, type1, pageLink);
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
            pageData = assetService.findAssetsByTenantIdAndCustomerIdAndType(tenantId1, customerId, type2, pageLink);
            loadedAssetsType2.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());

        Collections.sort(assetsType2, idComparator);
        Collections.sort(loadedAssetsType2, idComparator);

        Assert.assertEquals(assetsType2, loadedAssetsType2);

        for (Asset asset : loadedAssetsType1) {
            assetService.deleteAsset(asset.getId());
        }

        pageLink = new TextPageLink(4);
        pageData = assetService.findAssetsByTenantIdAndCustomerIdAndType(tenantId1, customerId, type1, pageLink);
        Assert.assertFalse(pageData.hasNext());
        Assert.assertEquals(0, pageData.getData().size());

        for (Asset asset : loadedAssetsType2) {
            assetService.deleteAsset(asset.getId());
        }

        pageLink = new TextPageLink(4);
        pageData = assetService.findAssetsByTenantIdAndCustomerIdAndType(tenantId1, customerId, type2, pageLink);
        Assert.assertFalse(pageData.hasNext());
        Assert.assertEquals(0, pageData.getData().size());
        customerService.deleteCustomer(customerId);
    }

    @Test
    public void testFindAll(){
        Customer customer1 = new Customer();
        customer1.setTitle("Test customer1");
        customer1.setTenantId(tenantId1);
        customer1 = customerService.saveCustomer(customer1);
        CustomerId customerId1 = customer1.getId();

        Customer customer2 = new Customer();
        customer2.setTitle("Test customer2");
        customer2.setTenantId(tenantId2);
        customer2 = customerService.saveCustomer(customer2);
        CustomerId customerId2 = customer2.getId();

        DataModelObjectId dataModelObjectId1 = new DataModelObjectId(UUIDs.timeBased());
        DataModelObjectId dataModelObjectId2 = new DataModelObjectId(UUIDs.timeBased());

        List<AssetId> allAssetIdsForTenant1 = new ArrayList<>();
        List<AssetId> allAssetIdsForTenant2 = new ArrayList<>();

        for (int i = 0; i < 60; i++) {
            TenantId tenantId = tenantId1;
            CustomerId customerId = customerId1;
            DataModelObjectId dataModelObjectId = dataModelObjectId1;
            String type = "TYPE_1";
            if(i % 2 != 0){
                tenantId = tenantId2;
                customerId = customerId2;
                dataModelObjectId = dataModelObjectId2;
                type = "TYPE_2";
            }

            Asset savedAsset = saveAsset(tenantId, dataModelObjectId, "ASSET_" + i, type);
            assetService.assignAssetToCustomer(savedAsset.getId(), customerId);
            if(i % 2 == 0) allAssetIdsForTenant1.add(savedAsset.getId());
            else allAssetIdsForTenant2.add(savedAsset.getId());

            if(i == 0){
                System.out.println(savedAsset);
            }
        }

        TextPageLink pageLink1 = new TextPageLink(20, "ASSET_");
        TempusResourceCriteriaSpec tempusResourceCriteriaSpec = new TempusResourceCriteriaSpec(EntityType.ASSET, tenantId1, dataModelObjectId1, customerId1);
        final TextPageData<Asset> page1 = assetService.findAll(tempusResourceCriteriaSpec, pageLink1);
        assertEquals(20, page1.getData().size());

        TextPageLink pageLink2 = page1.getNextPageLink();
        final TextPageData<Asset> page2 = assetService.findAll(tempusResourceCriteriaSpec, pageLink2);
        assertEquals(10, page2.getData().size());


        TextPageLink pageLink11 = new TextPageLink(20, "ASSET_");
        TempusResourceCriteriaSpec tempusResourceCriteriaSpec11 = new TempusResourceCriteriaSpec(EntityType.ASSET, tenantId1, dataModelObjectId1, customerId1);
        final HashSet<AssetId> accessibleIdsForGivenDataModelObject = new HashSet<>(allAssetIdsForTenant1.subList(0, 25));
        tempusResourceCriteriaSpec11.setAccessibleIdsForGivenDataModelObject(accessibleIdsForGivenDataModelObject);
        final TextPageData<Asset> page11 = assetService.findAll(tempusResourceCriteriaSpec11, pageLink11);
        assertEquals(20, page11.getData().size());

        TextPageLink pageLink22 = page11.getNextPageLink();
        final TextPageData<Asset> page22 = assetService.findAll(tempusResourceCriteriaSpec11, pageLink22);
        assertEquals(5, page22.getData().size());

    }

    private Asset saveAsset(TenantId tenantId, DataModelObjectId dataModelObjectId, String name, String type) {
        Asset asset = new Asset();
        asset.setTenantId(tenantId);
        asset.setDataModelObjectId(dataModelObjectId);
        asset.setName(name);
        asset.setType(type);
        return assetService.saveAsset(asset);
    }

}
