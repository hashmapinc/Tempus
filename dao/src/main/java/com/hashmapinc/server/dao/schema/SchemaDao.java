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

import com.hashmapinc.server.common.data.id.SchemaId;
import com.hashmapinc.server.common.data.schema.Schema;
import com.hashmapinc.server.dao.Dao;

import java.util.Optional;
import java.util.UUID;

/**
 * The Interface SchemaDao.
 */
public interface SchemaDao extends Dao<Schema> {

    /**
     * Save or update schema object
     *
     * @param schema the schema object
     * @return saved schema object
     */
    Schema save(Schema schema);
    
    /**
     * Find schema by schema id.
     *
     * @param schemaID the schema id
     * @return the schema object
     */
    Schema findSchemaById(SchemaId schemaID);

    /**
     * Find schema by name.
     *
     * @param name the schema name
     * @return the schema object
     */
    Schema findSchemaByName(String name);

    boolean removeById(UUID id);

}
