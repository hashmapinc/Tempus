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
package com.hashmapinc.server.dao.sql.gatewayconfiguration;

import com.hashmapinc.server.common.data.TempusGatewayConfiguration;
import com.hashmapinc.server.common.data.UUIDConverter;
import com.hashmapinc.server.dao.DaoUtil;
import com.hashmapinc.server.dao.gatewayconfiguration.TempusGatewayConfigurationDao;
import com.hashmapinc.server.dao.model.sql.TempusGatewayConfigurationEntity;
import com.hashmapinc.server.dao.sql.JpaAbstractDao;
import com.hashmapinc.server.dao.util.SqlDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@SqlDao
public class JpaTempusGatewayConfigurationDao extends JpaAbstractDao<TempusGatewayConfigurationEntity, TempusGatewayConfiguration> implements TempusGatewayConfigurationDao {
    @Autowired
    private TempusGatewayConfigurationRepository tempusGatewayConfigurationRepository;

    @Override
    protected Class<TempusGatewayConfigurationEntity> getEntityClass() {
        return TempusGatewayConfigurationEntity.class;
    }
    @Override
    protected CrudRepository<TempusGatewayConfigurationEntity, String> getCrudRepository() {
        return tempusGatewayConfigurationRepository;
    }

    @Override
    public Optional<TempusGatewayConfiguration> findTempusGatewayConfigurationByTenantId(UUID tenantId) {
        return Optional.ofNullable(
                DaoUtil.getData(tempusGatewayConfigurationRepository.findByTenantId(UUIDConverter.fromTimeUUID(tenantId)))
        );
    }
}
