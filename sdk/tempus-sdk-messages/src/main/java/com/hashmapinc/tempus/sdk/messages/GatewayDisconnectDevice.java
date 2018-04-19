package com.hashmapinc.tempus.sdk.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * In order to inform Tempus that device is disconnected from the Gateway, one needs to publish following message:
 *
 * Message: {"device":"Device A"}
 *
 * where Device A is your device name.  Once received, Tempus will no longer publish updates for this particular
 * device to this Gateway.
 */
public class GatewayDisconnectDevice {

    @JsonProperty(value = "device", index = 1)
    private String deviceName;

    /**
     * Instantiates a new Gateway disconnect device.
     */
    public GatewayDisconnectDevice() {
    }

    /**
     * Instantiates a new Gateway disconnect device.
     *
     * @param deviceName the device name
     */
    public GatewayDisconnectDevice(String deviceName) {
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

}
