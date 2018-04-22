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
package com.hashmapinc.server.dao.model.nosql;

import com.datastax.driver.core.utils.UUIDs;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hashmapinc.server.common.data.id.*;
import com.hashmapinc.server.common.data.plugin.ComponentLifecycleState;
import com.hashmapinc.server.common.data.Application;
import com.hashmapinc.server.common.data.DeviceType;
import com.hashmapinc.server.common.data.DeviceTypeConfigurations;
import com.hashmapinc.server.common.data.id.*;
import com.hashmapinc.server.dao.model.SearchTextEntity;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.datastax.driver.mapping.annotations.Transient;
import com.hashmapinc.server.dao.model.type.ComponentLifecycleStateCodec;
import com.hashmapinc.server.dao.model.type.JsonCodec;


import java.util.*;
import java.util.stream.Collectors;

import static com.hashmapinc.server.dao.model.ModelConstants.*;

@Table(name = APPLICATION_TABLE_NAME)
public final class ApplicationEntity implements SearchTextEntity<Application> {

    @Transient
    private static final long serialVersionUID = -5855480905292626926L;

    @PartitionKey(value = 0)
    @Column(name = ID_PROPERTY)
    private UUID id;

    @PartitionKey(value = 1)
    @Column(name = APPLICATION_TENANT_ID_PROPERTY)
    private UUID tenantId;

    @PartitionKey(value = 2)
    @Column(name = APPLICATION_CUSTOMER_ID_PROPERTY)
    private UUID customerId;

    @Column(name = APPLICATION_MINI_DASHBOARD_ID_PROPERTY)
    private UUID miniDashboardId;

    @Column(name = APPLICATION_DASHBOARD_ID_PROPERTY)
    private UUID dashboardId;

    @Column(name = APPLICATION_NAME)
    private String name;

    @Column(name = APPLICATION_IS_VALID)
    private Boolean isValid;

    @Column(name = APPLICATION_DESCRIPTION, codec = JsonCodec.class)
    private JsonNode additionalInfo;

    @Column(name = SEARCH_TEXT_PROPERTY)
    private String searchText;

    @Column(name = APPLICATION_RULES_COLUMN)
    private Set<UUID> rules;

    @Column(name = APPLICATION_COMPUTATION_JOBS_COLUMN)
    private Set<UUID> computationJobs;

    @Column(name = APPLICATION_DEVICE_TYPES_COLUMN)
    private Set<String> deviceTypes;

    @Column(name = APPLICATION_STATE_PROPERTY, codec = ComponentLifecycleStateCodec.class)
    private ComponentLifecycleState state;


    private static ObjectMapper mapper = new ObjectMapper();

    public ApplicationEntity() {
        super();
    }

    public ApplicationEntity(Application application) throws JsonProcessingException {
        if (application.getId() != null) {
            this.setId(application.getId().getId());
        }
        if (application.getTenantId() != null) {
            this.tenantId = application.getTenantId().getId();
        }
        if (application.getCustomerId() != null) {
            this.customerId = application.getCustomerId().getId();
        }

        if(application.getDashboardId() !=null) {
            this.dashboardId = application.getDashboardId().getId();
        }

        if(application.getMiniDashboardId() !=null) {
            this.miniDashboardId = application.getMiniDashboardId().getId();
        }

        if(application.getRules() !=null && application.getRules().size() !=0) {
            this.rules = application.getRules().stream().map(r -> (r.getId())).collect(Collectors.toSet());
        }

        if(application.getComputationJobIdSet() !=null && application.getComputationJobIdSet().size() !=0) {
            this.computationJobs = application.getComputationJobIdSet().stream().map(c -> (c.getId())).collect(Collectors.toSet());
        }

        this.name = application.getName();
        this.isValid = application.getIsValid();
        this.additionalInfo = application.getAdditionalInfo();
        this.deviceTypes = mapper.treeToValue(application.getDeviceTypes(), DeviceTypeConfigurations.class).getDeviceTypes().stream().map(DeviceType::getName).collect(Collectors.toSet());
        this.state = application.getState();
    }


