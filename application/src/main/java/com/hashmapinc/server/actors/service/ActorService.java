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
package com.hashmapinc.server.actors.service;

import com.hashmapinc.server.common.data.id.*;
import com.hashmapinc.server.common.data.plugin.ComponentLifecycleEvent;
import com.hashmapinc.server.common.transport.SessionMsgProcessor;
import com.hashmapinc.server.service.cluster.discovery.DiscoveryServiceListener;
import com.hashmapinc.server.service.cluster.rpc.RpcMsgListener;
import com.hashmapinc.server.common.data.id.*;
import com.hashmapinc.server.service.computation.ComputationMsgListener;

public interface ActorService extends SessionMsgProcessor, WebSocketMsgProcessor, RestMsgProcessor, RpcMsgListener, DiscoveryServiceListener, ComputationMsgListener {

    void onPluginStateChange(TenantId tenantId, PluginId pluginId, ComponentLifecycleEvent state);

    void onRuleStateChange(TenantId tenantId, RuleId ruleId, ComponentLifecycleEvent state);

    void onComputationJobStateChange(TenantId tenantId, ComputationId computationId, ComputationJobId computationJobId, ComponentLifecycleEvent state);

    void onCredentialsUpdate(TenantId tenantId, DeviceId deviceId);

    void onDeviceNameOrTypeUpdate(TenantId tenantId, DeviceId deviceId, String deviceName, String deviceType);

    void onDashboardStateChange(TenantId tenantId, DashboardId dashboardId, ComponentLifecycleEvent event);
}
