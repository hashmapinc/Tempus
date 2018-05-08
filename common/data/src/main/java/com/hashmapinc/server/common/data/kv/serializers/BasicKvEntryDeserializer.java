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
package com.hashmapinc.server.common.data.kv.serializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hashmapinc.server.common.data.kv.*;

import java.io.IOException;

public class BasicKvEntryDeserializer extends JsonDeserializer<BasicKvEntry> {

    @Override
    public BasicKvEntry deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        ObjectCodec oc = jsonParser.getCodec();
        ObjectNode node = oc.readTree(jsonParser);
        BasicKvEntry entry = null;
        if(node.get("dataType") != null){
            String dataType = node.get("dataType").asText();
            switch(dataType){
                case "BOOLEAN" :
                    entry = new BooleanDataEntry(node.get("key").asText(), node.get("value").asBoolean());
                    break;
                case "STRING" :
                    entry = new StringDataEntry(node.get("key").asText(), node.get("value").asText());
                    break;
                case "DOUBLE" :
                    entry = new DoubleDataEntry(node.get("key").asText(), node.get("value").asDouble());
                    break;
                case "LONG" :
                    entry = new LongDataEntry(node.get("key").asText(), node.get("value").asLong());
                    break;
                case "JSON" :
                    entry = new JsonDataEntry(node.get("key").asText(), node.get("value"));
                    break;
            }
        }

        return entry;
    }
}
