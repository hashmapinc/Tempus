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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Objects;

/**
 * To inform Tempus that device is connected to the Gateway, one needs to publish following message:
 * <p>
 * Message: {"device":"Device A", "type":"Pump"}
 * <p>
 * where Device A is your device name and Pump is the type of device.  is a Once received, Tempus
 * will lookup or create a device with the name specified and type.
 */
public class GatewayConnectDevice {

    @JsonProperty(value = "device", index = 1, required = true)
    private String deviceName;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty(value = "type", index = 2)
    private String deviceType;

    /**
     * Instantiates a new Gateway connect device.
     */
    public GatewayConnectDevice() {
    }

    /**
     * Instantiates a new Gateway connect device.
     *
     * @param deviceName the device name
     * @param deviceType the device type
     */
    public GatewayConnectDevice(String deviceName, String deviceType) {
        this.deviceName = deviceName;
        this.deviceType = deviceType;
    }

    /**
     * Instantiates a new Gateway connect device.
     *
     * @param deviceName the device name
     */
    public GatewayConnectDevice(String deviceName) {
        this.deviceName = deviceName;
    }

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
     * Gets device type.
     *
     * @return the device type
     */
    public String getDeviceType() {
        return deviceType;
    }

    /**
     * Sets device type.
     *
     * @param deviceType the device type
     */
    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    /**
     * Write value as string string.
     *
     * @param device the device
     * @return the string
     * @throws JsonProcessingException the json processing exception
     */
    public String writeValueAsString(GatewayConnectDevice device) throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(device);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GatewayConnectDevice)) return false;
        GatewayConnectDevice that = (GatewayConnectDevice) o;
        return Objects.equals(getDeviceName(), that.getDeviceName()) &&
                Objects.equals(getDeviceType(), that.getDeviceType());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getDeviceName(), getDeviceType());
    }

    @Override
    public String toString() {
        return "GatewayConnectDevice{" +
                "deviceName='" + deviceName + '\'' +
                ", deviceType='" + deviceType + '\'' +
                '}';
    }
}
