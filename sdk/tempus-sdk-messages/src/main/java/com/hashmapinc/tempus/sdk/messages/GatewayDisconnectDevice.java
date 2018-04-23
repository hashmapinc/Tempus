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

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * In order to inform Tempus that device is disconnected from the Gateway, one needs to publish following message:
 *
 * Message: {"device":"Device A"}
 *
 * where Device A is your device name.  Once received, Tempus will no longer publish updates for this particular
 * device to this Gateway.
 */
public class GatewayDisconnectDevice {

    @JsonProperty(value = "device", index = 1)
    private String deviceName;

    /**
     * Instantiates a new Gateway disconnect device.
     */
    public GatewayDisconnectDevice() {
    }

    /**
     * Instantiates a new Gateway disconnect device.
     *
     * @param deviceName the device name
     */
    public GatewayDisconnectDevice(String deviceName) {
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

}
