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

import com.hashmapinc.server.actors.rpc.RpcSessionTellMsg;
import com.hashmapinc.server.common.msg.cluster.ToAllNodesMsg;
import com.hashmapinc.server.actors.rpc.RpcBroadcastMsg;
import com.hashmapinc.server.actors.rpc.RpcSessionCreateRequestMsg;
import com.hashmapinc.server.common.msg.core.ToDeviceSessionActorMsg;
import com.hashmapinc.server.common.msg.device.ToDeviceActorMsg;
import com.hashmapinc.server.extensions.api.device.ToDeviceActorNotificationMsg;
import com.hashmapinc.server.extensions.api.plugins.msg.ToPluginActorMsg;

/**
 * @author Andrew Shvayka
 */
public interface RpcMsgListener {

    void onMsg(ToDeviceActorMsg msg);

    void onMsg(ToDeviceActorNotificationMsg msg);

    void onMsg(ToDeviceSessionActorMsg msg);

    void onMsg(ToAllNodesMsg nodeMsg);

    void onMsg(ToPluginActorMsg msg);

    void onMsg(RpcSessionCreateRequestMsg msg);

    void onMsg(RpcSessionTellMsg rpcSessionTellMsg);

    void onMsg(RpcBroadcastMsg rpcBroadcastMsg);

}
