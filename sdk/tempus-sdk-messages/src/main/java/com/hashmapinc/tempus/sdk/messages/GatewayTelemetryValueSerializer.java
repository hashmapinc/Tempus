package com.hashmapinc.tempus.sdk.messages;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

/**
 * The type Gateway Telemetry value serializer.
 */
public class GatewayTelemetryValueSerializer extends StdSerializer<GatewayTelemetryValue> {

    /**
     * Instantiates a new Gateway value serializer.
     */
    public GatewayTelemetryValueSerializer() {
        this(null);
    }

    /**
     * Instantiates a new Gateway value serializer.
     *
     * @param t the t
     */
    public GatewayTelemetryValueSerializer(Class<GatewayTelemetryValue> t){
        super(t);
    }

    @Override
    public void serialize(GatewayTelemetryValue gatewayTelemetryValue, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();

        jsonGenerator.writeObjectField(gatewayTelemetryValue.getDeviceName(), gatewayTelemetryValue.getDataValues());

        jsonGenerator.writeEndObject();
    }
}
