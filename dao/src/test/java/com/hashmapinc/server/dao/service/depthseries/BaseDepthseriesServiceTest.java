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
package com.hashmapinc.server.dao.service.depthseries;

import com.datastax.driver.core.utils.UUIDs;
import com.google.gson.JsonParser;
import com.hashmapinc.server.common.data.id.DeviceId;
import com.hashmapinc.server.common.data.kv.*;
import com.hashmapinc.server.dao.service.AbstractServiceTest;
import org.junit.Assert;
import org.junit.Test;
import com.hashmapinc.server.common.data.kv.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class BaseDepthseriesServiceTest extends AbstractServiceTest {

    private static final String STRING_KEY = "stringKey";
    private static final String LONG_KEY = "longKey";
    private static final String DOUBLE_KEY = "doubleKey";
    private static final String BOOLEAN_KEY = "booleanKey";
    private static final String JSON_KEY = "jsonKey";

    private static final Double DS = 4200D;

    JsonParser parser = new JsonParser();
    KvEntry stringKvEntry = new StringDataEntry(STRING_KEY, "value");
    KvEntry longKvEntry = new LongDataEntry(LONG_KEY, Long.MAX_VALUE);
    KvEntry doubleKvEntry = new DoubleDataEntry(DOUBLE_KEY, Double.MAX_VALUE);
    KvEntry booleanKvEntry = new BooleanDataEntry(BOOLEAN_KEY, Boolean.TRUE);
    KvEntry jsonKvEntry = new JsonDataEntry(JSON_KEY, parser.parse("{\"tag\": \"value\"}").getAsJsonObject());

    @Test
    public void testFindAllLatest() throws Exception {
        DeviceId deviceId = new DeviceId(UUIDs.timeBased());

        saveEntries(deviceId, DS - 200D);
        saveEntries(deviceId, DS - 100D);
        saveEntries(deviceId, DS);

        List<DsKvEntry> dsList = dsService.findAllLatest(deviceId).get();

        assertNotNull(dsList);
        assertEquals(5, dsList.size());
        for (int i = 0; i < dsList.size(); i++) {
            assertEquals(DS, dsList.get(i).getDs());
        }

        Collections.sort(dsList, (o1, o2) -> o1.getKey().compareTo(o2.getKey()));

        List<DsKvEntry> expected = new ArrayList<DsKvEntry>();
        expected.add(toDsEntry(DS, stringKvEntry));
        expected.add(toDsEntry(DS, longKvEntry));
        expected.add(toDsEntry(DS, doubleKvEntry));
        expected.add(toDsEntry(DS, booleanKvEntry));
        expected.add(toDsEntry(DS, jsonKvEntry));
        Collections.sort(expected, (o1, o2) -> o1.getKey().compareTo(o2.getKey()));

        assertEquals(expected.toString().trim(), dsList.toString().trim());
    }

    @Test
    public void testFindLatest() throws Exception {
        DeviceId deviceId = new DeviceId(UUIDs.timeBased());

        saveEntries(deviceId, DS - 200D);
        saveEntries(deviceId, DS - 100D);
        saveEntries(deviceId, DS);

        List<DsKvEntry> entries = dsService.findLatest(deviceId, Collections.singleton(STRING_KEY)).get();
        Assert.assertEquals(1, entries.size());
        Assert.assertEquals(toDsEntry(DS, stringKvEntry).toString().trim(),entries.get(0).toString().trim());
    }

    @Test
    public void testFindDeviceDsData() throws Exception {
        DeviceId deviceId = new DeviceId(UUIDs.timeBased());
        List<DsKvEntry> entries = new ArrayList<>();

        entries.add(save(deviceId, 5000D, 100));
        entries.add(save(deviceId, 15000D, 200));

        entries.add(save(deviceId, 25000D, 300));
        entries.add(save(deviceId, 35000D, 400));

        entries.add(save(deviceId, 45000D, 500));
        entries.add(save(deviceId, 55000D, 600));

        List<DsKvEntry> list = dsService.findAll(deviceId, Collections.singletonList(new BaseDsKvQuery(LONG_KEY, 0D,
                60000D, 20000D, 3, DepthAggregation.NONE))).get();
        assertEquals(3, list.size());
        assertEquals(new Double(55000), list.get(0).getDs());
        assertEquals(java.util.Optional.of(600L), list.get(0).getLongValue());

        assertEquals(new Double(45000), list.get(1).getDs());
        assertEquals(java.util.Optional.of(500L), list.get(1).getLongValue());

        assertEquals(new Double(35000), list.get(2).getDs());
        assertEquals(java.util.Optional.of(400L), list.get(2).getLongValue());

        list = dsService.findAll(deviceId, Collections.singletonList(new BaseDsKvQuery(LONG_KEY, 0D,
                60000D, 20000D, 3, DepthAggregation.AVG))).get();
        assertEquals(3, list.size());
        assertEquals(new Double(10000), list.get(0).getDs());
        assertEquals(java.util.Optional.of(150L), list.get(0).getLongValue());

        assertEquals(new Double(30000), list.get(1).getDs());
        assertEquals(java.util.Optional.of(350L), list.get(1).getLongValue());

        assertEquals(new Double(50000), list.get(2).getDs());
        assertEquals(java.util.Optional.of(550L), list.get(2).getLongValue());

        list = dsService.findAll(deviceId, Collections.singletonList(new BaseDsKvQuery(LONG_KEY, 0D,
                60000D, 20000D, 3, DepthAggregation.SUM))).get();

        assertEquals(3, list.size());
        assertEquals(new Double(10000), list.get(0).getDs());
        assertEquals(java.util.Optional.of(300L), list.get(0).getLongValue());

        assertEquals(new Double(30000), list.get(1).getDs());
        assertEquals(java.util.Optional.of(700L), list.get(1).getLongValue());

        assertEquals(new Double(50000), list.get(2).getDs());
        assertEquals(java.util.Optional.of(1100L), list.get(2).getLongValue());

        list = dsService.findAll(deviceId, Collections.singletonList(new BaseDsKvQuery(LONG_KEY, 0D,
                60000D, 20000D, 3, DepthAggregation.MIN))).get();

        assertEquals(3, list.size());
        assertEquals(new Double(10000), list.get(0).getDs());
        assertEquals(java.util.Optional.of(100L), list.get(0).getLongValue());

        assertEquals(new Double(30000), list.get(1).getDs());
        assertEquals(java.util.Optional.of(300L), list.get(1).getLongValue());

        assertEquals(new Double(50000), list.get(2).getDs());
        assertEquals(java.util.Optional.of(500L), list.get(2).getLongValue());

        list = dsService.findAll(deviceId, Collections.singletonList(new BaseDsKvQuery(LONG_KEY, 0D,
                60000D, 20000D, 3, DepthAggregation.AVG.MAX))).get();

        assertEquals(3, list.size());
        assertEquals(new Double(10000), list.get(0).getDs());
        assertEquals(java.util.Optional.of(200L), list.get(0).getLongValue());

        assertEquals(new Double(30000), list.get(1).getDs());
        assertEquals(java.util.Optional.of(400L), list.get(1).getLongValue());

        assertEquals(new Double(50000), list.get(2).getDs());
        assertEquals(java.util.Optional.of(600L), list.get(2).getLongValue());

        list = dsService.findAll(deviceId, Collections.singletonList(new BaseDsKvQuery(LONG_KEY, 0D,
                60000D, 20000D, 3, DepthAggregation.AVG.COUNT))).get();

        assertEquals(3, list.size());
        assertEquals(new Double(10000), list.get(0).getDs());
        assertEquals(java.util.Optional.of(2L), list.get(0).getLongValue());

        assertEquals(new Double(30000), list.get(1).getDs());
        assertEquals(java.util.Optional.of(2L), list.get(1).getLongValue());

        assertEquals(new Double(50000), list.get(2).getDs());
        assertEquals(java.util.Optional.of(2L), list.get(2).getLongValue());
    }

    private DsKvEntry save(DeviceId deviceId, Double ds, long value) throws Exception {
        DsKvEntry entry = new BasicDsKvEntry(ds, new LongDataEntry(LONG_KEY, value));
        dsService.save(deviceId, entry).get();
        return entry;
    }

    private void saveEntries(DeviceId deviceId, Double ds) throws ExecutionException, InterruptedException {
        dsService.save(deviceId, toDsEntry(ds, stringKvEntry)).get();
        dsService.save(deviceId, toDsEntry(ds, longKvEntry)).get();
        dsService.save(deviceId, toDsEntry(ds, doubleKvEntry)).get();
        dsService.save(deviceId, toDsEntry(ds, booleanKvEntry)).get();
        dsService.save(deviceId, toDsEntry(ds, jsonKvEntry)).get();
    }

    private static DsKvEntry toDsEntry(Double ds, KvEntry entry) {
        return new BasicDsKvEntry(ds, entry);
    }


}
