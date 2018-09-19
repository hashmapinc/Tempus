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
package com.hashmapinc.server.service.kubernetes;

import com.hashmapinc.server.common.data.kubernetes.ReplicaSetConfig;
import com.hashmapinc.server.utils.KubernetesConnectionClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.models.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class KubernetesReplicaSetServiceImpl implements   KubernetesReplicaSetService {

    public static final String DEFAULT_NAMESPACE = "default";
    public static final String REPLICA_KIND = "ReplicaSet";

    @Autowired
    KubernetesConnectionClient connectionClient;

    @Override
    public boolean deployReplicaSet(ReplicaSetConfig replicaSetConfig) {
        V1beta1ReplicaSet replicaSet = createReplicaSet(replicaSetConfig);
        try {
            connectionClient.extensionsV1beta1Api.createNamespacedReplicaSet(DEFAULT_NAMESPACE , replicaSet , null);
        } catch (ApiException e) {
            log.error("Error while deploying replica-set : [{}]", e.getMessage());
        }
        return false;
    }

    private V1beta1ReplicaSet createReplicaSet(ReplicaSetConfig replicaSetConfig) {
        V1beta1ReplicaSet replicaSet = new V1beta1ReplicaSet();
        replicaSet.setKind(REPLICA_KIND);
        replicaSet.setMetadata(createMetadata(replicaSetConfig.getReplicaName(), replicaSetConfig.getReplicaLabels()));
        replicaSet.setSpec(createReplicaSetSpec(replicaSetConfig));
        return replicaSet;
    }

    private V1beta1ReplicaSetSpec createReplicaSetSpec(ReplicaSetConfig replicaSetConfig) {
        V1beta1ReplicaSetSpec spec = new V1beta1ReplicaSetSpec();
        spec.setReplicas(replicaSetConfig.getReplica());
        spec.setSelector(new V1LabelSelector()
                .matchLabels(replicaSetConfig.getSelectorLabels())
        );
        spec.template(createPodTemplateSpec(replicaSetConfig));
        return spec;
    }

    private V1PodTemplateSpec createPodTemplateSpec(ReplicaSetConfig replicaSetConfig) {
        V1PodTemplateSpec podTemplateSpec = new V1PodTemplateSpec();
        podTemplateSpec.setMetadata(createMetadata(replicaSetConfig.getPodTemplateName(), replicaSetConfig.getPodTemplateLabels()));
        podTemplateSpec.setSpec(createPodSpec(replicaSetConfig));
        return podTemplateSpec;
    }

    private V1PodSpec createPodSpec(ReplicaSetConfig replicaSetConfig) {
        V1PodSpec podSpec = new V1PodSpec();
        podSpec.setContainers(createContainers(replicaSetConfig));
        return podSpec;
    }

    private List<V1Container> createContainers(ReplicaSetConfig replicaSetConfig) {
        return Collections.singletonList(new V1Container()
                .name(replicaSetConfig.getContainerName())
                .image(replicaSetConfig.getImage())
                .ports(createContainerPorts(replicaSetConfig.getPorts()))
                .env(createEnvVariables(replicaSetConfig.getEnvVariables()))
                .command(replicaSetConfig.getCommands())
        );
    }

    private List<V1ContainerPort> createContainerPorts(Map<Integer, String> ports) {
        List<V1ContainerPort> portList = new ArrayList<>();
        ports.forEach((portNum, portName) -> portList.add(new V1ContainerPort()
                .containerPort(portNum)
                .name(portName)
        ));
        return portList;
    }

    private List<V1EnvVar> createEnvVariables(Map<String, String> envVariables) {
        List<V1EnvVar> envVarList = new ArrayList<>();
        envVariables.forEach((envVarName , envVarValue) -> envVarList.add(new V1EnvVar()
                .name(envVarName)
                .value(envVarValue)
        ));
        return envVarList;
    }

    private V1ObjectMeta createMetadata(String name, Map labels) {
        V1ObjectMeta meta = new V1ObjectMeta();
        meta.setName(name);
        meta.setLabels(labels);
        return meta;
    }
}
