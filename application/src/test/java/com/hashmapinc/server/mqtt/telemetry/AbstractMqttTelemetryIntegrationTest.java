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
package com.hashmapinc.server.mqtt.telemetry;

import com.cirruslink.sparkplug.message.SparkplugBPayloadEncoder;
import com.cirruslink.sparkplug.message.model.MetaData;
import com.cirruslink.sparkplug.message.model.Metric;
import com.cirruslink.sparkplug.message.model.SparkplugBPayload;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.dao.device.DeviceService;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.Before;
import org.junit.Test;
import com.hashmapinc.server.common.data.Device;
import com.hashmapinc.server.common.data.security.DeviceCredentials;
import com.hashmapinc.server.controller.AbstractControllerTest;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.*;

import static com.cirruslink.sparkplug.message.model.MetricDataType.String;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@Slf4j
public abstract class AbstractMqttTelemetryIntegrationTest extends AbstractControllerTest {

    private static final String MQTT_URL = "tcp://localhost:1883";

    private Device savedDevice;
    private String accessToken;
    private Device savedGatewayDevice;
    private String gatewayAccessToken;
    @Autowired
    private DeviceService deviceService;

    @Before
    public void beforeTest() throws Exception {
        loginTenantAdmin();

        Device device = new Device();
        device.setName("Test device");
        device.setType("default");
        savedDevice = doPost("/api/device", device, Device.class);

        DeviceCredentials deviceCredentials =
                doGet("/api/device/" + savedDevice.getId().getId().toString() + "/credentials", DeviceCredentials.class);

        assertEquals(savedDevice.getId(), deviceCredentials.getDeviceId());
        accessToken = deviceCredentials.getCredentialsId();
        assertNotNull(accessToken);

        Device gatewayDevice = new Device();
        gatewayDevice.setName("Spark Plug gateway");
        gatewayDevice.setType("gateway");
        ObjectMapper mapper = new ObjectMapper();
        JsonNode additionalInfo = mapper.readTree("{\"gateway\":true}");
        gatewayDevice.setAdditionalInfo(additionalInfo);
        savedGatewayDevice = doPost("/api/device", gatewayDevice, Device.class);

        DeviceCredentials gatewayDeviceCredentials =
                doGet("/api/device/" + savedGatewayDevice.getId().getId().toString() + "/credentials", DeviceCredentials.class);

        assertEquals(savedGatewayDevice.getId(), gatewayDeviceCredentials.getDeviceId());
        gatewayAccessToken = gatewayDeviceCredentials.getCredentialsId();
        assertNotNull(gatewayAccessToken);
    }

    @Test
    public void testPushMqttRpcData() throws Exception {
        String clientId = MqttAsyncClient.generateClientId();
        MqttAsyncClient client = new MqttAsyncClient(MQTT_URL, clientId);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(accessToken);
        client.connect(options);
        Thread.sleep(3000);
        MqttMessage message = new MqttMessage();
        message.setPayload("{\"key1\":\"value1\", \"key2\":true, \"key3\": 3.0, \"key4\": 4}".getBytes());
        client.publish("v1/devices/me/telemetry", message);

        String deviceId = savedDevice.getId().getId().toString();

        Thread.sleep(1000);
        List<String> actualKeys = doGetAsync("/api/plugins/telemetry/DEVICE/" + deviceId +  "/keys/timeseries", List.class);
        Set<String> actualKeySet = new HashSet<>(actualKeys);

        List<String> expectedKeys = Arrays.asList("key1", "key2", "key3", "key4");
        Set<String> expectedKeySet = new HashSet<>(expectedKeys);

        assertEquals(expectedKeySet, actualKeySet);

        String getTelemetryValuesUrl = "/api/plugins/telemetry/DEVICE/" + deviceId +  "/values/timeseries?keys=" + java.lang.String.join(",", actualKeySet);
        Map<String, List<Map<String, String>>> values = doGetAsync(getTelemetryValuesUrl, Map.class);

        assertEquals("value1", values.get("key1").get(0).get("value"));
        assertEquals("true", values.get("key2").get(0).get("value"));
        assertEquals("3.0", values.get("key3").get(0).get("value"));
        assertEquals("4", values.get("key4").get(0).get("value"));
    }

