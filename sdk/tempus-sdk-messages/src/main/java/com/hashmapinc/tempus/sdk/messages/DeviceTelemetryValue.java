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
package com.hashmapinc.tempus.sdk.messages;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * In order to publish device telemetry to Tempus server node, one sends the following publish message:
 * <p>
 * Message:
 * <p>
 * [{"ts":1451649600512, "values":{"key1":"value1", "key2":"value2"}}]
 * <p>
 * Where ts is a unix timestamp in milliseconds, and the telemetry values are represented as key value pairs.
 */
//TODO: Add Attribute Messages
public class DeviceTelemetryValue {

    private List<TelemetryDataValue> telemetryDataValues;

    /**
     * Get data values list.
     *
     * @return the list
     */
    public List<TelemetryDataValue> getDataValues(){
        return telemetryDataValues;
    }

    /**
     * Instantiates a new Device value.
     */
    public DeviceTelemetryValue(){
        telemetryDataValues = new ArrayList<>();
    }

    /**
     * Add data value.
     *
     * @param value the value
     */
    public void addDataValue(TelemetryDataValue value){
        telemetryDataValues.add(value);
    }

    /**
     * Write DeviceTelemetryValue as string.
     *
     * @param msg the msg
     * @return the string
     * @throws JsonProcessingException the json processing exception
     */
    public String writeValueAsString(DeviceTelemetryValue msg) throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(DeviceTelemetryValue.class, new DeviceTelemetryValueSerializer());
        objectMapper.registerModule(module);
        return objectMapper.writeValueAsString(msg);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeviceTelemetryValue)) return false;
        DeviceTelemetryValue that = (DeviceTelemetryValue) o;
        return Objects.equals(telemetryDataValues, that.telemetryDataValues);
    }

    @Override
    public int hashCode() {

        return Objects.hash(telemetryDataValues);
    }

    @Override
    public String toString() {
        return "DeviceTelemetryValue{" +
                "telemetryDataValues=" + telemetryDataValues +
                '}';
    }
}