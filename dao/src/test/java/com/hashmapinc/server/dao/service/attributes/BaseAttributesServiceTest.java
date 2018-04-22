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
package com.hashmapinc.server.dao.service.attributes;

import com.datastax.driver.core.utils.UUIDs;
import com.hashmapinc.server.dao.attributes.AttributesService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.hashmapinc.server.common.data.DataConstants;
import com.hashmapinc.server.common.data.id.DeviceId;
import com.hashmapinc.server.common.data.kv.AttributeKvEntry;
import com.hashmapinc.server.common.data.kv.BaseAttributeKvEntry;
import com.hashmapinc.server.common.data.kv.KvEntry;
import com.hashmapinc.server.common.data.kv.StringDataEntry;
import com.hashmapinc.server.dao.service.AbstractServiceTest;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author Andrew Shvayka
 */
public abstract class BaseAttributesServiceTest extends AbstractServiceTest {

    @Autowired
    private AttributesService attributesService;

    @Before
    public void before() {
    }

    @Test
    public void saveAndFetch() throws Exception {
        DeviceId deviceId = new DeviceId(UUIDs.timeBased());
        KvEntry attrValue = new StringDataEntry("attribute1", "value1");
        AttributeKvEntry attr = new BaseAttributeKvEntry(attrValue, 42L);
        attributesService.save(deviceId, DataConstants.CLIENT_SCOPE, Collections.singletonList(attr)).get();
        Optional<AttributeKvEntry> saved = attributesService.find(deviceId, DataConstants.CLIENT_SCOPE, attr.getKey()).get();
        Assert.assertTrue(saved.isPresent());
        Assert.assertEquals(attr, saved.get());
    }

    @Test
    public void saveMultipleTypeAndFetch() throws Exception {
        DeviceId deviceId = new DeviceId(UUIDs.timeBased());
        KvEntry attrOldValue = new StringDataEntry("attribute1", "value1");
        AttributeKvEntry attrOld = new BaseAttributeKvEntry(attrOldValue, 42L);

        attributesService.save(deviceId, DataConstants.CLIENT_SCOPE, Collections.singletonList(attrOld)).get();
        Optional<AttributeKvEntry> saved = attributesService.find(deviceId, DataConstants.CLIENT_SCOPE, attrOld.getKey()).get();

        Assert.assertTrue(saved.isPresent());
        Assert.assertEquals(attrOld, saved.get());

        KvEntry attrNewValue = new StringDataEntry("attribute1", "value2");
        AttributeKvEntry attrNew = new BaseAttributeKvEntry(attrNewValue, 73L);
        attributesService.save(deviceId, DataConstants.CLIENT_SCOPE, Collections.singletonList(attrNew)).get();

        saved = attributesService.find(deviceId, DataConstants.CLIENT_SCOPE, attrOld.getKey()).get();
        Assert.assertEquals(attrNew, saved.get());
    }

    @Test
    public void findAll() throws Exception {
        DeviceId deviceId = new DeviceId(UUIDs.timeBased());

        KvEntry attrAOldValue = new StringDataEntry("A", "value1");
        AttributeKvEntry attrAOld = new BaseAttributeKvEntry(attrAOldValue, 42L);
        KvEntry attrANewValue = new StringDataEntry("A", "value2");
        AttributeKvEntry attrANew = new BaseAttributeKvEntry(attrANewValue, 73L);
        KvEntry attrBNewValue = new StringDataEntry("B", "value3");
        AttributeKvEntry attrBNew = new BaseAttributeKvEntry(attrBNewValue, 73L);

        attributesService.save(deviceId, DataConstants.CLIENT_SCOPE, Collections.singletonList(attrAOld)).get();
        attributesService.save(deviceId, DataConstants.CLIENT_SCOPE, Collections.singletonList(attrANew)).get();
        attributesService.save(deviceId, DataConstants.CLIENT_SCOPE, Collections.singletonList(attrBNew)).get();

        List<AttributeKvEntry> saved = attributesService.findAll(deviceId, DataConstants.CLIENT_SCOPE).get();

        Assert.assertNotNull(saved);
        Assert.assertEquals(2, saved.size());

        Assert.assertEquals(attrANew, saved.get(0));
        Assert.assertEquals(attrBNew, saved.get(1));
    }

}
