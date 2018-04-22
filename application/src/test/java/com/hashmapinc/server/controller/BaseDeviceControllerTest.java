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

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.gson.JsonParser;
import com.datastax.driver.core.utils.UUIDs;
import com.hashmapinc.server.common.data.*;
import com.hashmapinc.server.common.data.id.DeviceId;
import com.hashmapinc.server.common.data.kv.*;
import com.hashmapinc.server.common.data.page.TextPageData;
import com.hashmapinc.server.common.data.page.TextPageLink;
import com.hashmapinc.server.common.data.security.Authority;
import com.hashmapinc.server.common.data.security.DeviceCredentials;
import com.hashmapinc.server.dao.attributes.AttributesService;
import com.hashmapinc.server.dao.depthSeries.DepthSeriesService;
import com.hashmapinc.server.dao.model.ModelConstants;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;
import com.hashmapinc.server.common.data.*;
import com.hashmapinc.server.common.data.id.CustomerId;
import com.hashmapinc.server.common.data.id.DeviceCredentialsId;
import com.hashmapinc.server.common.data.kv.*;
import com.hashmapinc.server.common.data.security.DeviceCredentialsType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hashmapinc.server.dao.timeseries.TimeseriesService;

public abstract class BaseDeviceControllerTest extends AbstractControllerTest {
    
    private IdComparator<Device> idComparator = new IdComparator<>();
    
    private Tenant savedTenant;
    private User tenantAdmin;


    private String STRING_KEY_FOR_TS_DS_OR_AS = "stringKey";
    private String LONG_KEY_FOR_TS_DS_OR_AS = "longKey";
    private String DOUBLE_KEY_FOR_TS_DS_OR_AS = "doubleKey";
    private String BOOLEAN_KEY_FOR_TS_DS_OR_AS = "booleanKey";
    private String JSON_KEY_FOR_TS_DS_OR_AS = "jsonKey";

    private JsonParser parser = new JsonParser();
    private KvEntry stringKvEntry = new StringDataEntry(STRING_KEY_FOR_TS_DS_OR_AS, "value");
    private KvEntry longKvEntry = new LongDataEntry(LONG_KEY_FOR_TS_DS_OR_AS, Long.MAX_VALUE);
    private KvEntry doubleKvEntry = new DoubleDataEntry(DOUBLE_KEY_FOR_TS_DS_OR_AS, Double.MAX_VALUE);
    private KvEntry booleanKvEntry = new BooleanDataEntry(BOOLEAN_KEY_FOR_TS_DS_OR_AS, Boolean.TRUE);
    private KvEntry jsonKvEntry = new JsonDataEntry(JSON_KEY_FOR_TS_DS_OR_AS, parser.parse("{\"tag\": \"value\"}").getAsJsonObject());


    @Autowired
    private TimeseriesService timeseriesService;

    @Autowired
    private DepthSeriesService depthSeriesService;

    @Autowired
    private AttributesService attributesService;

    @Before
    public void beforeTest() throws Exception {
        loginSysAdmin();
        
        Tenant tenant = new Tenant();
        tenant.setTitle("My tenant");
        savedTenant = doPost("/api/tenant", tenant, Tenant.class);
        Assert.assertNotNull(savedTenant);
        
        tenantAdmin = new User();
        tenantAdmin.setAuthority(Authority.TENANT_ADMIN);
        tenantAdmin.setTenantId(savedTenant.getId());
        tenantAdmin.setEmail("tenant2@tempus.org");
        tenantAdmin.setFirstName("Joe");
        tenantAdmin.setLastName("Downs");
        
        tenantAdmin = createUserAndLogin(tenantAdmin, "testPassword1");
    }
    
    @After
    public void afterTest() throws Exception {
        loginSysAdmin();
        
        doDelete("/api/tenant/"+savedTenant.getId().getId().toString())
        .andExpect(status().isOk());
    }
    
    @Test
    public void testSaveDevice() throws Exception {
        Device device = new Device();
        device.setName("My device");
        device.setType("default");
        Device savedDevice = doPost("/api/device", device, Device.class);
        
        Assert.assertNotNull(savedDevice);
        Assert.assertNotNull(savedDevice.getId());
        Assert.assertTrue(savedDevice.getCreatedTime() > 0);
        Assert.assertEquals(savedTenant.getId(), savedDevice.getTenantId());
        Assert.assertNotNull(savedDevice.getCustomerId());
        Assert.assertEquals(ModelConstants.NULL_UUID, savedDevice.getCustomerId().getId());
        Assert.assertEquals(device.getName(), savedDevice.getName());
        
        DeviceCredentials deviceCredentials =
                doGet("/api/device/" + savedDevice.getId().getId().toString() + "/credentials", DeviceCredentials.class); 

        Assert.assertNotNull(deviceCredentials);
        Assert.assertNotNull(deviceCredentials.getId());
        Assert.assertEquals(savedDevice.getId(), deviceCredentials.getDeviceId());
        Assert.assertEquals(DeviceCredentialsType.ACCESS_TOKEN, deviceCredentials.getCredentialsType());
        Assert.assertNotNull(deviceCredentials.getCredentialsId());
        Assert.assertEquals(20, deviceCredentials.getCredentialsId().length());
        
        savedDevice.setName("My new device");
        doPost("/api/device", savedDevice, Device.class);
        
        Device foundDevice = doGet("/api/device/" + savedDevice.getId().getId().toString(), Device.class);
        Assert.assertEquals(foundDevice.getName(), savedDevice.getName());
    }
    
