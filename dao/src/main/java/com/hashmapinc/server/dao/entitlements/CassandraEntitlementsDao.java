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
package com.hashmapinc.server.dao.entitlements;

import com.datastax.driver.core.querybuilder.Select;
import com.hashmapinc.server.common.data.Entitlements;
import com.hashmapinc.server.dao.DaoUtil;
import com.hashmapinc.server.dao.model.nosql.EntitlementsEntity;
import com.hashmapinc.server.dao.nosql.CassandraAbstractModelDao;
import com.hashmapinc.server.dao.util.NoSqlDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
import static com.hashmapinc.server.dao.model.ModelConstants.*;


@Component
@Slf4j
@NoSqlDao
public class CassandraEntitlementsDao extends CassandraAbstractModelDao<EntitlementsEntity, Entitlements> implements EntitlementsDao {
    @Override
    protected Class<EntitlementsEntity> getColumnFamilyClass() {
        return EntitlementsEntity.class;
    }

    @Override
    protected String getColumnFamilyName() {
        return ENTITLEMENTS_COLUMN_FAMILY_NAME;
    }

    @Override
    public Optional<Entitlements> findEntitlementsByUserId(UUID userId) {
        log.debug("Try to find entitlements by userId", userId);
        Select select = select().from(ENTITLEMENTS_COLUMN_FAMILY_NAME);
        Select.Where query = select.where();
        query.and(eq(USER_ID_PROPERTY, userId));

        return Optional.ofNullable(DaoUtil.getData(findOneByStatement(query)));
    }


}
