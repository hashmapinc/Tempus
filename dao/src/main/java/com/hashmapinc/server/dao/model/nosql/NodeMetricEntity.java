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
package com.hashmapinc.server.dao.model.nosql;

import com.datastax.driver.mapping.annotations.PartitionKey;
import com.hashmapinc.server.common.data.cluster.NodeMetric;
import com.hashmapinc.server.common.data.cluster.NodeStatus;
import com.hashmapinc.server.common.data.id.NodeMetricId;
import com.hashmapinc.server.dao.model.BaseEntity;
import com.hashmapinc.server.dao.model.ModelConstants;

import com.datastax.driver.mapping.annotations.Table;
import com.datastax.driver.mapping.annotations.Column;
import com.hashmapinc.server.dao.model.type.NodeStatusCodec;

import java.util.UUID;

import static com.hashmapinc.server.dao.model.ModelConstants.ID_PROPERTY;

@Table(name = ModelConstants.NODE_METRIC_COLUMN_FAMILY_NAME)
public class NodeMetricEntity implements BaseEntity<NodeMetric> {

    @PartitionKey
    @Column(name = ID_PROPERTY)
    private UUID id;

    @Column(name = ModelConstants.NODE_METRIC_HOST_PROPERTY)
    private String host;

    @Column(name = ModelConstants.NODE_METRIC_PORT_PROPERTY)
    private int port;

    @Column(name = ModelConstants.NODE_METRIC_STATUS_PROPERTY, codec = NodeStatusCodec.class)
    private NodeStatus nodeStatus;

    @Column(name = ModelConstants.NODE_METRIC_RPC_SESSION_PROPERTY)
    private int rpcSessionCount;

    @Column(name = ModelConstants.NODE_METRIC_DEVICE_SESSION_PROPERTY)
    private int deviceSessionCount;

    public NodeMetricEntity() {
        super();
    }

    public NodeMetricEntity(NodeMetric nodeMetric) {
        if (nodeMetric.getId() != null) {
            this.setId(nodeMetric.getId().getId());
        }

        this.host = nodeMetric.getHost();
        this.port = nodeMetric.getPort();
        this.nodeStatus = nodeMetric.getNodeStatus();
        this.rpcSessionCount = nodeMetric.getRpcSessionCount();
        this.deviceSessionCount = nodeMetric.getDeviceSessionCount();
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void setId(UUID id) {
        this.id = id;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public NodeStatus getNodeStatus() {
        return nodeStatus;
    }

    public void setNodeStatus(NodeStatus nodeStatus) {
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
    public NodeMetric toData() {
        NodeMetric nodeMetric = new NodeMetric(new NodeMetricId(getId()));
        nodeMetric.setHost(host);
        nodeMetric.setPort(port);
        nodeMetric.setNodeStatus(nodeStatus);
        nodeMetric.setRpcSessionCount(rpcSessionCount);
        nodeMetric.setDeviceSessionCount(deviceSessionCount);
        return nodeMetric;
    }
}
