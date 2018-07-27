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

import com.hashmapinc.server.common.data.schema.Schema;
import com.hashmapinc.server.common.data.id.SchemaId;

import java.util.Optional;

/**
 * The interface Schema service.
 */
public interface SchemaService {

    /**
     * Find schema by name schema.
     *
     * @param name the name
     * @return the schema
     */
    Schema findSchemaByName(String name);

    /**
     * Find schema by schema id schema.
     *
     * @param schemaId the schema id
     * @return the schema
     */
    Schema findSchemaById(SchemaId schemaId);

    /**
     * Save schema schema.
     *
     * @param schema the schema
     * @return the saved schema
     */
    Schema saveSchema(Schema schema);

    /**
     * Delete schema.
     *
     * @param schemaId the schema id
     */
    void deleteSchema(SchemaId schemaId);

}
