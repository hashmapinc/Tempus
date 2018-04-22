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
import com.datastax.driver.mapping.annotations.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.hashmapinc.server.common.data.id.PluginId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.plugin.ComponentLifecycleState;
import com.hashmapinc.server.dao.model.ModelConstants;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import com.hashmapinc.server.common.data.plugin.PluginMetaData;
import com.hashmapinc.server.dao.model.SearchTextEntity;
import com.hashmapinc.server.dao.model.type.ComponentLifecycleStateCodec;
import com.hashmapinc.server.dao.model.type.JsonCodec;

import java.util.UUID;

@Table(name = ModelConstants.PLUGIN_COLUMN_FAMILY_NAME)
@EqualsAndHashCode
@ToString
public class PluginMetaDataEntity implements SearchTextEntity<PluginMetaData> {

    @PartitionKey
    @Column(name = ModelConstants.ID_PROPERTY)
    private UUID id;

    @Column(name = ModelConstants.PLUGIN_API_TOKEN_PROPERTY)
    private String apiToken;

    @ClusteringColumn
    @Column(name = ModelConstants.PLUGIN_TENANT_ID_PROPERTY)
    private UUID tenantId;

    @Column(name = ModelConstants.PLUGIN_NAME_PROPERTY)
    private String name;

    @Column(name = ModelConstants.PLUGIN_CLASS_PROPERTY)
    private String clazz;

    @Column(name = ModelConstants.PLUGIN_ACCESS_PROPERTY)
    private boolean publicAccess;

    @Column(name = ModelConstants.PLUGIN_STATE_PROPERTY, codec = ComponentLifecycleStateCodec.class)
    private ComponentLifecycleState state;

    @Column(name = ModelConstants.PLUGIN_CONFIGURATION_PROPERTY, codec = JsonCodec.class)
    private JsonNode configuration;

    @Column(name = ModelConstants.SEARCH_TEXT_PROPERTY)
    private String searchText;

    @Column(name = ModelConstants.ADDITIONAL_INFO_PROPERTY, codec = JsonCodec.class)
    private JsonNode additionalInfo;

    public PluginMetaDataEntity() {
    }

    public PluginMetaDataEntity(PluginMetaData pluginMetaData) {
        if (pluginMetaData.getId() != null) {
            this.id = pluginMetaData.getId().getId();
        }
        this.tenantId = pluginMetaData.getTenantId().getId();
        this.apiToken = pluginMetaData.getApiToken();
        this.clazz = pluginMetaData.getClazz();
        this.name = pluginMetaData.getName();
        this.publicAccess = pluginMetaData.isPublicAccess();
        this.state = pluginMetaData.getState();
        this.configuration = pluginMetaData.getConfiguration();
        this.searchText = pluginMetaData.getName();
        this.additionalInfo = pluginMetaData.getAdditionalInfo();
    }

    @Override
    public String getSearchTextSource() {
        return getSearchText();
    }

    @Override
    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void setId(UUID id) {
        this.id = id;
    }

    public String getApiToken() {
        return apiToken;
    }

    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public JsonNode getConfiguration() {
        return configuration;
    }

    public void setConfiguration(JsonNode configuration) {
        this.configuration = configuration;
    }

    public boolean isPublicAccess() {
        return publicAccess;
    }

    public void setPublicAccess(boolean publicAccess) {
        this.publicAccess = publicAccess;
    }

    public ComponentLifecycleState getState() {
        return state;
    }

    public void setState(ComponentLifecycleState state) {
        this.state = state;
    }

    public String getSearchText() {
        return searchText;
    }

    public JsonNode getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(JsonNode additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    @Override
    public PluginMetaData toData() {
        PluginMetaData data = new PluginMetaData(new PluginId(id));
        data.setTenantId(new TenantId(tenantId));
        data.setCreatedTime(UUIDs.unixTimestamp(id));
        data.setName(name);
        data.setConfiguration(configuration);
        data.setClazz(clazz);
        data.setPublicAccess(publicAccess);
        data.setState(state);
        data.setApiToken(apiToken);
        data.setAdditionalInfo(additionalInfo);
        return data;
    }

}
