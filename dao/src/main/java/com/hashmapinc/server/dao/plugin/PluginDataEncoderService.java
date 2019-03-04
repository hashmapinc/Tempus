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
package com.hashmapinc.server.dao.plugin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hashmapinc.server.common.data.plugin.ComponentDescriptor;
import com.hashmapinc.server.common.data.plugin.PluginMetaData;
import com.hashmapinc.server.dao.component.ComponentDescriptorService;
import com.hashmapinc.server.dao.encryption.EncryptionService;
import com.hashmapinc.server.dao.exception.IncorrectParameterException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.hashmapinc.server.dao.model.ModelConstants.*;

@Slf4j
@Service
public  class PluginDataEncoderService {

    @Autowired
    private ComponentDescriptorService componentDescriptorService;

    @Autowired
    private EncryptionService encryptionService;

    @Value("${encryption.aes_key}")
    private String aesKey;

     public  PluginMetaData encoder(PluginMetaData pluginMetaData) {
         if(pluginMetaData == null)
             return null;

         if(pluginMetaData.getConfiguration() == null)
             return pluginMetaData;

         PluginMetaData plugin = new PluginMetaData(pluginMetaData);
         ComponentDescriptor componentDescriptor = componentDescriptorService.findByClazz(plugin.getClazz());
         if (componentDescriptor == null)
             throw new IncorrectParameterException("Plugin descriptor not found!");

         JsonNode configurationDescriptor = componentDescriptor.getConfigurationDescriptor();
         List<String> attributesToEncrypt  = getAttributesOfPasswordType(configurationDescriptor.get("form"));
         JsonNode configuration = encodeJsonNode(plugin.getConfiguration() ,attributesToEncrypt);
         plugin.setConfiguration(configuration);
         return plugin;
     }

    private JsonNode encodeJsonNode(JsonNode jsonNode, List<String> attributesToEncrypt) {
         if(attributesToEncrypt.isEmpty())
             return jsonNode;
        ObjectMapper mapper = new ObjectMapper();
        Map<String,String> configuration = mapper.convertValue(jsonNode, Map.class);

        for(String attribute :attributesToEncrypt) {
            String encryptedAttributeValue = encryptionService.encrypt(configuration.get(attribute),aesKey);
            if(encryptedAttributeValue != null)
                configuration.put(attribute,encryptedAttributeValue);
        }
        return mapper.convertValue(configuration, JsonNode.class);
    }

    public  PluginMetaData decoder(PluginMetaData pluginMetaData) {
        if(pluginMetaData == null)
            return null;

        if(pluginMetaData.getConfiguration() == null)
            return pluginMetaData;

        PluginMetaData plugin = new PluginMetaData(pluginMetaData);
        ComponentDescriptor componentDescriptor = componentDescriptorService.findByClazz(plugin.getClazz());
        if (componentDescriptor == null)
            throw new IncorrectParameterException("Plugin descriptor not found!");

        JsonNode configurationDescriptor = componentDescriptor.getConfigurationDescriptor();
        List<String> attributesToDecrypt  = getAttributesOfPasswordType(configurationDescriptor.get("form"));
        JsonNode configuration = decodeJsonNode(plugin.getConfiguration() ,attributesToDecrypt);
        plugin.setConfiguration(configuration);
        return plugin;
    }

    private JsonNode decodeJsonNode(JsonNode jsonNode, List<String> attributesToDecrypt) {
        if(attributesToDecrypt.isEmpty())
            return jsonNode;
        ObjectMapper mapper = new ObjectMapper();
        Map<String,String> configuration = mapper.convertValue(jsonNode, Map.class);

        for(String attribute :attributesToDecrypt) {
            String decryptedAttributeValue = encryptionService.decrypt(configuration.get(attribute),aesKey);
            if(decryptedAttributeValue != null)
                configuration.put(attribute,decryptedAttributeValue);
        }
        return mapper.convertValue(configuration, JsonNode.class);
    }

    public List<String> getAttributesOfPasswordType(JsonNode jsonNode) {
        List<String> attributesOfPasswordType = new ArrayList<>();
        if (jsonNode.isArray()) {
            for (JsonNode objNode : jsonNode) {
                if(objNode.isObject()) {
                    JsonNode type = objNode.get(PLUGIN_DESCRIPTOR_TYPE);
                    JsonNode key =  objNode.get(PLUGIN_DESCRIPTOR_KEY);
                    if((type != null) && (key != null) && (type.asText().equals(PLUGIN_DESCRIPTOR_PASSWORD)))
                        attributesOfPasswordType.add(key.asText());
                }
            }
        }
        return attributesOfPasswordType;
    }
}
