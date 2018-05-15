package com.hashmapinc.server.controller;

import com.datastax.driver.core.utils.UUIDs;
import com.hashmapinc.server.common.data.cluster.NodeMetric;
import com.hashmapinc.server.common.data.id.NodeMetricId;
import com.hashmapinc.server.dao.cluster.NodeMetricService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@Slf4j
public class BaseNodeMetricControllerTest extends AbstractControllerTest {

    @Autowired
    NodeMetricService nodeMetricService;

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
        nodeMetric.setHost("192.168.1.10");
        nodeMetric.setPort(1234);
        nodeMetric.setRpcSessionCount(0);
        nodeMetric.setDeviceSessionCount(0);
        nodeMetric.setId(new NodeMetricId(UUIDs.timeBased()));
        return nodeMetric;
    }
}
