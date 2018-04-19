package com.hashmapinc.tempus.sdk.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * The type Telemetry Data value.  In order to publish telemetry data to Tempus server node, one sends PUBLISH message with the following format
 *
 * Message:
 *
 * [{"ts":"1451649600512", "values":{"key1":"value1", "key2":"value2"}}]
 *
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

}
