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
package com.hashmapinc.server.dao.gatewayconfiguration;

import com.hashmapinc.server.common.data.TempusGatewayConfiguration;
import com.hashmapinc.server.dao.Dao;

import java.util.Optional;
import java.util.UUID;

public interface TempusGatewayConfigurationDao extends Dao<TempusGatewayConfiguration> {
    /**
     * Save or update tempusGatewayConfiguration object
     *
     * @param tempusGatewayConfiguration the tempusGatewayConfiguration object
     * @return saved customerGroup object
     */
    TempusGatewayConfiguration save(TempusGatewayConfiguration tempusGatewayConfiguration);

    /**
     * Find tempusGatewayConfiguration by tenant id.
     *
     * @param tenantId the tenant id
     * @return optional tempusGatewayConfiguration object
     */
    Optional<TempusGatewayConfiguration> findTempusGatewayConfigurationByTenantId(UUID tenantId);

    /**
     * Find tempusGatewayConfiguration by tenantId and customer title.
     *
     * @param tenantId the tenantId
     * @param title the tempusGatewayConfiguration title
     * @return the optional tempusGatewayConfiguration object
     */
    Optional<TempusGatewayConfiguration> findTempusGatewayConfigurationByTenantIdAndTitle(UUID tenantId, String title);
}
