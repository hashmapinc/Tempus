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
package com.hashmapinc.server.dao.datamodel;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.querybuilder.Select;
import com.hashmapinc.server.common.data.datamodel.AttributeDefinition;
import com.hashmapinc.server.common.data.id.DataModelObjectId;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.nosql.CassandraAbstractDao;
import com.hashmapinc.server.dao.util.NoSqlDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;

@Slf4j
@Service
@NoSqlDao
public class CassandraBaseAttributeDefinitionDao extends CassandraAbstractDao implements AttributeDefinitionDao{

    private PreparedStatement saveStmt;

    @Override
    public AttributeDefinition save(AttributeDefinition attributeDefinition) {
        BoundStatement stmt = getSaveStmt().bind();
        stmt.setUUID(0, attributeDefinition.getDataModelObjectId().getId());
        stmt.setString(1, attributeDefinition.getName());
        stmt.setString(2, attributeDefinition.getValue());
        stmt.setString(3, attributeDefinition.getValueType());
        stmt.setString(4, attributeDefinition.getSource());

        executeWrite(stmt);

        return attributeDefinition;
    }

    private PreparedStatement getSaveStmt() {
        if (saveStmt == null) {
            saveStmt = getSession().prepare("INSERT INTO " + ModelConstants.ATTRIBUTE_DEFINITION_COLUMN_FAMILY_NAME +
                    "(" + ModelConstants.ATTRIBUTE_DEFINITION_MODEL_OBJECT_ID +
                    "," + ModelConstants.ATTRIBUTE_DEFINITION_NAME +
                    "," + ModelConstants.ATTRIBUTE_DEFINITION_VALUE +
                    "," + ModelConstants.ATTRIBUTE_DEFINITION_VALUE_TYPE +
                    "," + ModelConstants.ATTRIBUTE_DEFINITION_SOURCE +
                    ")" +
                    " VALUES(?, ?, ?, ?, ?)");
        }
        return saveStmt;
    }

    @Override
    public AttributeDefinition findByNameAndDataModelObjectId(String name, UUID id) {
        Select select = select().from(ModelConstants.ATTRIBUTE_DEFINITION_COLUMN_FAMILY_NAME).allowFiltering();
        Select.Where query = select.where();
        query.and(eq(ModelConstants.ATTRIBUTE_DEFINITION_NAME, name))
        .and(eq(ModelConstants.ATTRIBUTE_DEFINITION_MODEL_OBJECT_ID, id));
        ResultSet resultSet = executeRead(select);
        return convertResultToAttributeDefinitionList(resultSet).get(0);
    }

    @Override
    public List<AttributeDefinition> findByDataModelObjectId(DataModelObjectId dataModelObjectId) {
        Select select = select().from(ModelConstants.ATTRIBUTE_DEFINITION_COLUMN_FAMILY_NAME).allowFiltering();
        Select.Where query = select.where();
        query.and(eq(ModelConstants.ATTRIBUTE_DEFINITION_MODEL_OBJECT_ID, dataModelObjectId.getId()));
        ResultSet resultSet = executeRead(select);
        return convertResultToAttributeDefinitionList(resultSet);
    }

    private List<AttributeDefinition> convertResultToAttributeDefinitionList(ResultSet resultSet) {
        List<Row> rows = resultSet.all();
        List<AttributeDefinition> entries = new ArrayList<>(rows.size());
        if (!rows.isEmpty()) {
            rows.forEach(row -> {
                String name = row.getString(ModelConstants.ATTRIBUTE_DEFINITION_NAME);
                UUID dataModelObjectId = row.getUUID(ModelConstants.ATTRIBUTE_DEFINITION_MODEL_OBJECT_ID);
                String value = row.getString(ModelConstants.ATTRIBUTE_DEFINITION_VALUE);
                String valueType = row.getString(ModelConstants.ATTRIBUTE_DEFINITION_VALUE_TYPE);
                String source = row.getString(ModelConstants.ATTRIBUTE_DEFINITION_SOURCE);

                AttributeDefinition attributeDefinition = new AttributeDefinition();
                attributeDefinition.setName(name);
                attributeDefinition.setDataModelObjectId(new DataModelObjectId(dataModelObjectId));
                attributeDefinition.setSource(source);
                attributeDefinition.setValue(value);
                attributeDefinition.setValueType(valueType);

                entries.add(attributeDefinition);
            });
        }
        return entries;
    }
}
