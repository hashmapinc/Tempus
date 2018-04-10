package org.thingsboard.server.common.data;

import java.io.Serializable;
import java.util.List;

public class Configuration implements Serializable {
    private List<DeviceType> deviceTypes;

    public List<DeviceType> getDeviceTypes() {
        return deviceTypes;
    }

    public void setDeviceTypes(List<DeviceType> deviceTypes) {
        this.deviceTypes = deviceTypes;
    }
}
