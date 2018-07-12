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
package com.hashmapinc.server.dao.sql.schema;

import com.hashmapinc.server.common.data.id.SchemaId;
import com.hashmapinc.server.common.data.schema.Schema;
import com.hashmapinc.server.dao.DaoUtil;
import com.hashmapinc.server.dao.model.sql.SchemaEntity;
import com.hashmapinc.server.dao.schema.SchemaDao;
import com.hashmapinc.server.dao.sql.JpaAbstractDao;
import com.hashmapinc.server.dao.util.SqlDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@SqlDao
@Slf4j
public class JpaSchemaDao extends JpaAbstractDao<SchemaEntity, Schema> implements SchemaDao {

    @Autowired
    private SchemaRepository schemaRepository;

    @Override
    protected Class<SchemaEntity> getEntityClass() {
        return SchemaEntity.class;
    }

    @Override
    protected CrudRepository<SchemaEntity, String> getCrudRepository() {
        return schemaRepository;
    }

    @Override
    public Schema findSchemaById(SchemaId schemaId) {
        Schema schema = DaoUtil.getData(schemaRepository.findSchemaById(schemaId));
        if (schema != null) {
            return schema;
        } else {
            return null;
        }
    }

    @Override
    public Schema findSchemaByName(String name) {
        Schema schema = DaoUtil.getData(schemaRepository.findSchemaByName(name));
        if (schema != null) {
            return schema;
        } else {
            return null;
        }
    }
}
