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

import com.fasterxml.jackson.databind.JsonNode;
import lombok.ToString;

import java.util.Objects;
import java.util.Optional;

@ToString
public class BasicDsKvEntry implements DsKvEntry{
    private final Double ds;
    private final KvEntry kv;

    public BasicDsKvEntry(Double Ds, KvEntry kv) {
        this.ds = Ds;
        this.kv = kv;
    }

    @Override
    public String getKey() {
        return kv.getKey();
    }

    @Override
    public DataType getDataType() {
        return kv.getDataType();
    }

    @Override
    public Optional<String> getStrValue() {
        return kv.getStrValue();
    }

    @Override
    public Optional<Long> getLongValue() {
        return kv.getLongValue();
    }

    @Override
    public Optional<Boolean> getBooleanValue() {
        return kv.getBooleanValue();
    }

    @Override
    public Optional<Double> getDoubleValue() {
        return kv.getDoubleValue();
    }

    @Override
    public Optional<JsonNode> getJsonValue() {
        return kv.getJsonValue();
    }

    @Override
    public Object getValue() {
        return kv.getValue();
    }

    @Override
    public Double getDs() {
        return ds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BasicDsKvEntry)) return false;
        BasicDsKvEntry that = (BasicDsKvEntry) o;
        return getDs() == that.getDs() &&
                Objects.equals(kv, that.kv);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDs(), kv);
    }

    @Override
    public String getValueAsString() {
        return kv.getValueAsString();
    }
}
