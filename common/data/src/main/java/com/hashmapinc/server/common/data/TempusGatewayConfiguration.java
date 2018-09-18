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
package com.hashmapinc.server.common.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hashmapinc.server.common.data.id.TempusGatewayConfigurationId;
import com.hashmapinc.server.common.data.id.TenantId;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString
public class TempusGatewayConfiguration extends SearchTextBasedWithAdditionalInfo<TempusGatewayConfigurationId> implements HasName{
    private static final long serialVersionUID = -7052031036883142312L;
    private String title;
    private TenantId tenantId;
    private int replicas;
    private String gatewayToken;

    public TempusGatewayConfiguration(TempusGatewayConfigurationId id) {
        super(id);
    }

    public TempusGatewayConfiguration(TempusGatewayConfiguration tempusGatewayConfiguration){
        super(tempusGatewayConfiguration);
        this.title = tempusGatewayConfiguration.title;
        this.tenantId = tempusGatewayConfiguration.tenantId;
        this.replicas = tempusGatewayConfiguration.replicas;
        this.gatewayToken = tempusGatewayConfiguration.gatewayToken;
    }

    @Override
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String getName() {
        return title;
    }

    @Override
    public String getSearchText() {
        return getTitle();
    }
}
