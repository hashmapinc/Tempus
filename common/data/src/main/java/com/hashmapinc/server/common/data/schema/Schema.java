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
package com.hashmapinc.server.common.data.schema;

import com.hashmapinc.server.common.data.BaseData;
import com.hashmapinc.server.common.data.HasName;
import com.hashmapinc.server.common.data.id.SchemaId;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class Schema extends BaseData<SchemaId> implements HasName {

    private String name;
    private String description;
    private SchemaType type;
    private String schema_txt;


    public Schema() {
        super();
    }

    public Schema(SchemaId id) {
        super(id);
    }

    public Schema(Schema schema) {
        super(schema);
        this.name = schema.name;
        this.description = schema.description;
        this.type = schema.type;
        this.schema_txt = schema.schema_txt;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public SchemaType getType() {
        return type;
    }

    public void setType(SchemaType type) {
        this.type = type;
    }

    public String getSchema_txt() {
        return schema_txt;
    }

    public void setSchema_txt(String schema_txt) {
        this.schema_txt = schema_txt;
    }

    @Override
    public String toString() {
        return "Schema{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", type=" + type +
                ", schema_txt='" + schema_txt + '\'' +
                '}';
    }
}
