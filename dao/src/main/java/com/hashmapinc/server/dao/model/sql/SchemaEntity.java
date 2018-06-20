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
package com.hashmapinc.server.dao.model.sql;


import com.hashmapinc.server.common.data.Logo;
import com.hashmapinc.server.common.data.id.SchemaId;
import com.hashmapinc.server.common.data.schema.Schema;
import com.hashmapinc.server.common.data.schema.SchemaType;
import com.hashmapinc.server.dao.model.BaseEntity;
import com.hashmapinc.server.dao.model.BaseSqlEntity;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.util.mapping.JsonStringType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@TypeDef(name = "json", typeClass = JsonStringType.class)
@Table(name = ModelConstants.SCHEMA_REGISTRY_TABLE_NAME)

public class SchemaEntity extends BaseSqlEntity<Schema> implements BaseEntity<Schema> {

    @Column(name = ModelConstants.SCHEMA_REGISTRY_NAME)
    private String name;

    @Column(name = ModelConstants.SCHEMA_REGISTRY_DESCRIPTION)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = ModelConstants.SCHEMA_REGISTRY_TYPE)
    private SchemaType type;

    @Column(name = ModelConstants.SCHEMA_REGISTRY_SCHEMA_TXT)
    private String schema_txt;

    public SchemaEntity() {
        super();
    }

    public SchemaEntity(Schema schema) {
        if (schema.getId() != null) {
            this.setId(schema.getId().getId());
        }

        this.name = schema.getName();
        this.description = schema.getDescription();
        this.type = schema.getType();
        this.schema_txt = schema.getSchema_txt();
    }
    
    @Override
    public Schema toData() {
        Schema schema = new Schema(new SchemaId(getId()));
        schema.setName(name);
        schema.setDescription(description);
        schema.setType(type);
        schema.setSchema_txt(schema_txt);
        return schema;
    }
}