    @Test
    public void testFindDeviceById() throws Exception {
        Device device = new Device();
        device.setName("My device");
        device.setType("default");
        Device savedDevice = doPost("/api/device", device, Device.class);
        Device foundDevice = doGet("/api/device/" + savedDevice.getId().getId().toString(), Device.class);
        Assert.assertNotNull(foundDevice);
        Assert.assertEquals(savedDevice, foundDevice);
    }

    @Test
    public void testFindDeviceTypesByTenantId() throws Exception {
        List<Device> devices = new ArrayList<>();
        for (int i=0;i<3;i++) {
            Device device = new Device();
            device.setName("My device B"+i);
            device.setType("typeB");
            devices.add(doPost("/api/device", device, Device.class));
        }
        for (int i=0;i<7;i++) {
            Device device = new Device();
            device.setName("My device C"+i);
            device.setType("typeC");
            devices.add(doPost("/api/device", device, Device.class));
        }
        for (int i=0;i<9;i++) {
            Device device = new Device();
            device.setName("My device A"+i);
            device.setType("typeA");
            devices.add(doPost("/api/device", device, Device.class));
        }
        List<EntitySubtype> deviceTypes = doGetTyped("/api/device/types",
                new TypeReference<List<EntitySubtype>>(){});

        Assert.assertNotNull(deviceTypes);
        Assert.assertEquals(3, deviceTypes.size());
        Assert.assertEquals("typeA", deviceTypes.get(0).getType());
        Assert.assertEquals("typeB", deviceTypes.get(1).getType());
        Assert.assertEquals("typeC", deviceTypes.get(2).getType());
    }
    
    @Test
    public void testDeleteDevice() throws Exception {
        Device device = new Device();
        device.setName("My device");
        device.setType("default");
        Device savedDevice = doPost("/api/device", device, Device.class);
        
        doDelete("/api/device/"+savedDevice.getId().getId().toString())
        .andExpect(status().isOk());

        doGet("/api/device/"+savedDevice.getId().getId().toString())
        .andExpect(status().isNotFound());
    }

    @Test
    public void testSaveDeviceWithEmptyType() throws Exception {
        Device device = new Device();
        device.setName("My device");
        doPost("/api/device", device)
                .andExpect(status().isBadRequest())
                .andExpect(statusReason(containsString("Device type should be specified")));
    }

    @Test
    public void testSaveDeviceWithEmptyName() throws Exception {
        Device device = new Device();
        device.setType("default");
        doPost("/api/device", device)
        .andExpect(status().isBadRequest())
        .andExpect(statusReason(containsString("Device name should be specified")));
    }
    
    @Test
    public void testAssignUnassignDeviceToCustomer() throws Exception {
        Device device = new Device();
        device.setName("My device");
        device.setType("default");
        Device savedDevice = doPost("/api/device", device, Device.class);
        
        Customer customer = new Customer();
        customer.setTitle("My customer");
        Customer savedCustomer = doPost("/api/customer", customer, Customer.class);
        
        Device assignedDevice = doPost("/api/customer/" + savedCustomer.getId().getId().toString() 
                + "/device/" + savedDevice.getId().getId().toString(), Device.class);
        Assert.assertEquals(savedCustomer.getId(), assignedDevice.getCustomerId());
        
        Device foundDevice = doGet("/api/device/" + savedDevice.getId().getId().toString(), Device.class);
        Assert.assertEquals(savedCustomer.getId(), foundDevice.getCustomerId());

        Device unassignedDevice = 
                doDelete("/api/customer/device/" + savedDevice.getId().getId().toString(), Device.class);
        Assert.assertEquals(ModelConstants.NULL_UUID, unassignedDevice.getCustomerId().getId());
        
        foundDevice = doGet("/api/device/" + savedDevice.getId().getId().toString(), Device.class);
        Assert.assertEquals(ModelConstants.NULL_UUID, foundDevice.getCustomerId().getId());
    }
    
    @Test
    public void testAssignDeviceToNonExistentCustomer() throws Exception {
        Device device = new Device();
        device.setName("My device");
        device.setType("default");
        Device savedDevice = doPost("/api/device", device, Device.class);
        
        doPost("/api/customer/" + UUIDs.timeBased().toString()
                + "/device/" + savedDevice.getId().getId().toString())
        .andExpect(status().isNotFound());
    }
    
