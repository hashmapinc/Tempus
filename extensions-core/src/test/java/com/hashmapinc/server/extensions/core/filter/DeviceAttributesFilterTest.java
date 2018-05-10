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
package com.hashmapinc.server.extensions.core.filter;

import com.hashmapinc.server.common.data.id.DeviceId;
import com.hashmapinc.server.common.data.kv.AttributeKvEntry;
import com.hashmapinc.server.common.data.kv.BaseAttributeKvEntry;
import com.hashmapinc.server.common.data.kv.DoubleDataEntry;
import com.hashmapinc.server.extensions.api.rules.RuleContext;
import com.hashmapinc.server.common.data.kv.BooleanDataEntry;
import com.hashmapinc.server.extensions.api.device.DeviceAttributes;
import com.hashmapinc.server.extensions.api.device.DeviceMetaData;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@RunWith(MockitoJUnitRunner.class)
public class DeviceAttributesFilterTest {

    @Mock
    RuleContext ruleCtx;

    private static JsFilterConfiguration wrap(String filterBody) {
        return new JsFilterConfiguration(filterBody);
    }

    @Test
    public void basicMissingAttributesTest() {
        DeviceAttributesFilter filter = new DeviceAttributesFilter();
        filter.init(wrap("((typeof nonExistingVal === 'undefined') || nonExistingVal == true) && booleanValue == false"));
        List<AttributeKvEntry> clientAttributes = new ArrayList<>();
        clientAttributes.add(new BaseAttributeKvEntry(new BooleanDataEntry("booleanValue", false), 42));
        DeviceAttributes attributes = new DeviceAttributes(clientAttributes, new ArrayList<>(), new ArrayList<>());

        Mockito.when(ruleCtx.getDeviceMetaData()).thenReturn(new DeviceMetaData(new DeviceId(UUID.randomUUID()), "A", "A", attributes));
        Assert.assertTrue(filter.filter(ruleCtx, null));
        filter.stop();
    }

    @Test
    public void basicClientAttributesTest() {
        DeviceAttributesFilter filter = new DeviceAttributesFilter();
        filter.init(wrap("doubleValue == 1.0 && booleanValue == false"));
        List<AttributeKvEntry> clientAttributes = new ArrayList<>();
        clientAttributes.add(new BaseAttributeKvEntry(new DoubleDataEntry("doubleValue", 1.0), 42));
        clientAttributes.add(new BaseAttributeKvEntry(new BooleanDataEntry("booleanValue", false), 42));
        DeviceAttributes attributes = new DeviceAttributes(clientAttributes, new ArrayList<>(), new ArrayList<>());

        Mockito.when(ruleCtx.getDeviceMetaData()).thenReturn(new DeviceMetaData(new DeviceId(UUID.randomUUID()), "A", "A", attributes));
        Assert.assertTrue(filter.filter(ruleCtx, null));
        filter.stop();
    }

    @Test(timeout = 30000)
    public void basicClientAttributesStressTest() {
        DeviceAttributesFilter filter = new DeviceAttributesFilter();
        filter.init(wrap("doubleValue == 1.0 && booleanValue == false"));

        List<AttributeKvEntry> clientAttributes = new ArrayList<>();
        clientAttributes.add(new BaseAttributeKvEntry(new DoubleDataEntry("doubleValue", 1.0), 42));
        clientAttributes.add(new BaseAttributeKvEntry(new BooleanDataEntry("booleanValue", false), 42));
        DeviceAttributes attributes = new DeviceAttributes(clientAttributes, new ArrayList<>(), new ArrayList<>());

        Mockito.when(ruleCtx.getDeviceMetaData()).thenReturn(new DeviceMetaData(new DeviceId(UUID.randomUUID()), "A", "A", attributes));

        for (int i = 0; i < 10000; i++) {
            Assert.assertTrue(filter.filter(ruleCtx, null));
        }
        filter.stop();
    }

    @Test
    public void basicServerAttributesTest() {
        DeviceAttributesFilter filter = new DeviceAttributesFilter();
        filter.init(wrap("doubleValue == 1.0 && booleanValue == false"));

        List<AttributeKvEntry> serverAttributes = new ArrayList<>();
        serverAttributes.add(new BaseAttributeKvEntry(new DoubleDataEntry("doubleValue", 1.0), 42));
        serverAttributes.add(new BaseAttributeKvEntry(new BooleanDataEntry("booleanValue", false), 42));
        DeviceAttributes attributes = new DeviceAttributes(new ArrayList<>(), serverAttributes, new ArrayList<>());

        Mockito.when(ruleCtx.getDeviceMetaData()).thenReturn(new DeviceMetaData(new DeviceId(UUID.randomUUID()), "A", "A", attributes));
        Assert.assertTrue(filter.filter(ruleCtx, null));
        filter.stop();
    }

    @Test
    public void basicConflictServerAttributesTest() {
        DeviceAttributesFilter filter = new DeviceAttributesFilter();
        filter.init(wrap("cs.doubleValue == 1.0 && cs.booleanValue == true && ss.doubleValue == 0.0 && ss.booleanValue == false"));

        List<AttributeKvEntry> clientAttributes = new ArrayList<>();
        clientAttributes.add(new BaseAttributeKvEntry(new DoubleDataEntry("doubleValue", 1.0), 42));
        clientAttributes.add(new BaseAttributeKvEntry(new BooleanDataEntry("booleanValue", true), 42));

        List<AttributeKvEntry> serverAttributes = new ArrayList<>();
        serverAttributes.add(new BaseAttributeKvEntry(new DoubleDataEntry("doubleValue", 0.0), 42));
        serverAttributes.add(new BaseAttributeKvEntry(new BooleanDataEntry("booleanValue", false), 42));
        DeviceAttributes attributes = new DeviceAttributes(clientAttributes, serverAttributes, new ArrayList<>());

        Mockito.when(ruleCtx.getDeviceMetaData()).thenReturn(new DeviceMetaData(new DeviceId(UUID.randomUUID()), "A", "A", attributes));
        Assert.assertTrue(filter.filter(ruleCtx, null));
        filter.stop();
    }

}