    @Override
    public String getSearchTextSource() {
        return name;
    }

    @Override
    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public UUID getMiniDashboardId() {
        return miniDashboardId;
    }

    public void setMiniDashboardId(UUID miniDashboardId) {
        this.miniDashboardId = miniDashboardId;
    }

    public UUID getDashboardId() {
        return dashboardId;
    }

    public void setDashboardId(UUID dashboardId) {
        this.dashboardId = dashboardId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getIsValid() {
        return isValid;
    }

    public void setIsValid(Boolean valid) {
        isValid = valid;
    }

    public JsonNode getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(JsonNode additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public String getSearchText() {
        return searchText;
    }

    public Set<UUID> getRules() {
        return rules;
    }

    public void setRules(Set<UUID> rules) {
        this.rules = rules;
    }

    public Set<UUID> getComputationJobs() {
        return computationJobs;
    }

    public void setComputationJobs(Set<UUID> computationJobs) {
        this.computationJobs = computationJobs;
    }

    public Set<String> getDeviceTypes() {
        return deviceTypes;
    }

    public void setDeviceTypes(Set<String> deviceTypes) {
        this.deviceTypes = deviceTypes;
    }

    public ComponentLifecycleState getState() {
        return state;
    }

    public void setState(ComponentLifecycleState state) {
        this.state = state;
    }

    @Override
    public Application toData() {
        Application application = new Application(new ApplicationId(getId()));
        application.setCreatedTime(UUIDs.unixTimestamp(getId()));
        if (tenantId != null) {
            application.setTenantId(new TenantId(tenantId));
        }
        if (customerId != null) {
            application.setCustomerId(new CustomerId(customerId));
        }

        if(dashboardId !=null) {
            application.setDashboardId(new DashboardId(dashboardId));
        }

        if(miniDashboardId !=null) {
            application.setMiniDashboardId(new DashboardId(miniDashboardId));
        }

        if(rules !=null && rules.size() !=0) {
            application.setRules(rules.stream().map(RuleId::new).collect(Collectors.toSet()));
        }

        if(computationJobs !=null && computationJobs.size() !=0) {
            application.setComputationJobIdSet(computationJobs.stream().map(ComputationJobId::new).collect(Collectors.toSet()));
        }
        application.setName(name);
        application.setIsValid(isValid);
        application.setAdditionalInfo(additionalInfo);

        if(state != null) {
            application.setState(state);
        } else {
            application.setState(ComponentLifecycleState.SUSPENDED);
        }

        if(deviceTypes !=null) {
            DeviceTypeConfigurations deviceTypeConfigurations = new DeviceTypeConfigurations();
            List<DeviceType> deviceTypesModelList = new ArrayList<>();
            for(String dt: deviceTypes) {
                DeviceType deviceType = new DeviceType();
                deviceType.setName(dt);
                deviceTypesModelList.add(deviceType);
            }

            deviceTypeConfigurations.setDeviceTypes(deviceTypesModelList);
            application.setDeviceTypes(mapper.valueToTree(deviceTypeConfigurations));
        }
        return application;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApplicationEntity that = (ApplicationEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(tenantId, that.tenantId) &&
                Objects.equals(customerId, that.customerId) &&
                Objects.equals(miniDashboardId, that.miniDashboardId) &&
                Objects.equals(dashboardId, that.dashboardId) &&
                Objects.equals(name, that.name) &&
                Objects.equals(isValid, that.isValid) &&
                Objects.equals(additionalInfo, that.additionalInfo) &&
                Objects.equals(searchText, that.searchText) &&
                Objects.equals(rules, that.rules) &&
                Objects.equals(computationJobs, that.computationJobs) &&
                Objects.equals(deviceTypes, that.deviceTypes) &&
                state == that.state;
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, tenantId, customerId, miniDashboardId, dashboardId, name, isValid, additionalInfo, searchText, rules, computationJobs, deviceTypes, state);
    }
}
