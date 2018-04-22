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
package com.hashmapinc.server.dao.model.sql;

import com.datastax.driver.core.utils.UUIDs;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hashmapinc.server.common.data.id.*;
import com.hashmapinc.server.dao.util.mapping.JsonStringType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import com.hashmapinc.server.common.data.Application;
import com.hashmapinc.server.common.data.DeviceType;
import com.hashmapinc.server.common.data.DeviceTypeConfigurations;
import com.hashmapinc.server.common.data.id.*;
import com.hashmapinc.server.common.data.plugin.ComponentLifecycleState;
import com.hashmapinc.server.dao.model.BaseSqlEntity;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.model.SearchTextEntity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@TypeDef(name = "json", typeClass = JsonStringType.class)
@Table(name = ModelConstants.APPLICATION_TABLE_NAME)
public final class ApplicationEntity extends BaseSqlEntity<Application> implements SearchTextEntity<Application> {

    @Transient
    private static final long serialVersionUID = -3873737406462009031L;

    @Column(name = ModelConstants.APPLICATION_TENANT_ID_PROPERTY)
    private String tenantId;

    @Column(name = ModelConstants.APPLICATION_CUSTOMER_ID_PROPERTY)
    private String customerId;

    @Column(name = ModelConstants.APPLICATION_MINI_DASHBOARD_ID_PROPERTY)
    private String miniDashboardId;

    @Column(name = ModelConstants.APPLICATION_DASHBOARD_ID_PROPERTY)
    private String dashboardId;

    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    @CollectionTable(name = ModelConstants.APPLICATION_RULES_ASSOCIATION_TABLE, joinColumns = @JoinColumn(name = ModelConstants.APPLICATION_ID_COLUMN))
    @Column(name = ModelConstants.APPLICATION_RULE_ID_COLUMN)
    private Set<String> rules;

    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    @CollectionTable(name = ModelConstants.APPLICATION_COMPUTATION_JOBS_ASSOCIATION_TABLE, joinColumns = @JoinColumn(name = ModelConstants.APPLICATION_ID_COLUMN))
    @Column(name = ModelConstants.APPLICATION_COMPUTATION_JOB_ID_COLUMN)
    private Set<String> computationJobs;

    @Column(name = ModelConstants.APPLICATION_NAME)
    private String name;

    @Column(name = ModelConstants.APPLICATION_IS_VALID)
    private Boolean isValid;

    @Type(type = "json")
    @Column(name = ModelConstants.APPLICATION_DESCRIPTION)
    private JsonNode additionalInfo;

    @Column(name = ModelConstants.SEARCH_TEXT_PROPERTY)
    private String searchText;

    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    @CollectionTable(name = ModelConstants.APPLICATION_DEVICE_TYPES_TABLE, joinColumns = @JoinColumn(name = ModelConstants.APPLICATION_ID_COLUMN))
    @Column(name = ModelConstants.APPLICATION_DEVICE_TYPES)
    private Set<String> deviceTypes;

    @Enumerated(EnumType.STRING)
    @Column(name = ModelConstants.APPLICATION_STATE_PROPERTY)
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
            this.tenantId = toString(application.getTenantId().getId());
        }
        if (application.getCustomerId() != null) {
            this.customerId = toString(application.getCustomerId().getId());
        }

        if(application.getDashboardId() !=null) {
            this.dashboardId = toString(application.getDashboardId().getId());
        }

        if(application.getMiniDashboardId() !=null) {
            this.miniDashboardId = toString(application.getMiniDashboardId().getId());
        }

        if(application.getRules() !=null && application.getRules().size() !=0) {
            this.rules = application.getRules().stream().map(r -> toString(r.getId())).collect(Collectors.toSet());
        }

        if(application.getComputationJobIdSet() !=null && application.getComputationJobIdSet().size() !=0) {
            this.computationJobs = application.getComputationJobIdSet().stream().map(c -> toString(c.getId())).collect(Collectors.toSet());
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

    @Override
    public Application toData() {
        Application application = new Application(new ApplicationId(getId()));
        application.setCreatedTime(UUIDs.unixTimestamp(getId()));
        if (tenantId != null) {
            application.setTenantId(new TenantId(toUUID(tenantId)));
        }
        if (customerId != null) {
            application.setCustomerId(new CustomerId(toUUID(customerId)));
        }

        if(dashboardId !=null) {
           application.setDashboardId(new DashboardId(toUUID(dashboardId)));
        }

        if(miniDashboardId !=null) {
            application.setMiniDashboardId(new DashboardId(toUUID(miniDashboardId)));
        }

        if(rules !=null && rules.size() !=0) {
            application.setRules(rules.stream().map(r -> new RuleId(toUUID(r))).collect(Collectors.toSet()));
        }

        if(computationJobs !=null && computationJobs.size() !=0) {
            application.setComputationJobIdSet(computationJobs.stream().map(c -> new ComputationJobId(toUUID(c))).collect(Collectors.toSet()));
        }
        application.setName(name);
        application.setIsValid(isValid);
        application.setAdditionalInfo(additionalInfo);
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
        application.setState(state);
        return application;

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ApplicationEntity that = (ApplicationEntity) o;
        return Objects.equals(tenantId, that.tenantId) &&
                Objects.equals(customerId, that.customerId) &&
                Objects.equals(miniDashboardId, that.miniDashboardId) &&
                Objects.equals(dashboardId, that.dashboardId) &&
                Objects.equals(rules, that.rules) &&
                Objects.equals(computationJobs, that.computationJobs) &&
                Objects.equals(name, that.name) &&
                Objects.equals(isValid, that.isValid) &&
                Objects.equals(additionalInfo, that.additionalInfo) &&
                Objects.equals(searchText, that.searchText) &&
                Objects.equals(deviceTypes, that.deviceTypes) &&
                state == that.state;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), tenantId, customerId, miniDashboardId, dashboardId, rules, computationJobs, name, isValid, additionalInfo, searchText, deviceTypes, state);
    }
}
