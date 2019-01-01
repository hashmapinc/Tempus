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
package com.hashmapinc.server.mqtt.attribute;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hashmapinc.server.common.data.Customer;
import com.hashmapinc.server.common.data.CustomerGroup;
import com.hashmapinc.server.common.data.Device;
import com.hashmapinc.server.common.data.asset.Asset;
import com.hashmapinc.server.common.data.datamodel.DataModel;
import com.hashmapinc.server.common.data.datamodel.DataModelObject;
import com.hashmapinc.server.common.data.id.CustomerId;
import com.hashmapinc.server.common.data.id.DataModelId;
import com.hashmapinc.server.common.data.id.DataModelObjectId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.relation.EntityRelationInfo;
import com.hashmapinc.server.common.data.security.DeviceCredentials;
import com.hashmapinc.server.controller.AbstractControllerTest;
import com.hashmapinc.server.dao.asset.AssetService;
import com.hashmapinc.server.dao.datamodel.DataModelObjectService;
import com.hashmapinc.server.dao.datamodel.DataModelService;
import com.hashmapinc.server.dao.device.DeviceService;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

import static com.hashmapinc.server.dao.model.ModelConstants.NULL_UUID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@Slf4j
public abstract class AbstractMqttAttributeIntegrationTest extends AbstractControllerTest {

    private static final String MQTT_URL = "tcp://localhost:1883";

    private Device savedGatewayDevice;
    private String gatewayAccessToken;
    private CustomerId customerId;
    private DataModel dataModel;
    private DataModelObject dataModelObjectOfTypeAsset;
    private DataModelObject dataModelObjectOfTypeDevice;
    private Asset asset;

    @Autowired
    private DeviceService deviceService;
    @Autowired
    private DataModelService dataModelService;
    @Autowired
    private DataModelObjectService dataModelObjectService;
    @Autowired
    private AssetService assetService;

    @Before
    public void beforeTest() throws Exception {
        loginTenantAdmin();

        dataModel = createDataModel();
        dataModelObjectOfTypeAsset = createDataModelObject(dataModel.getId() , "well", "Asset", null);
        dataModelObjectOfTypeDevice = createDataModelObject(dataModel.getId(), "Temperature_Device", "Device", dataModelObjectOfTypeAsset.getId());

        customerId = createCustomer("My customer", dataModel.getId(),tenantId).getId();

        CustomerGroup savedCustomerGroup = createCustomerGroup("My Customer Group", customerId, tenantId);

        asset = createAsset(dataModelObjectOfTypeAsset.getId(), customerId, "well123");

        savedGatewayDevice = createGatewayDevice("Device Gateway", "gateway", dataModelObjectOfTypeDevice.getId(),customerId);

        DeviceCredentials gatewayDeviceCredentials =
                doGet("/api/device/" + savedGatewayDevice.getId().getId().toString() + "/credentials", DeviceCredentials.class);

        assertEquals(savedGatewayDevice.getId(), gatewayDeviceCredentials.getDeviceId());
        gatewayAccessToken = gatewayDeviceCredentials.getCredentialsId();
        assertNotNull(gatewayAccessToken);
    }


    @Test
    public void testPushMqttDeviceAttribute() throws Exception {
        String clientId = MqttAsyncClient.generateClientId();
        MqttAsyncClient client = new MqttAsyncClient(MQTT_URL, clientId);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(gatewayAccessToken);
        client.connect(options);
        Thread.sleep(3000); //NOSONAR
        MqttMessage message = new MqttMessage();
            message.setPayload("{\"DeviceA\":{\"attribute1\":\"value1\", \"attribute2\": 42 , \"parent_asset\": \"well123\"}, \"DeviceB\":{\"attribute1\":\"value1\", \"attribute2\": 42 }}".getBytes());

        client.publish("v1/gateway/attributes", message);
        Thread.sleep(1000); //NOSONAR Added for test

        Device savedDeviceA = deviceService.findDeviceByTenantIdAndName(tenantId,"DeviceA");
        Device savedDeviceB = deviceService.findDeviceByTenantIdAndName(tenantId,"DeviceB");

        assertNotNull(savedDeviceA);
        assertNotNull(savedDeviceB);

        List<EntityRelationInfo> entityRelationInfos =  doGetTyped("/api/relations/info?fromId="+savedDeviceA.getId().getId().toString()+"&fromType=DEVICE",new TypeReference<List<EntityRelationInfo>>() {});

        assertEquals("DeviceA",savedDeviceA.getName());
        assertEquals(customerId,savedDeviceA.getCustomerId());
        assertEquals(dataModelObjectOfTypeDevice.getId(),savedDeviceA.getDataModelObjectId());

        assertNotNull(entityRelationInfos);
        assertEquals(1,entityRelationInfos.size());
        assertEquals("well123",entityRelationInfos.get(0).getToName());
    }


