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
package com.hashmapinc.server.common.data.cluster;

import com.hashmapinc.server.common.data.BaseData;
import com.hashmapinc.server.common.data.id.NodeMetricId;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class NodeMetric extends BaseData<NodeMetricId> {

    private String host;
    private int port;
    private boolean nodeStatus;
    private int rpcSessionCount;
    private int deviceSessionCount;

    public NodeMetric() {
        super();
    }

    public NodeMetric(NodeMetricId id) {
        super(id);
    }

    public NodeMetric(NodeMetric nodeMetric) {
        super(nodeMetric);
        this.host = nodeMetric.host;
        this.port = nodeMetric.port;
        this.nodeStatus = nodeMetric.nodeStatus;
        this.rpcSessionCount = nodeMetric.rpcSessionCount;
        this.deviceSessionCount = nodeMetric.deviceSessionCount;
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
    public String toString() {
        return "NodeMetric{" +
                "host=" + host +
                ", port=" + port +
                ", rpcSessionCount=" + rpcSessionCount +
                ", deviceSessionCount=" + deviceSessionCount +
                ", nodeStatus=" + nodeStatus +
                '}';
    }
}
