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
import com.hashmapinc.server.common.data.DataModelObject.AttributeDefinition;
import com.hashmapinc.server.common.data.id.DataModelObjectId;
import com.hashmapinc.server.dao.DaoUtil;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.model.nosql.AttributeDefinitionEntity;
import com.hashmapinc.server.dao.nosql.CassandraAbstractModelDao;
import com.hashmapinc.server.dao.util.NoSqlDao;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;

@Service
@NoSqlDao
public class CassandraBaseAttributeDefinitionDao extends CassandraAbstractModelDao<AttributeDefinitionEntity, AttributeDefinition> implements AttributeDefinitionDao{

    @Override
    protected Class getColumnFamilyClass() {
        return AttributeDefinitionEntity.class;
    }

    @Override
    protected String getColumnFamilyName() {
        return ModelConstants.ATTRIBUTE_DEFINITION_COLUMN_FAMILY_NAME;
    }


    @Override
    public List<AttributeDefinition> findByDataModelObjectId(DataModelObjectId dataModelObjectId) {
        Select select = select().from(ModelConstants.ATTRIBUTE_DEFINITION_COLUMN_FAMILY_NAME).allowFiltering();
        Select.Where query = select.where();
        query.and(eq(ModelConstants.ATTRIBUTE_DEFINITION_MODEL_OBJECT_ID, dataModelObjectId.getId()));
        List<AttributeDefinitionEntity> entities = findListByStatement(query);
        return DaoUtil.convertDataList(entities);
    }

    @Override
    public boolean deleteById(UUID id) {
        return super.removeById(id);
    }
}
