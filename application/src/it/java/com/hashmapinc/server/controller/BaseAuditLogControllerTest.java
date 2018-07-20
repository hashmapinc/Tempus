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
package com.hashmapinc.server.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hashmapinc.server.common.data.Device;
import com.hashmapinc.server.common.data.audit.AuditLog;
import com.hashmapinc.server.common.data.page.TimePageData;
import com.hashmapinc.server.common.data.page.TimePageLink;
import com.hashmapinc.server.dao.model.ModelConstants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseAuditLogControllerTest extends AbstractControllerTest {

    @Before
    public void beforeTest() throws Exception {
        loginTenantAdmin();
    }

    @Test
    public void testAuditLogs() throws Exception {
        for (int i = 0; i < 78; i++) {
            Device device = new Device();
            device.setName("Device" + i);
            device.setType("default");
            doPost("/api/device", device, Device.class);
        }

        List<AuditLog> loadedAuditLogs = new ArrayList<>();
        TimePageLink pageLink = new TimePageLink(23);
        TimePageData<AuditLog> pageData;
        do {
            pageData = doGetTypedWithTimePageLink("/api/audit/logs?",
                    new TypeReference<TimePageData<AuditLog>>() {
                    }, pageLink);
            loadedAuditLogs.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());

        Assert.assertEquals(80, loadedAuditLogs.size());

        loadedAuditLogs = new ArrayList<>();
        pageLink = new TimePageLink(23);
        do {
            pageData = doGetTypedWithTimePageLink("/api/audit/logs/customer/" + ModelConstants.NULL_UUID + "?",
                    new TypeReference<TimePageData<AuditLog>>() {
                    }, pageLink);
            loadedAuditLogs.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());

        Assert.assertEquals(78, loadedAuditLogs.size());

        loadedAuditLogs = new ArrayList<>();
        pageLink = new TimePageLink(23);
        do {
            pageData = doGetTypedWithTimePageLink("/api/audit/logs/user/" + tenantAdmin.getId().getId().toString() + "?",
                    new TypeReference<TimePageData<AuditLog>>() {
                    }, pageLink);
            loadedAuditLogs.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());

        Assert.assertEquals(80, loadedAuditLogs.size());
    }

    @Test
    public void testAuditLogs_byTenantIdAndEntityId() throws Exception {
        Device device = new Device();
        device.setName("Device name");
        device.setType("default");
        Device savedDevice = doPost("/api/device", device, Device.class);
        for (int i = 0; i < 178; i++) {
            savedDevice.setName("Device name" + i);
            doPost("/api/device", savedDevice, Device.class);
        }

        List<AuditLog> loadedAuditLogs = new ArrayList<>();
        TimePageLink pageLink = new TimePageLink(23);
        TimePageData<AuditLog> pageData;
        do {
            pageData = doGetTypedWithTimePageLink("/api/audit/logs/entity/DEVICE/" + savedDevice.getId().getId() + "?",
                    new TypeReference<TimePageData<AuditLog>>() {
                    }, pageLink);
            loadedAuditLogs.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());

        Assert.assertEquals(179, loadedAuditLogs.size());
    }
}
