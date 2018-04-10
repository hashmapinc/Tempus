package org.thingsboard.server.common.data;

import java.io.Serializable;

public class DeviceTypeConfigurations implements Serializable {
    private Configuration configuration;

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

}
