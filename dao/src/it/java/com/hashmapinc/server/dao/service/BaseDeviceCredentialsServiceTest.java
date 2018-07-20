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
package com.hashmapinc.server.dao.service;

import com.datastax.driver.core.utils.UUIDs;
import com.hashmapinc.server.common.data.Device;
import com.hashmapinc.server.common.data.id.DeviceId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.security.DeviceCredentials;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.hashmapinc.server.common.data.Tenant;
import com.hashmapinc.server.common.data.id.DeviceCredentialsId;
import com.hashmapinc.server.common.data.security.DeviceCredentialsType;
import com.hashmapinc.server.dao.exception.DataValidationException;

public abstract class BaseDeviceCredentialsServiceTest extends AbstractServiceTest {

    private TenantId tenantId;

    @Before
    public void before() {
        Tenant tenant = new Tenant();
        tenant.setTitle("My tenant");
        Tenant savedTenant = tenantService.saveTenant(tenant);
        Assert.assertNotNull(savedTenant);
        tenantId = savedTenant.getId();
    }

    @After
    public void after() {
        tenantService.deleteTenant(tenantId);
    }

    @Test(expected = DataValidationException.class)
    public void testCreateDeviceCredentials() {
        DeviceCredentials deviceCredentials = new DeviceCredentials();
        deviceCredentialsService.updateDeviceCredentials(deviceCredentials);
    }

    @Test(expected = DataValidationException.class)
    public void testSaveDeviceCredentialsWithEmptyDevice() {
        Device device = new Device();
        device.setName("My device");
        device.setType("default");
        device.setTenantId(tenantId);
        device = deviceService.saveDevice(device);
        DeviceCredentials deviceCredentials = deviceCredentialsService.findDeviceCredentialsByDeviceId(device.getId());
        deviceCredentials.setDeviceId(null);
        try {
            deviceCredentialsService.updateDeviceCredentials(deviceCredentials);
        } finally {
            deviceService.deleteDevice(device.getId());
        }
    }

    @Test(expected = DataValidationException.class)
    public void testSaveDeviceCredentialsWithEmptyCredentialsType() {
        Device device = new Device();
        device.setName("My device");
        device.setType("default");
        device.setTenantId(tenantId);
        device = deviceService.saveDevice(device);
        DeviceCredentials deviceCredentials = deviceCredentialsService.findDeviceCredentialsByDeviceId(device.getId());
        deviceCredentials.setCredentialsType(null);
        try {
            deviceCredentialsService.updateDeviceCredentials(deviceCredentials);
        } finally {
            deviceService.deleteDevice(device.getId());
        }
    }

    @Test(expected = DataValidationException.class)
    public void testSaveDeviceCredentialsWithEmptyCredentialsId() {
        Device device = new Device();
        device.setName("My device");
        device.setType("default");
        device.setTenantId(tenantId);
        device = deviceService.saveDevice(device);
        DeviceCredentials deviceCredentials = deviceCredentialsService.findDeviceCredentialsByDeviceId(device.getId());
        deviceCredentials.setCredentialsId(null);
        try {
            deviceCredentialsService.updateDeviceCredentials(deviceCredentials);
        } finally {
            deviceService.deleteDevice(device.getId());
        }
    }

    @Test(expected = DataValidationException.class)
    public void testSaveNonExistentDeviceCredentials() {
        Device device = new Device();
        device.setName("My device");
        device.setType("default");
        device.setTenantId(tenantId);
        device = deviceService.saveDevice(device);
        DeviceCredentials deviceCredentials = deviceCredentialsService.findDeviceCredentialsByDeviceId(device.getId());
        DeviceCredentials newDeviceCredentials = new DeviceCredentials(new DeviceCredentialsId(UUIDs.timeBased()));
        newDeviceCredentials.setCreatedTime(deviceCredentials.getCreatedTime());
        newDeviceCredentials.setDeviceId(deviceCredentials.getDeviceId());
        newDeviceCredentials.setCredentialsType(deviceCredentials.getCredentialsType());
        newDeviceCredentials.setCredentialsId(deviceCredentials.getCredentialsId());
        try {
            deviceCredentialsService.updateDeviceCredentials(newDeviceCredentials);
        } finally {
            deviceService.deleteDevice(device.getId());
        }
    }

    @Test(expected = DataValidationException.class)
    public void testSaveDeviceCredentialsWithNonExistentDevice() {
        Device device = new Device();
        device.setName("My device");
        device.setType("default");
        device.setTenantId(tenantId);
        device = deviceService.saveDevice(device);
        DeviceCredentials deviceCredentials = deviceCredentialsService.findDeviceCredentialsByDeviceId(device.getId());
        deviceCredentials.setDeviceId(new DeviceId(UUIDs.timeBased()));
        try {
            deviceCredentialsService.updateDeviceCredentials(deviceCredentials);
        } finally {
            deviceService.deleteDevice(device.getId());
        }
    }

    @Test
    public void testFindDeviceCredentialsByDeviceId() {
        Device device = new Device();
        device.setTenantId(tenantId);
        device.setName("My device");
        device.setType("default");
        Device savedDevice = deviceService.saveDevice(device);
        DeviceCredentials deviceCredentials = deviceCredentialsService.findDeviceCredentialsByDeviceId(savedDevice.getId());
        Assert.assertEquals(savedDevice.getId(), deviceCredentials.getDeviceId());
        deviceService.deleteDevice(savedDevice.getId());
        deviceCredentials = deviceCredentialsService.findDeviceCredentialsByDeviceId(savedDevice.getId());
        Assert.assertNull(deviceCredentials);
    }

    @Test
    public void testFindDeviceCredentialsByCredentialsId() {
        Device device = new Device();
        device.setTenantId(tenantId);
        device.setName("My device");
        device.setType("default");
        Device savedDevice = deviceService.saveDevice(device);
        DeviceCredentials deviceCredentials = deviceCredentialsService.findDeviceCredentialsByDeviceId(savedDevice.getId());
        Assert.assertEquals(savedDevice.getId(), deviceCredentials.getDeviceId());
        DeviceCredentials foundDeviceCredentials = deviceCredentialsService.findDeviceCredentialsByCredentialsId(deviceCredentials.getCredentialsId());
        Assert.assertEquals(deviceCredentials, foundDeviceCredentials);
        deviceService.deleteDevice(savedDevice.getId());
        foundDeviceCredentials = deviceCredentialsService.findDeviceCredentialsByCredentialsId(deviceCredentials.getCredentialsId());
        Assert.assertNull(foundDeviceCredentials);
    }

    @Test
    public void testSaveDeviceCredentials() {
        Device device = new Device();
        device.setTenantId(tenantId);
        device.setName("My device");
        device.setType("default");
        Device savedDevice = deviceService.saveDevice(device);
        DeviceCredentials deviceCredentials = deviceCredentialsService.findDeviceCredentialsByDeviceId(savedDevice.getId());
        Assert.assertEquals(savedDevice.getId(), deviceCredentials.getDeviceId());
        deviceCredentials.setCredentialsType(DeviceCredentialsType.ACCESS_TOKEN);
        deviceCredentials.setCredentialsId("access_token");
        deviceCredentialsService.updateDeviceCredentials(deviceCredentials);
        DeviceCredentials foundDeviceCredentials = deviceCredentialsService.findDeviceCredentialsByDeviceId(savedDevice.getId());
        Assert.assertEquals(deviceCredentials, foundDeviceCredentials);
        deviceService.deleteDevice(savedDevice.getId());
    }
}

