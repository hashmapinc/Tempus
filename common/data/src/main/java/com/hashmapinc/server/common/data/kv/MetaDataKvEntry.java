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

public class MetaDataKvEntry {
    private final long lastUpdateTs;
    private final StringDataEntry kvEntry;

    public MetaDataKvEntry(StringDataEntry kv, long lastUpdateTs) {
        this.kvEntry = kv;
        this.lastUpdateTs = lastUpdateTs;
    }


    public String getKey() {
        return kvEntry.getKey();
    }

    public String getValue() {
        return kvEntry.getValueAsString();
    }

    public long getLastUpdateTs() {
        return lastUpdateTs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetaDataKvEntry that = (MetaDataKvEntry) o;
        return lastUpdateTs == that.lastUpdateTs &&
                Objects.equals(kvEntry, that.kvEntry);
    }

    @Override
    public int hashCode() {

        return Objects.hash(lastUpdateTs, kvEntry);
    }

    @Override
    public String toString() {
        return "MetaDataKvEntry{" +
                "lastUpdateTs=" + lastUpdateTs +
                ", kvEntry=" + kvEntry +
                '}';
    }
}