    @Test
    public void testPushMqttDeviceAttributeWithInvalidParentAsset() throws Exception {
        String clientId = MqttAsyncClient.generateClientId();
        MqttAsyncClient client = new MqttAsyncClient(MQTT_URL, clientId);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(gatewayAccessToken);
        client.connect(options);
        Thread.sleep(3000); //NOSONAR
        MqttMessage message = new MqttMessage();
        message.setPayload("{\"DeviceA\":{\"attribute1\":\"value1\", \"attribute2\": 42 , \"parent_asset\": \"well1234\"}, \"DeviceB\":{\"attribute1\":\"value1\", \"attribute2\": 42 }}".getBytes());

        client.publish("v1/gateway/attributes", message);
        Thread.sleep(1000); //NOSONAR Added for test

        Device savedDeviceA = deviceService.findDeviceByTenantIdAndName(tenantId,"DeviceA");
        Device savedDeviceB = deviceService.findDeviceByTenantIdAndName(tenantId,"DeviceB");

        assertNotNull(savedDeviceA);
        assertNotNull(savedDeviceB);

        List<EntityRelationInfo> entityRelationInfos =  doGetTyped("/api/relations/info?fromId="+savedDeviceA.getId().getId().toString()+"&fromType=DEVICE",new TypeReference<List<EntityRelationInfo>>() {});

        assertEquals("DeviceA",savedDeviceA.getName());
        assertEquals(customerId,savedDeviceA.getCustomerId());
        assertEquals(dataModelObjectOfTypeDevice.getId(),savedDeviceA.getDataModelObjectId());

        assertNotNull(entityRelationInfos);
        assertEquals(0,entityRelationInfos.size());
    }

    @Test
    public void testPushMqttDeviceAttributeWithEmptyParentAsset() throws Exception {
        String clientId = MqttAsyncClient.generateClientId();
        MqttAsyncClient client = new MqttAsyncClient(MQTT_URL, clientId);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(gatewayAccessToken);
        client.connect(options);
        Thread.sleep(3000); //NOSONAR
        MqttMessage message = new MqttMessage();
        message.setPayload("{\"DeviceA\":{\"attribute1\":\"value1\", \"attribute2\": 42 , \"parent_asset\": \" \"}, \"DeviceB\":{\"attribute1\":\"value1\", \"attribute2\": 42 }}".getBytes());

        client.publish("v1/gateway/attributes", message);
        Thread.sleep(1000); //NOSONAR Added for test

        Device savedDeviceA = deviceService.findDeviceByTenantIdAndName(tenantId,"DeviceA");
        Device savedDeviceB = deviceService.findDeviceByTenantIdAndName(tenantId,"DeviceB");

        assertNotNull(savedDeviceA);
        assertNotNull(savedDeviceB);

        List<EntityRelationInfo> entityRelationInfos =  doGetTyped("/api/relations/info?fromId="+savedDeviceA.getId().getId().toString()+"&fromType=DEVICE",new TypeReference<List<EntityRelationInfo>>() {});

        assertEquals("DeviceA",savedDeviceA.getName());
        assertEquals(customerId,savedDeviceA.getCustomerId());
        assertEquals(dataModelObjectOfTypeDevice.getId(),savedDeviceA.getDataModelObjectId());

        assertNotNull(entityRelationInfos);
        assertEquals(0,entityRelationInfos.size());
    }

