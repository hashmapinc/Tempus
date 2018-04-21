package com.hashmapinc.server.common.data;


import java.io.Serializable;

public class DeviceType implements Serializable{
    public String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
