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
package com.hashmapinc.server.dao.model.sql;


import com.hashmapinc.server.common.data.cluster.ClusterMetric;
import com.hashmapinc.server.common.data.id.ClusterMetricId;
import com.hashmapinc.server.dao.model.BaseEntity;
import com.hashmapinc.server.dao.model.BaseSqlEntity;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.util.mapping.JsonStringType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@TypeDef(name = "json", typeClass = JsonStringType.class)
@Table(name = ModelConstants.CLUSTER_METRIC_TALBE_NAME)
public class ClusterMetricEntity extends BaseSqlEntity<ClusterMetric> implements BaseEntity<ClusterMetric> {

    @Column(name = ModelConstants.CLUSTER_METRIC_IP)
    private String nodeIp;

    @Column(name = ModelConstants.CLUSTER_METRIC_PORT)
    private int nodePort;

    @Column(name = ModelConstants.CLUSTER_METRIC_NODE_STATUS)
    private boolean nodeStatus;

    @Column(name = ModelConstants.CLUSTER_METRIC_RPC_SESSION)
    private int rpcSessionCount;

    @Column(name = ModelConstants.CLUSTER_METRIC_DEVICE_SESSION)
    private int deviceSessionCount;

    public ClusterMetricEntity() {
        super();
    }

    public ClusterMetricEntity(ClusterMetric clusterMetric) {
        if (clusterMetric.getId() != null) {
            this.setId(clusterMetric.getId().getId());
        }

        this.nodeIp = clusterMetric.getNodeIp();
        this.nodePort = clusterMetric.getNodePort();
        this.nodeStatus = clusterMetric.isNodeStatus();
        this.rpcSessionCount = clusterMetric.getRpcSessionCount();
        this.deviceSessionCount = clusterMetric.getDeviceSessionCount();
    }

    @Override
    public ClusterMetric toData() {
        ClusterMetric clusterMetric = new ClusterMetric(new ClusterMetricId(getId()));
        clusterMetric.setNodeIp(nodeIp);
        clusterMetric.setNodePort(nodePort);
        clusterMetric.setNodeStatus(nodeStatus);
        clusterMetric.setRpcSessionCount(rpcSessionCount);
        clusterMetric.setDeviceSessionCount(deviceSessionCount);
        return clusterMetric;
    }
}
