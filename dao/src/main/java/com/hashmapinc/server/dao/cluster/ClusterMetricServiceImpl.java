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

import com.hashmapinc.server.common.data.cluster.ClusterMetric;
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
    public ClusterMetric save(ClusterMetric clusterMetric) {
        log.error("Executing ClusterMetricServiceImpl saveClusterMetric [{}]", clusterMetric);
        return clusterMetricDao.save(clusterMetric);
    }

    @Override
    public Optional<ClusterMetric> findClusterMetricByNodeIpAndNodePort(String nodeIp, int nodePort) {
        log.error("Executing ClusterMetricServiceImpl findByNodeIpAndNodePort, nodeIp [{}], nodePort [{}]", nodeIp, nodePort);
        return clusterMetricDao.findClusterMetricByNodeIpAndNodePort(nodeIp, nodePort);
    }

    @Override
    public List<ClusterMetric> findAll() {
        log.error("Executing ClusterMetricServiceImpl findAll ClusterMetric");
        return clusterMetricDao.find();
    }

    @Override
    public ClusterMetric incrementRpcSessionCount(String nodeIp, int nodePort) {
        log.error("Executing ClusterMetricServiceImpl incrementRpcSessionCount");
        Optional<ClusterMetric> clusterMetric = findClusterMetricByNodeIpAndNodePort(nodeIp, nodePort);
        if (clusterMetric.isPresent()) {
            int rpcSessionCount = clusterMetric.get().getRpcSessionCount();
            log.error("ClusterMetricServiceImpl rpcSessionCount [{}]", rpcSessionCount);
            clusterMetric.get().setRpcSessionCount(rpcSessionCount + 1);
            return clusterMetricDao.save(clusterMetric.get());
        }
        return clusterMetric.get();
    }

    @Override
    public ClusterMetric decrementRpcSessionCount(String nodeIp, int nodePort) {
        log.error("Executing decrementRpcSessionCount");
        Optional<ClusterMetric> clusterMetric = findClusterMetricByNodeIpAndNodePort(nodeIp, nodePort);
        if (clusterMetric.isPresent()) {
            int rpcSessionCount = clusterMetric.get().getRpcSessionCount();
            log.error("ClusterMetricServiceImpl rpcSessionCount [{}]", rpcSessionCount);
            rpcSessionCount--;
            if (rpcSessionCount < 0) {
                clusterMetric.get().setRpcSessionCount(0);
            } else {
                clusterMetric.get().setRpcSessionCount(rpcSessionCount);
            }
            return clusterMetricDao.save(clusterMetric.get());
        }
        return clusterMetric.get();
    }

    @Override
    public ClusterMetric incrementDeviceSessionCount(String nodeIp, int nodePort) {
        log.error("######################Executing incrementDeviceSessionCount################");
        Optional<ClusterMetric> clusterMetric = findClusterMetricByNodeIpAndNodePort(nodeIp, nodePort);
        if (clusterMetric.isPresent()) {
            int deviceSessionCount = clusterMetric.get().getDeviceSessionCount();
            log.error("ClusterMetricServiceImpl deviceSessionCount [{}]", deviceSessionCount);
            clusterMetric.get().setDeviceSessionCount(deviceSessionCount + 1);
            return clusterMetricDao.save(clusterMetric.get());
        }
        return clusterMetric.get();
    }

    @Override
    public ClusterMetric decrementDeviceSessionCount(String nodeIp, int nodePort) {
        log.error("Executing decrementDeviceSessionCount");
        Optional<ClusterMetric> clusterMetric = findClusterMetricByNodeIpAndNodePort(nodeIp, nodePort);
        if (clusterMetric.isPresent()) {
            int deviceSessionCount = clusterMetric.get().getDeviceSessionCount();
            log.error("ClusterMetricServiceImpl deviceSessionCount [{}]", deviceSessionCount);
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
