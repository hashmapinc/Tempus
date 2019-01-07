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
package com.hashmapinc.server.dao.cluster;

import com.hashmapinc.server.common.data.cluster.NodeMetric;
import com.hashmapinc.server.common.data.cluster.NodeStatus;

import java.util.List;
import java.util.Optional;

public interface NodeMetricService {

    Optional<NodeMetric> findNodeMetricByHostAndPort(String host, int port);

    NodeMetric save(NodeMetric nodeMetric);

    List<NodeMetric> findAll();

    NodeMetric incrementRpcSessionCount(String host, int port);

    NodeMetric decrementRpcSessionCount(String host, int port);

    NodeMetric incrementDeviceSessionCount(String host, int port);

    NodeMetric decrementDeviceSessionCount(String host, int port);

    void deleteNodeEntryByHostAndPort(String host, int port);

    NodeMetric updateNodeStatus(NodeStatus nodeStatus, String host, int port);
}
