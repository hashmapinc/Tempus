package com.hashmapinc.tempus.sdk.messages;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;


public class GatewayDepthValue {

    private String deviceName;

    private List<DepthDataValue> dataValues;

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public List<DepthDataValue> getDepthDataValues(){
        return dataValues;
    }

    public GatewayDepthValue(){
        dataValues = new ArrayList<>();
    }

    public void addDepthDataValue(DepthDataValue value){
        dataValues.add(value);
    }
}