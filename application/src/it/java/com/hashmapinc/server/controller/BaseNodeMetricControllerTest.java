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
package com.hashmapinc.server.controller;

import com.datastax.driver.core.utils.UUIDs;
import com.hashmapinc.server.common.data.cluster.NodeMetric;
import com.hashmapinc.server.common.data.cluster.NodeStatus;
import com.hashmapinc.server.common.data.id.NodeMetricId;
import com.hashmapinc.server.dao.cluster.NodeMetricService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class BaseNodeMetricControllerTest extends AbstractControllerTest {

    @Autowired
    NodeMetricService nodeMetricService;

    private String host = "192.168.1.1";
    private int port = 1234;

    @After
    public final void tearDown() {
        nodeMetricService.deleteNodeEntryByHostAndPort(host, port);
    }

    @Test
    public void getNodeMetric() throws Exception {
        loginSysAdmin();
        NodeMetric nodeMetric = createNodeMetric();
        nodeMetricService.save(nodeMetric);
        List<NodeMetric> nodeMetricList = doGet("/api/nodes", List.class);
        Assert.assertNotNull(nodeMetricList);
    }

    NodeMetric createNodeMetric() {
        NodeMetric nodeMetric = new NodeMetric();
        nodeMetric.setHost(host);
        nodeMetric.setPort(port);
        nodeMetric.setNodeStatus(NodeStatus.UP);
        nodeMetric.setRpcSessionCount(0);
        nodeMetric.setDeviceSessionCount(0);
        nodeMetric.setId(new NodeMetricId(UUIDs.timeBased()));
        return nodeMetric;
    }
}
