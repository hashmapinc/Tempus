package com.hashmapinc.tempus.sdk.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

<<<<<<< Updated upstream
=======
/**
 * In order to publish telemetry data to Tempus server node, one sends PUBLISH message with the following format
 *
 * Message:
 *
 * [{"ds": 315.0, "values":{"key1":"value1", "key2":"value2"}}]
 *
 * Where ds is a depth value,  and the depth measurement values are represented as key value pairs.
 */
>>>>>>> Stashed changes
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
        values.put(key, value);
    }

}
