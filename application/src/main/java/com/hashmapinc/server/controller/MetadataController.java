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

import com.hashmapinc.server.common.data.Device;
import com.hashmapinc.server.common.data.MetadataIngestionEntries;
import com.hashmapinc.server.common.data.UUIDConverter;
import com.hashmapinc.server.common.data.asset.Asset;
import com.hashmapinc.server.common.data.id.AssetId;
import com.hashmapinc.server.common.data.id.DataModelObjectId;
import com.hashmapinc.server.common.data.id.DeviceId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.kv.MetaDataKvEntry;
import com.hashmapinc.server.common.data.kv.StringDataEntry;
import com.hashmapinc.server.common.data.metadata.MetadataConfig;
import com.hashmapinc.server.common.data.metadata.MetadataConfigId;
import com.hashmapinc.server.common.data.metadata.MetadataQuery;
import com.hashmapinc.server.common.data.metadata.MetadataQueryId;
import com.hashmapinc.server.common.data.page.TextPageData;
import com.hashmapinc.server.common.data.page.TextPageLink;
import com.hashmapinc.server.exception.TempusErrorCode;
import com.hashmapinc.server.exception.TempusException;
import com.hashmapinc.server.requests.IngestMetadataRequest;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@Slf4j
public class MetadataController extends BaseController {

