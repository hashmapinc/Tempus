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

public class DepthDataValue {

    @JsonProperty(value = "ds", index = 1)
    private  double depth;

    @JsonProperty(index = 2)
    private Map<String, Object> values;

    public double getDepth(){

        return depth;
    }

    public Map getValues(){
        return values;
    }

    public DepthDataValue(){
        values = new HashMap<>();
    }

    public void setDepth (double ds){
        this.depth = ds;
    }

    public void addValue(String key, Object value){
        //TODO: add type check on supported values (boolean, string, double, long) throw un supported type exception
        values.put(key, value);
    }
}
