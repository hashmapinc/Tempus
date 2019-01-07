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
package com.hashmapinc.server.extensions.api.device;

import com.hashmapinc.server.common.data.kv.TsKvEntry;

import java.util.*;

public class DeviceTelemetry {

    private final Map<String, TsKvEntry> deviceTelemetryMap;

    public DeviceTelemetry(List<TsKvEntry> tsKvEntries) {
        this.deviceTelemetryMap = mapDeviceTelemetry(tsKvEntries);
    }

    private static Map<String, TsKvEntry> mapDeviceTelemetry(List<TsKvEntry> tsKvEntries) {
        Map<String, TsKvEntry> result = new HashMap<>();
        for (TsKvEntry tsKvEntry : tsKvEntries) {
            result.put(tsKvEntry.getKey(), tsKvEntry);
        }
        return result;
    }

    public Collection<TsKvEntry> getDeviceTelemetry() {
        return deviceTelemetryMap.values();
    }

    public Optional<TsKvEntry> getDeviceTelemetry(String attribute) {
        return Optional.ofNullable(deviceTelemetryMap.get(attribute));
    }

    public void update(List<TsKvEntry> values) {
        values.forEach(v -> deviceTelemetryMap.put(v.getKey(), v));
    }

    @Override
    public String toString() {
        return "DeviceTelemetry{ deviceTelemetryMap= " + deviceTelemetryMap +
                '}';
    }
}
