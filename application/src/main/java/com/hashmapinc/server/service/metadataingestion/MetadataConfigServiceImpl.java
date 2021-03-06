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

import com.hashmapinc.server.common.data.UUIDConverter;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.metadata.MetadataConfig;
import com.hashmapinc.server.common.data.metadata.MetadataConfigId;
import com.hashmapinc.server.common.data.page.TextPageData;
import com.hashmapinc.server.common.data.page.TextPageLink;
import com.hashmapinc.server.common.data.metadata.source.jdbc.JdbcMetadataSource;
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
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import static com.hashmapinc.server.dao.service.Validator.validateId;

@Service
@Slf4j
public class MetadataConfigServiceImpl implements MetadataConfigService {

    @Value("${metadata-ingestion.config.service_url}")
    private String serviceUrl;

    @Value("${metadata-ingestion.config.owner_path}")
    private String ownerQueryPath;

    @Value("${metadata-ingestion.config.ingest_path}")
    private String ingestionPath;

    @Value("${metadata-ingestion.config.connect_path}")
    private String connectPath;

    private static final String PATH_SEPARATOR = "/";
    private static final String INCORRECT_CONFIG_ID = "Incorrect metadata config id ";
    private static final String INCORRECT_TENANT_ID = "Incorrect tenant id ";

    private static final String CONNECTED = "{\"status\": \"CONNECTED\" }";

    @Autowired
    @Qualifier("clientRestTemplate")
    private RestTemplate restTemplate;

    @Override
    public MetadataConfig save(MetadataConfig metadataConfig) {
        log.trace("Executing MetadataConfigServiceImpl.save [{}]", metadataConfig);
        encodePassword(metadataConfig);
        if (metadataConfig.getId() == null) {
            metadataConfig =  restTemplate.postForObject(serviceUrl, metadataConfig, MetadataConfig.class);
        } else {
            restTemplate.put(serviceUrl, metadataConfig);
        }
        decodePassword(metadataConfig);
        return metadataConfig;
    }

    @Override
    public MetadataConfig findById(MetadataConfigId id) {
        log.trace("Executing MetadataConfigServiceImpl.findById [{}]", id);
        validateId(id, INCORRECT_CONFIG_ID + id);
        String url = serviceUrl + PATH_SEPARATOR + id.getId();
        MetadataConfig foundMetadataConfig = restTemplate.getForObject(url , MetadataConfig.class);
        decodePassword(foundMetadataConfig);
        return foundMetadataConfig;
    }

    @Override
    public TextPageData<MetadataConfig> findByTenant(TenantId tenantId, TextPageLink pageLink) {
        log.trace("Executing MetadataConfigServiceImpl.findByTenant [{}]", tenantId);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);

        String url = serviceUrl + PATH_SEPARATOR + ownerQueryPath + PATH_SEPARATOR + UUIDConverter.fromTimeUUID(tenantId.getId());
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("limit", pageLink.getLimit());

        if (pageLink.getIdOffset() != null)
            builder.queryParam("idOffset", pageLink.getIdOffset());

        ResponseEntity<TextPageData<MetadataConfig>> response = restTemplate.exchange(builder.build().encode().toUri(),
                HttpMethod.GET, null, new ParameterizedTypeReference<TextPageData<MetadataConfig>>() {
        });
        TextPageData<MetadataConfig> metadataConfigTextPageData = response.getBody();
        List<MetadataConfig> metadataConfigs = metadataConfigTextPageData.getData().stream().peek(this::decodePassword).collect(Collectors.toList());
        TextPageLink nextPageLink = metadataConfigTextPageData.getNextPageLink();
        if (nextPageLink == null) {
           nextPageLink = new TextPageLink(0, null);
        }
        return new TextPageData<>(metadataConfigs, nextPageLink);
    }

    @Override
    public void delete(MetadataConfigId id) {
        log.trace("Executing MetadataConfigServiceImpl.delete [{}]", id);
        validateId(id, INCORRECT_CONFIG_ID + id);
        String url = serviceUrl + PATH_SEPARATOR + id.getId();
        restTemplate.delete(url);
    }

    @Override
    public Boolean testSource(MetadataConfigId id) {
        log.trace("Executing MetadataConfigServiceImpl.testSource [{}]", id);
        validateId(id, INCORRECT_CONFIG_ID + id);
        String url = serviceUrl + PATH_SEPARATOR + id.getId() + PATH_SEPARATOR + connectPath;
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        if (response.getBody().equals(CONNECTED)) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    @Override
    public MetadataConfig runIngestion(MetadataConfigId id) {
        log.trace("Executing MetadataConfigServiceImpl.runIngestion [{}]", id);
        validateId(id, INCORRECT_CONFIG_ID + id);
        String url = serviceUrl + PATH_SEPARATOR + id.getId() + PATH_SEPARATOR + ingestionPath;
        MetadataConfig metadataConfig = restTemplate.getForObject(url , MetadataConfig.class);
        decodePassword(metadataConfig);
        return metadataConfig;
    }

    private void encodePassword(MetadataConfig metadataConfig) {
        if (metadataConfig.getSource() instanceof JdbcMetadataSource) {
            String password = ((JdbcMetadataSource) metadataConfig.getSource()).getPassword();
            ((JdbcMetadataSource) metadataConfig.getSource()).setPassword(Base64.getEncoder().encodeToString(password.getBytes()));
        }
    }

    private void decodePassword(MetadataConfig metadataConfig) {
        if (metadataConfig.getSource() instanceof JdbcMetadataSource) {
            String password = ((JdbcMetadataSource) metadataConfig.getSource()).getPassword();
            ((JdbcMetadataSource) metadataConfig.getSource()).setPassword(new String(Base64.getDecoder().decode(password)));
        }
    }
}
