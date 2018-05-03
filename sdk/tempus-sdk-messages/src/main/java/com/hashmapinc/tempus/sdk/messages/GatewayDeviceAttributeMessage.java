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
 * In order to publish Gateway device attributes to Tempus server node, one sends the following publish message:
 * <p>
 * Message:
 * <p>
 * Message: {"Device A":{"attribute1":"value1", "attribute2": 42}, "Device B":{"attribute1":"value1", "attribute2": 42}}
 * <p>
 * Where Device A and Device B are your device names, attribute1 and attribute2 are attribute keys.
 */
public class GatewayDeviceAttributeMessage {

    private String deviceName;

    private List<AttributeDataValue> attributeValues;

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
     * Get data values list.
     *
     * @return the list
     */
    public List<AttributeDataValue> getAttributeValues(){
        return attributeValues;
    }

    /**
     * Instantiates a new GatewayDeviceAttributeMessage.
     */
    public GatewayDeviceAttributeMessage() { attributeValues = new ArrayList<>(); }

    /**
     * Add data value.
     *
     * @param value the value
     */
    public void addAttributeValue(AttributeDataValue value){
        attributeValues.add(value);
    }

    /**
     * Write GatewayDeviceAttributeMessage as string.
     *
     * @param msg the msg
     * @return the string
     * @throws JsonProcessingException the json processing exception
     */
    public String writeValueAsString(GatewayDeviceAttributeMessage msg) throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(GatewayDeviceAttributeMessage.class, new GatewayDeviceAttributeMessageSerializer());
        objectMapper.registerModule(module);
        return objectMapper.writeValueAsString(msg);
    }

}