    @Test
    public void testPushMqttWithMetaDataJson() throws Exception {
        String clientId = MqttAsyncClient.generateClientId();
        MqttAsyncClient client = new MqttAsyncClient(MQTT_URL, clientId);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(accessToken);
        client.connect(options);
        Thread.sleep(3000);
        MqttMessage message = new MqttMessage();
        message.setPayload("{\"values\":{\"humidity\":{\"unit\":\"%\",\"value\":15.616},\"viscosity\":{\"unit\":\"cgs\",\"value\":0.158}},\"ts\":1526804557313}".getBytes());
        client.publish("v1/devices/me/telemetry", message);

        String deviceId = savedDevice.getId().getId().toString();

        Thread.sleep(1000);
        List<String> actualKeys = doGetAsync("/api/plugins/telemetry/DEVICE/" + deviceId +  "/keys/timeseries", List.class);
        Set<String> actualKeySet = new HashSet<>(actualKeys);

        List<String> expectedKeys = Arrays.asList("humidity", "viscosity");
        Set<String> expectedKeySet = new HashSet<>(expectedKeys);

        assertEquals(expectedKeySet, actualKeySet);

        String getTelemetryValuesUrl = "/api/plugins/telemetry/DEVICE/" + deviceId +  "/values/timeseries?keys=" + java.lang.String.join(",", actualKeySet);
        Map<String, List<Map<String, String>>> values = doGetAsync(getTelemetryValuesUrl, Map.class);

        assertEquals("15.616", values.get("humidity").get(0).get("value"));
        assertEquals("0.158", values.get("viscosity").get(0).get("value"));
    }

    @Test
    public void testPushMqttSparkPlugData() throws Exception {
        String clientId = MqttAsyncClient.generateClientId();
        MqttAsyncClient client = new MqttAsyncClient(MQTT_URL, clientId);

        SparkplugBPayload.SparkplugBPayloadBuilder deathPayload = new SparkplugBPayload.SparkplugBPayloadBuilder().setTimestamp(new Date());
        deathPayload.setSeq(0);
        byte [] deathBytes = new SparkplugBPayloadEncoder().getBytes(deathPayload.createPayload());

        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(gatewayAccessToken);
        options.setWill("spBv1.0/hashmap/DDEATH/tempus device", deathBytes, 0, false);
        client.connect(options);
        Thread.sleep(3000);
        byte[] sparkPlugMsgByeArray = createSparkPlugMsg();

        client.publish("spBv1.0/hashmap/DBIRTH/tempus device/SparkplugBdevice", sparkPlugMsgByeArray, 0, false);

        Thread.sleep(10000);
        Device device = deviceService.findDeviceByTenantIdAndName(tenantId,"SparkplugBdevice");
        String deviceId = device.getId().getId().toString();
        Thread.sleep(1000);
        List<String> actualKeys = doGetAsync("/api/plugins/telemetry/DEVICE/" + deviceId +  "/keys/timeseries", List.class);
        Set<String> actualKeySet = new HashSet<>(actualKeys);

        List<String> expectedKeys = Arrays.asList("key1", "key2");
        Set<String> expectedKeySet = new HashSet<>(expectedKeys);

        assertEquals(expectedKeySet, actualKeySet);

        String getTelemetryValuesUrl = "/api/plugins/telemetry/DEVICE/" + deviceId +  "/values/timeseries?keys=" + java.lang.String.join(",", actualKeySet);
        Map<String, List<Map<String, String>>> values = doGetAsync(getTelemetryValuesUrl, Map.class);

        assertEquals("value1", values.get("key1").get(0).get("value"));
        assertEquals("value2", values.get("key2").get(0).get("value"));
    }

    private byte[] createSparkPlugMsg(){
        byte[] sparkplugByteArray = null;
        try {
            List<Metric> metrics = new ArrayList<Metric>();
            metrics.add(new Metric.MetricBuilder("key1", String, "value1").createMetric());
            //Setting meta data unit
            MetaData metaData = new MetaData.MetaDataBuilder()
                    .contentType("json")
                    .size(12L)
                    .seq(0L)
                    .fileName("none")
                    .fileType("none")
                    .md5("none")
                    .description("{\"unit\":\"unit\"}").createMetaData();
            metrics.get(0).setMetaData(metaData);
            metrics.add(new Metric.MetricBuilder("key2", String, "value2").createMetric());
            SparkplugBPayload payload = new SparkplugBPayload(
                    new Date(),
                    metrics,
                    1,
                    java.util.UUID.randomUUID().toString(),
                    null);
            sparkplugByteArray = new SparkplugBPayloadEncoder().getBytes(payload);
        }catch (Exception e){

        }
        return sparkplugByteArray;
    }
}
