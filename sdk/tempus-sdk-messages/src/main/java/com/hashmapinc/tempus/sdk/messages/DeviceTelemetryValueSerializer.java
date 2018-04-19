package com.hashmapinc.tempus.sdk.messages;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

/**
 * The type Device value serializer.
 */
public class DeviceTelemetryValueSerializer extends StdSerializer<DeviceTelemetryValue> {

    /**
     * Instantiates a new Device value serializer.
     */
    public DeviceTelemetryValueSerializer() {
        this(null);
    }

    /**
     * Instantiates a new Device value serializer.
     *
     * @param t the t
     */
    public DeviceTelemetryValueSerializer(Class<DeviceTelemetryValue> t){
        super(t);
    }

    @Override
    public void serialize(DeviceTelemetryValue deviceTelemetryValue, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

        jsonGenerator.writeObject(deviceTelemetryValue.getDataValues());

    }
}
