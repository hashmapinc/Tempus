package com.hashmapinc.tempus.sdk.messages;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

/**
 * The type Device depth value serializer.
 */
public class DeviceDepthValueSerializer extends StdSerializer<DeviceDepthValue> {

    /**
     * Instantiates a new Device depth value serializer.
     */
    public DeviceDepthValueSerializer() {
        this(null);
    }

    /**
     * Instantiates a new Device depth value serializer.
     *
     * @param t the t
     */
    public DeviceDepthValueSerializer(Class<DeviceDepthValue> t){
        super(t);
    }

    @Override
    public void serialize(DeviceDepthValue deviceValue, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

        jsonGenerator.writeObject(deviceValue.getDepthDataValues());

    }
}
