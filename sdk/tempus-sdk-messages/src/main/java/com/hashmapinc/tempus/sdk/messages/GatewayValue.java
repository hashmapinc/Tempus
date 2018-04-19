package com.hashmapinc.tempus.sdk.messages;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;


public class GatewayValue {

    private String deviceName;

    private List<DataValue> dataValues;

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public List<DataValue> getDataValues(){
        return dataValues;
    }

    public GatewayValue(){
        dataValues = new ArrayList<>();
    }

    public void addDataValue(DataValue value){
        dataValues.add(value);
    }
}