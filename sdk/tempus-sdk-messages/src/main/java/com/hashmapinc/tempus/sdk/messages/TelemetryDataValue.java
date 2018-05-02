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
import java.util.Objects;

/**
 * The type Telemetry Data value.  In order to publish telemetry data to Tempus server node, one sends PUBLISH message with the following format
 * <p>
 * Message:
 * <p>
 * [{"ts":"1451649600512", "values":{"key1":"value1", "key2":"value2"}}]
 * <p>
 * Where ts is a unix timestamp in milliseconds,  and the telemetry values are represented as key value pairs.
 */
public class TelemetryDataValue {

    @JsonProperty(value = "ts", index = 1)
    private String timeStamp;

    @JsonProperty(index = 2)
    private Map<String, Object> values;


    /**
     * Get time stamp string.
     *
     * @return the string
     */
    public String getTimeStamp(){
        return timeStamp;
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
     * Instantiates a new Data value.
     */
    public TelemetryDataValue(){
        values = new HashMap<>();
    }

    /**
     * Set time stamp.
     *
     * @param timeStamp the time stamp
     */
    public void setTimeStamp(String timeStamp){
        this.timeStamp = timeStamp;
    }

    /**
     * Add value.
     *
     * @param key   the key
     * @param value the value
     */
    public void addValue(String key, Object value){
        values.put(key, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TelemetryDataValue)) return false;
        TelemetryDataValue that = (TelemetryDataValue) o;
        return Objects.equals(getTimeStamp(), that.getTimeStamp()) &&
                Objects.equals(getValues(), that.getValues());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getTimeStamp(), getValues());
    }

    @Override
    public String toString() {
        return "TelemetryDataValue{" +
                "timeStamp='" + timeStamp + '\'' +
                ", values=" + values +
                '}';
    }
}
