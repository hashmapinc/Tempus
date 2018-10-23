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
package com.hashmapinc.server.dao.model.nosql;

import com.datastax.driver.core.utils.UUIDs;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.datastax.driver.mapping.annotations.Transient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hashmapinc.server.common.data.DashboardType;
import com.hashmapinc.server.common.data.Dashboard;
import com.hashmapinc.server.common.data.ShortCustomerInfo;
import com.hashmapinc.server.common.data.id.DashboardId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.model.SearchTextEntity;
import com.hashmapinc.server.dao.model.sql.AssetLandingInfoEntity;
import com.hashmapinc.server.dao.model.type.DashboardTypeCodec;
import com.hashmapinc.server.dao.model.type.JsonCodec;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.HashSet;
import java.util.UUID;

@Table(name = ModelConstants.DASHBOARD_COLUMN_FAMILY_NAME)
@EqualsAndHashCode
@ToString
@Slf4j
public final class DashboardEntity implements SearchTextEntity<Dashboard> {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final JavaType assignedCustomersType =
            objectMapper.getTypeFactory().constructCollectionType(HashSet.class, ShortCustomerInfo.class);

    @PartitionKey(value = 0)
    @Column(name = ModelConstants.ID_PROPERTY)
    private UUID id;
    
    @PartitionKey(value = 1)
    @Column(name = ModelConstants.DASHBOARD_TENANT_ID_PROPERTY)
    private UUID tenantId;

    @Column(name = ModelConstants.DASHBOARD_TITLE_PROPERTY)
    private String title;
    
    @Column(name = ModelConstants.SEARCH_TEXT_PROPERTY)
    private String searchText;

    @Column(name = ModelConstants.DASHBOARD_ASSIGNED_CUSTOMERS_PROPERTY)
    private String assignedCustomers;

    @Column(name = ModelConstants.DASHBOARD_CONFIGURATION_PROPERTY, codec = JsonCodec.class)
    private JsonNode configuration;

    @Column(name = ModelConstants.DASHBOARD_TYPE_PROPERTY, codec = DashboardTypeCodec.class)
    private DashboardType type;

    @Transient
    private AssetLandingInfoEntity assetLandingInfoEntity;

    public DashboardEntity() {
        super();
    }

    public DashboardEntity(Dashboard dashboard) {
        if (dashboard.getId() != null) {
            this.id = dashboard.getId().getId();
        } else {
            this.id = UUIDs.timeBased();
        }
        if (dashboard.getTenantId() != null) {
            this.tenantId = dashboard.getTenantId().getId();
        }
        if (dashboard.getType() != null) {
            this.type = dashboard.getType();
        }
        else {
            this.type = DashboardType.DEFAULT;
        }
        if (dashboard.getAssetLandingInfo() != null) {
            if (dashboard.getAssetLandingInfo().getDashboardId() == null) {
                dashboard.getAssetLandingInfo().setDashboardId(new DashboardId(this.id));
            }
            this.assetLandingInfoEntity = new AssetLandingInfoEntity(dashboard.getAssetLandingInfo());
        }
        this.title = dashboard.getTitle();
        if (dashboard.getAssignedCustomers() != null) {
            try {
                this.assignedCustomers = objectMapper.writeValueAsString(dashboard.getAssignedCustomers());
            } catch (JsonProcessingException e) {
                log.error("Unable to serialize assigned customers to string!", e);
            }
        }
        this.configuration = dashboard.getConfiguration();
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAssignedCustomers() {
        return assignedCustomers;
    }

    public void setAssignedCustomers(String assignedCustomers) {
        this.assignedCustomers = assignedCustomers;
    }

    public JsonNode getConfiguration() {
        return configuration;
    }

    public void setConfiguration(JsonNode configuration) {
        this.configuration = configuration;
    }
    
    @Override
    public String getSearchTextSource() {
        return getTitle();
    }

    @Override
    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }
    
    public String getSearchText() {
        return searchText;
    }

    public DashboardType getType() {
        return type;
    }

    public void setType(DashboardType type) {
        this.type = type;
    }

    public AssetLandingInfoEntity getAssetLandingInfoEntity() {
        return assetLandingInfoEntity;
    }

    public void setAssetLandingInfoEntity(AssetLandingInfoEntity assetLandingInfoEntity) {
        this.assetLandingInfoEntity = assetLandingInfoEntity;
    }

    @Override
    public Dashboard toData() {
        Dashboard dashboard = new Dashboard(new DashboardId(id));
        dashboard.setCreatedTime(UUIDs.unixTimestamp(id));
        if (tenantId != null) {
            dashboard.setTenantId(new TenantId(tenantId));
        }
        dashboard.setTitle(title);
        if (!StringUtils.isEmpty(assignedCustomers)) {
            try {
                dashboard.setAssignedCustomers(objectMapper.readValue(assignedCustomers, assignedCustomersType));
            } catch (IOException e) {
                log.warn("Unable to parse assigned customers!", e);
            }
        }
        if(type != null){
            dashboard.setType(type);
        }
        if(assetLandingInfoEntity != null) {
            dashboard.setAssetLandingInfo(assetLandingInfoEntity.toData());
        }
        dashboard.setConfiguration(configuration);
        return dashboard;
    }

}