    private static final String METADATA_CONFIG_ID = "metadataConfigId";
    private static final String METADATA_QUERY_ID = "metadataQueryId";
    private static final String ENTITY_TYPE = "entityType";
    private static final String ENTITY_ID = "entityId";
    private static final String KEY_ATTRIBUTE_NOT_FOUND = "Key Attribute not found with this name.";

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @PostMapping(value = "/metadata/config")
    @ResponseBody
    public MetadataConfig saveMetadataConfig(@RequestBody MetadataConfig metadataConfig) throws TempusException {
        try {
            TenantId tenantId = getCurrentUser().getTenantId();
            metadataConfig.setOwnerId(UUIDConverter.fromTimeUUID(tenantId.getId()));
            return metadataConfigService.save(metadataConfig);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @GetMapping(value = "/metadata/config/{metadataConfigId}")
    @ResponseBody
    public MetadataConfig getMetadataConfigById(@PathVariable(METADATA_CONFIG_ID) String strMetadataConfigId) throws TempusException {
        checkParameter(METADATA_CONFIG_ID, strMetadataConfigId);
        try {
            MetadataConfigId metadataConfigId = new MetadataConfigId(toUUID(strMetadataConfigId));
            return metadataConfigService.findById(metadataConfigId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @GetMapping(value = "/metadata/tenant/configs", params = {"limit"})
    @ResponseBody
    public TextPageData<MetadataConfig> getTenantMetadataConfigs(@RequestParam("limit") int limit,
                                                                 @RequestParam(required = false) String idOffset) throws TempusException {
        try {
            TenantId tenantId = getCurrentUser().getTenantId();
            TextPageLink pageLink = createPageLink(limit, null, idOffset, null);
            return metadataConfigService.findByTenant(tenantId, pageLink);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @DeleteMapping(value = "/metadata/config/{metadataConfigId}")
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteMetaDataConfig(@PathVariable(METADATA_CONFIG_ID) String strMetadataConfigId) throws TempusException {
        checkParameter(METADATA_CONFIG_ID, strMetadataConfigId);
        try {
            MetadataConfigId metadataConfigId = new MetadataConfigId(toUUID(strMetadataConfigId));
            metadataConfigService.delete(metadataConfigId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @GetMapping(value = "/metadata/config/{metadataConfigId}/test")
    @ResponseStatus(value = HttpStatus.OK)
    public Boolean testSource(@PathVariable(METADATA_CONFIG_ID) String strMetadataConfigId) throws TempusException {
        checkParameter(METADATA_CONFIG_ID, strMetadataConfigId);
        try {
            MetadataConfigId metadataConfigId = new MetadataConfigId(toUUID(strMetadataConfigId));
            return metadataConfigService.testSource(metadataConfigId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @GetMapping(value = "/metadata/config/{metadataConfigId}/ingest")
    @ResponseBody
    public MetadataConfig runIngestion(@PathVariable(METADATA_CONFIG_ID) String strMetadataConfigId) throws TempusException {
        checkParameter(METADATA_CONFIG_ID, strMetadataConfigId);
        try {
            MetadataConfigId metadataConfigId = new MetadataConfigId(toUUID(strMetadataConfigId));
            return metadataConfigService.runIngestion(metadataConfigId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @PostMapping(value = "/metadata/query")
    @ResponseBody
    public MetadataQuery saveMetadataQuery(@RequestBody MetadataQuery metadataQuery) throws TempusException {
        try {
            return metadataQueryService.save(metadataQuery);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @GetMapping(value = "/metadata/query/{metadataQueryId}")
    @ResponseBody
    public MetadataQuery getMetadataQueryById(@PathVariable(METADATA_QUERY_ID) String strMetadataQueryId) throws TempusException {
        checkParameter(METADATA_QUERY_ID, strMetadataQueryId);
        try {
            MetadataQueryId metadataQueryId = new MetadataQueryId(toUUID(strMetadataQueryId));
            return metadataQueryService.findById(metadataQueryId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @GetMapping(value = "/metadata/config/{metadataConfigId}/query", params = {"limit"})
    @ResponseBody
    public TextPageData<MetadataQuery> getMetadataQueriesForConfig(@PathVariable(METADATA_CONFIG_ID) String strMetadataConfigId,
                                                                   @RequestParam("limit") int limit,
                                                                   @RequestParam(required = false) String idOffset) throws TempusException {
        checkParameter(METADATA_CONFIG_ID, strMetadataConfigId);
        try {
            MetadataConfigId metadataConfigId = new MetadataConfigId(toUUID(strMetadataConfigId));
            TextPageLink pageLink = createPageLink(limit, null, idOffset, null);
            return metadataQueryService.findAllByMetadataConfigId(metadataConfigId, pageLink);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @DeleteMapping(value = "/metadata/query/{metadataQueryId}")
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteMetaDataQuery(@PathVariable(METADATA_QUERY_ID) String strMetadataQueryId) throws TempusException {
        checkParameter(METADATA_QUERY_ID, strMetadataQueryId);
        try {
            MetadataQueryId metadataQueryId = new MetadataQueryId(toUUID(strMetadataQueryId));
            metadataQueryService.delete(metadataQueryId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("#oauth2.isClient() and #oauth2.hasScope('server')")
    @PostMapping(value = "/metadata")
    @ResponseStatus(value = HttpStatus.ACCEPTED)
    public void insert(@RequestBody IngestMetadataRequest request) throws TempusException {
        try {
            final MetadataIngestionEntries ingestionEntries = MetadataIngestionEntries.builder()
                    .metadataConfigId(request.getConfigId())
                    .metadataSourceName(request.getConfigName())
                    .tenantId(new TenantId(UUIDConverter.fromString(request.getOwnerId())))
                    .attribute(request.getAttribute())
                    .metaDataKvEntries(request.getData().entrySet().stream()
                            .map(e -> new MetaDataKvEntry(
                                            new StringDataEntry(e.getKey(), e.getValue().toString()),
                                            DateTime.now().getMillis()
                                    )
                            ).collect(Collectors.toList())
                    )
                    .build();
            metadataIngestionService.save(ingestionEntries);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @GetMapping(value = "/metadata/attribute/{entityType}/{entityId}", params = {"key", "value"})
    @ResponseBody
    public List<MetaDataKvEntry> getMetaDataKvEntryForEntity(
            @PathVariable(ENTITY_TYPE) String entityType,
            @PathVariable(ENTITY_ID) String entityId,
            @RequestParam String key,
            @RequestParam String value) throws TempusException {

        checkParameter(ENTITY_TYPE, entityType);
        checkParameter(ENTITY_ID, entityId);

        DataModelObjectId dataModelObjectId = null;
        String keyAttribute;
        TenantId tenantId = getCurrentUser().getTenantId();

        try {
            if (entityType.equals("ASSET")) {
                AssetId assetId = new AssetId(toUUID(entityId));
                Asset asset = checkAssetId(assetId);
                dataModelObjectId = asset.getDataModelObjectId();
            } else {
                DeviceId deviceId = new DeviceId(toUUID(entityId));
                Device device = checkDeviceId(deviceId);
                /* TODO : Remove this comment when we have Device and DataModelObject mapping
                dataModelObjectId = device.getDataModelObjectId();
                */
            }
            checkDataModelObjectId(dataModelObjectId);
            keyAttribute = dataModelObjectService.findKeyAttributeByDataModelObjectId(dataModelObjectId);
            checkNotNull(keyAttribute);
            if (key.equals(keyAttribute)) {
                return metadataIngestionService.findKvEntryByKeyAttributeAndTenantId(value, tenantId);
            } else {
                throw new TempusException(KEY_ATTRIBUTE_NOT_FOUND, TempusErrorCode.ITEM_NOT_FOUND);
            }
        } catch (Exception e) {
            throw handleException(e);
        }
    }
}