    @Test
    public void testAssignDeviceToCustomerFromDifferentTenant() throws Exception {
        loginSysAdmin();
        
        Tenant tenant2 = new Tenant();
        tenant2.setTitle("Different tenant");
        Tenant savedTenant2 = doPost("/api/tenant", tenant2, Tenant.class);
        Assert.assertNotNull(savedTenant2);

        User tenantAdmin2 = new User();
        tenantAdmin2.setAuthority(Authority.TENANT_ADMIN);
        tenantAdmin2.setTenantId(savedTenant2.getId());
        tenantAdmin2.setEmail("tenant3@tempus.org");
        tenantAdmin2.setFirstName("Joe");
        tenantAdmin2.setLastName("Downs");
        
        tenantAdmin2 = createUserAndLogin(tenantAdmin2, "testPassword1");
        
        Customer customer = new Customer();
        customer.setTitle("Different customer");
        Customer savedCustomer = doPost("/api/customer", customer, Customer.class);

        login(tenantAdmin.getEmail(), "testPassword1");
        
        Device device = new Device();
        device.setName("My device");
        device.setType("default");
        Device savedDevice = doPost("/api/device", device, Device.class);
        
        doPost("/api/customer/" + savedCustomer.getId().getId().toString()
                + "/device/" + savedDevice.getId().getId().toString())
        .andExpect(status().isForbidden());
        
        loginSysAdmin();
        
        doDelete("/api/tenant/"+savedTenant2.getId().getId().toString())
        .andExpect(status().isOk());
    }
    
    @Test
    public void testFindDeviceCredentialsByDeviceId() throws Exception {
        Device device = new Device();
        device.setName("My device");
        device.setType("default");
        Device savedDevice = doPost("/api/device", device, Device.class);
        DeviceCredentials deviceCredentials = 
                doGet("/api/device/" + savedDevice.getId().getId().toString() + "/credentials", DeviceCredentials.class); 
        Assert.assertEquals(savedDevice.getId(), deviceCredentials.getDeviceId());
    }
    
    @Test
    public void testSaveDeviceCredentials() throws Exception {
        Device device = new Device();
        device.setName("My device");
        device.setType("default");
        Device savedDevice = doPost("/api/device", device, Device.class);
        DeviceCredentials deviceCredentials = 
                doGet("/api/device/" + savedDevice.getId().getId().toString() + "/credentials", DeviceCredentials.class); 
        Assert.assertEquals(savedDevice.getId(), deviceCredentials.getDeviceId());
        deviceCredentials.setCredentialsType(DeviceCredentialsType.ACCESS_TOKEN);
        deviceCredentials.setCredentialsId("access_token");
        doPost("/api/device/credentials", deviceCredentials)
        .andExpect(status().isOk());
        
        DeviceCredentials foundDeviceCredentials = 
                doGet("/api/device/" + savedDevice.getId().getId().toString() + "/credentials", DeviceCredentials.class);
        
        Assert.assertEquals(deviceCredentials, foundDeviceCredentials);
    }
    
    @Test
    public void testSaveDeviceCredentialsWithEmptyDevice() throws Exception {
        DeviceCredentials deviceCredentials = new DeviceCredentials();
        doPost("/api/device/credentials", deviceCredentials)
        .andExpect(status().isBadRequest());
    }
    
    @Test
    public void testSaveDeviceCredentialsWithEmptyCredentialsType() throws Exception {
        Device device = new Device();
        device.setName("My device");
        device.setType("default");
        Device savedDevice = doPost("/api/device", device, Device.class);
        DeviceCredentials deviceCredentials = 
                doGet("/api/device/" + savedDevice.getId().getId().toString() + "/credentials", DeviceCredentials.class);
        deviceCredentials.setCredentialsType(null);
        doPost("/api/device/credentials", deviceCredentials)
        .andExpect(status().isBadRequest())
        .andExpect(statusReason(containsString("Device credentials type should be specified")));
    }
    
    @Test
    public void testSaveDeviceCredentialsWithEmptyCredentialsId() throws Exception {
        Device device = new Device();
        device.setName("My device");
        device.setType("default");
        Device savedDevice = doPost("/api/device", device, Device.class);
        DeviceCredentials deviceCredentials = 
                doGet("/api/device/" + savedDevice.getId().getId().toString() + "/credentials", DeviceCredentials.class);
        deviceCredentials.setCredentialsId(null);
        doPost("/api/device/credentials", deviceCredentials)
        .andExpect(status().isBadRequest())
        .andExpect(statusReason(containsString("Device credentials id should be specified")));
    }
    
    @Test
    public void testSaveNonExistentDeviceCredentials() throws Exception {
        Device device = new Device();
        device.setName("My device");
        device.setType("default");
        Device savedDevice = doPost("/api/device", device, Device.class);
        DeviceCredentials deviceCredentials = 
                doGet("/api/device/" + savedDevice.getId().getId().toString() + "/credentials", DeviceCredentials.class);
        DeviceCredentials newDeviceCredentials = new DeviceCredentials(new DeviceCredentialsId(UUIDs.timeBased()));
        newDeviceCredentials.setCreatedTime(deviceCredentials.getCreatedTime());
        newDeviceCredentials.setDeviceId(deviceCredentials.getDeviceId());
        newDeviceCredentials.setCredentialsType(deviceCredentials.getCredentialsType());
        newDeviceCredentials.setCredentialsId(deviceCredentials.getCredentialsId());
        doPost("/api/device/credentials", newDeviceCredentials)
        .andExpect(status().isBadRequest())
        .andExpect(statusReason(containsString("Unable to update non-existent device credentials")));
    }
    
    @Test
    public void testSaveDeviceCredentialsWithNonExistentDevice() throws Exception {
        Device device = new Device();
        device.setName("My device");
        device.setType("default");
        Device savedDevice = doPost("/api/device", device, Device.class);
        DeviceCredentials deviceCredentials = 
                doGet("/api/device/" + savedDevice.getId().getId().toString() + "/credentials", DeviceCredentials.class);
        deviceCredentials.setDeviceId(new DeviceId(UUIDs.timeBased()));
        doPost("/api/device/credentials", deviceCredentials)
        .andExpect(status().isNotFound());
    }

