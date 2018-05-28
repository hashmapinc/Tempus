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
import com.hashmapinc.server.common.data.cluster.NodeMetric;
import com.hashmapinc.server.common.data.cluster.NodeStatus;

public class NodeMetricActor extends ContextAwareActor {

    private final String host;
    private final int port;
    protected final LoggingAdapter logger = Logging.getLogger(getContext().system(), this);

    private NodeMetricActor(ActorSystemContext systemContext, String host, int port) {
        super(systemContext);
        this.host = host;
        this.port = port;
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof RegisterNodeMsg) {
            logger.debug("message type RegisterNodeMsg");
            systemContext.getNodeMetricService().save(getNodeMetric());
        } else if (message instanceof IncrementRpcSessionCountMsg) {
            logger.debug("message type IncrementRpcSessionCountMsg");
            systemContext.getNodeMetricService().incrementRpcSessionCount(host, port);
        } else if (message instanceof DecrementRpcSessionCountMsg) {
            logger.debug("message type DecrementRpcSessionCountMsg");
            systemContext.getNodeMetricService().decrementRpcSessionCount(host, port);
        } else if (message instanceof IncrementDeviceSessionCountMsg) {
            logger.debug("message type IncrementDeviceSessionCountMsg");
            systemContext.getNodeMetricService().incrementDeviceSessionCount(host, port);
        } else if (message instanceof DecrementDeviceSessionCountMsg) {
            logger.debug("message type DecrementDeviceSessionCountMsg");
            systemContext.getNodeMetricService().decrementDeviceSessionCount(host, port);
        } else if (message instanceof UpdateNodeStatusMsg) {
            logger.debug("message type UpdateNodeStatusMsg");
            systemContext.getNodeMetricService().updateNodeStatus(((UpdateNodeStatusMsg) message).getNodeStatus(), ((UpdateNodeStatusMsg) message).getHost(), ((UpdateNodeStatusMsg) message).getPort());
        } else if (message instanceof DeleteNodeEntryMsg) {
            logger.error("message type DeleteNodeEntryMsg");
            systemContext.getNodeMetricService().deleteNodeEntryByHostAndPort(((DeleteNodeEntryMsg) message).getHost(), ((DeleteNodeEntryMsg) message).getPort());
        }
    }

    private NodeMetric getNodeMetric() {
        NodeMetric nodeMetric = new NodeMetric();
        nodeMetric.setHost(host);
        nodeMetric.setPort(port);
        nodeMetric.setNodeStatus(NodeStatus.UP);
        nodeMetric.setDeviceSessionCount(0);
        nodeMetric.setRpcSessionCount(0);
        return nodeMetric;
    }

    public static class ActorCreator extends ContextBasedCreator<NodeMetricActor> {
        private static final long serialVersionUID = 1L;
        private String host;
        private int port;

        public ActorCreator(ActorSystemContext context, String host, int port) {
            super(context);
            this.host = host;
            this.port = port;
        }

        @Override
        public NodeMetricActor create() throws Exception {
            return new NodeMetricActor(context, host, port);
        }
    }
}
