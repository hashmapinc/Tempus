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
package com.hashmapinc.server.common.data.kv;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Optional;

/**
 * @author Andrew Shvayka
 */
public class BaseAttributeKvEntry implements AttributeKvEntry {

    @JsonProperty
    private final long lastUpdateTs;
    @JsonProperty
    private final KvEntry kv;

    @JsonCreator
    public BaseAttributeKvEntry(@JsonProperty("kv") KvEntry kv,
                                @JsonProperty("lastUpdateTs") long lastUpdateTs) {
        this.kv = kv;
        this.lastUpdateTs = lastUpdateTs;
    }

    @Override
    public long getLastUpdateTs() {
        return lastUpdateTs;
    }

    @JsonIgnore
    @Override
    public String getKey() {
        return kv.getKey();
    }

    @JsonIgnore
    @Override
    public DataType getDataType() {
        return kv.getDataType();
    }

    @JsonIgnore
    @Override
    public Optional<String> getStrValue() {
        return kv.getStrValue();
    }

    @JsonIgnore
    @Override
    public Optional<Long> getLongValue() {
        return kv.getLongValue();
    }

    @JsonIgnore
    @Override
    public Optional<Boolean> getBooleanValue() {
        return kv.getBooleanValue();
    }

    @JsonIgnore
    @Override
    public Optional<Double> getDoubleValue() {
        return kv.getDoubleValue();
    }

    @JsonIgnore
    @Override
    public  Optional<JsonNode> getJsonValue() {
        return kv.getJsonValue();
    }

    @JsonIgnore
    @Override
    public String getValueAsString() {
        return kv.getValueAsString();
    }

    @JsonIgnore
    @Override
    public Object getValue() {
        return kv.getValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BaseAttributeKvEntry that = (BaseAttributeKvEntry) o;

        if (lastUpdateTs != that.lastUpdateTs) return false;
        return kv.equals(that.kv);

    }

    @Override
    public int hashCode() {
        int result = (int) (lastUpdateTs ^ (lastUpdateTs >>> 32));
        result = 31 * result + kv.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "BaseAttributeKvEntry{" +
                "lastUpdateTs=" + lastUpdateTs +
                ", kv=" + kv +
                '}';
    }
}
