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

import com.datastax.driver.core.querybuilder.Select;
import com.hashmapinc.server.common.data.TempusGatewayConfiguration;
import com.hashmapinc.server.dao.DaoUtil;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.model.nosql.TempusGatewayConfigurationEntity;
import com.hashmapinc.server.dao.nosql.CassandraAbstractModelDao;
import com.hashmapinc.server.dao.nosql.CassandraAbstractSearchTextDao;
import com.hashmapinc.server.dao.util.NoSqlDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;

//@Component
@Slf4j
//@NoSqlDao
public class CassandraTempusGatewayConfigurationDao extends CassandraAbstractModelDao<TempusGatewayConfigurationEntity, TempusGatewayConfiguration>
        implements TempusGatewayConfigurationDao {

    @Override
    protected Class<TempusGatewayConfigurationEntity> getColumnFamilyClass() {
        return TempusGatewayConfigurationEntity.class;
    }

    @Override
    protected String getColumnFamilyName() {
        return ModelConstants.TEMPUS_GATEWAY_CONFIGURATION_FAMILY_NAME;
    }


    @Override
    public Optional<TempusGatewayConfiguration> findTempusGatewayConfigurationByTenantId(UUID tenantId) {
        log.debug("Try to find TempusGatewayConfiguration by tenantId [{}]", tenantId);
        Select select = select().from(ModelConstants.TEMPUS_GATEWAY_CONFIGURATION_BY_TENANT);
        Select.Where query = select.where();
        query.and(eq(ModelConstants.TEMPUS_GATEWAY_CONFIGURATION_TENANT_ID_PROPERTY, tenantId));
        TempusGatewayConfigurationEntity tempusGatewayConfigurationEntity = findOneByStatement(query);
        TempusGatewayConfiguration tempusGatewayConfiguration = DaoUtil.getData(tempusGatewayConfigurationEntity);
        return Optional.ofNullable(tempusGatewayConfiguration);
    }
}
