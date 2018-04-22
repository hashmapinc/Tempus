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
package com.hashmapinc.server.service.cluster.rpc;

import com.hashmapinc.server.common.msg.cluster.ServerAddress;
import com.hashmapinc.server.common.msg.cluster.ToAllNodesMsg;
import com.hashmapinc.server.common.msg.core.ToDeviceSessionActorMsg;
import com.hashmapinc.server.extensions.api.device.ToDeviceActorNotificationMsg;
import com.hashmapinc.server.extensions.api.plugins.msg.ToDeviceRpcRequestPluginMsg;
import com.hashmapinc.server.extensions.api.plugins.msg.ToPluginRpcResponseDeviceMsg;
import com.hashmapinc.server.extensions.api.plugins.rpc.PluginRpcMsg;
import com.hashmapinc.server.gen.cluster.ClusterAPIProtos;
import io.grpc.stub.StreamObserver;
import com.hashmapinc.server.common.msg.device.ToDeviceActorMsg;

import java.util.UUID;

/**
 * @author Andrew Shvayka
 */
public interface ClusterRpcService {

    void init(RpcMsgListener listener);

    void tell(ServerAddress serverAddress, ToDeviceActorMsg toForward);

    void tell(ServerAddress serverAddress, ToDeviceSessionActorMsg toForward);

    void tell(ServerAddress serverAddress, ToDeviceActorNotificationMsg toForward);

    void tell(ServerAddress serverAddress, ToDeviceRpcRequestPluginMsg toForward);

    void tell(ServerAddress serverAddress, ToPluginRpcResponseDeviceMsg toForward);

    void tell(PluginRpcMsg toForward);

    void broadcast(ToAllNodesMsg msg);

    void onSessionCreated(UUID msgUid, StreamObserver<ClusterAPIProtos.ToRpcServerMessage> inputStream);
}
