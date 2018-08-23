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
package com.hashmapinc.server.common.data.kv;

import java.util.Objects;
import java.util.Optional;

public class StringDataEntry extends BasicKvEntry {

    private static final long serialVersionUID = 1L;
    private final String value;

    public StringDataEntry(String key, String value) {
        super(key);
        this.value = value;
    }

    public StringDataEntry(String key, String unit, String value) {
        super(key, unit);
        this.value = value;
    }

    @Override
    public DataType getDataType() {
        return DataType.STRING;
    }

    @Override
    public Optional<String> getStrValue() {
        return Optional.of(value);
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof StringDataEntry))
            return false;
        if (!super.equals(o))
            return false;
        StringDataEntry that = (StringDataEntry) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getValueAsString());
    }

    @Override
    public String toString() {
        return "StringDataEntry{" + "value='" + getValueAsString() + '\'' + "} " + super.toString();
    }
    
    @Override
    public String getValueAsString() {
        return (String) getValue();
    }
}
