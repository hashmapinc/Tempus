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
package com.hashmapinc.kubeless.apis;

import com.google.common.collect.ImmutableMap;
import com.hashmapinc.kubeless.models.triggers.V1beta1HttpTrigger;
import com.squareup.okhttp.Call;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Configuration;

import java.util.Map;

public class KubelessV1beta1HTTPTriggerApi {

    private ApiClient apiClient;
    private final String kubelessHTTPTriggerUri;
    private final Map<String, String> headers;
    private String[] authNames = new String[] { "BearerToken" };

    public KubelessV1beta1HTTPTriggerApi(ApiClient client, String namespace){
        this.apiClient = client;
        this.kubelessHTTPTriggerUri = "/apis/kubeless.io/v1beta1/namespaces/"+ namespace +"/httptriggers/";
        this.headers = ImmutableMap.of("Accept", "application/json", "Content-type", "application/json");
    }

    public KubelessV1beta1HTTPTriggerApi(String namespace){
        this(Configuration.getDefaultApiClient(), namespace);
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public Call listHTTPTriggerCall() throws ApiException {
        return apiClient.buildCall(kubelessHTTPTriggerUri, "GET", null, null, null,
                headers, null, authNames, null);
    }

    public Call getHTTPTriggerCall(String triggerName) throws ApiException {
        return apiClient.buildCall(kubelessHTTPTriggerUri + triggerName, "GET", null, null, null,
                headers, null, authNames, null);
    }

    public Call createHTTPTriggerCall(V1beta1HttpTrigger trigger) throws ApiException {
        return apiClient.buildCall(kubelessHTTPTriggerUri, "POST", null, null, trigger,
                headers, null, authNames, null);
    }

    public Call patchHTTPTriggerCall(V1beta1HttpTrigger trigger) throws ApiException {
        String name = trigger.getMetadata().getName();
        return apiClient.buildCall(kubelessHTTPTriggerUri + name, "PUT", null, null, trigger,
                headers, null, authNames, null);
    }

    public Call deleteHTTPTriggerCall(String triggerName) throws ApiException {
        return apiClient.buildCall(kubelessHTTPTriggerUri + triggerName, "DELETE", null, null, null,
                headers, null, authNames, null);
    }
}
