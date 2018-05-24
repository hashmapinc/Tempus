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

import com.hashmapinc.server.common.data.MetadataConfig;
import com.hashmapinc.server.common.data.MetadataIngestionEntries;
import com.hashmapinc.server.common.data.UUIDConverter;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.exception.TempusErrorCode;
import com.hashmapinc.server.exception.TempusException;
import com.hashmapinc.server.service.security.model.SecurityUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api")
@Slf4j
public class MetadataConfigController extends BaseController {

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/metadataconfig", method = RequestMethod.POST)
    @ResponseBody
    public MetadataConfig saveMetadataConfig(MetadataConfig metadataConfig) throws TempusException {
        try {
            metadataConfig.setTenantId(UUIDConverter.fromTimeUUID(getCurrentUser().getTenantId().getId()));
            RestTemplate restTemplate = new RestTemplate();
            String url = "localhost:9005/metadataconfig" ;
            return restTemplate.postForObject(url, metadataConfig, MetadataConfig.class);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/metadataconfig/{metadataConfigId}", method = RequestMethod.GET)
    @ResponseBody
    public MetadataConfig getMetadataConfigById(@PathVariable("metadataConfigId") String strMetadataConfigId) throws TempusException {
        checkParameter("metadataConfigId", strMetadataConfigId);
        try {
            String url = "localhost:9005"+ "/" +strMetadataConfigId;
            RestTemplate restTemplate = new RestTemplate();
            return restTemplate.getForObject(url , MetadataConfig.class);
        } catch (Exception e){
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/tenant/metadataconfigs",  method = RequestMethod.GET)
    @ResponseBody
    public MetadataConfig[] getTenantMetadataConfigs() throws TempusException {
        TenantId tenantId = getCurrentUser().getTenantId();
        try {
            String url = "localhost:9005/"+tenantId.getId().toString();
            RestTemplate restTemplate = new RestTemplate();
            return restTemplate.getForObject(url, MetadataConfig[].class);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/metadataconfig/{metadataConfigId}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteMetaDataConfig(@PathVariable("metadataConfigId") String strMetadataConfigId) throws TempusException {
        checkParameter("metadataConfigId", strMetadataConfigId);
        try {
            String url = "localhost:9005/"+strMetadataConfigId;
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.delete(url);
        } catch (Exception e) {
            throw handleException(e);
        }
    }


    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/metadataconfig/test/{metadataConfigId}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK)
    public Boolean testSource(@PathVariable("metadataConfigId") String strMetadataConfigId) throws TempusException {
        checkParameter("metadataConfigId", strMetadataConfigId);
        try {
            String url = "run_test_ingestion_endpoint/"+strMetadataConfigId;
            RestTemplate restTemplate = new RestTemplate();
            MetadataIngestionEntries metadataIngestionEntries = restTemplate.getForObject(url, MetadataIngestionEntries.class);
            return metadataIngestionEntries.getMetaDataKvEntries().size() > 0;
        } catch (Exception e){
            throw handleException(e);
        }

    }

    @PreAuthorize("hasAuthority('API_USER')")
    @RequestMapping(value = "/metadataconfig/insert", method = RequestMethod.POST)
    @ResponseBody
    public void insert(MetadataIngestionEntries metadataIngestionEntries) throws TempusException {
        SecurityUser securityUser = getCurrentUser();
        //TODO: Fix this before checkin
        if(true) {
            metadataIngestionService.save(
                    metadataIngestionEntries.getTenantId(),
                    metadataIngestionEntries.getMetadataConfigId(),
                    metadataIngestionEntries.getMetadataSourceName(),
                    metadataIngestionEntries.getMetaDataKvEntries());
        } else {
            throw new TempusException("API with registered "+securityUser.getEmail() + " is not entitled to insert metadata", TempusErrorCode.PERMISSION_DENIED);
        }
    }

}
