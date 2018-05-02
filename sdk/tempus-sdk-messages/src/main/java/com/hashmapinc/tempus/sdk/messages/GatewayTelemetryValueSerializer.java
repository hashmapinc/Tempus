/**
 * Copyright © 2017-2018 Hashmap, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
