package com.hashmapinc.tempus.sdk.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

public class DepthDataValue {

    @JsonProperty(value = "ds", index = 1)
    private  double depthSeries;

    @JsonProperty(index = 2)
    private Map<String, Object> values;

    public double getDepth(){
        return depthSeries;
    }

    public Map getValues(){
        return values;
    }

    public DepthDataValue(){
        values = new HashMap<>();
    }

    public void setDepth (double ds){
        this.depthSeries = ds;
    }

    public void addValue(String key, Object value){
        values.put(key, value);
    }

}
