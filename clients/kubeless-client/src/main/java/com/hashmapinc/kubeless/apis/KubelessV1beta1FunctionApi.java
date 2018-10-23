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
import com.google.common.collect.Maps;
import com.hashmapinc.kubeless.models.V1beta1Function;
import com.squareup.okhttp.Call;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Configuration;

import java.util.Map;

public class KubelessV1beta1FunctionApi {
    private ApiClient apiClient;
    private final String kubelessFunctionsUri;
    private final String namespace;
    private final Map<String, String> headers;
    private String[] authNames = new String[] { "BearerToken" };

    public KubelessV1beta1FunctionApi(ApiClient client, String namespace){
        this.apiClient = client;
        this.namespace = namespace;
        this.kubelessFunctionsUri = "/apis/kubeless.io/v1beta1/namespaces/"+ namespace +"/functions/";
        this.headers = Maps.newHashMap();
        this.headers.put("Accept", "application/json");
        this.headers.put("Content-type", "application/json");
    }

    public KubelessV1beta1FunctionApi(String namespace){
        this(Configuration.getDefaultApiClient(), namespace);
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public Call listFunctionsCall() throws ApiException {
        return apiClient.buildCall(kubelessFunctionsUri, "GET", null, null, null,
                headers, null, authNames, null);
    }

    public Call getFunctionCall(String functionName) throws ApiException {
        return apiClient.buildCall(kubelessFunctionsUri + functionName, "GET", null, null, null,
                headers, null, authNames, null);
    }

    public Call createFunctionCall(V1beta1Function function) throws ApiException {
        String currentNamespace = function.getMetadata().getNamespace();
        if(!this.namespace.equals(currentNamespace)){
            function.getMetadata().namespace(this.namespace);
        }
        return apiClient.buildCall(kubelessFunctionsUri, "POST", null, null, function,
                headers, null, authNames, null);
    }

    public Call patchFunctionCall(V1beta1Function function) throws ApiException {
        String name = function.getMetadata().getName();
        return apiClient.buildCall(kubelessFunctionsUri + name, "PUT", null, null, function,
                headers, null, authNames, null);
    }

    public Call deleteFunctionCall(String functionName) throws ApiException {
        return apiClient.buildCall(kubelessFunctionsUri + functionName, "DELETE", null, null, null,
                headers, null, authNames, null);
    }
}
