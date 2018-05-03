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
 * In order to publish device attributes to Tempus server node, one sends the following publish message:
 * <p>
 * Message:
 * <p>
 * [{"attribute1":"value1", "attribute2": 42}]
 * <p>
 * Where attribute1 and attribute2 are as key value pairs.
 */
public class DeviceAttributeMessage {

    private List<AttributeDataValue> attributeValues;

    /**
     * Get data values list.
     *
     * @return the list
     */
    public List<AttributeDataValue> getAttributeValues(){
        return attributeValues;
    }

    /**
     * Instantiates a new Device value.
     */
    public DeviceAttributeMessage(){
        attributeValues = new ArrayList<>();
    }

    /**
     * Add data value.
     *
     * @param value the value
     */
    public void addAttributeValue(AttributeDataValue value){
        attributeValues.add(value);
    }

    /**
     * Write DeviceAttributeMessage as string.
     *
     * @param msg the msg
     * @return the string
     * @throws JsonProcessingException the json processing exception
     */
    public String writeValueAsString(DeviceAttributeMessage msg) throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(DeviceAttributeMessage.class, new DeviceAttributeMessageSerializer());
        objectMapper.registerModule(module);
        return objectMapper.writeValueAsString(msg);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeviceAttributeMessage)) return false;
        DeviceAttributeMessage that = (DeviceAttributeMessage) o;
        return Objects.equals(getAttributeValues(), that.getAttributeValues());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getAttributeValues());
    }

    @Override
    public String toString() {
        return "DeviceAttributeMessage{" +
                "attributeValues=" + attributeValues +
                '}';
    }
}