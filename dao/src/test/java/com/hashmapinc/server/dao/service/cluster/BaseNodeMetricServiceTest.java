package com.hashmapinc.server.dao.service.cluster;

import com.hashmapinc.server.common.data.cluster.NodeMetric;
import com.hashmapinc.server.dao.service.AbstractServiceTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

@Slf4j
public class BaseNodeMetricServiceTest extends AbstractServiceTest {

    @Test
    public void saveNodeMetric() throws Exception {
        NodeMetric nodeMetric = nodeMetricService.save(generateNodeMetric("192.168.1.1", 1234));
        Assert.assertNotNull(nodeMetric.getId());
        NodeMetric newNodeMetric = nodeMetricService.save(nodeMetric);
        Assert.assertEquals(nodeMetric.getHost(), newNodeMetric.getHost());
        Assert.assertEquals(nodeMetric.getPort(), newNodeMetric.getPort());
    }

    @Test
    public void findNodeMetricByHostAndPort() throws Exception {
        String host = "192.168.1.2";
        int port = 1234;
        NodeMetric expected = nodeMetricService.save(generateNodeMetric(host, port));
        Assert.assertNotNull(expected.getHost());
        Optional<NodeMetric> found = nodeMetricService.findNodeMetricByHostAndPort(host, port);
        Assert.assertEquals(expected.getId(), found.get().getId());
        Assert.assertEquals(expected.getHost(), found.get().getHost());
    }

    @Test
    public void findAllNodeMetric() throws Exception {
        NodeMetric nodeMetric1 = nodeMetricService.save(generateNodeMetric("192.168.1.3", 1234));
        Assert.assertNotNull(nodeMetric1.getHost());
        NodeMetric nodeMetric2 = nodeMetricService.save(generateNodeMetric("192.168.2.4", 2345));
        Assert.assertNotNull(nodeMetric2.getHost());
        List<NodeMetric> nodeMetricsList = nodeMetricService.findAll();
        Assert.assertEquals(2, nodeMetricsList.size());
    }

    @Test
    public void incrementRpcSessionCount() throws Exception {
        String host = "192.168.1.5";
        int port = 1234;
        NodeMetric nodeMetric = nodeMetricService.save(generateNodeMetric(host, port));
        Assert.assertNotNull(nodeMetric.getHost());
        NodeMetric nodeMetric1 = nodeMetricService.incrementRpcSessionCount(host, port);
        Assert.assertNotNull(nodeMetric1.getHost());
        Optional<NodeMetric> found = nodeMetricService.findNodeMetricByHostAndPort(host, port);
        Assert.assertEquals(1, found.get().getRpcSessionCount());
    }

    @Test
    public void decrementRpcSessionCount() throws Exception {
        String host = "192.168.1.6";
        int port = 1234;
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
        String host = "192.168.1.7";
        int port = 1234;
        NodeMetric nodeMetric = nodeMetricService.save(generateNodeMetric(host, port));
        Assert.assertNotNull(nodeMetric.getHost());
        NodeMetric nodeMetric1 = nodeMetricService.incrementDeviceSessionCount(host, port);
        Assert.assertNotNull(nodeMetric1.getHost());
        Optional<NodeMetric> found = nodeMetricService.findNodeMetricByHostAndPort(host, port);
        Assert.assertEquals(1, found.get().getDeviceSessionCount());
    }

    @Test
    public void decrementDeviceSessionCount() throws Exception {
        String host = "192.168.1.8";
        int port = 1234;
        NodeMetric nodeMetric = nodeMetricService.save(generateNodeMetric(host, port));
        Assert.assertNotNull(nodeMetric.getHost());
        NodeMetric nodeMetric1 = nodeMetricService.incrementDeviceSessionCount(host, port);
        Assert.assertNotNull(nodeMetric1.getHost());
        NodeMetric nodeMetric2 = nodeMetricService.decrementDeviceSessionCount(host, port);
        Assert.assertNotNull(nodeMetric2.getHost());
        Optional<NodeMetric> found = nodeMetricService.findNodeMetricByHostAndPort(host, port);
        Assert.assertEquals(0, found.get().getDeviceSessionCount());
    }
}
