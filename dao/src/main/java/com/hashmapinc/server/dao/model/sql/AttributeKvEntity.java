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

import com.fasterxml.jackson.databind.JsonNode;
import com.hashmapinc.server.common.data.EntityType;
import com.hashmapinc.server.common.data.kv.*;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.model.ToData;
import com.hashmapinc.server.dao.util.mapping.JsonStringType;
import lombok.Data;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import com.hashmapinc.server.common.data.kv.*;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@TypeDef(name = "json", typeClass = JsonStringType.class)
@Table(name = "attribute_kv")
@IdClass(AttributeKvCompositeKey.class)
public class AttributeKvEntity implements ToData<AttributeKvEntry>, Serializable {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = ModelConstants.ENTITY_TYPE_COLUMN)
    private EntityType entityType;

    @Id
    @Column(name = ModelConstants.ENTITY_ID_COLUMN)
    private String entityId;

    @Id
    @Column(name = ModelConstants.ATTRIBUTE_TYPE_COLUMN)
    private String attributeType;

    @Id
    @Column(name = ModelConstants.ATTRIBUTE_KEY_COLUMN)
    private String attributeKey;

    @Column(name = ModelConstants.BOOLEAN_VALUE_COLUMN)
    private Boolean booleanValue;

    @Column(name = ModelConstants.STRING_VALUE_COLUMN)
    private String strValue;

    @Column(name = ModelConstants.LONG_VALUE_COLUMN)
    private Long longValue;

    @Column(name = ModelConstants.DOUBLE_VALUE_COLUMN)
    private Double doubleValue;

    @Type(type = "json")
    @Column(name = ModelConstants.JSON_VALUE_COLUMN)
    private JsonNode jsonValue;

    @Column(name = ModelConstants.LAST_UPDATE_TS_COLUMN)
    private Long lastUpdateTs;

    @Override
    public AttributeKvEntry toData() {
        KvEntry kvEntry = null;
        if (strValue != null) {
            kvEntry = new StringDataEntry(attributeKey, strValue);
        } else if (booleanValue != null) {
            kvEntry = new BooleanDataEntry(attributeKey, booleanValue);
        } else if (doubleValue != null) {
            kvEntry = new DoubleDataEntry(attributeKey, doubleValue);
        } else if (longValue != null) {
            kvEntry = new LongDataEntry(attributeKey, longValue);
        } else if (jsonValue != null) {
            kvEntry = new JsonDataEntry(attributeKey, jsonValue);
        }
        return new BaseAttributeKvEntry(kvEntry, lastUpdateTs);
    }
}