    @Test
    public void testFindTenantDevices() throws Exception {
        List<Device> devices = new ArrayList<>();
        for (int i=0;i<178;i++) {
            Device device = new Device();
            device.setName("Device"+i);
            device.setType("default");
            devices.add(doPost("/api/device", device, Device.class));
        }
        List<Device> loadedDevices = new ArrayList<>();
        TextPageLink pageLink = new TextPageLink(23);
        TextPageData<Device> pageData = null;
        do {
            pageData = doGetTypedWithPageLink("/api/tenant/devices?", 
                    new TypeReference<TextPageData<Device>>(){}, pageLink);
            loadedDevices.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());
        
        Collections.sort(devices, idComparator);
        Collections.sort(loadedDevices, idComparator);
        
        Assert.assertEquals(devices, loadedDevices);
    }
    
    @Test
    public void testFindTenantDevicesByName() throws Exception {
        String title1 = "Device title 1";
        List<Device> devicesTitle1 = new ArrayList<>();
        for (int i=0;i<143;i++) {
            Device device = new Device();
            String suffix = RandomStringUtils.randomAlphanumeric(15);
            String name = title1+suffix;
            name = i % 2 == 0 ? name.toLowerCase() : name.toUpperCase();
            device.setName(name);
            device.setType("default");
            devicesTitle1.add(doPost("/api/device", device, Device.class));
        }
        String title2 = "Device title 2";
        List<Device> devicesTitle2 = new ArrayList<>();
        for (int i=0;i<75;i++) {
            Device device = new Device();
            String suffix = RandomStringUtils.randomAlphanumeric(15);
            String name = title2+suffix;
            name = i % 2 == 0 ? name.toLowerCase() : name.toUpperCase();
            device.setName(name);
            device.setType("default");
            devicesTitle2.add(doPost("/api/device", device, Device.class));
        }
        
        List<Device> loadedDevicesTitle1 = new ArrayList<>();
        TextPageLink pageLink = new TextPageLink(15, title1);
        TextPageData<Device> pageData = null;
        do {
            pageData = doGetTypedWithPageLink("/api/tenant/devices?", 
                    new TypeReference<TextPageData<Device>>(){}, pageLink);
            loadedDevicesTitle1.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());
        
        Collections.sort(devicesTitle1, idComparator);
        Collections.sort(loadedDevicesTitle1, idComparator);
        
        Assert.assertEquals(devicesTitle1, loadedDevicesTitle1);
        
        List<Device> loadedDevicesTitle2 = new ArrayList<>();
        pageLink = new TextPageLink(4, title2);
        do {
            pageData = doGetTypedWithPageLink("/api/tenant/devices?", 
                    new TypeReference<TextPageData<Device>>(){}, pageLink);
            loadedDevicesTitle2.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());

        Collections.sort(devicesTitle2, idComparator);
        Collections.sort(loadedDevicesTitle2, idComparator);
        
        Assert.assertEquals(devicesTitle2, loadedDevicesTitle2);
        
        for (Device device : loadedDevicesTitle1) {
            doDelete("/api/device/"+device.getId().getId().toString())
            .andExpect(status().isOk());
        }
        
        pageLink = new TextPageLink(4, title1);
        pageData = doGetTypedWithPageLink("/api/tenant/devices?", 
                new TypeReference<TextPageData<Device>>(){}, pageLink);
        Assert.assertFalse(pageData.hasNext());
        Assert.assertEquals(0, pageData.getData().size());
        
        for (Device device : loadedDevicesTitle2) {
            doDelete("/api/device/"+device.getId().getId().toString())
            .andExpect(status().isOk());
        }
        
        pageLink = new TextPageLink(4, title2);
        pageData = doGetTypedWithPageLink("/api/tenant/devices?", 
                new TypeReference<TextPageData<Device>>(){}, pageLink);
        Assert.assertFalse(pageData.hasNext());
        Assert.assertEquals(0, pageData.getData().size());
    }

