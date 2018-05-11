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
import com.hashmapinc.server.common.data.id.ClusterMetricId;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class ClusterMetric extends BaseData<ClusterMetricId> {

    private String nodeIp;
    private int nodePort;
    private boolean nodeStatus;
    private int rpcSessionCount;
    private int deviceSessionCount;

    public ClusterMetric() {
        super();
    }

    public ClusterMetric(ClusterMetricId id) {
        super(id);
    }

    public ClusterMetric(ClusterMetric clusterMetric) {
        super(clusterMetric);
        this.nodeIp = clusterMetric.nodeIp;
        this.nodePort = clusterMetric.nodePort;
        this.nodeStatus = clusterMetric.nodeStatus;
        this.rpcSessionCount = clusterMetric.rpcSessionCount;
        this.deviceSessionCount = clusterMetric.deviceSessionCount;
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
    public String toString() {
        return "ClusterMetric{" +
                "nodeIp=" + nodeIp +
                ", nodePort=" + nodePort +
                ", rpcSessionCount=" + rpcSessionCount +
                ", deviceSessionCount=" + deviceSessionCount +
                ", nodeStatus=" + nodeStatus +
                '}';
    }
}
