package com.hashmapinc.tempus.sdk.messages;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

/**
 * The type Gateway depth value serializer.
 */
public class GatewayDepthValueSerializer extends StdSerializer<GatewayDepthValue> {

    /**
     * Instantiates a new Gateway depth value serializer.
     */
    public GatewayDepthValueSerializer() {
        this(null);
    }

    /**
     * Instantiates a new Gateway depth value serializer.
     *
     * @param t the t
     */
    public GatewayDepthValueSerializer(Class<GatewayDepthValue> t){
        super(t);
    }

    @Override
    public void serialize(GatewayDepthValue gatewayValue, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();

        jsonGenerator.writeObjectField(gatewayValue.getDeviceName(), gatewayValue.getDepthDataValues());

        jsonGenerator.writeEndObject();
    }
}
