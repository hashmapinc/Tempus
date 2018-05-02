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
