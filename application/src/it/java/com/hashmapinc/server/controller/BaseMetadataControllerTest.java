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
import com.github.tomakehurst.wiremock.client.RemoteMappingBuilder;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.hashmapinc.server.common.data.UUIDConverter;
import com.hashmapinc.server.common.data.metadata.*;
import com.hashmapinc.server.common.data.metadata.source.jdbc.JdbcMetadataSource;
import com.hashmapinc.server.common.data.metadata.source.rest.RestMetadataSource;
import com.hashmapinc.server.common.data.page.TextPageData;
import com.hashmapinc.server.common.data.page.TextPageLink;
import com.hashmapinc.server.requests.IngestMetadataRequest;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class BaseMetadataControllerTest extends AbstractControllerTest {

    private MetadataConfig metadataConfig;
    private MetadataQuery metadataQuery;

    private static final String INTERNAL_SERVER_ERROR_RESPONSE = "{\"error\": \"Internal Server Error Found\"}";
    private static final String NOT_FOUND_RESPONSE = "{\"error\": \"Not Found\"}";
    private final String CONNECTED_RESPONSE = "{\"status\": \"CONNECTED\" }";

    @Before
    public void beforeTest() throws Exception {
        loginTenantAdmin();
        this.metadataConfig = createMetadataConfig();
        this.metadataQuery = createMetadataQuery();
    }

    @Test
    public void testSaveMetadataConfig() throws Exception {
        assertNotNull(metadataConfig);
        assertNotNull(metadataConfig.getOwnerId());
        assertNotNull(metadataConfig.getId());
    }

    @Test
    public void testUpdateMetadataConfig() throws Exception {
        String updatedName = "Updated name";
        RemoteMappingBuilder stubMappingBuilder = setupStub(HttpMethod.PUT.name(),
                "/api/metaconfig",
                matchingJsonPath(String.format("$[?(@.name == '%s')]", updatedName)),
                json(getMetadataConfigResponse(updatedName)),
                HttpStatus.OK.value()
        );
        metadataConfig.setName(updatedName);

        ResultActions resultActions = doPost("/api/metadata/config", metadataConfig);
        removeStub(stubMappingBuilder);
        MetadataConfig updatedConfig = readResponse(resultActions, MetadataConfig.class);

        resultActions.andExpect(status().isOk());
        assertEquals(updatedName, updatedConfig.getName());
    }

    @Test
    public void testSaveOrUpdateMetadataConfigWithUnauthorizedUser() throws Exception {
        loginCustomerUser();
        ResultActions resultActions = doPost("/api/metadata/config", metadataConfig);
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    public void testSaveOrUpdateMetadataConfigForErrorFromMetadataService() throws Exception {
        metadataConfig.setId(null);
        String configNameForError = "Test Config Error";
        metadataConfig.setName(configNameForError);
        RemoteMappingBuilder stubMappingBuilder = setupStub(HttpMethod.POST.name(),
                "/api/metaconfig",
                matchingJsonPath(String.format("$[?(@.name == '%s')]", configNameForError)),
                INTERNAL_SERVER_ERROR_RESPONSE,
                HttpStatus.INTERNAL_SERVER_ERROR.value());

        ResultActions resultActions = doPost("/api/metadata/config", metadataConfig);
        removeStub(stubMappingBuilder);
        resultActions.andExpect(status().isInternalServerError());
    }

    @Test
    public void testGetMetadataConfigById() throws Exception {
        RemoteMappingBuilder stubMappingBuilder = setupStub(HttpMethod.GET.name(),
                "/api/metaconfig/" + metadataConfig.getId().getId(),
                null,
                json(getMetadataConfigResponse(metadataConfig.getName())),
                HttpStatus.OK.value()
        );

        ResultActions resultActions = doGet("/api/metadata/config/" + metadataConfig.getId().getId());
        removeStub(stubMappingBuilder);
        MetadataConfig metadataConfig = readResponse(resultActions, MetadataConfig.class);
        resultActions.andExpect(status().isOk());
        assertEquals("Test Config", metadataConfig.getName());
    }

    @Test
    public void testGetMetadataConfigByIdWithUnauthorizedUser() throws Exception {
        loginCustomerUser();
        ResultActions resultActions = doGet("/api/metadata/config/" + metadataConfig.getId().getId());
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    public void testGetMetadataConfigByNonExistentId() throws Exception {
        String configId = "55f38250-aab3-11e8-b469-536b04774737";
        RemoteMappingBuilder stubMappingBuilder = setupStub(HttpMethod.GET.name(),
                "/api/metaconfig/" + configId,
                null,
                NOT_FOUND_RESPONSE,
                HttpStatus.NOT_FOUND.value()
        );

        ResultActions resultActions = doGet("/api/metadata/config/" + configId);
        removeStub(stubMappingBuilder);
        resultActions.andExpect(status().isNotFound());
    }

    @Test
    public void testGetMetadataConfigForErrorFromMetadataService() throws Exception {
        String configId = "55f38250-aab3-11e8-b469-536b00000000";
        RemoteMappingBuilder stubMappingBuilder = setupStub(HttpMethod.GET.name(),
                "/api/metaconfig/" + configId,
                null,
                INTERNAL_SERVER_ERROR_RESPONSE,
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );

        ResultActions resultActions = doGet("/api/metadata/config/" + configId);
        removeStub(stubMappingBuilder);
        resultActions.andExpect(status().isInternalServerError());
    }

    @Test
    public void testGetMetadataConfigsForTenant() throws Exception {
        RemoteMappingBuilder stubMappingBuilder = setupStub(HttpMethod.GET.name(),
                "/api/metaconfig/owner/" + metadataConfig.getOwnerId() + "?limit=1",
                null,
                json(new TextPageData<>(Arrays.asList(getMetadataConfigResponse(metadataConfig.getName())), new TextPageLink(1))),
                HttpStatus.OK.value()
        );

        ResultActions resultActions = doGet("/api/metadata/tenant/configs?limit=1");
        removeStub(stubMappingBuilder);
        List<MetadataConfig> metadataConfigs = readResponse(resultActions, new TypeReference<TextPageData<MetadataConfig>>() {
        }).getData();
        resultActions.andExpect(status().isOk());
        assertEquals(1, metadataConfigs.size());
    }

    @Test
    public void testGetMetadataConfigsForTenantWithUnauthorizedUser() throws Exception {
        loginCustomerUser();
        ResultActions resultActions = doGet("/api/metadata/tenant/configs?limit=1");
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    public void testGetMetadataConfigsForTenantHavingNoConfig() throws Exception {
        RemoteMappingBuilder stubMappingBuilder = setupStub(HttpMethod.GET.name(),
                "/api/metaconfig/owner/" + metadataConfig.getOwnerId() + "?limit=1",
                null,
                NOT_FOUND_RESPONSE,
                HttpStatus.NOT_FOUND.value()
        );

        ResultActions resultActions = doGet("/api/metadata/tenant/configs?limit=1");
        removeStub(stubMappingBuilder);
        resultActions.andExpect(status().isNotFound());
    }

    @Test
    public void testGetMetadataConfigsForTenantWithErrorFromMetadataService() throws Exception {
        RemoteMappingBuilder stubMappingBuilder = setupStub(HttpMethod.GET.name(),
                "/api/metaconfig/owner/" + metadataConfig.getOwnerId() + "?limit=1",
                null,
                INTERNAL_SERVER_ERROR_RESPONSE,
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );

        ResultActions resultActions = doGet("/api/metadata/tenant/configs?limit=1");
        removeStub(stubMappingBuilder);
        resultActions.andExpect(status().isInternalServerError());
    }

    @Test
    public void testDeleteMetadataConfigById() throws Exception {
        RemoteMappingBuilder stubMappingBuilder = setupStub(HttpMethod.DELETE.name(),
                "/api/metaconfig/" + metadataConfig.getId().getId(),
                null,
                "{}",
                HttpStatus.OK.value()
        );

        ResultActions resultActions = doDelete("/api/metadata/config/" + metadataConfig.getId().getId());
        removeStub(stubMappingBuilder);
        resultActions.andExpect(status().isOk());
    }

    @Test
    public void testDeleteMetadataConfigByIdWithUnauthorizedUser() throws Exception {
        loginCustomerUser();
        ResultActions resultActions = doDelete("/api/metadata/config/" + metadataConfig.getId().getId());
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    public void testDeleteMetadataConfigForErrorFromMetadataService() throws Exception {
        RemoteMappingBuilder stubMappingBuilder = setupStub(HttpMethod.DELETE.name(),
                "/api/metaconfig/" + metadataConfig.getId().getId(),
                null,
                INTERNAL_SERVER_ERROR_RESPONSE,
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );

        ResultActions resultActions = doDelete("/api/metadata/config/" + metadataConfig.getId().getId());
        removeStub(stubMappingBuilder);
        resultActions.andExpect(status().isInternalServerError());
    }

    @Test
    public void testMetadataConfigConnectionById() throws Exception {
        RemoteMappingBuilder stubMappingBuilder = setupStub(HttpMethod.GET.name(),
                "/api/metaconfig/" + metadataConfig.getId().getId() + "/connection",
                null,
                CONNECTED_RESPONSE,
                HttpStatus.OK.value()
        );

        ResultActions resultActions = doGet("/api/metadata/config/" + metadataConfig.getId().getId() + "/test");
        removeStub(stubMappingBuilder);
        resultActions.andExpect(status().isOk());
    }

    @Test
    public void testGetMetadataConfigConnectionByIdWithUnauthorizedUser() throws Exception {
        loginCustomerUser();
        ResultActions resultActions = doGet("/api/metadata/config/" + metadataConfig.getId().getId() + "/test");
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    public void testGetMetadataConfigConnectionByNonExistentId() throws Exception {
        RemoteMappingBuilder stubMappingBuilder = setupStub(HttpMethod.GET.name(),
                "/api/metaconfig/" + metadataConfig.getId().getId() + "/connection",
                null,
                NOT_FOUND_RESPONSE,
                HttpStatus.NOT_FOUND.value()
        );

        ResultActions resultActions = doGet("/api/metadata/config/" + metadataConfig.getId().getId() + "/test");
        removeStub(stubMappingBuilder);
        resultActions.andExpect(status().isNotFound());
    }

    @Test
    public void testGetMetadataConfigConnectionForErrorFromMetadataService() throws Exception {
        RemoteMappingBuilder stubMappingBuilder = setupStub(HttpMethod.GET.name(),
                "/api/metaconfig/" + metadataConfig.getId().getId() + "/connection",
                null,
                INTERNAL_SERVER_ERROR_RESPONSE,
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );

        ResultActions resultActions = doGet("/api/metadata/config/" + metadataConfig.getId().getId() + "/test");
        removeStub(stubMappingBuilder);
        resultActions.andExpect(status().isInternalServerError());
    }

    @Test
    public void testGetMetadataConfigIngestById() throws Exception {
        RemoteMappingBuilder stubMappingBuilder = setupStub(HttpMethod.GET.name(),
                "/api/metaconfig/" + metadataConfig.getId().getId() + "/ingest",
                null,
                json(getMetadataConfigResponse(metadataConfig.getName())),
                HttpStatus.OK.value()
        );

        ResultActions resultActions = doGet("/api/metadata/config/" + metadataConfig.getId().getId() + "/ingest");
        removeStub(stubMappingBuilder);
        resultActions.andExpect(status().isOk());
    }

    @Test
    public void testGetMetadataConfigIngestByIdWithUnauthorizedUser() throws Exception {
        loginCustomerUser();
        ResultActions resultActions = doGet("/api/metadata/config/" + metadataConfig.getId().getId() + "/ingest");
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    public void testGetMetadataConfigIngestByNonExistentId() throws Exception {
        RemoteMappingBuilder stubMappingBuilder = setupStub(HttpMethod.GET.name(),
                "/api/metaconfig/" + metadataConfig.getId().getId() + "/ingest",
                null,
                NOT_FOUND_RESPONSE,
                HttpStatus.NOT_FOUND.value()
        );

        ResultActions resultActions = doGet("/api/metadata/config/" + metadataConfig.getId().getId() + "/ingest");
        removeStub(stubMappingBuilder);
        resultActions.andExpect(status().isNotFound());
    }

    @Test
    public void testGetMetadataConfigIngestForErrorFromMetadataService() throws Exception {
        RemoteMappingBuilder stubMappingBuilder = setupStub(HttpMethod.GET.name(),
                "/api/metaconfig/" + metadataConfig.getId().getId() + "/ingest",
                null,
                INTERNAL_SERVER_ERROR_RESPONSE,
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );

        ResultActions resultActions = doGet("/api/metadata/config/" + metadataConfig.getId().getId() + "/ingest");
        removeStub(stubMappingBuilder);
        resultActions.andExpect(status().isInternalServerError());
    }

    @Test
    public void testSaveMetadataQuery() throws Exception {
        assertNotNull(metadataQuery);
        assertNotNull(metadataQuery.getId());
    }

    @Test
    public void testUpdateMetadataQuery() throws Exception {
        String updatedQuery = "Select * from new_table";
        metadataQuery.setQueryStmt(updatedQuery);
        RemoteMappingBuilder stubMappingBuilder = setupStub(HttpMethod.PUT.name(),
                "/api/metaquery",
                matchingJsonPath(String.format("$[?(@.queryStmt == '%s')]", updatedQuery)),
                json(metadataQuery),
                HttpStatus.OK.value()
        );

        ResultActions resultActions = doPost("/api/metadata/query", metadataQuery);
        removeStub(stubMappingBuilder);
        MetadataQuery updatedMQ = readResponse(resultActions, MetadataQuery.class);

        resultActions.andExpect(status().isOk());
        assertEquals(updatedQuery, updatedMQ.getQueryStmt());
    }

    @Test
    public void testSaveOrUpdateMetadataQueryWithUnauthorizedUser() throws Exception {
        loginCustomerUser();
        ResultActions resultActions = doPost("/api/metadata/query", metadataQuery);
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    public void testSaveOrUpdateMetadataQueryForErrorFromMetadataService() throws Exception {
        metadataQuery.setId(null);
        String updatedQuery = "Query for error";
        metadataQuery.setQueryStmt(updatedQuery);
        RemoteMappingBuilder stubMappingBuilder = setupStub(HttpMethod.POST.name(),
                "/api/metaquery",
                matchingJsonPath(String.format("$[?(@.queryStmt == '%s')]", updatedQuery)),
                INTERNAL_SERVER_ERROR_RESPONSE,
                HttpStatus.INTERNAL_SERVER_ERROR.value());

        ResultActions resultActions = doPost("/api/metadata/query", metadataQuery);
        removeStub(stubMappingBuilder);
        resultActions.andExpect(status().isInternalServerError());
    }

    @Test
    public void testGetMetadataQueryById() throws Exception {
        RemoteMappingBuilder stubMappingBuilder = setupStub(HttpMethod.GET.name(),
                "/api/metaquery/" + metadataQuery.getId().getId(),
                null,
                json(metadataQuery),
                HttpStatus.OK.value()
        );

        ResultActions resultActions = doGet("/api/metadata/query/" + metadataQuery.getId().getId());
        removeStub(stubMappingBuilder);
        MetadataQuery metadataQuery = readResponse(resultActions, MetadataQuery.class);
        resultActions.andExpect(status().isOk());
        assertEquals("Select * from kv_table", metadataQuery.getQueryStmt());
    }

    @Test
    public void testGetMetadataQueryByIdWithUnauthorizedUser() throws Exception {
        loginCustomerUser();
        ResultActions resultActions = doGet("/api/metadata/query/" + metadataQuery.getId().getId());
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    public void testGetMetadataQueryByNonExistentId() throws Exception {
        RemoteMappingBuilder stubMappingBuilder = setupStub(HttpMethod.GET.name(),
                "/api/metaquery/" + metadataQuery.getId().getId(),
                null,
                NOT_FOUND_RESPONSE,
                HttpStatus.NOT_FOUND.value()
        );

        ResultActions resultActions = doGet("/api/metadata/query/" + metadataQuery.getId().getId());
        removeStub(stubMappingBuilder);
        resultActions.andExpect(status().isNotFound());
    }

    @Test
    public void testGetMetadataQueryForErrorFromMetadataService() throws Exception {
        RemoteMappingBuilder stubMappingBuilder = setupStub(HttpMethod.GET.name(),
                "/api/metaquery/" + metadataQuery.getId().getId(),
                null,
                INTERNAL_SERVER_ERROR_RESPONSE,
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );

        ResultActions resultActions = doGet("/api/metadata/query/" + metadataQuery.getId().getId());
        removeStub(stubMappingBuilder);
        resultActions.andExpect(status().isInternalServerError());
    }

    @Test
    public void testGetMetadataQueriesForConfig() throws Exception {
        RemoteMappingBuilder stubMappingBuilder = setupStub(HttpMethod.GET.name(),
                "/api/metaquery/metaconfig/" + metadataQuery.getMetadataConfigId().getId() + "?limit=1",
                null,
                json(new TextPageData<>(Arrays.asList(metadataQuery), new TextPageLink(1))),
                HttpStatus.OK.value()
        );

        ResultActions resultActions = doGet("/api/metadata/config/{metadataConfigId}/query?limit=1", metadataQuery.getMetadataConfigId().getId().toString());
        removeStub(stubMappingBuilder);
        List<MetadataQuery> metadataQueries = readResponse(resultActions, new TypeReference<TextPageData<MetadataQuery>>() {
        }).getData();
        resultActions.andExpect(status().isOk());
        assertEquals(1, metadataQueries.size());
    }

    @Test
    public void testGetMetadataQueriesForConfigWithUnauthorizedUser() throws Exception {
        loginCustomerUser();
        ResultActions resultActions = doGet("/api/metadata/config/{metadataConfigId}/query?limit=1", metadataQuery.getMetadataConfigId().getId().toString());
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    public void testGetMetadataQueriesForConfigHavingNoConfig() throws Exception {
        RemoteMappingBuilder stubMappingBuilder = setupStub(HttpMethod.GET.name(),
                "/api/metaquery/metaconfig/" + metadataQuery.getMetadataConfigId().getId() + "?limit=1",
                null,
                NOT_FOUND_RESPONSE,
                HttpStatus.NOT_FOUND.value()
        );

        ResultActions resultActions = doGet("/api/metadata/config/{metadataConfigId}/query?limit=1", metadataQuery.getMetadataConfigId().getId().toString());
        removeStub(stubMappingBuilder);
        resultActions.andExpect(status().isNotFound());
    }

    @Test
    public void testGetMetadataQueriesForConfigWithErrorFromMetadataService() throws Exception {
        RemoteMappingBuilder stubMappingBuilder = setupStub(HttpMethod.GET.name(),
                "/api/metaquery/metaconfig/" + metadataQuery.getMetadataConfigId().getId() + "?limit=1",
                null,
                INTERNAL_SERVER_ERROR_RESPONSE,
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );

        ResultActions resultActions = doGet("/api/metadata/config/{metadataConfigId}/query?limit=1", metadataQuery.getMetadataConfigId().getId().toString());
        removeStub(stubMappingBuilder);
        resultActions.andExpect(status().isInternalServerError());
    }

    @Test
    public void testDeleteMetadataQueryById() throws Exception {
        RemoteMappingBuilder stubMappingBuilder = setupStub(HttpMethod.DELETE.name(),
                "/api/metaquery/" + metadataQuery.getId().getId(),
                null,
                "{}",
                HttpStatus.OK.value()
        );

        ResultActions resultActions = doDelete("/api/metadata/query/" + metadataQuery.getId().getId());
        removeStub(stubMappingBuilder);
        resultActions.andExpect(status().isOk());
    }

    @Test
    public void testDeleteMetadataQueryByIdWithUnauthorizedUser() throws Exception {
        loginCustomerUser();
        ResultActions resultActions = doDelete("/api/metadata/query/" + metadataQuery.getId().getId());
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    public void testDeleteMetadataQueryForErrorFromMetadataService() throws Exception {
        RemoteMappingBuilder stubMappingBuilder = setupStub(HttpMethod.DELETE.name(),
                "/api/metaquery/" + metadataQuery.getId().getId(),
                null,
                INTERNAL_SERVER_ERROR_RESPONSE,
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );

        ResultActions resultActions = doDelete("/api/metadata/query/" + metadataQuery.getId().getId());
        removeStub(stubMappingBuilder);
        resultActions.andExpect(status().isInternalServerError());
    }

    @Test
    public void testInsertMetadataRequestFromMetadataService() throws Exception {
        logout();
        String clientCredentialToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzY29wZSI6WyJzZXJ2ZXIiXSwiZXhwIjoxNTM5MTY4MjY5LCJpYXQiOjE1MzAxNjgyNjksImp0aSI6IjY3NzI2NmE5LWY4ZWQtNDQ1NS04NGZkLTVjODg4Y2IxNzE3MCIsImNsaWVudF9pZCI6InRlbXB1cyJ9.TxT516_Y0UriNOy8LtG5wcCDBCevzzBrx-jAELAwEXM";

        Map<String, Object> keyValueMap = new HashMap<>();
        keyValueMap.put("Key 1", "Value 1");
        keyValueMap.put("Key 2", "Value 2");

        IngestMetadataRequest content = new IngestMetadataRequest(metadataConfig.getId(), metadataConfig.getOwnerId(), metadataConfig.getName(), "attribute", keyValueMap);
        MockHttpServletRequestBuilder postRequest = post("/api/metadata")
                .header("authorization", "bearer " + clientCredentialToken)
                .contentType(contentType)
                .content(json(content));

        mockMvc.perform(postRequest)
                .andExpect(status().isAccepted());
    }

    @Test
    public void testInsertMetadataRequestAsATempusUser() throws Exception {
        Map<String, Object> keyValueMap = new HashMap<>();
        keyValueMap.put("Key 1", "Value 1");
        keyValueMap.put("Key 2", "Value 2");

        IngestMetadataRequest content = new IngestMetadataRequest(metadataConfig.getId(), metadataConfig.getOwnerId(), metadataConfig.getName(), "attribute", keyValueMap);

        doPost("/api/metadata", content).andExpect(status().isForbidden());
    }

    private MetadataConfig createMetadataConfig() throws Exception {
        String configName = "Test Config";
        MetadataConfig mc = new MetadataConfig();
        mc.setName(configName);
        mc.setSink(new RestMetadataSource("http://localhost:8080/api/metadata", null, null));
        mc.setSource(new JdbcMetadataSource("jdbc:hsqldb:file:/tmp/tempusDb", "sa", ""));

        RemoteMappingBuilder stubMappingBuilder = setupStub(HttpMethod.POST.name(),
                "/api/metaconfig",
                matchingJsonPath(String.format("$[?(@.name == '%s')]", configName)),
                json(getMetadataConfigResponse(configName)),
                HttpStatus.CREATED.value()
        );

        ResultActions resultActions = doPost("/api/metadata/config", mc);
        removeStub(stubMappingBuilder);

        resultActions.andExpect(status().isOk());
        return readResponse(resultActions, MetadataConfig.class);
    }

    private MetadataQuery createMetadataQuery() throws Exception {
        String queryStmt = "Select * from kv_table";
        MetadataQuery mq = new MetadataQuery();
        mq.setId(new MetadataQueryId(UUIDs.timeBased()));
        mq.setMetadataConfigId(metadataConfig.getId());
        mq.setQueryStmt(queryStmt);
        mq.setAttribute("Test Attribute");
        mq.setTriggerSchedule("0/10 * * ? * * *");
        mq.setTriggerType(MetadataIngestionTriggerType.CRON);


        RemoteMappingBuilder stubMappingBuilder = setupStub(HttpMethod.POST.name(),
                "/api/metaquery",
                matchingJsonPath(String.format("$[?(@.queryStmt == '%s')]", queryStmt)),
                json(mq),
                HttpStatus.CREATED.value()
        );

        mq.setId(null);
        ResultActions resultActions = doPost("/api/metadata/query", mq);
        removeStub(stubMappingBuilder);

        resultActions.andExpect(status().isOk());
        return readResponse(resultActions, MetadataQuery.class);
    }

    private RemoteMappingBuilder setupStub(String method, String path, StringValuePattern requestBodyPattern, String jsonResponseBody, int status) {
        RemoteMappingBuilder stubMappingBuilder = request(method, urlEqualTo(path))
                .willReturn(aResponse()
                        .withStatus(status)
                        .withHeader("Content-Type", "application/json")
                        .withBody(jsonResponseBody)
                );
        if (requestBodyPattern != null) {
            stubMappingBuilder.withRequestBody(requestBodyPattern);
        }
        stubFor(stubMappingBuilder);
        return stubMappingBuilder;
    }

    private MetadataConfig getMetadataConfigResponse(String name) {

        JdbcMetadataSource jdbcMetadataSource = new JdbcMetadataSource("jdbc:hsqldb:file:/tmp/tempusDb", "sa", "");
        jdbcMetadataSource.setId(new MetadataSourceId(UUIDs.timeBased()));
        jdbcMetadataSource.setCreatedTime(DateTime.now().getMillis());

        RestMetadataSource restMetadataSource = new RestMetadataSource("http://localhost:8080/api/metadata", null, null);
        restMetadataSource.setId(new MetadataSourceId(UUIDs.timeBased()));
        restMetadataSource.setCreatedTime(DateTime.now().getMillis());

        MetadataConfig metadataConfig = new MetadataConfig(UUIDConverter.fromTimeUUID(tenantId.getId()), name, jdbcMetadataSource, restMetadataSource);
        metadataConfig.setId(new MetadataConfigId(UUID.fromString("6a882d28-a6b9-11e8-8cc3-8d3f4d7592c2")));
        metadataConfig.setCreatedTime(DateTime.now().getMillis());

        return metadataConfig;
    }
}
