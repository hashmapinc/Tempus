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
 * A Gateway is a special type of device in Tempus that is able to act as a bridge between external devices
 * connected to different systems and Tempus. Gateway API provides the ability to exchange data between multiple
 * devices and the platform using single connection.
 * <p>
 * In order to publish device telemetry to Tempus server node, one sends the following publish message:
 * <p>
 * Message:
 * <p>
 * {
 * "Device A": [
 * {
 * "ts": "1483228800000",
 * "values": {
 * "temperature": 42,
 * "humidity": 80
 * }
 * },
 * {
 * "ts": "1483228801000",
 * "values": {
 * "temperature": 43,
 * "humidity": 82
 * }
 * }
 * ]
 * }
 * where Device A id the device names, temperature and humidity are telemetry keys and ts is unix timestamp in milliseconds.
 */
public class GatewayTelemetryValue {

    private String deviceName;

    private List<TelemetryDataValue> telemetryDataValues;

    /**
     * Gets device name.
     *
     * @return the device name
     */
    public String getDeviceName() {
        return deviceName;
    }

    /**
     * Sets device name.
     *
     * @param deviceName the device name
     */
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    /**
     * Get Telemetry data values list.
     *
     * @return the list
     */
    public List<TelemetryDataValue> getDataValues() {
        return telemetryDataValues;
    }

    /**
     * Instantiates a new Gateway Telemetry value.
     */
    public GatewayTelemetryValue() {
        telemetryDataValues = new ArrayList<>();
    }

    /**
     * Add Telemetry data value.
     *
     * @param value the value
     */
    public void addDataValue(TelemetryDataValue value) {
        telemetryDataValues.add(value);
    }

    /**
     * Write GatewayTelemetryValue as string.
     *
     * @param gateway the gateway
     * @return the string
     * @throws JsonProcessingException the json processing exception
     */
    public String writeValueAsString(GatewayTelemetryValue gateway) throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(GatewayTelemetryValue.class, new GatewayTelemetryValueSerializer());
        objectMapper.registerModule(module);
        return objectMapper.writeValueAsString(gateway);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GatewayTelemetryValue)) return false;
        GatewayTelemetryValue that = (GatewayTelemetryValue) o;
        return Objects.equals(getDeviceName(), that.getDeviceName()) &&
                Objects.equals(telemetryDataValues, that.telemetryDataValues);
    }

    @Override
    public int hashCode() {

        return Objects.hash(getDeviceName(), telemetryDataValues);
    }

    @Override
    public String toString() {
        return "GatewayTelemetryValue{" +
                "deviceName='" + deviceName + '\'' +
                ", telemetryDataValues=" + telemetryDataValues +
                '}';
    }
}