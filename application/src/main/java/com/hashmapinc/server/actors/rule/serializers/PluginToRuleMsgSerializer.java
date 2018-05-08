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
package com.hashmapinc.server.actors.rule.serializers;

import akka.serialization.JSerializer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class PluginToRuleMsgSerializer extends JSerializer{
    private final ObjectMapper mapper;

    public PluginToRuleMsgSerializer() {
        this.mapper = new ObjectMapper();
        mapper.enableDefaultTyping();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    @Override
    public Object fromBinaryJava(byte[] bytes, Class<?> manifest) {
        try {
            return mapper.readValue(bytes, manifest);
        } catch (IOException e) {
            log.error("Error while deserializing data for manifest: [{}]", manifest.getName(), e);
        }

        return null;
    }

    @Override
    public int identifier() {
        return 52;
    }

    @Override
    public byte[] toBinary(Object o) {
        try {
            return mapper.writeValueAsBytes(o);
        } catch (JsonProcessingException e) {
            log.error("Error while serializing data for manifest: [{}]", o.getClass().getName(), e);
        }
        return new byte[0];
    }

    @Override
    public boolean includeManifest() {
        return true;
    }
}
