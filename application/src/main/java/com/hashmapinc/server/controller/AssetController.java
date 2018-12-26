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

import com.google.common.util.concurrent.ListenableFuture;
import com.hashmapinc.server.common.data.Customer;
import com.hashmapinc.server.common.data.EntitySubtype;
import com.hashmapinc.server.common.data.TempusResourceCriteriaSpec;
import com.hashmapinc.server.common.data.asset.Asset;
import com.hashmapinc.server.common.data.audit.ActionType;
import com.hashmapinc.server.common.data.id.*;
import com.hashmapinc.server.common.data.page.TextPageData;
import com.hashmapinc.server.common.data.page.TextPageLink;
import com.hashmapinc.server.service.security.model.SecurityUser;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.hashmapinc.server.common.data.EntityType;
import com.hashmapinc.server.common.data.asset.AssetSearchQuery;
import com.hashmapinc.server.common.data.security.Authority;
import com.hashmapinc.server.dao.exception.IncorrectParameterException;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.exception.TempusException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class AssetController extends BaseController {

    public static final String ASSET_ID = "assetId";
    public static final String DATA_MODEL_OBJECT_ID = "dataModelObjectId";


    @PostAuthorize("hasPermission(returnObject, 'ASSET_READ')")
    @GetMapping(value = "/asset/{assetId}")
    @ResponseBody
    public Asset getAssetById(@PathVariable(ASSET_ID) String strAssetId) throws TempusException {
        checkParameter(ASSET_ID, strAssetId);
        try {
            AssetId assetId = new AssetId(toUUID(strAssetId));
            return checkAssetId(assetId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasPermission(#asset, 'ASSET_CREATE') or hasPermission(#asset, 'ASSET_UPDATE')")
    @PostMapping(value = "/asset")
    @ResponseBody
    public Asset saveAsset(@RequestBody Asset asset) throws TempusException {
        try {
            asset.setTenantId(getCurrentUser().getTenantId());
            if (getCurrentUser().getAuthority() == Authority.CUSTOMER_USER){
                checkCustomerId(asset.getCustomerId());
            }
            Asset savedAsset  = checkNotNull(assetService.saveAsset(asset));

            logEntityAction(savedAsset.getId(), savedAsset,
                    savedAsset.getCustomerId(),
                    asset.getId() == null ? ActionType.ADDED : ActionType.UPDATED, null);

            return  savedAsset;
        } catch (Exception e) {
            logEntityAction(emptyId(EntityType.ASSET), asset,
                    null, asset.getId() == null ? ActionType.ADDED : ActionType.UPDATED, e);
            throw handleException(e);
        }
    }

    @PreAuthorize("hasPermission(#strAssetId, 'ASSET', 'ASSET_DELETE')")
    @DeleteMapping(value = "/asset/{assetId}")
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteAsset(@PathVariable(ASSET_ID) String strAssetId) throws TempusException {
        checkParameter(ASSET_ID, strAssetId);
        try {
            AssetId assetId = new AssetId(toUUID(strAssetId));
            Asset asset = checkAssetId(assetId);
            assetService.deleteAsset(assetId);

            logEntityAction(assetId, asset,
                    asset.getCustomerId(),
                    ActionType.DELETED, null, strAssetId);

        } catch (Exception e) {
            logEntityAction(emptyId(EntityType.ASSET),
                    null,
                    null,
                    ActionType.DELETED, e, strAssetId);
            throw handleException(e);
        }
    }

    @PreAuthorize("hasPermission(#strAssetId, 'ASSET', 'ASSET_ASSIGN')")
    @PostMapping(value = "/customer/{customerId}/asset/{assetId}")
    @ResponseBody
    public Asset assignAssetToCustomer(@PathVariable("customerId") String strCustomerId,
                                       @PathVariable(ASSET_ID) String strAssetId) throws TempusException {
        checkParameter("customerId", strCustomerId);
        checkParameter(ASSET_ID, strAssetId);
        try {
            CustomerId customerId = new CustomerId(toUUID(strCustomerId));
            Customer customer = checkCustomerId(customerId);

            AssetId assetId = new AssetId(toUUID(strAssetId));
            checkAssetId(assetId);

            Asset savedAsset = checkNotNull(assetService.assignAssetToCustomer(assetId, customerId));

            logEntityAction(assetId, savedAsset,
                    savedAsset.getCustomerId(),
                    ActionType.ASSIGNED_TO_CUSTOMER, null, strAssetId, strCustomerId, customer.getName());

            return  savedAsset;
        } catch (Exception e) {

            logEntityAction(emptyId(EntityType.ASSET), null,
                    null,
                    ActionType.ASSIGNED_TO_CUSTOMER, e, strAssetId, strCustomerId);

            throw handleException(e);
        }
    }

    @PreAuthorize("hasPermission(#strAssetId, 'ASSET', 'ASSET_ASSIGN')")
    @DeleteMapping(value = "/customer/asset/{assetId}")
    @ResponseBody
    public Asset unassignAssetFromCustomer(@PathVariable(ASSET_ID) String strAssetId) throws TempusException {
        checkParameter(ASSET_ID, strAssetId);
        try {
            AssetId assetId = new AssetId(toUUID(strAssetId));
            Asset asset = checkAssetId(assetId);
            if (asset.getCustomerId() == null || asset.getCustomerId().getId().equals(ModelConstants.NULL_UUID)) {
                throw new IncorrectParameterException("Asset isn't assigned to any customer!");
            }

            Customer customer = checkCustomerId(asset.getCustomerId());

            Asset savedAsset = checkNotNull(assetService.unassignAssetFromCustomer(assetId));

            logEntityAction(assetId, asset,
                    asset.getCustomerId(),
                    ActionType.UNASSIGNED_FROM_CUSTOMER, null, strAssetId, customer.getId().toString(), customer.getName());

            return savedAsset;
        } catch (Exception e) {

            logEntityAction(emptyId(EntityType.ASSET), null,
                    null,
                    ActionType.UNASSIGNED_FROM_CUSTOMER, e, strAssetId);

            throw handleException(e);
        }
    }

    @PreAuthorize("hasPermission(#strAssetId, 'ASSET', 'ASSET_ASSIGN')")
    @PostMapping(value = "/customer/public/asset/{assetId}")
    @ResponseBody
    public Asset assignAssetToPublicCustomer(@PathVariable(ASSET_ID) String strAssetId) throws TempusException {
        checkParameter(ASSET_ID, strAssetId);
        try {
            AssetId assetId = new AssetId(toUUID(strAssetId));
            Asset asset = checkAssetId(assetId);
            Customer publicCustomer = customerService.findOrCreatePublicCustomer(asset.getTenantId());
            Asset savedAsset = checkNotNull(assetService.assignAssetToCustomer(assetId, publicCustomer.getId()));

            logEntityAction(assetId, savedAsset,
                    savedAsset.getCustomerId(),
                    ActionType.ASSIGNED_TO_CUSTOMER, null, strAssetId, publicCustomer.getId().toString(), publicCustomer.getName());

            return savedAsset;
        } catch (Exception e) {

            logEntityAction(emptyId(EntityType.ASSET), null,
                    null,
                    ActionType.ASSIGNED_TO_CUSTOMER, e, strAssetId);

            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    // TODO return type should be collection of Asset. Create new action ASSET_READ_TENANT
//    @PostFilter("hasPermission(filterObject, 'ASSET_CREATE')")
    @GetMapping(value = "/tenant/assets", params = {"limit"})
    @ResponseBody
    public TextPageData<Asset> getTenantAssets(
            @RequestParam int limit,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String textSearch,
            @RequestParam(required = false) String idOffset,
            @RequestParam(required = false) String textOffset) throws TempusException {
        try {
            final TempusResourceCriteriaSpec tempusResourceCriteriaSpec = getTempusResourceCriteriaSpec(getCurrentUser(), EntityType.ASSET, null, null, type, null);
            TextPageLink pageLink = createPageLink(limit, textSearch, idOffset, textOffset);
            return assetService.findAll(tempusResourceCriteriaSpec, pageLink);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @GetMapping(value = "/tenant/assets", params = {"assetName"})
    @ResponseBody
    public Asset getTenantAsset(
            @RequestParam String assetName) throws TempusException {
        try {
            TenantId tenantId = getCurrentUser().getTenantId();
            return checkNotNull(assetService.findAssetByTenantIdAndName(tenantId, assetName));
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @GetMapping(value = "/customer/{customerId}/assets", params = {"limit"})
    @ResponseBody
    public TextPageData<Asset> getCustomerAssets(
            @PathVariable("customerId") String strCustomerId,
            @RequestParam int limit,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String textSearch,
            @RequestParam(required = false) String idOffset,
            @RequestParam(required = false) String textOffset) throws TempusException {
        checkParameter("customerId", strCustomerId);
        try {
            CustomerId customerId = new CustomerId(toUUID(strCustomerId));
            checkCustomerId(customerId);
            final TempusResourceCriteriaSpec tempusResourceCriteriaSpec = getTempusResourceCriteriaSpec(getCurrentUser(), EntityType.ASSET, null, customerId, type, null);
            TextPageLink pageLink = createPageLink(limit, textSearch, idOffset, textOffset);
            return assetService.findAll(tempusResourceCriteriaSpec, pageLink);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PostFilter("hasPermission(filterObject, 'ASSET_READ')")
    @GetMapping(value = "/assets", params = {"assetIds"})
    @ResponseBody
    public List<Asset> getAssetsByIds(
            @RequestParam("assetIds") String[] strAssetIds) throws TempusException {
        checkArrayParameter("assetIds", strAssetIds);
        try {
            SecurityUser user = getCurrentUser();
            TenantId tenantId = user.getTenantId();
            CustomerId customerId = user.getCustomerId();
            List<AssetId> assetIds = new ArrayList<>();
            for (String strAssetId : strAssetIds) {
                assetIds.add(new AssetId(toUUID(strAssetId)));
            }
            ListenableFuture<List<Asset>> assets;
            if (customerId == null || customerId.isNullUid()) {
                assets = assetService.findAssetsByTenantIdAndIdsAsync(tenantId, assetIds);
            } else {
                assets = assetService.findAssetsByTenantIdCustomerIdAndIdsAsync(tenantId, customerId, assetIds);
            }
            return checkNotNull(assets.get());
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PostFilter("hasPermission(filterObject, 'ASSET_READ')")
    @PostMapping(value = "/assets")
    @ResponseBody
    public List<Asset> findByQuery(@RequestBody AssetSearchQuery query) throws TempusException {
        checkNotNull(query);
        checkNotNull(query.getParameters());
        checkNotNull(query.getAssetTypes());
        checkEntityId(query.getParameters().getEntityId());
        try {
            List<Asset> assets = checkNotNull(assetService.findAssetsByQuery(query).get());
            assets = assets.stream().filter(asset -> {
                try {
                    checkAsset(asset);
                    return true;
                } catch (TempusException e) {
                    return false;
                }
            }).collect(Collectors.toList());
            return assets;
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @GetMapping(value = "/asset/types")
    @ResponseBody
    public List<EntitySubtype> getAssetTypes() throws TempusException {
        try {
            SecurityUser user = getCurrentUser();
            TenantId tenantId = user.getTenantId();
            ListenableFuture<List<EntitySubtype>> assetTypes = assetService.findAssetTypesByTenantId(tenantId);
            return checkNotNull(assetTypes.get());
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @GetMapping(value = "/datamodelobject/assets/{dataModelObjectId}")
    @ResponseBody
    public TextPageData<Asset> getAssetsByDataModelObjectId(@PathVariable(DATA_MODEL_OBJECT_ID) String strDataModelObjectId,
                                                            @RequestParam int limit,
                                                            @RequestParam(required = false) String textSearch,
                                                            @RequestParam(required = false) String idOffset,
                                                            @RequestParam(required = false) String textOffset) throws TempusException {
        checkParameter(DATA_MODEL_OBJECT_ID, strDataModelObjectId);
        try {
            DataModelObjectId dataModelObjectId = new DataModelObjectId(toUUID(strDataModelObjectId));
            final TempusResourceCriteriaSpec tempusResourceCriteriaSpec = getTempusResourceCriteriaSpec(getCurrentUser(), EntityType.ASSET, dataModelObjectId, null, null, null);
            TextPageLink pageLink = createPageLink(limit, textSearch, idOffset, textOffset);
            return assetService.findAll(tempusResourceCriteriaSpec, pageLink);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

}
