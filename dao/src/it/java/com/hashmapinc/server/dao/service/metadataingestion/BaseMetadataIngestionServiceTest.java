/**
 * Copyright © 2016-2018 The Thingsboard Authors
 * Modifications © 2017-2018 Hashmap, Inc
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hashmapinc.server.dao.service.metadataingestion;

import com.datastax.driver.core.utils.UUIDs;
import com.hashmapinc.server.common.data.MetadataIngestionEntries;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.kv.MetaDataKvEntry;
import com.hashmapinc.server.common.data.kv.StringDataEntry;
import com.hashmapinc.server.common.data.metadata.MetadataConfigId;
import com.hashmapinc.server.dao.exception.IncorrectParameterException;
import com.hashmapinc.server.dao.service.AbstractServiceTest;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.List;

public abstract class BaseMetadataIngestionServiceTest extends AbstractServiceTest {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void ingestAndFindMetadataEntries() throws Exception {
        MetadataConfigId metadataConfigId = new MetadataConfigId(UUIDs.timeBased());
        MetaDataKvEntry metaDataKvEntry1 = new MetaDataKvEntry(new StringDataEntry("Key 1", "Value 1"), DateTime.now().getMillis());
        MetaDataKvEntry metaDataKvEntry2 = new MetaDataKvEntry(new StringDataEntry("Key 2", "Value 2"), DateTime.now().getMillis());
        MetadataIngestionEntries metadataEntries = MetadataIngestionEntries.builder()
                .tenantId(new TenantId(UUIDs.timeBased()))
                .metadataConfigId(metadataConfigId)
                .metadataSourceName("Metadata Source 1")
                .metaDataKvEntries(Arrays.asList(metaDataKvEntry1, metaDataKvEntry2))
                .build();

        metadataIngestionService.save(metadataEntries);

        Thread.sleep(100); //NOSONAR

        List<MetaDataKvEntry> allEntries = metadataIngestionService.findAll(metadataConfigId).get();

        Assert.assertNotNull(allEntries);
        Assert.assertEquals(2, allEntries.size());
        Assert.assertEquals(metaDataKvEntry1, allEntries.get(0));
        Assert.assertEquals(metaDataKvEntry2, allEntries.get(1));
    }

    @Test
    public void testSaveMetadataEntriesWithIncorrectTenant() {
        expectedEx.expect(IncorrectParameterException.class);
        expectedEx.expectMessage("Incorrect tenand id null");

        MetadataIngestionEntries metadataEntries = MetadataIngestionEntries.builder()
                .metadataConfigId(new MetadataConfigId(UUIDs.timeBased()))
                .metadataSourceName("Metadata Source 1")
                .metaDataKvEntries(Arrays.asList(new MetaDataKvEntry(new StringDataEntry("Key 1", "Value 1"), DateTime.now().getMillis())))
                .build();

        metadataIngestionService.save(metadataEntries);
    }

    @Test
    public void testSaveMetadataEntriesWithIncorrectConfig() {
        expectedEx.expect(IncorrectParameterException.class);
        expectedEx.expectMessage("Incorrect metadata config id null");

        MetadataIngestionEntries metadataEntries = MetadataIngestionEntries.builder()
                .tenantId(new TenantId(UUIDs.timeBased()))
                .metadataSourceName("Metadata Source 1")
                .metaDataKvEntries(Arrays.asList(new MetaDataKvEntry(new StringDataEntry("Key 1", "Value 1"), DateTime.now().getMillis())))
                .build();

        metadataIngestionService.save(metadataEntries);
    }

    @Test
    public void testSaveMetadataEntriesWithIncorrectSource() {
        expectedEx.expect(IncorrectParameterException.class);
        expectedEx.expectMessage("Incorrect data source name. Value can't be empty");

        MetadataIngestionEntries metadataEntries = MetadataIngestionEntries.builder()
                .tenantId(new TenantId(UUIDs.timeBased()))
                .metadataConfigId(new MetadataConfigId(UUIDs.timeBased()))
                .metaDataKvEntries(Arrays.asList(new MetaDataKvEntry(new StringDataEntry("Key 1", "Value 1"), DateTime.now().getMillis())))
                .build();

        metadataIngestionService.save(metadataEntries);
    }

    @Test
    public void testSaveMetadataEntriesWithNoMetadataEntry() {
        expectedEx.expect(IncorrectParameterException.class);
        expectedEx.expectMessage("Metadata Entry can't be null");

        MetadataIngestionEntries metadataEntries = MetadataIngestionEntries.builder()
                .tenantId(new TenantId(UUIDs.timeBased()))
                .metadataConfigId(new MetadataConfigId(UUIDs.timeBased()))
                .metadataSourceName("Metadata Source 1")
                .build();

        metadataIngestionService.save(metadataEntries);
    }

    @Test
    public void testSaveMetadataEntriesWithInvalidMetadataKey() {
        expectedEx.expect(IncorrectParameterException.class);
        expectedEx.expectMessage("Incorrect kvEntry. Key can't be empty");

        MetadataIngestionEntries metadataEntries = MetadataIngestionEntries.builder()
                .tenantId(new TenantId(UUIDs.timeBased()))
                .metadataConfigId(new MetadataConfigId(UUIDs.timeBased()))
                .metadataSourceName("Metadata Source 1")
                .metaDataKvEntries(Arrays.asList(new MetaDataKvEntry(new StringDataEntry("", "Value 1"), DateTime.now().getMillis())))
                .build();

        metadataIngestionService.save(metadataEntries);
    }

    @Test
    public void testSaveMetadataEntriesWithInvalidMetadataValue() {
        expectedEx.expect(IncorrectParameterException.class);
        expectedEx.expectMessage("Incorrect kvEntry. Value can't be empty");

        MetadataIngestionEntries metadataEntries = MetadataIngestionEntries.builder()
                .tenantId(new TenantId(UUIDs.timeBased()))
                .metadataConfigId(new MetadataConfigId(UUIDs.timeBased()))
                .metadataSourceName("Metadata Source 1")
                .metaDataKvEntries(Arrays.asList(new MetaDataKvEntry(new StringDataEntry("Key 1", ""), DateTime.now().getMillis())))
                .build();

        metadataIngestionService.save(metadataEntries);
    }

    @Test
    public void testSaveMetadataEntriesWithInvalidMetadataTs() {
        expectedEx.expect(IncorrectParameterException.class);
        expectedEx.expectMessage("Incorrect last update ts. Ts should be positive");

        MetadataIngestionEntries metadataEntries = MetadataIngestionEntries.builder()
                .tenantId(new TenantId(UUIDs.timeBased()))
                .metadataConfigId(new MetadataConfigId(UUIDs.timeBased()))
                .metadataSourceName("Metadata Source 1")
                .metaDataKvEntries(Arrays.asList(new MetaDataKvEntry(new StringDataEntry("Key 1", "Value 1"), -1L)))
                .build();

        metadataIngestionService.save(metadataEntries);
    }

    @Test
    public void testFindMetadataEntriesWithInvalidConfig() {
        expectedEx.expect(IncorrectParameterException.class);
        expectedEx.expectMessage("Incorrect metadata config id null");

        metadataIngestionService.findAll(null);
    }
}