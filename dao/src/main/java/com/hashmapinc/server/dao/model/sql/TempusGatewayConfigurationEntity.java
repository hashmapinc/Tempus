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
import com.hashmapinc.server.common.data.TempusGatewayConfiguration;
import com.hashmapinc.server.common.data.UUIDConverter;
import com.hashmapinc.server.common.data.id.TempusGatewayConfigurationId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.dao.model.BaseEntity;
import com.hashmapinc.server.dao.model.BaseSqlEntity;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.util.mapping.JsonStringType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
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
public class TempusGatewayConfigurationEntity extends BaseSqlEntity<TempusGatewayConfiguration> implements BaseEntity<TempusGatewayConfiguration> {
    @Column(name = ModelConstants.TEMPUS_GATEWAY_CONFIGURATION_TENANT_ID)
    private String tenantId;

    @Column(name = ModelConstants.TEMPUS_GATEWAY_CONFIGURATION_REPLICAS)
    private int replicas;

    @Column(name = ModelConstants.TEMPUS_GATEWAY_CONFIGURATION_GATEWAY_TOKEN)
    private String gatewayToken;

    public TempusGatewayConfigurationEntity(TempusGatewayConfiguration tempusGatewayConfiguration) {
        if (tempusGatewayConfiguration.getId() != null) {
            this.setId(tempusGatewayConfiguration.getId().getId());
        }
        this.tenantId = UUIDConverter.fromTimeUUID(tempusGatewayConfiguration.getTenantId().getId());
        this.replicas = tempusGatewayConfiguration.getReplicas();
        this.gatewayToken = tempusGatewayConfiguration.getGatewayToken();
    }


    @Override
    public TempusGatewayConfiguration toData() {
        TempusGatewayConfiguration tempusGatewayConfiguration = new TempusGatewayConfiguration(new TempusGatewayConfigurationId(getId()));
        tempusGatewayConfiguration.setCreatedTime(UUIDs.unixTimestamp(getId()));
        tempusGatewayConfiguration.setTenantId(new TenantId(UUIDConverter.fromString(tenantId)));
        tempusGatewayConfiguration.setReplicas(replicas);
        tempusGatewayConfiguration.setGatewayToken(gatewayToken);
        return tempusGatewayConfiguration;
    }
}
