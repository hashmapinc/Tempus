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
package com.hashmapinc.server.dao.cluster;

import com.hashmapinc.server.common.data.cluster.NodeMetric;
import com.hashmapinc.server.dao.entity.AbstractEntityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ClusterMetricServiceImpl extends AbstractEntityService implements ClusterMetricService {

    @Autowired
    private ClusterMetricDao clusterMetricDao;

    @Override
    public NodeMetric save(NodeMetric nodeMetric) {
        log.debug("Executing ClusterMetricServiceImpl saveClusterMetric [{}]", nodeMetric);
        return clusterMetricDao.save(nodeMetric);
    }

    @Override
    public Optional<NodeMetric> findClusterMetricByNodeIpAndNodePort(String nodeIp, int nodePort) {
        log.debug("Executing ClusterMetricServiceImpl findByNodeIpAndNodePort, nodeIp [{}], nodePort [{}]", nodeIp, nodePort);
        return clusterMetricDao.findClusterMetricByNodeIpAndNodePort(nodeIp, nodePort);
    }

    @Override
    public List<NodeMetric> findAll() {
        log.debug("Executing ClusterMetricServiceImpl findAll NodeMetric");
        return clusterMetricDao.find();
    }

    @Override
    public NodeMetric incrementRpcSessionCount(String nodeIp, int nodePort) {
        Optional<NodeMetric> clusterMetric = findClusterMetricByNodeIpAndNodePort(nodeIp, nodePort);
        if (clusterMetric.isPresent()) {
            int rpcSessionCount = clusterMetric.get().getRpcSessionCount();
            log.debug("ClusterMetricServiceImpl rpcSessionCount [{}]", rpcSessionCount);
            clusterMetric.get().setRpcSessionCount(rpcSessionCount + 1);
            return clusterMetricDao.save(clusterMetric.get());
        }
        return clusterMetric.get();
    }

    @Override
    public NodeMetric decrementRpcSessionCount(String nodeIp, int nodePort) {
        Optional<NodeMetric> clusterMetric = findClusterMetricByNodeIpAndNodePort(nodeIp, nodePort);
        if (clusterMetric.isPresent()) {
            int rpcSessionCount = clusterMetric.get().getRpcSessionCount();
            log.debug("ClusterMetricServiceImpl rpcSessionCount [{}]", rpcSessionCount);
            rpcSessionCount--;
            clusterMetric.get().setRpcSessionCount(rpcSessionCount);
            /*if (rpcSessionCount < 0) {
                clusterMetric.get().setRpcSessionCount(0);
            } else {
                clusterMetric.get().setRpcSessionCount(rpcSessionCount);
            }*/
            return clusterMetricDao.save(clusterMetric.get());
        }
        return clusterMetric.get();
    }

    @Override
    public NodeMetric incrementDeviceSessionCount(String nodeIp, int nodePort) {
        Optional<NodeMetric> clusterMetric = findClusterMetricByNodeIpAndNodePort(nodeIp, nodePort);
        if (clusterMetric.isPresent()) {
            int deviceSessionCount = clusterMetric.get().getDeviceSessionCount();
            log.debug("ClusterMetricServiceImpl deviceSessionCount [{}]", deviceSessionCount);
            clusterMetric.get().setDeviceSessionCount(deviceSessionCount + 1);
            return clusterMetricDao.save(clusterMetric.get());
        }
        return clusterMetric.get();
    }

    @Override
    public NodeMetric decrementDeviceSessionCount(String nodeIp, int nodePort) {
        Optional<NodeMetric> clusterMetric = findClusterMetricByNodeIpAndNodePort(nodeIp, nodePort);
        if (clusterMetric.isPresent()) {
            int deviceSessionCount = clusterMetric.get().getDeviceSessionCount();
            log.debug("ClusterMetricServiceImpl deviceSessionCount [{}]", deviceSessionCount);
            deviceSessionCount--;
            clusterMetric.get().setDeviceSessionCount(deviceSessionCount);
            /*if (deviceSessionCount < 0) {
                clusterMetric.get().setDeviceSessionCount(0);
            } else {
                clusterMetric.get().setDeviceSessionCount(deviceSessionCount);
            }*/
            return clusterMetricDao.save(clusterMetric.get());
        }
        return clusterMetric.get();
    }
}