    @Test
    public void testFindTenantDevicesByType() throws Exception {
        String title1 = "Device title 1";
        String type1 = "typeA";
        List<Device> devicesType1 = new ArrayList<>();
        for (int i=0;i<143;i++) {
            Device device = new Device();
            String suffix = RandomStringUtils.randomAlphanumeric(15);
            String name = title1+suffix;
            name = i % 2 == 0 ? name.toLowerCase() : name.toUpperCase();
            device.setName(name);
            device.setType(type1);
            devicesType1.add(doPost("/api/device", device, Device.class));
        }
        String title2 = "Device title 2";
        String type2 = "typeB";
        List<Device> devicesType2 = new ArrayList<>();
        for (int i=0;i<75;i++) {
            Device device = new Device();
            String suffix = RandomStringUtils.randomAlphanumeric(15);
            String name = title2+suffix;
            name = i % 2 == 0 ? name.toLowerCase() : name.toUpperCase();
            device.setName(name);
            device.setType(type2);
            devicesType2.add(doPost("/api/device", device, Device.class));
        }

        List<Device> loadedDevicesType1 = new ArrayList<>();
        TextPageLink pageLink = new TextPageLink(15);
        TextPageData<Device> pageData = null;
        do {
            pageData = doGetTypedWithPageLink("/api/tenant/devices?type={type}&",
                    new TypeReference<TextPageData<Device>>(){}, pageLink, type1);
            loadedDevicesType1.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());

        Collections.sort(devicesType1, idComparator);
        Collections.sort(loadedDevicesType1, idComparator);

        Assert.assertEquals(devicesType1, loadedDevicesType1);

        List<Device> loadedDevicesType2 = new ArrayList<>();
        pageLink = new TextPageLink(4);
        do {
            pageData = doGetTypedWithPageLink("/api/tenant/devices?type={type}&",
                    new TypeReference<TextPageData<Device>>(){}, pageLink, type2);
            loadedDevicesType2.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());

        Collections.sort(devicesType2, idComparator);
        Collections.sort(loadedDevicesType2, idComparator);

        Assert.assertEquals(devicesType2, loadedDevicesType2);

        for (Device device : loadedDevicesType1) {
            doDelete("/api/device/"+device.getId().getId().toString())
                    .andExpect(status().isOk());
        }

        pageLink = new TextPageLink(4);
        pageData = doGetTypedWithPageLink("/api/tenant/devices?type={type}&",
                new TypeReference<TextPageData<Device>>(){}, pageLink, type1);
        Assert.assertFalse(pageData.hasNext());
        Assert.assertEquals(0, pageData.getData().size());

        for (Device device : loadedDevicesType2) {
            doDelete("/api/device/"+device.getId().getId().toString())
                    .andExpect(status().isOk());
        }

        pageLink = new TextPageLink(4);
        pageData = doGetTypedWithPageLink("/api/tenant/devices?type={type}&",
                new TypeReference<TextPageData<Device>>(){}, pageLink, type2);
        Assert.assertFalse(pageData.hasNext());
        Assert.assertEquals(0, pageData.getData().size());
    }
    
    @Test
    public void testFindCustomerDevices() throws Exception {
        Customer customer = new Customer();
        customer.setTitle("Test customer");
        customer = doPost("/api/customer", customer, Customer.class);
        CustomerId customerId = customer.getId();
        
        List<Device> devices = new ArrayList<>();
        for (int i=0;i<128;i++) {
            Device device = new Device();
            device.setName("Device"+i);
            device.setType("default");
            device = doPost("/api/device", device, Device.class);
            devices.add(doPost("/api/customer/" + customerId.getId().toString() 
                            + "/device/" + device.getId().getId().toString(), Device.class));
        }
        
        List<Device> loadedDevices = new ArrayList<>();
        TextPageLink pageLink = new TextPageLink(23);
        TextPageData<Device> pageData = null;
        do {
            pageData = doGetTypedWithPageLink("/api/customer/" + customerId.getId().toString() + "/devices?", 
                    new TypeReference<TextPageData<Device>>(){}, pageLink);
            loadedDevices.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());
        
        Collections.sort(devices, idComparator);
        Collections.sort(loadedDevices, idComparator);
        
        Assert.assertEquals(devices, loadedDevices);
    }
    
    @Test
    public void testFindCustomerDevicesByName() throws Exception {
        Customer customer = new Customer();
        customer.setTitle("Test customer");
        customer = doPost("/api/customer", customer, Customer.class);
        CustomerId customerId = customer.getId();

        String title1 = "Device title 1";
        List<Device> devicesTitle1 = new ArrayList<>();
        for (int i=0;i<125;i++) {
            Device device = new Device();
            String suffix = RandomStringUtils.randomAlphanumeric(15);
            String name = title1+suffix;
            name = i % 2 == 0 ? name.toLowerCase() : name.toUpperCase();
            device.setName(name);
            device.setType("default");
            device = doPost("/api/device", device, Device.class);
            devicesTitle1.add(doPost("/api/customer/" + customerId.getId().toString() 
                    + "/device/" + device.getId().getId().toString(), Device.class));
        }
        String title2 = "Device title 2";
        List<Device> devicesTitle2 = new ArrayList<>();
        for (int i=0;i<143;i++) {
            Device device = new Device();
            String suffix = RandomStringUtils.randomAlphanumeric(15);
            String name = title2+suffix;
            name = i % 2 == 0 ? name.toLowerCase() : name.toUpperCase();
            device.setName(name);
            device.setType("default");
            device = doPost("/api/device", device, Device.class);
            devicesTitle2.add(doPost("/api/customer/" + customerId.getId().toString() 
                    + "/device/" + device.getId().getId().toString(), Device.class));
        }
        
        List<Device> loadedDevicesTitle1 = new ArrayList<>();
        TextPageLink pageLink = new TextPageLink(15, title1);
        TextPageData<Device> pageData = null;
        do {
            pageData = doGetTypedWithPageLink("/api/customer/" + customerId.getId().toString() + "/devices?", 
                    new TypeReference<TextPageData<Device>>(){}, pageLink);
            loadedDevicesTitle1.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());
        
        Collections.sort(devicesTitle1, idComparator);
        Collections.sort(loadedDevicesTitle1, idComparator);
        
        Assert.assertEquals(devicesTitle1, loadedDevicesTitle1);
        
        List<Device> loadedDevicesTitle2 = new ArrayList<>();
        pageLink = new TextPageLink(4, title2);
        do {
            pageData = doGetTypedWithPageLink("/api/customer/" + customerId.getId().toString() + "/devices?", 
                    new TypeReference<TextPageData<Device>>(){}, pageLink);
            loadedDevicesTitle2.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());

        Collections.sort(devicesTitle2, idComparator);
        Collections.sort(loadedDevicesTitle2, idComparator);
        
        Assert.assertEquals(devicesTitle2, loadedDevicesTitle2);
        
        for (Device device : loadedDevicesTitle1) {
            doDelete("/api/customer/device/" + device.getId().getId().toString())
            .andExpect(status().isOk());
        }
        
        pageLink = new TextPageLink(4, title1);
        pageData = doGetTypedWithPageLink("/api/customer/" + customerId.getId().toString() + "/devices?", 
                new TypeReference<TextPageData<Device>>(){}, pageLink);
        Assert.assertFalse(pageData.hasNext());
        Assert.assertEquals(0, pageData.getData().size());
        
        for (Device device : loadedDevicesTitle2) {
            doDelete("/api/customer/device/" + device.getId().getId().toString())
            .andExpect(status().isOk());
        }
        
        pageLink = new TextPageLink(4, title2);
        pageData = doGetTypedWithPageLink("/api/customer/" + customerId.getId().toString() + "/devices?", 
                new TypeReference<TextPageData<Device>>(){}, pageLink);
        Assert.assertFalse(pageData.hasNext());
        Assert.assertEquals(0, pageData.getData().size());
    }

