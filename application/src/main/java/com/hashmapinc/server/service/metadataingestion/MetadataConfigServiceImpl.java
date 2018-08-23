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
package com.hashmapinc.server.service.metadataingestion;

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

import static com.hashmapinc.server.dao.service.Validator.validateId;

@Service
@Slf4j
public class MetadataConfigServiceImpl implements MetadataConfigService {

    @Value("${metadata-ingestion.config.service_url}")
    private String serviceUrl;

    @Value("${metadata-ingestion.config.owner_path}")
    private String getByOwnerPath;

    @Value("${metadata-ingestion.config.ingest_path}")
    private String ingestionPath;

    @Value("${metadata-ingestion.config.connect_path}")
    private String connectPath;

    private static final String PATH_SEPARATOR = "/";
    private static final String INCORRECT_CONFIG_ID = "Incorrect metadata config id ";
    private static final String INCORRECT_TENANT_ID = "Incorrect tenant id ";

    @Autowired
    @Qualifier("clientRestTemplate")
    private RestTemplate restTemplate;

    @Override
    public MetadataConfig save(MetadataConfig metadataConfig) {
        log.trace("Executing MetadataConfigServiceImpl.save [{}]", metadataConfig);
        if (metadataConfig.getId() == null) {
            return restTemplate.postForObject(serviceUrl, metadataConfig, MetadataConfig.class);
        } else {
            restTemplate.put(serviceUrl, metadataConfig);
            return metadataConfig;
        }
    }

    @Override
    public MetadataConfig findById(MetadataConfigId id) {
        log.trace("Executing MetadataConfigServiceImpl.findById [{}]", id);
        validateId(id, INCORRECT_CONFIG_ID + id);
        String getByIdUrl = serviceUrl + PATH_SEPARATOR + id.getId();
        return restTemplate.getForObject(getByIdUrl, MetadataConfig.class);
    }

    @Override
    public List<MetadataConfig> findByTenant(TenantId tenantId) {
        log.trace("Executing MetadataConfigServiceImpl.findByTenant [{}]", tenantId);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        String getByOwnerUrl = serviceUrl + PATH_SEPARATOR + getByOwnerPath + PATH_SEPARATOR + tenantId.getId();
        ResponseEntity<List<MetadataConfig>> response = restTemplate.exchange(getByOwnerUrl, HttpMethod.GET, null, new ParameterizedTypeReference<List<MetadataConfig>>() {
        });
        return response.getBody();
    }

    @Override
    public void delete(MetadataConfigId id) {
        log.trace("Executing MetadataConfigServiceImpl.delete [{}]", id);
        validateId(id, INCORRECT_CONFIG_ID + id);
        String deleteByIdUrl = serviceUrl + PATH_SEPARATOR + id.getId();
        restTemplate.delete(deleteByIdUrl);
    }

    @Override
    public Boolean testSource(MetadataConfigId id) {
        log.trace("Executing MetadataConfigServiceImpl.testSource [{}]", id);
        validateId(id, INCORRECT_CONFIG_ID + id);
        String testSourceUrl = serviceUrl + PATH_SEPARATOR + id.getId() + PATH_SEPARATOR + connectPath;
        ResponseEntity<String> response = restTemplate.getForEntity(testSourceUrl, String.class);
        if (response.getStatusCode().equals(HttpStatus.OK)) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    @Override
    public MetadataConfig runIngestion(MetadataConfigId id) {
        log.trace("Executing MetadataConfigServiceImpl.runIngestion [{}]", id);
        validateId(id, INCORRECT_CONFIG_ID + id);
        String getByIdUrl = serviceUrl + PATH_SEPARATOR + id.getId() + PATH_SEPARATOR + ingestionPath;
        return restTemplate.getForObject(getByIdUrl, MetadataConfig.class);
    }
}
