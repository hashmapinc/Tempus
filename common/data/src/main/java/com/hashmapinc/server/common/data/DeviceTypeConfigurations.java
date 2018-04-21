package com.hashmapinc.server.common.data;

import java.io.Serializable;
import java.util.List;

public class DeviceTypeConfigurations implements Serializable {
    private List<DeviceType> deviceTypes;

    public List<DeviceType> getDeviceTypes() {
        return deviceTypes;
    }

    public void setDeviceTypes(List<DeviceType> deviceTypes) {
        this.deviceTypes = deviceTypes;
    }
}
