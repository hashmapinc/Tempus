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
import com.hashmapinc.server.common.data.plugin.ComponentDescriptor;
import com.hashmapinc.server.common.data.plugin.PluginMetaData;
import com.hashmapinc.server.dao.component.ComponentDescriptorService;
import com.hashmapinc.server.dao.encoder.DescriptorEncoderDecoderService;
import com.hashmapinc.server.dao.exception.IncorrectParameterException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public  class PluginDataEncoderService {

    @Autowired
    private ComponentDescriptorService componentDescriptorService;

    @Autowired
    private DescriptorEncoderDecoderService descriptorEncoderDecoderService;

     public  PluginMetaData encoder(PluginMetaData pluginMetaData) {
         if(pluginMetaData == null)
             return null;

         if(pluginMetaData.getConfiguration() == null)
             return pluginMetaData;

         PluginMetaData plugin = new PluginMetaData(pluginMetaData);
         ComponentDescriptor componentDescriptor = componentDescriptorService.findByClazz(plugin.getClazz());
         if (componentDescriptor == null)
             throw new IncorrectParameterException("Plugin descriptor not found!");

         JsonNode configuration = descriptorEncoderDecoderService.encode(plugin.getConfiguration() ,componentDescriptor);
         plugin.setConfiguration(configuration);
         return plugin;
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

        JsonNode configuration = descriptorEncoderDecoderService.decode(plugin.getConfiguration() ,componentDescriptor);
        plugin.setConfiguration(configuration);
        return plugin;
    }
}
