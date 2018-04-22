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
package com.hashmapinc.server.extensions.api.plugins.msg;

import com.hashmapinc.server.common.data.id.DeviceId;
import com.hashmapinc.server.common.data.id.PluginId;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.msg.cluster.ServerAddress;
import com.hashmapinc.server.extensions.api.device.ToDeviceActorNotificationMsg;

import java.util.Optional;

/**
 * @author Andrew Shvayka
 */
@ToString
@RequiredArgsConstructor
public class ToDeviceRpcRequestPluginMsg implements ToDeviceActorNotificationMsg {

    private final ServerAddress serverAddress;
    @Getter
    private final PluginId pluginId;
    @Getter
    private final TenantId pluginTenantId;
    @Getter
    private final ToDeviceRpcRequest msg;

    public ToDeviceRpcRequestPluginMsg(PluginId pluginId, TenantId pluginTenantId, ToDeviceRpcRequest msg) {
        this(null, pluginId, pluginTenantId, msg);
    }

    public Optional<ServerAddress> getServerAddress() {
        return Optional.ofNullable(serverAddress);
    }

    @Override
    public DeviceId getDeviceId() {
        return msg.getDeviceId();
    }

    @Override
    public TenantId getTenantId() {
        return msg.getTenantId();
    }
}

