package com.hashmapinc.tempus.sdk.messages;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * To inform Tempus that device is connected to the Gateway, one needs to publish following message:
 *
 *       Message: {"device":"Device A", "type":"Pump"}
 *
 * where Device A is your device name and Pump is the type of device.  is a Once received, Tempus
 * will lookup or create a device with the name specified and type.
 */
public class GatewayConnectDevice {

    @JsonProperty(value = "device", index = 1, required = true)
    private String deviceName;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty(value = "type", index = 2)
    private String deviceType;

    /**
     * Instantiates a new Gateway connect device.
     */
    public GatewayConnectDevice() {
    }

    /**
     * Instantiates a new Gateway connect device.
     *
     * @param deviceName the device name
     * @param deviceType the device type
     */
    public GatewayConnectDevice(String deviceName, String deviceType) {
        this.deviceName = deviceName;
        this.deviceType = deviceType;
    }

    /**
     * Instantiates a new Gateway connect device.
     *
     * @param deviceName the device name
     */
    public GatewayConnectDevice(String deviceName) {
        this.deviceName = deviceName;
    }

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
     * Gets device type.
     *
     * @return the device type
     */
    public String getDeviceType() {
        return deviceType;
    }

    /**
     * Sets device type.
     *
     * @param deviceType the device type
     */
    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }





}
