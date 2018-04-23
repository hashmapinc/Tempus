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

import java.util.HashMap;
import java.util.Map;

<<<<<<< HEAD
/**
 * In order to publish telemetry data to Tempus server node, one sends PUBLISH message with the following format
 *
 * Message:
 *
 * [{"ds": 315.0, "values":{"key1":"value1", "key2":"value2"}}]
 *
 * Where ds is a depth value,  and the depth measurement values are represented as key value pairs.
 */
=======
//TODO: javadoc format for each file //*  */ with min of 50%
>>>>>>> add license:format goal to pom

public class DepthDataValue {

    @JsonProperty(value = "ds", index = 1)
    private  double depth;

    @JsonProperty(index = 2)
    private Map<String, Object> values;

    /**
     * Get depth double.
     *
     * @return the double
     */
    public double getDepth(){
        return depth;
    }

    /**
     * Get values map.
     *
     * @return the map
     */
    public Map getValues(){
        return values;
    }

    /**
     * Instantiates a new Depth data value.
     */
    public DepthDataValue(){
        values = new HashMap<>();
    }

    /**
     * Set depth.
     *
     * @param ds the ds
     */
    public void setDepth (double ds){
        this.depth = ds;
    }

    /**
     * Add value.
     *
     * @param key   the key
     * @param value the value
     */
    public void addValue(String key, Object value){
        //TODO: add type check on supported values (boolean, string, double, long) throw un supported type exception
        values.put(key, value);
    }

    //TODO:  add 4 helper methods

}
