/**
 * Copyright © 2016-2018 The Thingsboard Authors
 * Modifications © 2017-2018 Hashmap, Inc
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
package com.hashmapinc.server.dao.encoder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hashmapinc.server.common.data.plugin.ComponentDescriptor;
import com.hashmapinc.server.dao.encryption.EncryptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.hashmapinc.server.dao.model.ModelConstants.*;

@Service
public class DescriptorEncoderDecoderService implements EncoderService ,DecoderService{

    @Autowired
    private EncryptionService encryptionService;

    @Override
    public JsonNode encode(JsonNode jsonNode,ComponentDescriptor componentDescriptor) {
        List<String> attributesToEncrypt  = getAttributesOfPasswordType(componentDescriptor);
        if(attributesToEncrypt.isEmpty())
            return jsonNode;
        ObjectMapper mapper = new ObjectMapper();
        Map<String,String> configuration = mapper.convertValue(jsonNode, Map.class);

        for(String attribute :attributesToEncrypt) {
            String encryptedAttributeValue = encryptionService.encrypt(configuration.get(attribute));
            if(encryptedAttributeValue != null)
                configuration.put(attribute,encryptedAttributeValue);
        }
        return mapper.convertValue(configuration, JsonNode.class);
    }

    @Override
    public JsonNode decode(JsonNode jsonNode, ComponentDescriptor componentDescriptor) {
        List<String> attributesToDecrypt  = getAttributesOfPasswordType(componentDescriptor);
        if(attributesToDecrypt.isEmpty())
            return jsonNode;
        ObjectMapper mapper = new ObjectMapper();
        Map<String,String> configuration = mapper.convertValue(jsonNode, Map.class);

        for(String attribute :attributesToDecrypt) {
            String decryptedAttributeValue = encryptionService.decrypt(configuration.get(attribute));
            if(decryptedAttributeValue != null)
                configuration.put(attribute,decryptedAttributeValue);
        }
        return mapper.convertValue(configuration, JsonNode.class);
    }

    public List<String> getAttributesOfPasswordType(ComponentDescriptor componentDescriptor) {
        JsonNode configurationDescriptor = componentDescriptor.getConfigurationDescriptor();
        JsonNode jsonNode  = configurationDescriptor.get("form");
        List<String> attributesOfPasswordType = new ArrayList<>();
        if (jsonNode.isArray()) {
            for (JsonNode objNode : jsonNode) {
                if(objNode.isObject()) {
                    JsonNode type = objNode.get(DESCRIPTOR_TYPE);
                    JsonNode key =  objNode.get(DESCRIPTOR_KEY);
                    if((type != null) && (key != null) && (type.asText().equals(DESCRIPTOR_PASSWORD)))
                        attributesOfPasswordType.add(key.asText());
                }
            }
        }
        return attributesOfPasswordType;
    }
}
