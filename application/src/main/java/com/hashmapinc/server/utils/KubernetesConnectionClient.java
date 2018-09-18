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
