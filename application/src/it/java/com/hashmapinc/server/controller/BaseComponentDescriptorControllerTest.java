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
package com.hashmapinc.server.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hashmapinc.server.common.data.plugin.ComponentDescriptor;
import com.hashmapinc.server.common.data.plugin.ComponentScope;
import com.hashmapinc.server.common.data.plugin.ComponentType;
import com.hashmapinc.server.extensions.core.action.telemetry.TelemetryPluginAction;
import com.hashmapinc.server.extensions.core.plugin.telemetry.TelemetryStoragePlugin;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class BaseComponentDescriptorControllerTest extends AbstractControllerTest {

    private static final int AMOUNT_OF_DEFAULT_PLUGINS_DESCRIPTORS = 8;

    @Before
    public void beforeTest() throws Exception {
        loginTenantAdmin();
    }

    @Test
    public void testGetByClazz() throws Exception {
        ComponentDescriptor descriptor =
                doGet("/api/component/" + TelemetryStoragePlugin.class.getName(), ComponentDescriptor.class);

        Assert.assertNotNull(descriptor);
        Assert.assertNotNull(descriptor.getId());
        Assert.assertNotNull(descriptor.getName());
        Assert.assertEquals(ComponentScope.TENANT, descriptor.getScope());
        Assert.assertEquals(ComponentType.PLUGIN, descriptor.getType());
        Assert.assertEquals(descriptor.getClazz(), descriptor.getClazz());
    }

    @Test
    public void testGetByType() throws Exception {
        List<ComponentDescriptor> descriptors = readResponse(
                doGet("/api/components/" + ComponentType.PLUGIN).andExpect(status().isOk()), new TypeReference<List<ComponentDescriptor>>() {
                });

        Assert.assertNotNull(descriptors);
        Assert.assertEquals(AMOUNT_OF_DEFAULT_PLUGINS_DESCRIPTORS, descriptors.size());

        for (ComponentType type : ComponentType.values()) {
            doGet("/api/components/" + type).andExpect(status().isOk());
        }
    }

    @Test
    public void testGetActionsByType() throws Exception {
        List<ComponentDescriptor> descriptors = readResponse(
                doGet("/api/components/actions/" + TelemetryStoragePlugin.class.getName()).andExpect(status().isOk()), new TypeReference<List<ComponentDescriptor>>() {
                });

        Assert.assertNotNull(descriptors);
        Assert.assertEquals(1, descriptors.size());
        Assert.assertEquals(TelemetryPluginAction.class.getName(), descriptors.get(0).getClazz());
    }
}
