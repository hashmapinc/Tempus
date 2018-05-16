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
package com.hashmapinc.server.dao.service.tagmetadata;

import com.datastax.driver.core.utils.UUIDs;
import com.hashmapinc.server.common.data.EntityType;
import com.hashmapinc.server.common.data.TagMetaData;
import com.hashmapinc.server.common.data.id.DeviceId;
import com.hashmapinc.server.dao.service.AbstractServiceTest;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.Random;

public abstract class BaseTagMetaDataServiceTest extends AbstractServiceTest{
    private final String source = "Well Wore";
    private final EntityType entityType = EntityType.DEVICE;
    private final String unit = "mm";
    private final DeviceId deviceId = new DeviceId(UUIDs.timeBased());

    @Test
    public void createTagMetaDataTest() throws Exception{
        String key = "tag";
        TagMetaData tagMetaData = createTagMetaData(key);
        tagMetaDataService.saveTagMetaData(tagMetaData);
        Thread.sleep(2000);
        TagMetaData result = tagMetaDataService.getTagMetaDataByEntityIdAndKey(deviceId, key).get();
        assertNotNull(result);
        assertEquals(key, result.getKey());
    }

    @Test
    public void getAllTagMetaDataByEntityIdTest() throws Exception{
        String key = "tag1";
        TagMetaData tagMetaData = createTagMetaData(key);
        tagMetaDataService.saveTagMetaData(tagMetaData);

        key = "tag2";
        tagMetaData = createTagMetaData(key);
        tagMetaDataService.saveTagMetaData(tagMetaData);

        key = "tag3";
        tagMetaData = createTagMetaData(key);
        tagMetaDataService.saveTagMetaData(tagMetaData);

        Thread.sleep(4000);

        List<TagMetaData> tagMetaDataList = tagMetaDataService.getAllTagMetaDataByEntityId(deviceId).get();
        assertNotNull(tagMetaDataList);
        assertEquals(3, tagMetaDataList.size());
    }

    private TagMetaData createTagMetaData(String key){

        TagMetaData tagMetaData = new TagMetaData();
        tagMetaData.setEntityId(deviceId.getId().toString());
        tagMetaData.setEntityType(entityType);
        tagMetaData.setSource(source);
        tagMetaData.setUnit(unit);
        tagMetaData.setKey(key);

        Random random = new Random();

        tagMetaData.setMinFrequency(random.nextDouble());
        tagMetaData.setMaxFrequency(random.nextDouble() + 1);
        tagMetaData.setMedianFrequency(random.nextDouble());
        tagMetaData.setAvgFrequency(random.nextDouble());
        tagMetaData.setMeanFrequency(tagMetaData.getAvgFrequency());

        return tagMetaData;
    }
}
