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

/**
 * In order to publish telemetry data to Tempus server node, one sends PUBLISH message with the following format
 *
 * Message:
 *
 * [{"ds": 315.0, "values":{"key1":"value1", "key2":"value2"}}]
 *
 * Where ds is a depth value,  and the depth measurement values are represented as key value pairs.
 */
public class DeviceDepthValue {

    private List<DepthDataValue> dataValues;

    /**
     * Get depth data values list.
     *
     * @return the list
     */
    public List<DepthDataValue> getDepthDataValues(){
        return dataValues;
    }

    /**
     * Instantiates a new Device depth value.
     */
    public DeviceDepthValue(){
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
}