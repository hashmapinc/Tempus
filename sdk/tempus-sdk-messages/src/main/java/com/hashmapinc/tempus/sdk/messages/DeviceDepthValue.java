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