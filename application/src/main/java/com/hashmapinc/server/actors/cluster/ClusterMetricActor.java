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
package com.hashmapinc.server.actors.cluster;

import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.hashmapinc.server.actors.ActorSystemContext;
import com.hashmapinc.server.actors.service.ContextAwareActor;
import com.hashmapinc.server.actors.service.ContextBasedCreator;
import com.hashmapinc.server.common.data.cluster.ClusterMetric;

public class ClusterMetricActor extends ContextAwareActor {

    private final String nodeIp;
    private final int nodePort;
    protected final LoggingAdapter logger = Logging.getLogger(getContext().system(), this);

    private ClusterMetricActor(ActorSystemContext systemContext, String nodeIp, int nodePort) {
        super(systemContext);
        this.nodeIp = nodeIp;
        this.nodePort = nodePort;
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof RegisterNodeMsg) {
            logger.error("message type RegisterNodeMsg");
            systemContext.getClusterMetricService().save(getClusterMetric());
        } else if (message instanceof IncrementRpcSessionCountMsg) {
            logger.error("message type IncrementRpcSessionCountMsg");
            systemContext.getClusterMetricService().incrementRpcSessionCount(nodeIp, nodePort);
        } else if (message instanceof DecrementRpcSessionCountMsg) {
            logger.error("message type DecrementRpcSessionCountMsg");
            systemContext.getClusterMetricService().decrementRpcSessionCount(nodeIp, nodePort);
        } else if (message instanceof IncrementDeviceSessionCountMsg) {
            logger.error("message type IncrementDeviceSessionCountMsg");
            systemContext.getClusterMetricService().incrementDeviceSessionCount(nodeIp, nodePort);
        } else if (message instanceof DecrementDeviceSessionCountMsg) {
            logger.error("message type DecrementDeviceSessionCountMsg");
            systemContext.getClusterMetricService().decrementDeviceSessionCount(nodeIp, nodePort);
        }
    }

    private ClusterMetric getClusterMetric() {
        logger.error("In get Cluster Metric...");
        ClusterMetric clusterMetric = new ClusterMetric();
        clusterMetric.setNodeIp(nodeIp);
        clusterMetric.setNodePort(nodePort);
        clusterMetric.setNodeStatus(true);
        clusterMetric.setDeviceSessionCount(0);
        clusterMetric.setRpcSessionCount(0);
        return clusterMetric;
    }

    public static class ActorCreator extends ContextBasedCreator<ClusterMetricActor> {
        private static final long serialVersionUID = 1L;
        private String nodeIp;
        private int nodePort;

        public ActorCreator(ActorSystemContext context, String nodeIp, int nodePort) {
            super(context);
            this.nodeIp = nodeIp;
            this.nodePort = nodePort;
        }

        @Override
        public ClusterMetricActor create() throws Exception {
            return new ClusterMetricActor(context, nodeIp, nodePort);
        }
    }
}
