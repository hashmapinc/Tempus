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


package com.hashmapinc.server.dao.schema;

import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.querybuilder.Select.Where;
import com.hashmapinc.server.common.data.id.SchemaId;
import com.hashmapinc.server.common.data.schema.Schema;
import com.hashmapinc.server.dao.DaoUtil;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.model.sql.SchemaEntity;
import com.hashmapinc.server.dao.nosql.CassandraAbstractModelDao;
import com.hashmapinc.server.dao.util.NoSqlDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;

@Component
@Slf4j
@NoSqlDao


public class CassandraSchemaDao extends CassandraAbstractModelDao<SchemaEntity,Schema> implements SchemaDao{

    @Override
    protected Class<SchemaEntity> getColumnFamilyClass() {
        return SchemaEntity.class;
    }

    @Override
    protected String getColumnFamilyName() {
        return ModelConstants.SCHEMA_REGISTRY_COLUMN_FAMILY_NAME;
    }

    @Override
    public Schema findSchemaById(SchemaId id) {
        Select select = select().from(ModelConstants.SCHEMA_REGISTRY_COLUMN_FAMILY_NAME).allowFiltering();
        Where query = select.where();
        query.and(eq(ModelConstants.ID_PROPERTY,id));
        return DaoUtil.getData(findOneByStatement(query));
    }

    @Override
    public Schema findSchemaByName(String name) {
        Select select = select().from(ModelConstants.SCHEMA_REGISTRY_COLUMN_FAMILY_NAME).allowFiltering();
        Where query = select.where();
        query.and(eq(ModelConstants.SCHEMA_REGISTRY_NAME_PROPERTY,name));
        return DaoUtil.getData(findOneByStatement(query));
    }
}
