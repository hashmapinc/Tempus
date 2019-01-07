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
package com.hashmapinc.server.dao.model.sql;


import com.hashmapinc.server.common.data.cluster.NodeMetric;
import com.hashmapinc.server.common.data.cluster.NodeStatus;
import com.hashmapinc.server.common.data.id.NodeMetricId;
import com.hashmapinc.server.dao.model.BaseEntity;
import com.hashmapinc.server.dao.model.BaseSqlEntity;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.util.mapping.JsonStringType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@TypeDef(name = "json", typeClass = JsonStringType.class)
@Table(name = ModelConstants.NODE_METRIC_TALBE_NAME)
public class NodeMetricEntity extends BaseSqlEntity<NodeMetric> implements BaseEntity<NodeMetric> {

    @Column(name = ModelConstants.NODE_METRIC_HOST)
    private String host;

    @Column(name = ModelConstants.NODE_METRIC_PORT)
    private int port;

    @Enumerated(EnumType.STRING)
    @Column(name = ModelConstants.NODE_METRIC_STATUS)
    private NodeStatus nodeStatus;

    @Column(name = ModelConstants.NODE_METRIC_RPC_SESSION)
    private int rpcSessionCount;

    @Column(name = ModelConstants.NODE_METRIC_DEVICE_SESSION)
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
