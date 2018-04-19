package com.hashmapinc.tempus.sdk.messages;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;


/**
 * A Gateway is a special type of device in Tempus that is able to act as a bridge between external devices
 * connected to different systems and Tempus. Gateway API provides the ability to exchange data between multiple
 * devices and the platform using single connection.
 *
 * In order to publish device depth data to Tempus server node, one sends the following publish message:
 *
 * Message:
 *
 * {
 *   "Device A": [
 *     {
 *       "ds": 315.0,
 *       "values": {
 *         "WOB": 42,
 *         "ROP": 80
 *       }
 *     },
 *     {
 *       "ds": "320.0",
 *       "values": {
 *         "WOB": 43,
 *         "ROP": 82
 *       }
 *     }
 *   ]
 * }
 *
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
}