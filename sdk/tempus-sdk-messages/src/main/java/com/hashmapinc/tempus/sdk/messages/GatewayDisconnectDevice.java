package com.hashmapinc.tempus.sdk.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GatewayDisconnectDevice {

    @JsonProperty(value = "device", index = 1)
    private String deviceName;

    public GatewayDisconnectDevice() {
    }

    public GatewayDisconnectDevice(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

}
