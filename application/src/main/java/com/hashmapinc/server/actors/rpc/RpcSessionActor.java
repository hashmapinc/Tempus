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
package com.hashmapinc.server.actors.rpc;

import akka.actor.ActorRef;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.hashmapinc.server.actors.ActorSystemContext;
import com.hashmapinc.server.actors.cluster.DecrementRpcSessionCountMsg;
import com.hashmapinc.server.actors.cluster.IncrementRpcSessionCountMsg;
import com.hashmapinc.server.actors.service.ContextAwareActor;
import com.hashmapinc.server.actors.service.ContextBasedCreator;
import com.hashmapinc.server.common.msg.cluster.ServerAddress;
import com.hashmapinc.server.gen.cluster.ClusterAPIProtos;
import com.hashmapinc.server.gen.cluster.ClusterRpcServiceGrpc;
import com.hashmapinc.server.service.cluster.rpc.GrpcSession;
import com.hashmapinc.server.service.cluster.rpc.GrpcSessionListener;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;


public class RpcSessionActor extends ContextAwareActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private GrpcSession session;

    private final ActorRef nodeMetricActor;

    public RpcSessionActor(ActorSystemContext systemContext, ActorRef nodeMetricActor) {
        super(systemContext);
        this.nodeMetricActor = nodeMetricActor;
    }

    @Override
    public void onReceive(Object msg) throws Exception {
        if (msg instanceof RpcSessionTellMsg) {
            tell((RpcSessionTellMsg) msg);
        } else if (msg instanceof RpcSessionCreateRequestMsg) {
            initSession((RpcSessionCreateRequestMsg) msg);
        }
    }

    private void tell(RpcSessionTellMsg msg) {
        session.sendMsg(msg.getMsg());
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        nodeMetricActor.tell(new IncrementRpcSessionCountMsg(), ActorRef.noSender());
    }

    @Override
    public void postStop() {
        log.info("Closing session -> {}", session.getRemoteServer());
        session.close();
        nodeMetricActor.tell(new DecrementRpcSessionCountMsg(), ActorRef.noSender());
    }

    private void initSession(RpcSessionCreateRequestMsg msg) {
        log.info("[{}] Initializing session", context().self());
        ServerAddress remoteServer = msg.getRemoteAddress();
        GrpcSessionListener listener = new BasicRpcSessionListener(systemContext, context().parent(), context().self());
        if (msg.getRemoteAddress() == null) {
            // Server session
            session = new GrpcSession(listener);
            session.setOutputStream(msg.getResponseObserver());
            session.initInputStream();
            session.initOutputStream();
            systemContext.getRpcService().onSessionCreated(msg.getMsgUid(), session.getInputStream());
        } else {
            // Client session
            Channel channel = ManagedChannelBuilder.forAddress(remoteServer.getHost(), remoteServer.getPort()).usePlaintext(true).build();
            session = new GrpcSession(remoteServer, listener);
            session.initInputStream();

            ClusterRpcServiceGrpc.ClusterRpcServiceStub stub = ClusterRpcServiceGrpc.newStub(channel);
            StreamObserver<ClusterAPIProtos.ToRpcServerMessage> outputStream = stub.handlePluginMsgs(session.getInputStream());

            session.setOutputStream(outputStream);
            session.initOutputStream();
            outputStream.onNext(toConnectMsg());
        }
    }

    public static class ActorCreator extends ContextBasedCreator<RpcSessionActor> {
        private static final long serialVersionUID = 1L;

        private final ActorRef nodeMetricActor;

        public ActorCreator(ActorSystemContext context, ActorRef nodeMetricActor) {
            super(context);
            this.nodeMetricActor = nodeMetricActor;
        }

        @Override
        public RpcSessionActor create() throws Exception {
            return new RpcSessionActor(context, nodeMetricActor);
        }
    }

    private ClusterAPIProtos.ToRpcServerMessage toConnectMsg() {
        ServerAddress instance = systemContext.getDiscoveryService().getCurrentServer().getServerAddress();
        return ClusterAPIProtos.ToRpcServerMessage.newBuilder().setConnectMsg(
                ClusterAPIProtos.ConnectRpcMessage.newBuilder().setServerAddress(
                        ClusterAPIProtos.ServerAddress.newBuilder().setHost(instance.getHost()).setPort(instance.getPort()).build()).build()).build();

    }
}
