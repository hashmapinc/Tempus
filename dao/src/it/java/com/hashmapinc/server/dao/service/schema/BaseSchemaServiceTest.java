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
package com.hashmapinc.server.dao.service.schema;

import com.hashmapinc.server.common.data.id.SchemaId;
import com.hashmapinc.server.common.data.schema.Schema;
import com.hashmapinc.server.common.data.schema.SchemaType;
import com.hashmapinc.server.dao.service.AbstractServiceTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;


@Slf4j
public class BaseSchemaServiceTest extends AbstractServiceTest {

    private String name = "schemaName";
    private String description = "schemaDescription";
    private SchemaType type = SchemaType.VELOCITY;
    private String body = "Sensor: $name.  Temp: $value";
    private SchemaId id = null;

    @After
    public final void tearDown() {
        schemaService.deleteSchema(id);
    }

    @Test
    public void saveSchemaTest() throws Exception {
        Schema savedSchema = schemaService.saveSchema(generateSchema(name,description,type,body));
        id = savedSchema.getId();
        Assert.assertNotNull(id);
    }


}
