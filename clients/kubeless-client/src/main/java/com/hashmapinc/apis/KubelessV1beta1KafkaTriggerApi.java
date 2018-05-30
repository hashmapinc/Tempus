/**
 * Copyright © 2017-2018 Hashmap, Inc
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
package com.hashmapinc.apis;

import com.google.common.collect.ImmutableMap;
import com.hashmapinc.models.triggers.V1beta1KafkaTrigger;
import com.squareup.okhttp.Call;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Configuration;

import java.util.Map;

public class KubelessV1beta1KafkaTriggerApi {

    private ApiClient apiClient;
    private final String kubelessKafkaTriggerUri;
    private final Map<String, String> headers;
    private String[] authNames = new String[] { "BearerToken" };

    public KubelessV1beta1KafkaTriggerApi(ApiClient client, String namespace){
        this.apiClient = client;
        this.kubelessKafkaTriggerUri = "/apis/kubeless.io/v1beta1/namespaces/"+ namespace +"/kafkatriggers";
        this.headers = ImmutableMap.of("Accept", "application/json", "Content-type", "application/json");
    }

    public KubelessV1beta1KafkaTriggerApi(String namespace){
        this(Configuration.getDefaultApiClient(), namespace);
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public Call listKafkaTriggerCall() throws ApiException {
        return apiClient.buildCall(kubelessKafkaTriggerUri, "GET", null, null, null,
                headers, null, authNames, null);
    }

    public Call getKafkaTriggerCall(String triggerName) throws ApiException {
        return apiClient.buildCall(kubelessKafkaTriggerUri+"/"+triggerName, "GET", null, null, null,
                headers, null, authNames, null);
    }

    public Call createKafkaTriggerCall(V1beta1KafkaTrigger trigger) throws ApiException {
        return apiClient.buildCall(kubelessKafkaTriggerUri, "POST", null, null, trigger,
                headers, null, authNames, null);
    }

    public Call patchKafkaTriggerCall(V1beta1KafkaTrigger trigger) throws ApiException {
        String name = trigger.getMetadata().getName();
        return apiClient.buildCall(kubelessKafkaTriggerUri + name, "PUT", null, null, trigger,
                headers, null, authNames, null);
    }

    public Call deleteKafkaTriggerCall(String triggerName) throws ApiException {
        return apiClient.buildCall(kubelessKafkaTriggerUri + triggerName, "DELETE", null, null, null,
                headers, null, authNames, null);
    }
}