    @Test
    public void testPushMqttDeviceAttributeWithoutParentAsset() throws Exception {
        String clientId = MqttAsyncClient.generateClientId();
        MqttAsyncClient client = new MqttAsyncClient(MQTT_URL, clientId);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(gatewayAccessToken);
        client.connect(options);
        Thread.sleep(3000); //NOSONAR
        MqttMessage message = new MqttMessage();
        message.setPayload("{\"DeviceA\":{\"attribute1\":\"value1\", \"attribute2\": 42 }, \"DeviceB\":{\"attribute1\":\"value1\", \"attribute2\": 42 }}".getBytes());

        client.publish("v1/gateway/attributes", message);
        Thread.sleep(1000); //NOSONAR Added for test

        Device savedDeviceA = deviceService.findDeviceByTenantIdAndName(tenantId,"DeviceA");
        Device savedDeviceB = deviceService.findDeviceByTenantIdAndName(tenantId,"DeviceB");

        assertNotNull(savedDeviceA);
        assertNotNull(savedDeviceB);

        List<EntityRelationInfo> entityRelationInfos =  doGetTyped("/api/relations/info?fromId="+savedDeviceA.getId().getId().toString()+"&fromType=DEVICE",new TypeReference<List<EntityRelationInfo>>() {});

        assertEquals("DeviceA",savedDeviceA.getName());
        assertEquals(customerId,savedDeviceA.getCustomerId());
        assertEquals(dataModelObjectOfTypeDevice.getId(),savedDeviceA.getDataModelObjectId());

        assertNotNull(entityRelationInfos);
        assertEquals(0,entityRelationInfos.size());
    }

    private Customer createCustomer(String title, DataModelId dataModelId, TenantId tenantId) throws Exception{
        Customer customer = new Customer();
        customer.setTitle(title);
        customer.setDataModelId(dataModelId);
        customer.setTenantId(tenantId);
        return doPost("/api/customer", customer, Customer.class);
    }

    private CustomerGroup createCustomerGroup(String title, CustomerId customerId, TenantId tenantId) throws Exception{
        CustomerGroup customerGroup = new CustomerGroup();
        customerGroup.setTitle(title);
        customerGroup.setTenantId(tenantId);
        customerGroup.setCustomerId(customerId);
        CustomerGroup savedCustomerGroup = doPost("/api/customer/group", customerGroup, CustomerGroup.class);

        assertNotNull(savedCustomerGroup);
        assertNotNull(savedCustomerGroup.getId());
        Assert.assertTrue(savedCustomerGroup.getCreatedTime() > 0);
        assertEquals(customerGroup.getTitle(), savedCustomerGroup.getTitle());
        return savedCustomerGroup;
    }

    private Device createGatewayDevice(String name , String deviceType , DataModelObjectId dataModelObjectId , CustomerId customerId ) throws Exception{
        Device gatewayDevice = new Device();
        gatewayDevice.setName(name);
        gatewayDevice.setType(deviceType);
        gatewayDevice.setDataModelObjectId(dataModelObjectId);
        gatewayDevice.setCustomerId(customerId);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode additionalInfo = mapper.readTree("{\"gateway\":true}");
        gatewayDevice.setAdditionalInfo(additionalInfo);
        savedGatewayDevice = doPost("/api/device", gatewayDevice, Device.class);
        return savedGatewayDevice;
    }
    private DataModelObject createDataModelObject(DataModelId dataModelId , String name , String type ,DataModelObjectId parentId) throws Exception {
        DataModelObject dataModelObject = new DataModelObject();
        dataModelObject.setName(name);
        dataModelObject.setDataModelId(dataModelId);
        dataModelObject.setType(type);
        dataModelObject.setParentId(parentId);
        DataModelObject savedDataModelObject = doPost("/api/data-model/" + dataModel.getId().toString() + "/objects", dataModelObject, DataModelObject.class);
        assertNotNull(savedDataModelObject);
        assertNotNull(savedDataModelObject.getId());
        Assert.assertTrue(savedDataModelObject.getCreatedTime() > 0);
        assertNotNull(savedDataModelObject.getCustomerId());
        if(customerId == null){
            assertEquals(NULL_UUID, savedDataModelObject.getCustomerId().getId());
        }else{
            assertEquals(customerId, savedDataModelObject.getCustomerId());
        }
        assertEquals(dataModelObject.getName(), savedDataModelObject.getName());
        return savedDataModelObject;
    }

}
