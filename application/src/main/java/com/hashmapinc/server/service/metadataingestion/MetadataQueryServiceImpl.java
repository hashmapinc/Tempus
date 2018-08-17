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

import com.hashmapinc.server.common.data.metadata.MetadataConfigId;
import com.hashmapinc.server.common.data.metadata.MetadataQuery;
import com.hashmapinc.server.common.data.metadata.MetadataQueryId;
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
public class MetadataQueryServiceImpl implements MetadataQueryService {

    @Value("${metadata-ingestion.query.service_url}")
    private String SERVICE_URL;

    @Value("${metadata-ingestion.query.config_path}")
    private String GET_BY_CONFIG_PATH;

    private final static String PATH_SEPARATOR = "/";

    @Autowired
    @Qualifier("clientRestTemplate")
    private RestTemplate restTemplate;

    @Override
    public MetadataQuery save(MetadataQuery query) {
        log.trace("Executing MetadataQueryServiceImpl.save [{}]", query);
        if (query.getId() == null) {
            ResponseEntity<MetadataQuery> response = restTemplate.postForEntity(SERVICE_URL, query, MetadataQuery.class);
            if (response.getStatusCode().equals(HttpStatus.CREATED)) {
                return response.getBody();
            }
        } else {
            restTemplate.put(SERVICE_URL, query);
            return query;
        }
        return null;
    }

    @Override
    public MetadataQuery findById(MetadataQueryId id) {
        log.trace("Executing MetadataQueryServiceImpl.findById [{}]", id);
        String getByIdUrl = SERVICE_URL + PATH_SEPARATOR + id.getId();
        ResponseEntity<MetadataQuery> response = restTemplate.getForEntity(getByIdUrl, MetadataQuery.class);
        if (response.getStatusCode().equals(HttpStatus.OK)) {
            return response.getBody();
        }
        return null;
    }

    @Override
    public List<MetadataQuery> findAllByMetadataConfigId(MetadataConfigId metadataConfigId) {
        log.trace("Executing MetadataQueryServiceImpl.findAllByMetadataConfigId [{}]", metadataConfigId);
        String getByOwnerUrl = SERVICE_URL + PATH_SEPARATOR + GET_BY_CONFIG_PATH + PATH_SEPARATOR + metadataConfigId.getId();
        ResponseEntity<List<MetadataQuery>> response = restTemplate.exchange(getByOwnerUrl, HttpMethod.GET, null, new ParameterizedTypeReference<List<MetadataQuery>>() {
        });
        if (response.getStatusCode().equals(HttpStatus.OK)) {
            return response.getBody();
        }
        return null;
    }

    @Override
    public void delete(MetadataQueryId id) {
        log.trace("Executing MetadataQueryServiceImpl.delete [{}]", id);
        String deleteByIdUrl = SERVICE_URL + PATH_SEPARATOR + id.getId();
        restTemplate.delete(deleteByIdUrl);
    }
}