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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

public class JsonDataEntry extends BasicKvEntry{
    private final JsonNode value;

    public JsonDataEntry(String key, JsonNode value) {
        super(key);
        this.value = value;
    }

    public JsonDataEntry(String key, JsonObject value) {
        super(key);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode= null;
        try {
            jsonNode= mapper.readTree(value.toString());
        } catch (IOException ex) {

        }
        this.value=jsonNode;
    }

    @Override
    public DataType getDataType() {
        return DataType.JSON;
    }

    @Override
    public Optional<JsonNode> getJsonValue() {
        return Optional.of(value);
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JsonDataEntry)) return false;
        if (!super.equals(o)) return false;
        JsonDataEntry that = (JsonDataEntry) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), value.toString());
    }

    @Override
    public String toString() {
        return "JsonDataEntry{" +
                "value=" + value.toString() +
                "} " + super.toString();
    }

    @Override
    public String getValueAsString() {
        return value.toString();
    }
}
