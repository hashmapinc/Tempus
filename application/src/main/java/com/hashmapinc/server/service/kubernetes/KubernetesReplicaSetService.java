package com.hashmapinc.server.service.kubernetes;

import com.hashmapinc.server.common.data.kubernetes.ReplicaSetConfig;

public interface KubernetesReplicaSetService {

    boolean deployReplicaSet(ReplicaSetConfig replicaSetConfig);

}
