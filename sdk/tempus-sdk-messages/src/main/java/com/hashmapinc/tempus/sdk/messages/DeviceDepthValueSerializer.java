package com.hashmapinc.tempus.sdk.messages;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class DeviceDepthValueSerializer extends StdSerializer<DeviceDepthValue> {

    public DeviceDepthValueSerializer() {
        this(null);
    }

    public DeviceDepthValueSerializer(Class<DeviceDepthValue> t){
        super(t);
    }

    @Override
    public void serialize(DeviceDepthValue deviceValue, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

        jsonGenerator.writeObject(deviceValue.getDepthDataValues());

    }
}
