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
package com.hashmapinc.server.service.kubernetes.gateway;

import com.hashmapinc.server.common.data.TempusGatewayConfiguration;
import com.hashmapinc.server.common.data.UUIDConverter;
import com.hashmapinc.server.common.data.kubernetes.ReplicaSetConfig;
import com.hashmapinc.server.config.TempusGatewayProperties;
import com.hashmapinc.server.service.kubernetes.KubernetesReplicaSetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class TempusGatewayKubernetesServiceImpl implements TempusGatewayKubernetesService {

    private static final String GATEWAY_ACCESS_TOKEN = "GATEWAY_ACCESS_TOKEN";
    private static final String CONTAINER_NAME = "gateway";
    private static final String TENANT_ID = "tenantId";
    private static final String GATEWAY_PORT = "port";
    private static final String GATEWAY_HOST = "GATEWAY_HOST";

    @Autowired
    TempusGatewayProperties tempusGatewayProperties;

    @Autowired
    KubernetesReplicaSetService replicaSetService;

    @Override
    public boolean deployTempusGateway(TempusGatewayConfiguration gatewayConfiguration) {
        ReplicaSetConfig replicaSetConfig = createReplicaSetConfig(gatewayConfiguration);
        return replicaSetService.deployReplicaSet(replicaSetConfig);
    }

    private ReplicaSetConfig createReplicaSetConfig(TempusGatewayConfiguration gatewayConfiguration) {
        ReplicaSetConfig replicaSetConfig = new ReplicaSetConfig();
        String replicaName = UUIDConverter.fromTimeUUID(gatewayConfiguration.getTenantId().getId());
        Map<String, String> labels = new HashMap<>();
        labels.put(TENANT_ID, replicaName);

        replicaSetConfig.setReplicaName(replicaName);
        replicaSetConfig.setPodTemplateName(replicaName);
        replicaSetConfig.setContainerName(CONTAINER_NAME);
        replicaSetConfig.setReplicaLabels(labels);
        replicaSetConfig.setSelectorLabels(labels);
        replicaSetConfig.setPodTemplateLabels(labels);
        replicaSetConfig.setReplica(gatewayConfiguration.getReplicas());
        replicaSetConfig.setImage(tempusGatewayProperties.getImage());
        replicaSetConfig.setPorts(new HashMap<Integer , String>() {{
            put(tempusGatewayProperties.getGatewayPort(), GATEWAY_PORT);
        }});
        replicaSetConfig.setEnvVariables(new HashMap<String , String>() {{
            put(GATEWAY_ACCESS_TOKEN, gatewayConfiguration.getGatewayToken());
            put(GATEWAY_HOST, tempusGatewayProperties.getGatewayHost());
        }});
        replicaSetConfig.setCommands(tempusGatewayProperties.getCommands());
        return replicaSetConfig;
    }
}
