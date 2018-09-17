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
package com.hashmapinc.server.dao.model.sql;

import com.datastax.driver.core.utils.UUIDs;
import com.fasterxml.jackson.databind.JsonNode;
import com.hashmapinc.server.common.data.TempusGatewayConfiguration;
import com.hashmapinc.server.common.data.UUIDConverter;
import com.hashmapinc.server.common.data.id.TempusGatewayConfigurationId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.dao.model.BaseSqlEntity;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.model.SearchTextEntity;
import com.hashmapinc.server.dao.util.mapping.JsonStringType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@TypeDef(name = "json", typeClass = JsonStringType.class)
@Table(name = ModelConstants.TEMPUS_GATEWAY_CONFIGURATION_TABLE_NAME)
public class TempusGatewayConfigurationEntity extends BaseSqlEntity<TempusGatewayConfiguration> implements SearchTextEntity<TempusGatewayConfiguration> {
    @Column(name = ModelConstants.TEMPUS_GATEWAY_CONFIGURATION_TITLE)
    private String title;

    @Column(name = ModelConstants.TEMPUS_GATEWAY_CONFIGURATION_TENANT_ID)
    private String tenantId;

    @Column(name = ModelConstants.TEMPUS_GATEWAY_CONFIGURATION_HOST)
    private String host;

    @Column(name = ModelConstants.TEMPUS_GATEWAY_CONFIGURATION_PORT)
    private int port;

    @Column(name = ModelConstants.TEMPUS_GATEWAY_CONFIGURATION_REPLICAS)
    private int replicas;

    @Column(name = ModelConstants.TEMPUS_GATEWAY_CONFIGURATION_GATEWAY_TOKEN)
    private String gatewayToken;

    @Type(type = "json")
    @Column(name = ModelConstants.CUSTOMER_ADDITIONAL_INFO_PROPERTY)
    private JsonNode additionalInfo;

    @Column(name = ModelConstants.SEARCH_TEXT_PROPERTY)
    private String searchText;

    public TempusGatewayConfigurationEntity(TempusGatewayConfiguration tempusGatewayConfiguration) {
        if (tempusGatewayConfiguration.getId() != null) {
            this.setId(tempusGatewayConfiguration.getId().getId());
        }
        this.tenantId = UUIDConverter.fromTimeUUID(tempusGatewayConfiguration.getTenantId().getId());
        this.title = tempusGatewayConfiguration.getTitle();
        this.host = tempusGatewayConfiguration.getHost();
        this.port = tempusGatewayConfiguration.getPort();
        this.replicas = tempusGatewayConfiguration.getReplicas();
        this.gatewayToken = tempusGatewayConfiguration.getGatewayToken();
        this.additionalInfo = tempusGatewayConfiguration.getAdditionalInfo();
    }

    @Override
    public String getSearchTextSource() {
        return title;
    }

    @Override
    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    @Override
    public TempusGatewayConfiguration toData() {
        TempusGatewayConfiguration tempusGatewayConfiguration = new TempusGatewayConfiguration(new TempusGatewayConfigurationId(getId()));
        tempusGatewayConfiguration.setCreatedTime(UUIDs.unixTimestamp(getId()));
        tempusGatewayConfiguration.setTenantId(new TenantId(UUIDConverter.fromString(tenantId)));
        tempusGatewayConfiguration.setTitle(title);
        tempusGatewayConfiguration.setHost(host);
        tempusGatewayConfiguration.setPort(port);
        tempusGatewayConfiguration.setReplicas(replicas);
        tempusGatewayConfiguration.setGatewayToken(gatewayToken);
        tempusGatewayConfiguration.setAdditionalInfo(additionalInfo);
        return tempusGatewayConfiguration;
    }
}
