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

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * A Gateway is a special type of device in Tempus that is able to act as a bridge between external devices
 * connected to different systems and Tempus. Gateway API provides the ability to exchange data between multiple
 * devices and the platform using single connection.
 * <p>
 * In order to publish device depth data to Tempus server node, one sends the following publish message:
 * <p>
 * Message:
 * <p>
 * {
 * "Device A": [
 * {
 * "ds": 315.0,
 * "values": {
 * "WOB": 42,
 * "ROP": 80
 * }
 * },
 * {
 * "ds": "320.0",
 * "values": {
 * "WOB": 43,
 * "ROP": 82
 * }
 * }
 * ]
 * }
 * <p>
 * Where Device A id the device names, WOB and ROP are parameters keys and values and ds the depth value.
 */
public class GatewayDepthValue {

    private String deviceName;

    private List<DepthDataValue> dataValues;

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
     * Get depth data values list.
     *
     * @return the list
     */
    public List<DepthDataValue> getDepthDataValues(){
        return dataValues;
    }

    /**
     * Instantiates a new Gateway depth value.
     */
    public GatewayDepthValue(){
        dataValues = new ArrayList<>();
    }

    /**
     * Add depth data value.
     *
     * @param value the value
     */
    public void addDepthDataValue(DepthDataValue value){
        dataValues.add(value);
    }

    /**
     * Write value as string string.
     *
     * @param msg the msg
     * @return the string
     * @throws JsonProcessingException the json processing exception
     */
    public String writeValueAsString(GatewayDepthValue msg) throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(GatewayDepthValue.class, new GatewayDepthValueSerializer());
        objectMapper.registerModule(module);
        return objectMapper.writeValueAsString(msg);
    }
}