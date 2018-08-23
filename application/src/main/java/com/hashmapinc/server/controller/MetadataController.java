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

import com.hashmapinc.server.common.data.MetadataIngestionEntries;
import com.hashmapinc.server.common.data.UUIDConverter;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.kv.MetaDataKvEntry;
import com.hashmapinc.server.common.data.kv.StringDataEntry;
import com.hashmapinc.server.common.data.metadata.MetadataConfig;
import com.hashmapinc.server.common.data.metadata.MetadataConfigId;
import com.hashmapinc.server.common.data.metadata.MetadataQuery;
import com.hashmapinc.server.common.data.metadata.MetadataQueryId;
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

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @PostMapping(value = "/metadata/config")
    @ResponseBody
    public MetadataConfig saveMetadataConfig(@RequestBody MetadataConfig metadataConfig) throws TempusException {
        try {
            TenantId tenantId = getCurrentUser().getTenantId();
            metadataConfig.setOwnerId(tenantId.getId().toString());
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
    @GetMapping(value = "/metadata/tenant/configs")
    @ResponseBody
    public List<MetadataConfig> getTenantMetadataConfigs() throws TempusException {
        try {
            TenantId tenantId = getCurrentUser().getTenantId();
            return metadataConfigService.findByTenant(tenantId);
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
    @GetMapping(value = "/metadata/config/{metadataConfigId}/query")
    @ResponseBody
    public List<MetadataQuery> getMetadataQueriesForConfig(@PathVariable(METADATA_CONFIG_ID) String strMetadataConfigId) throws TempusException {
        checkParameter(METADATA_CONFIG_ID, strMetadataConfigId);
        try {
            MetadataConfigId metadataConfigId = new MetadataConfigId(toUUID(strMetadataConfigId));
            return metadataQueryService.findAllByMetadataConfigId(metadataConfigId);
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
    @PostMapping(value = "/metadata/insert")
    @ResponseStatus(value = HttpStatus.ACCEPTED)
    public void insert(@RequestBody IngestMetadataRequest request) throws TempusException {
        try {
            final MetadataIngestionEntries ingestionEntries = MetadataIngestionEntries.builder()
                    .metadataConfigId(UUIDConverter.fromTimeUUID(request.getConfigId().getId()))
                    .metadataSourceName(request.getConfigName())
                    .tenantId(UUIDConverter.fromTimeUUID(toUUID(request.getOwnerId())))
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
}