    @Test
    public void testFindCustomerDevicesByType() throws Exception {
        Customer customer = new Customer();
        customer.setTitle("Test customer");
        customer = doPost("/api/customer", customer, Customer.class);
        CustomerId customerId = customer.getId();

        String title1 = "Device title 1";
        String type1 = "typeC";
        List<Device> devicesType1 = new ArrayList<>();
        for (int i=0;i<125;i++) {
            Device device = new Device();
            String suffix = RandomStringUtils.randomAlphanumeric(15);
            String name = title1+suffix;
            name = i % 2 == 0 ? name.toLowerCase() : name.toUpperCase();
            device.setName(name);
            device.setType(type1);
            device = doPost("/api/device", device, Device.class);
            devicesType1.add(doPost("/api/customer/" + customerId.getId().toString()
                    + "/device/" + device.getId().getId().toString(), Device.class));
        }
        String title2 = "Device title 2";
        String type2 = "typeD";
        List<Device> devicesType2 = new ArrayList<>();
        for (int i=0;i<143;i++) {
            Device device = new Device();
            String suffix = RandomStringUtils.randomAlphanumeric(15);
            String name = title2+suffix;
            name = i % 2 == 0 ? name.toLowerCase() : name.toUpperCase();
            device.setName(name);
            device.setType(type2);
            device = doPost("/api/device", device, Device.class);
            devicesType2.add(doPost("/api/customer/" + customerId.getId().toString()
                    + "/device/" + device.getId().getId().toString(), Device.class));
        }

        List<Device> loadedDevicesType1 = new ArrayList<>();
        TextPageLink pageLink = new TextPageLink(15);
        TextPageData<Device> pageData = null;
        do {
            pageData = doGetTypedWithPageLink("/api/customer/" + customerId.getId().toString() + "/devices?type={type}&",
                    new TypeReference<TextPageData<Device>>(){}, pageLink, type1);
            loadedDevicesType1.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());

        Collections.sort(devicesType1, idComparator);
        Collections.sort(loadedDevicesType1, idComparator);

        Assert.assertEquals(devicesType1, loadedDevicesType1);

        List<Device> loadedDevicesType2 = new ArrayList<>();
        pageLink = new TextPageLink(4);
        do {
            pageData = doGetTypedWithPageLink("/api/customer/" + customerId.getId().toString() + "/devices?type={type}&",
                    new TypeReference<TextPageData<Device>>(){}, pageLink, type2);
            loadedDevicesType2.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());

        Collections.sort(devicesType2, idComparator);
        Collections.sort(loadedDevicesType2, idComparator);

        Assert.assertEquals(devicesType2, loadedDevicesType2);

        for (Device device : loadedDevicesType1) {
            doDelete("/api/customer/device/" + device.getId().getId().toString())
                    .andExpect(status().isOk());
        }

        pageLink = new TextPageLink(4);
        pageData = doGetTypedWithPageLink("/api/customer/" + customerId.getId().toString() + "/devices?type={type}&",
                new TypeReference<TextPageData<Device>>(){}, pageLink, type1);
        Assert.assertFalse(pageData.hasNext());
        Assert.assertEquals(0, pageData.getData().size());

        for (Device device : loadedDevicesType2) {
            doDelete("/api/customer/device/" + device.getId().getId().toString())
                    .andExpect(status().isOk());
        }

        pageLink = new TextPageLink(4);
        pageData = doGetTypedWithPageLink("/api/customer/" + customerId.getId().toString() + "/devices?type={type}&",
                new TypeReference<TextPageData<Device>>(){}, pageLink, type2);
        Assert.assertFalse(pageData.hasNext());
        Assert.assertEquals(0, pageData.getData().size());
    }

