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
package com.hashmapinc.server.controller;

import com.hashmapinc.server.common.data.*;
import com.hashmapinc.server.common.data.audit.ActionType;
import com.hashmapinc.server.common.data.id.SchemaId;
import com.hashmapinc.server.common.data.schema.Schema;
import com.hashmapinc.server.exception.TempusException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Slf4j
public class SchemaController extends BaseController {

    public static final String SCHEMA_ID = "schemaId";

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/schema/{schemaId}", method = RequestMethod.GET)
    @ResponseBody
    public Schema getSchemaById(@PathVariable(SCHEMA_ID) String strSchemaId) throws TempusException {
        checkParameter(SCHEMA_ID,strSchemaId);
        try {
            SchemaId schemaId = new SchemaId((toUUID(strSchemaId)));
            return checkSchemaId(schemaId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }


    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/schema", method = RequestMethod.POST)
    @ResponseBody
    public Schema saveSchema(@RequestBody Schema schema) throws TempusException {
        try {
            Schema savedSchema = checkNotNull(schemaService.saveSchema(schema));
            logEntityAction(savedSchema.getId(), savedSchema, null,
                schema.getId() == null ? ActionType.ADDED : ActionType.UPDATED, null);
            return savedSchema;
        } catch (Exception e) {
            logEntityAction(emptyId(EntityType.SCHEMA), schema,
                    null, schema.getId() == null ? ActionType.ADDED : ActionType.UPDATED, e);
            throw handleException(e);
        }
    }


    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/schema/{schemaId}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteSchema(@PathVariable(SCHEMA_ID) String strSchemaId) throws TempusException {
        checkParameter(SCHEMA_ID, strSchemaId);
        try {
            SchemaId schemaId = new SchemaId(toUUID(strSchemaId));
            Schema schema = checkSchemaId(schemaId);
            schemaService.deleteSchema(schemaId);

            logEntityAction(schemaId, schema, null,
                    ActionType.DELETED, null, strSchemaId);

        } catch (Exception e) {
            logEntityAction(emptyId(EntityType.SCHEMA),
                    null,
                    null,
                    ActionType.DELETED, e, strSchemaId);
            throw handleException(e);
        }
    }

}
