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
import com.fasterxml.jackson.databind.JsonNode;
import com.hashmapinc.server.common.data.id.PluginId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.plugin.ComponentLifecycleState;
import com.hashmapinc.server.common.data.plugin.PluginMetaData;
import com.hashmapinc.server.dao.model.BaseSqlEntity;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.model.SearchTextEntity;
import com.hashmapinc.server.dao.util.mapping.JsonStringType;

import javax.persistence.*;

import static com.hashmapinc.server.common.data.UUIDConverter.fromString;
import static com.hashmapinc.server.common.data.UUIDConverter.fromTimeUUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@TypeDef(name = "json", typeClass = JsonStringType.class)
@Table(name = ModelConstants.PLUGIN_COLUMN_FAMILY_NAME)
public class PluginMetaDataEntity extends BaseSqlEntity<PluginMetaData> implements SearchTextEntity<PluginMetaData> {

    @Column(name = ModelConstants.PLUGIN_API_TOKEN_PROPERTY)
    private String apiToken;

    @Column(name = ModelConstants.PLUGIN_TENANT_ID_PROPERTY)
    private String tenantId;

    @Column(name = ModelConstants.PLUGIN_NAME_PROPERTY)
    private String name;

    @Column(name = ModelConstants.PLUGIN_CLASS_PROPERTY)
    private String clazz;

    @Column(name = ModelConstants.PLUGIN_ACCESS_PROPERTY)
    private boolean publicAccess;

    @Enumerated(EnumType.STRING)
    @Column(name = ModelConstants.PLUGIN_STATE_PROPERTY)
    private ComponentLifecycleState state;

    @Type(type = "json")
    @Column(name = ModelConstants.PLUGIN_CONFIGURATION_PROPERTY)
    private JsonNode configuration;

    @Type(type = "json")
    @Column(name = ModelConstants.ADDITIONAL_INFO_PROPERTY)
    private JsonNode additionalInfo;

    @Column(name = ModelConstants.SEARCH_TEXT_PROPERTY)
    private String searchText;

    public PluginMetaDataEntity() {
    }

    public PluginMetaDataEntity(PluginMetaData pluginMetaData) {
        if (pluginMetaData.getId() != null) {
            this.setId(pluginMetaData.getId().getId());
        }
        this.tenantId = fromTimeUUID(pluginMetaData.getTenantId().getId());
        this.apiToken = pluginMetaData.getApiToken();
        this.clazz = pluginMetaData.getClazz();
        this.name = pluginMetaData.getName();
        this.publicAccess = pluginMetaData.isPublicAccess();
        this.state = pluginMetaData.getState();
        this.searchText = pluginMetaData.getName();
        this.configuration = pluginMetaData.getConfiguration();
        this.additionalInfo = pluginMetaData.getAdditionalInfo();
    }

    @Override
    public String getSearchTextSource() {
        return searchText;
    }

    @Override
    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    @Override
    public PluginMetaData toData() {
        PluginMetaData data = new PluginMetaData(new PluginId(getId()));
        data.setTenantId(new TenantId(fromString(tenantId)));
        data.setCreatedTime(UUIDs.unixTimestamp(getId()));
        data.setName(name);
        data.setClazz(clazz);
        data.setPublicAccess(publicAccess);
        data.setState(state);
        data.setApiToken(apiToken);
        data.setConfiguration(configuration);
        data.setAdditionalInfo(additionalInfo);
        return data;
    }

}
