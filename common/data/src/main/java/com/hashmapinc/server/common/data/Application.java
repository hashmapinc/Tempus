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
package com.hashmapinc.server.common.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.hashmapinc.server.common.data.id.*;
import com.hashmapinc.server.common.data.plugin.ComponentLifecycleState;
import com.hashmapinc.server.common.data.id.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Application extends SearchTextBased<ApplicationId> implements HasName {

    private static final long serialVersionUID = 1533755382827939542L;

    private TenantId tenantId;
    private CustomerId customerId;
    private DashboardId dashboardId;
    private DashboardId miniDashboardId;
    private Set<ComputationJobId> computationJobIdSet = new HashSet<>();
    private Set<RuleId> rules = new HashSet<>();
    private String name;
    private JsonNode additionalInfo;
    private JsonNode deviceTypes;
    private Boolean isValid = Boolean.TRUE;
    private ComponentLifecycleState state;

    public Application() {
        super();
    }

    public Application(ApplicationId id) {
        super(id);
    }

    public Application(Application application) {
        super(application);
        this.tenantId = application.tenantId;
        this.customerId = application.customerId;
        this.dashboardId = application.dashboardId;
        this.miniDashboardId = application.miniDashboardId;
        this.computationJobIdSet = application.computationJobIdSet;
        this.rules = application.rules;
        this.name = application.name;
        this.additionalInfo = application.getAdditionalInfo();
        this.deviceTypes = application.deviceTypes;
        this.isValid = application.isValid;
        this.state = application.state;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public ComponentLifecycleState getState() {
        return state;
    }

    public void setState(ComponentLifecycleState state) {
        this.state = state;
    }

    @Override
    public String getSearchText() {
        return name;
    }

    public DashboardId getDashboardId() {
        return dashboardId;
    }

    public void setDashboardId(DashboardId dashboardId) {
        this.dashboardId = dashboardId;
    }

    public DashboardId getMiniDashboardId() {
        return miniDashboardId;
    }

    public void setMiniDashboardId(DashboardId miniDashboardId) {
        this.miniDashboardId = miniDashboardId;
    }

    public Set<RuleId> getRules() {
        return rules;
    }

    public void setRules(Set<RuleId> rules) {
        this.rules = rules;
    }

    public void addRules(Set<RuleId> rules) {
        this.rules.addAll(rules);
    }

    public void addComputationJobs(Set<ComputationJobId> computationJobIds) {
        this.computationJobIdSet.addAll(computationJobIds);
    }

    public Set<ComputationJobId> getComputationJobIdSet() {
        return computationJobIdSet;
    }

    public void setComputationJobIdSet(Set<ComputationJobId> computationJobIdSet) {
        this.computationJobIdSet = computationJobIdSet;
    }

    public JsonNode getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(JsonNode additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public JsonNode getDeviceTypes() {
        return deviceTypes;
    }

    public void setDeviceTypes(JsonNode deviceTypes) {
        this.deviceTypes = deviceTypes;
    }

    public TenantId getTenantId() {
        return tenantId;
    }

    public void setTenantId(TenantId tenantId) {
        this.tenantId = tenantId;
    }

    public CustomerId getCustomerId() {
        return customerId;
    }

    public void setCustomerId(CustomerId customerId) {
        this.customerId = customerId;
    }

    public Boolean getIsValid() {
        return isValid;
    }

    public void setIsValid(Boolean valid) {
        isValid = valid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Application that = (Application) o;
        return Objects.equals(tenantId, that.tenantId) &&
                Objects.equals(customerId, that.customerId) &&
                Objects.equals(dashboardId, that.dashboardId) &&
                Objects.equals(miniDashboardId, that.miniDashboardId) &&
                Objects.equals(computationJobIdSet, that.computationJobIdSet) &&
                Objects.equals(rules, that.rules) &&
                Objects.equals(name, that.name) &&
                Objects.equals(additionalInfo, that.additionalInfo) &&
                Objects.equals(deviceTypes, that.deviceTypes) &&
                Objects.equals(isValid, that.isValid) &&
                state == that.state;
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), tenantId, customerId, dashboardId, miniDashboardId, computationJobIdSet, rules, name, additionalInfo, deviceTypes, isValid, state);
    }

    @Override
    public String toString() {
        return "Application{" +
                "tenantId=" + tenantId +
                ", customerId=" + customerId +
                ", dashboardId=" + dashboardId +
                ", miniDashboardId=" + miniDashboardId +
                ", computationJobIdSet=" + computationJobIdSet +
                ", rules=" + rules +
                ", name='" + name + '\'' +
                ", additionalInfo=" + additionalInfo +
                ", deviceTypes=" + deviceTypes +
                ", isValid=" + isValid +
                ", state=" + state +
                '}';
    }
}
