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
package com.hashmapinc.server.dao.service.cluster;

import com.hashmapinc.server.common.data.cluster.NodeMetric;
import com.hashmapinc.server.common.data.cluster.NodeStatus;
import com.hashmapinc.server.dao.service.AbstractServiceTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

@Slf4j
public class BaseNodeMetricServiceTest extends AbstractServiceTest {

    private String host = "192.168.1.1";
    private int port = 1234;

    @After
    public final void tearDown() {
        nodeMetricService.deleteNodeEntryByHostAndPort(host, port);
    }

    @Test
    public void saveNodeMetric() throws Exception {
        NodeMetric nodeMetric = nodeMetricService.save(generateNodeMetric(host, port));
        Assert.assertNotNull(nodeMetric.getId());
        NodeMetric newNodeMetric = nodeMetricService.save(nodeMetric);
        Assert.assertEquals(nodeMetric.getHost(), newNodeMetric.getHost());
        Assert.assertEquals(nodeMetric.getPort(), newNodeMetric.getPort());
    }

    @Test
    public void findNodeMetricByHostAndPort() throws Exception {
        NodeMetric expected = nodeMetricService.save(generateNodeMetric(host, port));
        Assert.assertNotNull(expected.getHost());
        Optional<NodeMetric> found = nodeMetricService.findNodeMetricByHostAndPort(host, port);
        Assert.assertEquals(expected.getId(), found.get().getId());
        Assert.assertEquals(expected.getHost(), found.get().getHost());
    }

    @Test
    public void findAllNodeMetric() throws Exception {
        NodeMetric nodeMetric1 = nodeMetricService.save(generateNodeMetric(host, port));
        Assert.assertNotNull(nodeMetric1.getHost());
        String host2 = "192.168.1.2";
        NodeMetric nodeMetric2 = nodeMetricService.save(generateNodeMetric(host2, port));
        Assert.assertNotNull(nodeMetric2.getHost());
        List<NodeMetric> nodeMetricsList = nodeMetricService.findAll();
        Assert.assertEquals(2, nodeMetricsList.size());
        nodeMetricService.deleteNodeEntryByHostAndPort(host2, port);
    }

    @Test
    public void incrementRpcSessionCount() throws Exception {
        NodeMetric nodeMetric = nodeMetricService.save(generateNodeMetric(host, port));
        Assert.assertNotNull(nodeMetric.getHost());
        NodeMetric nodeMetric1 = nodeMetricService.incrementRpcSessionCount(host, port);
        Assert.assertNotNull(nodeMetric1.getHost());
        Optional<NodeMetric> found = nodeMetricService.findNodeMetricByHostAndPort(host, port);
        Assert.assertEquals(1, found.get().getRpcSessionCount());
    }

    @Test
    public void decrementRpcSessionCount() throws Exception {
        NodeMetric nodeMetric = nodeMetricService.save(generateNodeMetric(host, port));
        Assert.assertNotNull(nodeMetric.getHost());
        NodeMetric nodeMetric1 = nodeMetricService.incrementRpcSessionCount(host, port);
        Assert.assertNotNull(nodeMetric1.getHost());
        NodeMetric nodeMetric2 = nodeMetricService.decrementRpcSessionCount(host, port);
        Assert.assertNotNull(nodeMetric2.getHost());
        Optional<NodeMetric> found = nodeMetricService.findNodeMetricByHostAndPort(host, port);
        Assert.assertEquals(0, found.get().getRpcSessionCount());
    }

    @Test
    public void incrementDeviceSessionCount() throws Exception {
        NodeMetric nodeMetric = nodeMetricService.save(generateNodeMetric(host, port));
        Assert.assertNotNull(nodeMetric.getHost());
        NodeMetric nodeMetric1 = nodeMetricService.incrementDeviceSessionCount(host, port);
        Assert.assertNotNull(nodeMetric1.getHost());
        Optional<NodeMetric> found = nodeMetricService.findNodeMetricByHostAndPort(host, port);
        Assert.assertEquals(1, found.get().getDeviceSessionCount());
    }

    @Test
    public void decrementDeviceSessionCount() throws Exception {
        NodeMetric nodeMetric = nodeMetricService.save(generateNodeMetric(host, port));
        Assert.assertNotNull(nodeMetric.getHost());
        NodeMetric nodeMetric1 = nodeMetricService.incrementDeviceSessionCount(host, port);
        Assert.assertNotNull(nodeMetric1.getHost());
        NodeMetric nodeMetric2 = nodeMetricService.decrementDeviceSessionCount(host, port);
        Assert.assertNotNull(nodeMetric2.getHost());
        Optional<NodeMetric> found = nodeMetricService.findNodeMetricByHostAndPort(host, port);
        Assert.assertEquals(0, found.get().getDeviceSessionCount());
    }

    @Test
    public void deleteNodeEntryByHostAndPort() throws Exception {
        NodeMetric nodeMetric = nodeMetricService.save(generateNodeMetric(host, port));
        Assert.assertNotNull(nodeMetric.getHost());
        nodeMetricService.deleteNodeEntryByHostAndPort(host, port);
        Optional<NodeMetric> found = nodeMetricService.findNodeMetricByHostAndPort(host, port);
        Assert.assertEquals(false, found.isPresent());
    }

    @Test
    public void updateNodeStatus() throws Exception {
        NodeMetric nodeMetric = nodeMetricService.save(generateNodeMetric(host, port));
        Assert.assertNotNull(nodeMetric.getHost());
        NodeMetric updated = nodeMetricService.updateNodeStatus(NodeStatus.DOWN, host, port);
        Assert.assertEquals(NodeStatus.DOWN, updated.getNodeStatus());
    }
}
