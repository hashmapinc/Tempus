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
import com.hashmapinc.server.common.data.kv.*;
import lombok.Data;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import com.hashmapinc.server.common.data.EntityType;
import com.hashmapinc.server.common.data.kv.*;
import com.hashmapinc.server.dao.model.ToData;
import com.hashmapinc.server.dao.util.mapping.JsonStringType;

import javax.persistence.*;

import static com.hashmapinc.server.dao.model.ModelConstants.*;

@Data
@Entity
@TypeDef(name = "json", typeClass = JsonStringType.class)
@Table(name = "ts_kv")
@IdClass(TsKvCompositeKey.class)
public final class TsKvEntity implements ToData<TsKvEntry> {

    public TsKvEntity() {
    }

    public TsKvEntity(Double avgLongValue, Double avgDoubleValue) {
        if(avgLongValue != null) {
            this.longValue = avgLongValue.longValue();
        }
        this.doubleValue = avgDoubleValue;
    }

    public TsKvEntity(Long sumLongValue, Double sumDoubleValue) {
        this.longValue = sumLongValue;
        this.doubleValue = sumDoubleValue;
    }

    public TsKvEntity(String strValue, Long longValue, Double doubleValue) {
        this.strValue = strValue;
        this.longValue = longValue;
        this.doubleValue = doubleValue;
    }

    public TsKvEntity(Long booleanValueCount, Long strValueCount, Long longValueCount, Long doubleValueCount, Long jsonValueCount) {
        if (booleanValueCount != 0) {
            this.longValue = booleanValueCount;
        } else if (strValueCount != 0) {
            this.longValue = strValueCount;
        } else if (longValueCount != 0) {
            this.longValue = longValueCount;
        } else if (doubleValueCount != 0) {
            this.longValue = doubleValueCount;
        } else if (jsonValueCount != 0) {
            this.longValue = jsonValueCount;
        }
    }

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = ENTITY_TYPE_COLUMN)
    private EntityType entityType;

    @Id
    @Column(name = ENTITY_ID_COLUMN)
    private String entityId;

    @Id
    @Column(name = KEY_COLUMN)
    private String key;

    @Id
    @Column(name = TS_COLUMN)
    private long ts;

    @Column(name = BOOLEAN_VALUE_COLUMN)
    private Boolean booleanValue;

    @Column(name = STRING_VALUE_COLUMN)
    private String strValue;

    @Column(name = LONG_VALUE_COLUMN)
    private Long longValue;

    @Column(name = DOUBLE_VALUE_COLUMN)
    private Double doubleValue;

    @Type(type = "json")
    @Column(name = JSON_VALUE_COLUMN)
    private JsonNode jsonValue;

    @Override
    public TsKvEntry toData() {
        KvEntry kvEntry = null;
        if (strValue != null) {
            kvEntry = new StringDataEntry(key, strValue);
        } else if (longValue != null) {
            kvEntry = new LongDataEntry(key, longValue);
        } else if (doubleValue != null) {
            kvEntry = new DoubleDataEntry(key, doubleValue);
        } else if (booleanValue != null) {
            kvEntry = new BooleanDataEntry(key, booleanValue);
        } else if (jsonValue != null) {
            kvEntry = new JsonDataEntry(key, jsonValue);
        }
        return new BasicTsKvEntry(ts, kvEntry);
    }

    public boolean isNotEmpty() {
        return strValue != null || longValue != null || doubleValue != null || booleanValue != null | jsonValue != null;
    }
}