    @Test
    public void testDownloadCsvTSData() throws Exception {
        Device device = new Device();
        device.setName("D1");
        device.setType("default");
        Device savedDevice = doPost("/api/device", device, Device.class);
        createTimeSeriesData(savedDevice.getId());

        ResultActions result = doGet("/api/download/deviceSeriesData?type=ts&deviceId="+savedDevice.getId().getId().toString()+"&startValue=40&endValue=42");
        String s = result.andReturn().getResponse().getContentAsString();
        String[] rows = s.split("\n");

        Assert.assertEquals( 4, rows.length);

        List<String> headers = new ArrayList<String>(Arrays.asList(rows[0].split(",")));
        Assert.assertTrue(headers.containsAll(Arrays.asList("ts", "booleanKey", "doubleKey", "jsonKey", "longKey", "stringKey")));

        List<List<String>> valuesRows = Arrays.asList(
                new ArrayList<String>(Arrays.asList(rows[1].split(","))),
                new ArrayList<String>(Arrays.asList(rows[2].split(","))),
                new ArrayList<String>(Arrays.asList(rows[3].split(",")))
        );

        List<String> rowWithTs42 = geRowWithSpecifiedKeyValue("42", valuesRows);
        List<String> rowWithTs40 = geRowWithSpecifiedKeyValue("40", valuesRows);
        List<String> rowWithTs41 = geRowWithSpecifiedKeyValue("41", valuesRows);


        Assert.assertTrue(rowWithTs40.containsAll(Arrays.asList("40","true","1.7976931348623157E308","{\"\"tag\"\":\"\"value\"\"}","9223372036854775807","value")));
        Assert.assertTrue(rowWithTs41.containsAll(Arrays.asList("41","true","1.7976931348623157E308","{\"\"tag\"\":\"\"value\"\"}","9223372036854775807","value")));
        Assert.assertTrue(rowWithTs42.containsAll(Arrays.asList("42","true","1.7976931348623157E308","{\"\"tag\"\":\"\"value\"\"}","9223372036854775807","value")));
    }

    @Test
    public void testDownloadCsvDSData() throws Exception {
        Device device = new Device();
        device.setName("D1");
        device.setType("default");
        Device savedDevice = doPost("/api/device", device, Device.class);
        createDepthSeriesData(savedDevice.getId());

        ResultActions result = doGet("/api/download/deviceSeriesData?type=ds&deviceId="+savedDevice.getId().getId().toString()+"&startValue=4000&endValue=4200");
        String s = result.andReturn().getResponse().getContentAsString();
        String[] rows = s.split("\n");
        Assert.assertEquals( 4, rows.length);

        List<String> headers = new ArrayList<String>(Arrays.asList(rows[0].split(",")));
        Assert.assertTrue(headers.containsAll(Arrays.asList("ds", "booleanKey", "doubleKey", "jsonKey", "longKey", "stringKey")));

        List<List<String>> valuesRows = Arrays.asList(
                new ArrayList<String>(Arrays.asList(rows[1].split(","))),
                new ArrayList<String>(Arrays.asList(rows[2].split(","))),
                new ArrayList<String>(Arrays.asList(rows[3].split(",")))
        );

        List<String> rowWithDs4200 = geRowWithSpecifiedKeyValue("4200.0", valuesRows);
        List<String> rowWithDs4000 = geRowWithSpecifiedKeyValue("4000.0", valuesRows);
        List<String> rowWithDs4100 = geRowWithSpecifiedKeyValue("4100.0", valuesRows);


        Assert.assertTrue(rowWithDs4200.containsAll(Arrays.asList("4200.0","true","1.7976931348623157E308","{\"\"tag\"\":\"\"value\"\"}","9223372036854775807","value")));
        Assert.assertTrue(rowWithDs4000.containsAll(Arrays.asList("4000.0","true","1.7976931348623157E308","{\"\"tag\"\":\"\"value\"\"}","9223372036854775807","value")));
        Assert.assertTrue(rowWithDs4100.containsAll(Arrays.asList("4100.0","true","1.7976931348623157E308","{\"\"tag\"\":\"\"value\"\"}","9223372036854775807","value")));
    }

    @Test(expected = Exception.class)
    public void testDownloadCsvDSData_incorrectFormatStartValue() throws Exception {
        DeviceId deviceId = new DeviceId(UUIDs.timeBased());

        ResultActions result = doGet("/api/download/deviceSeriesData?type=ds&deviceId="+deviceId.getId().toString()+"&startValue=40J00&endValue=4200");
    }

    @Test(expected = Exception.class)
    public void testDownloadCsvTSData_incorrectFormatStartValue() throws Exception {
        DeviceId deviceId = new DeviceId(UUIDs.timeBased());

        ResultActions result = doGet("/api/download/deviceSeriesData?type=ts&deviceId="+deviceId.getId().toString()+"&startValue=40J00&endValue=4200");
    }


