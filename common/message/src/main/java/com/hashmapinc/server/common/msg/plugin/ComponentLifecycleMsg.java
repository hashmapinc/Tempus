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
package com.hashmapinc.server.common.msg.plugin;

import com.hashmapinc.server.common.data.id.*;
import com.hashmapinc.server.common.data.plugin.ComponentLifecycleEvent;
import com.hashmapinc.server.common.msg.aware.TenantAwareMsg;
import com.hashmapinc.server.common.msg.cluster.ToAllNodesMsg;
import lombok.Getter;
import lombok.ToString;
import com.hashmapinc.server.common.data.id.*;

import java.util.Optional;

/**
 * @author Andrew Shvayka
 */
@ToString
public class ComponentLifecycleMsg implements TenantAwareMsg, ToAllNodesMsg {
    @Getter
    private final TenantId tenantId;
    private final PluginId pluginId;
    private final RuleId ruleId;
    private final DashboardId dashboardId;
    private final ComputationId computationId;
    private final ComputationJobId computationJobId;
    @Getter
    private final ComponentLifecycleEvent event;

    public static ComponentLifecycleMsg forPlugin(TenantId tenantId, PluginId pluginId, ComponentLifecycleEvent event) {
        return new ComponentLifecycleMsg(tenantId, pluginId, null, null, null, null, event);
    }

    public static ComponentLifecycleMsg forRule(TenantId tenantId, RuleId ruleId, ComponentLifecycleEvent event) {
        return new ComponentLifecycleMsg(tenantId, null, ruleId, null, null, null, event);
    }

    public static ComponentLifecycleMsg forDashboard(TenantId tenantId, DashboardId dashboardId, ComponentLifecycleEvent event) {
        return new ComponentLifecycleMsg(tenantId, null, null, dashboardId, null, null,  event);
    }

    public static ComponentLifecycleMsg forComputation(TenantId tenantId, ComputationId computationId, ComponentLifecycleEvent event) {
        return new ComponentLifecycleMsg(tenantId, null, null, null, computationId, null, event);
    }

    public static ComponentLifecycleMsg forComputationJob(TenantId tenantId, ComputationId computationId, ComputationJobId computationJobId, ComponentLifecycleEvent event) {
        return new ComponentLifecycleMsg(tenantId, null, null, null, computationId, computationJobId, event);
    }

    private ComponentLifecycleMsg(TenantId tenantId, PluginId pluginId,
                                  RuleId ruleId, DashboardId dashboardId, ComputationId computationId,
                                  ComputationJobId computationJobId, ComponentLifecycleEvent event) {
        this.tenantId = tenantId;
        this.pluginId = pluginId;
        this.ruleId = ruleId;
        this.dashboardId = dashboardId;
        this.computationId = computationId;
        this.computationJobId = computationJobId;
        this.event = event;
    }

    public Optional<PluginId> getPluginId() {
        return Optional.ofNullable(pluginId);
    }

    public Optional<RuleId> getRuleId() {
        return Optional.ofNullable(ruleId);
    }

    public Optional<DashboardId> getDashboardId() {
        return Optional.ofNullable(dashboardId);
    }

    public Optional<ComputationId> getComputationId() {
        return Optional.ofNullable(computationId);
    }

    public Optional<ComputationJobId> getComputationJobId() {
        return Optional.ofNullable(computationJobId);
    }
}
