package com.hashmapinc.tempus.sdk.messages;

import java.util.ArrayList;
import java.util.List;

public class DeviceDepthValue {

    private List<DepthDataValue> dataValues;

    public List<DepthDataValue> getDepthDataValues(){
        return dataValues;
    }

    public DeviceDepthValue(){
        dataValues = new ArrayList<>();
    }

    public void addDepthDataValue(DepthDataValue value){
        dataValues.add(value);
    }
}