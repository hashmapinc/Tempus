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
package com.hashmapinc.server.dao.metadataingestion;

import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.metadata.MetadataConfig;
import com.hashmapinc.server.common.data.metadata.MetadataConfigId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@Slf4j
public class MetadataConfigServiceImpl implements MetadataConfigService {

    @Value("${metadata-ingestion.config.service_url}")
    private String SERVICE_URL;

    @Value("${metadata-ingestion.config.owner_path}")
    private String GET_BY_OWNER_PATH;

    @Value("${metadata-ingestion.config.ingest_path}")
    private String INGESTION_PATH;

    @Value("${metadata-ingestion.config.connect_path}")
    private String CONNECT_PATH;

    private final static String PATH_SEPARATOR = "/";

    @Autowired
    @Qualifier("clientRestTemplate")
    private RestTemplate restTemplate;

    @Override
    public MetadataConfig save(MetadataConfig metadataConfig) {
        log.trace("Executing MetadataConfigServiceImpl.save [{}]", metadataConfig);
        if (metadataConfig.getId() == null) {
            ResponseEntity<MetadataConfig> response = restTemplate.postForEntity(SERVICE_URL, metadataConfig, MetadataConfig.class);
            if (response.getStatusCode().equals(HttpStatus.CREATED)) {
                return response.getBody();
            }
        } else {
            restTemplate.put(SERVICE_URL, metadataConfig);
            return metadataConfig;
        }
        return null;
    }

    @Override
    public MetadataConfig findById(MetadataConfigId id) {
        log.trace("Executing MetadataConfigServiceImpl.findById [{}]", id);
        String getByIdUrl = SERVICE_URL + PATH_SEPARATOR + id.getId();
        ResponseEntity<MetadataConfig> response = restTemplate.getForEntity(getByIdUrl, MetadataConfig.class);
        if (response.getStatusCode().equals(HttpStatus.OK)) {
            return response.getBody();
        }
        return null;
    }

    @Override
    public List<MetadataConfig> findByTenant(TenantId tenantId) {
        log.trace("Executing MetadataConfigServiceImpl.findByTenant [{}]", tenantId);
        String getByOwnerUrl = SERVICE_URL + PATH_SEPARATOR + GET_BY_OWNER_PATH + PATH_SEPARATOR + tenantId.getId();
        ResponseEntity<List<MetadataConfig>> response = restTemplate.exchange(getByOwnerUrl, HttpMethod.GET, null, new ParameterizedTypeReference<List<MetadataConfig>>() {
        });
        if (response.getStatusCode().equals(HttpStatus.OK)) {
            return response.getBody();
        }
        return null;
    }

    @Override
    public void delete(MetadataConfigId id) {
        log.trace("Executing MetadataConfigServiceImpl.delete [{}]", id);
        String deleteByIdUrl = SERVICE_URL + PATH_SEPARATOR + id.getId();
        restTemplate.delete(deleteByIdUrl);
    }

    @Override
    public Boolean testSource(MetadataConfigId id) {
        log.trace("Executing MetadataConfigServiceImpl.testSource [{}]", id);
        String testSourceUrl = SERVICE_URL + PATH_SEPARATOR + id.getId() + PATH_SEPARATOR + CONNECT_PATH;
        ResponseEntity<String> response = restTemplate.getForEntity(testSourceUrl, String.class);
        if (response.getStatusCode().equals(HttpStatus.OK)) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }
}
