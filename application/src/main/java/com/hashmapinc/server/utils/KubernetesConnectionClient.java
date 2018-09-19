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
package com.hashmapinc.server.utils;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.apis.ExtensionsV1beta1Api;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.Configuration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class KubernetesConnectionClient {

    public CoreV1Api coreV1Api = new CoreV1Api();
    public ExtensionsV1beta1Api extensionsV1beta1Api = new ExtensionsV1beta1Api(apiClient);

    @Value("${kubeless.cluster_mode_enabled}")
    private static boolean clusterModeEnabled;


    @Value("${kubeless.kube_config_path}")
    private static String configPath;

    private static ApiClient apiClient;

    static {
        try {
            if (clusterModeEnabled) {
                apiClient = Config.fromCluster();
                Configuration.setDefaultApiClient(apiClient);
            } else {
                apiClient = Config.fromConfig(configPath);
                Configuration.setDefaultApiClient(apiClient);
            }
        } catch (IOException e) {
            log.error("Exception in apiClient creation : [{}]", e.getMessage());
        }
    }
}
