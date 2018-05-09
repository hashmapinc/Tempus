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
package com.hashmapinc.server.dao.model.nosql;

import com.datastax.driver.mapping.annotations.PartitionKey;
import com.hashmapinc.server.common.data.cluster.ClusterMetric;
import com.hashmapinc.server.common.data.id.ClusterMetricId;
import com.hashmapinc.server.dao.model.BaseEntity;
import com.hashmapinc.server.dao.model.ModelConstants;

import javax.persistence.Column;
import javax.persistence.Table;

import java.util.UUID;

import static com.hashmapinc.server.dao.model.ModelConstants.ID_PROPERTY;

@Table(name = ModelConstants.CLUSTER_METRIC_COLUMN_FAMILY_NAME)
public class ClusterMetricEntity implements BaseEntity<ClusterMetric> {

    @PartitionKey
    @Column(name = ID_PROPERTY)
    private UUID id;

    @Column(name = ModelConstants.CLUSTER_METRIC_IP_PROPERTY)
    private String nodeIp;

    @Column(name = ModelConstants.CLUSTER_METRIC_PORT_PROPERTY)
    private int nodePort;

    @Column(name = ModelConstants.CLUSTER_METRIC_NODE_STATUS_PROPERTY)
    private boolean nodeStatus;

    @Column(name = ModelConstants.CLUSTER_METRIC_RPC_SESSION_PROPERTY)
    private int rpcSessionCount;

    @Column(name = ModelConstants.CLUSTER_METRIC_DEVICE_SESSION_PROPERTY)
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
    public UUID getId() {
        return id;
    }

    @Override
    public void setId(UUID id) {
        this.id = id;
    }

    public String getNodeIp() {
        return nodeIp;
    }

    public void setNodeIp(String nodeIp) {
        this.nodeIp = nodeIp;
    }

    public int getNodePort() {
        return nodePort;
    }

    public void setNodePort(int nodePort) {
        this.nodePort = nodePort;
    }

    public boolean isNodeStatus() {
        return nodeStatus;
    }

    public void setNodeStatus(boolean nodeStatus) {
        this.nodeStatus = nodeStatus;
    }

    public int getRpcSessionCount() {
        return rpcSessionCount;
    }

    public void setRpcSessionCount(int rpcSessionCount) {
        this.rpcSessionCount = rpcSessionCount;
    }

    public int getDeviceSessionCount() {
        return deviceSessionCount;
    }

    public void setDeviceSessionCount(int deviceSessionCount) {
        this.deviceSessionCount = deviceSessionCount;
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