    @Test
    public void testDownloadCsvASData() throws Exception {
        Device device = new Device();
        device.setName("D1");
        device.setType("default");
        Device savedDevice = doPost("/api/device", device, Device.class);
        createDepthSeriesData(savedDevice.getId());

        AttributeKvEntry attr1 = new BaseAttributeKvEntry(new StringDataEntry("attribute1", "value1"), 42L);
        AttributeKvEntry attr2 = new BaseAttributeKvEntry(new StringDataEntry("attribute2", "value2"), 73L);
        AttributeKvEntry attr3 = new BaseAttributeKvEntry(new StringDataEntry("attribute3", "value3"), 74L);
        AttributeKvEntry attr4 = new BaseAttributeKvEntry(new StringDataEntry("attribute4", "value4"), 75L);
        AttributeKvEntry attr5 = new BaseAttributeKvEntry(new StringDataEntry("attribute5", "value5"), 76L);
        attributesService.save(savedDevice.getId(), DataConstants.CLIENT_SCOPE, Arrays.asList(attr1, attr2)).get();
        attributesService.save(savedDevice.getId(), DataConstants.SERVER_SCOPE, Arrays.asList(attr3, attr4)).get();
        attributesService.save(savedDevice.getId(), DataConstants.SHARED_SCOPE, Arrays.asList(attr5)).get();

        ResultActions result = doGet("/api/download/deviceAttributesData?deviceId="+savedDevice.getId().getId().toString());
        String s = result.andReturn().getResponse().getContentAsString();
        String[] rows = s.split("\n");
        Assert.assertEquals( 6, rows.length);

        List<String> headers = new ArrayList<String>(Arrays.asList(rows[0].split(",")));
        List<List<String>> valuesRows = Arrays.asList(
                new ArrayList<String>(Arrays.asList(rows[1].split(","))),
                new ArrayList<String>(Arrays.asList(rows[2].split(","))),
                new ArrayList<String>(Arrays.asList(rows[3].split(","))),
                new ArrayList<String>(Arrays.asList(rows[4].split(","))),
                new ArrayList<String>(Arrays.asList(rows[5].split(",")))
        );

        List<String> rowWithTs42 = geRowWithSpecifiedKeyValue("42", valuesRows);
        List<String> rowWithTs73 = geRowWithSpecifiedKeyValue("73", valuesRows);
        List<String> rowWithTs74 = geRowWithSpecifiedKeyValue("74", valuesRows);
        List<String> rowWithTs75 = geRowWithSpecifiedKeyValue("75", valuesRows);
        List<String> rowWithTs76 = geRowWithSpecifiedKeyValue("76", valuesRows);

        Assert.assertTrue(headers.containsAll(Arrays.asList("last_update_ts", "attribute1", "attribute2", "attribute3", "attribute4", "attribute5")));
        Assert.assertTrue(rowWithTs42.containsAll(Arrays.asList("42", "value1")));
        Assert.assertTrue(rowWithTs73.containsAll(Arrays.asList("73", "value2")));
        Assert.assertTrue(rowWithTs74.containsAll(Arrays.asList("74", "value3")));
        Assert.assertTrue(rowWithTs75.containsAll(Arrays.asList("75", "value4")));
        Assert.assertTrue(rowWithTs76.containsAll(Arrays.asList("76", "value5")));

    }

    private List<String> geRowWithSpecifiedKeyValue(String key, List<List<String>> rows){
        for(List<String> row: rows) {
            if(row.contains(key)) return row;
        }
        return null;
    }

    private void createTimeSeriesData(DeviceId deviceId) throws ExecutionException, InterruptedException {
        long TS = 42L;
        saveTsEntries(deviceId, TS - 2);
        saveTsEntries(deviceId, TS - 1);
        saveTsEntries(deviceId, TS);
    }

    private void createDepthSeriesData(DeviceId deviceId) throws ExecutionException, InterruptedException {
        Double DS = 4200D;
        saveDsEntries(deviceId, DS - 200D);
        saveDsEntries(deviceId, DS - 100D);
        saveDsEntries(deviceId, DS);
    }

    private void saveTsEntries(DeviceId deviceId, long ts) throws ExecutionException, InterruptedException {
        timeseriesService.save(deviceId, toTsEntry(ts, stringKvEntry)).get();
        timeseriesService.save(deviceId, toTsEntry(ts, longKvEntry)).get();
        timeseriesService.save(deviceId, toTsEntry(ts, doubleKvEntry)).get();
        timeseriesService.save(deviceId, toTsEntry(ts, booleanKvEntry)).get();
        timeseriesService.save(deviceId, toTsEntry(ts, jsonKvEntry)).get();
    }

    private static TsKvEntry toTsEntry(long ts, KvEntry entry) {
        return new BasicTsKvEntry(ts, entry);
    }

    private void saveDsEntries(DeviceId deviceId, Double ds) throws ExecutionException, InterruptedException {
        depthSeriesService.save(deviceId, toDsEntry(ds, stringKvEntry)).get();
        depthSeriesService.save(deviceId, toDsEntry(ds, longKvEntry)).get();
        depthSeriesService.save(deviceId, toDsEntry(ds, doubleKvEntry)).get();
        depthSeriesService.save(deviceId, toDsEntry(ds, booleanKvEntry)).get();
        depthSeriesService.save(deviceId, toDsEntry(ds, jsonKvEntry)).get();
    }

    private static DsKvEntry toDsEntry(Double ds, KvEntry entry) {
        return new BasicDsKvEntry(ds, entry);
    }
}
