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
package com.hashmapinc.server.dao.service.plugin;

import com.hashmapinc.server.common.data.plugin.ComponentDescriptor;
import com.hashmapinc.server.common.data.plugin.PluginMetaData;
import com.hashmapinc.server.dao.service.AbstractServiceTest;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public abstract class  PluginDataEncoderTest  extends AbstractServiceTest {

    @Test
    public void saveKinesisPluginWithOneEncryptedPasswordAttribute() throws Exception {
        PluginMetaData pluginMetaData = generatePlugin(null, null, "com.hashmapinc.server.extensions.kinesis.plugin.KinesisPlugin", "com.hashmapinc.component.ActionTest", "plugin/kinesis/TestKinesisPluginDescriptor.json", "plugin/kinesis/TestKinesisPluginConfigurationData.json");
        PluginMetaData savedPluginMetaData = pluginService.savePlugin(pluginMetaData);

        ComponentDescriptor componentDescriptor = componentDescriptorService.findByClazz(pluginMetaData.getClazz());
        List<String> keysToEncrypt  = descriptorEncoderDecoderService.getAttributesOfPasswordType(componentDescriptor);
        assertEquals(2, keysToEncrypt.size());

        for (String key : keysToEncrypt) {
            assertNotEquals(pluginMetaData.getConfiguration().get(key), savedPluginMetaData.getConfiguration().get(key));
        }
    }

    @Test
    public void saveKafkaPluginWithNoEncryptedPasswordAttribute() throws Exception {
        PluginMetaData pluginMetaData = generatePlugin(null, null, "com.hashmapinc.server.extensions.kafka.plugin.KafkaPlugin", "com.hashmapinc.component.ActionTest", "plugin/kafka/TestKafkaPluginDescriptor.json", "plugin/kafka/TestKafkaPluginConfigurationData.json");
        PluginMetaData savedPluginMetaData = pluginService.savePlugin(pluginMetaData);

        ComponentDescriptor componentDescriptor = componentDescriptorService.findByClazz(pluginMetaData.getClazz());
        List<String> keysToEncrypt  = descriptorEncoderDecoderService.getAttributesOfPasswordType(componentDescriptor);
        assertEquals(0, keysToEncrypt.size());

        for (String key : keysToEncrypt) {
            assertNotEquals(pluginMetaData.getConfiguration().get(key), savedPluginMetaData.getConfiguration().get(key));
        }
    }

    @Test
    public void saveSnsPluginWithTwoEncryptedPasswordAttribute() throws Exception {
        PluginMetaData pluginMetaData = generatePlugin(null, null, "com.hashmapinc.server.extensions.sns.plugin.SnsPlugin", "com.hashmapinc.component.ActionTest", "plugin/sns/TestSnsPluginDescriptor.json", "plugin/sns/TestSnsPluginConfigurationData.json");
        PluginMetaData savedPluginMetaData = pluginService.savePlugin(pluginMetaData);

        ComponentDescriptor componentDescriptor = componentDescriptorService.findByClazz(pluginMetaData.getClazz());
        List<String> keysToEncrypt  = descriptorEncoderDecoderService.getAttributesOfPasswordType(componentDescriptor);
        assertEquals(2, keysToEncrypt.size());

        for (String key : keysToEncrypt) {
            assertNotEquals(pluginMetaData.getConfiguration().get(key), savedPluginMetaData.getConfiguration().get(key));
        }
    }

    @Test
    public void saveEventHubPluginWithNoEncryptedPasswordAttribute() throws Exception {
        PluginMetaData pluginMetaData = generatePlugin(null, null, "com.hashmapinc.server.extensions.eventhub.plugin.EventHubPlugin", "com.hashmapinc.component.ActionTest", "plugin/event-hub/TestEventHubPluginDescriptor.json", "plugin/event-hub/TestEventHubConfigurationData.json");
        PluginMetaData savedPluginMetaData = pluginService.savePlugin(pluginMetaData);

        ComponentDescriptor componentDescriptor = componentDescriptorService.findByClazz(pluginMetaData.getClazz());
        List<String> keysToEncrypt  = descriptorEncoderDecoderService.getAttributesOfPasswordType(componentDescriptor);
        assertEquals(0, keysToEncrypt.size());

        for (String key : keysToEncrypt) {
            assertNotEquals(pluginMetaData.getConfiguration().get(key), savedPluginMetaData.getConfiguration().get(key));
        }
    }

    @Test
    public void saveRestApiCallPluginWithOneEncryptedPasswordAttribute() throws Exception {
        PluginMetaData pluginMetaData = generatePlugin(null, null, "com.hashmapinc.server.extensions.rest.plugin.RestApiCallPlugin", "com.hashmapinc.component.ActionTest", "plugin/rest-api-call/TestRestApiCallPluginDescriptor.json", "plugin/rest-api-call/TestRestApiCallPluginConfigurationData.json");
        PluginMetaData savedPluginMetaData = pluginService.savePlugin(pluginMetaData);

        ComponentDescriptor componentDescriptor = componentDescriptorService.findByClazz(pluginMetaData.getClazz());
        List<String> keysToEncrypt  = descriptorEncoderDecoderService.getAttributesOfPasswordType(componentDescriptor);
        assertEquals(1, keysToEncrypt.size());

        for (String key : keysToEncrypt) {
            assertNotEquals(pluginMetaData.getConfiguration().get(key), savedPluginMetaData.getConfiguration().get(key));
        }
    }
}

