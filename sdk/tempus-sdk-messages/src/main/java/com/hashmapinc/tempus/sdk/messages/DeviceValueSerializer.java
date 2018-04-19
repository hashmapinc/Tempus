package com.hashmapinc.tempus.sdk.messages;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class DeviceValueSerializer extends StdSerializer<DeviceValue> {

    public DeviceValueSerializer() {
        this(null);
    }

    public DeviceValueSerializer(Class<DeviceValue> t){
        super(t);
    }

    @Override
    public void serialize(DeviceValue deviceValue, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

        jsonGenerator.writeObject(deviceValue.getDataValues());

    }
}
