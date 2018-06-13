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
package com.hashmapinc.server.dao.datamodel;

import com.datastax.driver.core.querybuilder.Select;
import com.hashmapinc.server.common.data.datamodel.DataModel;
import com.hashmapinc.server.dao.DaoUtil;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.model.nosql.DataModelEntity;
import com.hashmapinc.server.dao.nosql.CassandraAbstractSearchTextDao;
import com.hashmapinc.server.dao.util.NoSqlDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.UUID;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;

@Component
@Slf4j
@NoSqlDao
public class CassandraDataModelDao extends CassandraAbstractSearchTextDao<DataModelEntity, DataModel> implements DataModelDao {

    @Override
    public Optional<DataModel> findDataModelByTenantIdAndName(UUID tenantId, String name) {
        Select select = select().from(ModelConstants.DATA_MODEL_BY_TENANT_AND_NAME_VIEW_NAME);
        Select.Where query = select.where();
        query.and(eq(ModelConstants.DATA_MODEL_TENANT_ID_PROPERTY, tenantId));
        query.and(eq(ModelConstants.DATA_MODEL_NAME_PROPERTY, name));
        return Optional.ofNullable(DaoUtil.getData(findOneByStatement(query)));
    }

    @Override
    protected Class<DataModelEntity> getColumnFamilyClass() {
        return DataModelEntity.class;
    }

    @Override
    protected String getColumnFamilyName() {
        return ModelConstants.DATA_MODEL_TABLE_NAME;
    }
}
