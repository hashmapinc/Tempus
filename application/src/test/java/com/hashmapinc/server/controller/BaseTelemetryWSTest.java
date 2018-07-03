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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hashmapinc.server.common.data.Device;
import com.hashmapinc.server.common.data.Tenant;
import com.hashmapinc.server.common.data.User;
import com.hashmapinc.server.common.data.plugin.PluginMetaData;
import com.hashmapinc.server.common.data.rule.RuleMetaData;
import com.hashmapinc.server.common.data.security.Authority;
import com.hashmapinc.server.common.data.security.DeviceCredentials;
import com.hashmapinc.server.dao.plugin.PluginService;
import com.hashmapinc.server.extensions.core.plugin.telemetry.TelemetryStoragePlugin;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.client.ClientProperties;
import org.glassfish.tyrus.container.jdk.client.JdkClientContainer;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.websocket.*;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
public abstract class BaseTelemetryWSTest extends AbstractControllerTest{

    private Tenant savedTenant;
    private User tenantAdmin;
    private CountDownLatch messageLatch;
    private Device savedDevice;
    private long timeZoneOffset = 19800000L;
    private long telemetryTs = 1451649600512L;
    private String URL = "";
    private MqttAsyncClient mqttAsyncClient;
    private String accessToken;

    @Autowired
    private PluginService pluginService;
    @Value("${server.port}")
    protected int port;

    private static org.glassfish.tyrus.client.ThreadPoolConfig workerThreadPoolConfig = null;
    private static ClientEndpointConfig cec = null;
    private static ClientManager client = null;
    private static final String MQTT_URL = "tcp://localhost:1883";
    private static final ObjectMapper mapper = new ObjectMapper();

    @Before
    public void before() throws Exception{

        URL = "ws://localhost:" + port + "/api/ws/plugins/";
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

        if(pluginService.findPluginByApiToken("telemetry") == null) {
            PluginMetaData tenantPlugin = new PluginMetaData();
            tenantPlugin.setName("Telemetry plugin");
            tenantPlugin.setApiToken("telemetry");
            tenantPlugin.setConfiguration(mapper.readTree("{}"));
            tenantPlugin.setClazz(TelemetryStoragePlugin.class.getName());
            tenantPlugin = doPost("/api/plugin", tenantPlugin, PluginMetaData.class);
            Assert.assertNotNull(tenantPlugin);

            doPost("/api/plugin/" + tenantPlugin.getId().getId().toString() + "/activate").andExpect(status().isOk());

            RuleMetaData rule = new RuleMetaData();
            doPost("/api/rule", rule).andExpect(status().isBadRequest());
            rule.setName("My Rule");
            doPost("/api/rule", rule).andExpect(status().isBadRequest());
            rule.setPluginToken(tenantPlugin.getApiToken());
            doPost("/api/rule", rule).andExpect(status().isBadRequest());
            rule.setFilters(mapper.readTree("[{\"clazz\":\"com.hashmapinc.server.extensions.core.filter.MsgTypeFilter\", " +
                    "\"name\":\"TelemetryFilter\", " +
                    "\"configuration\": {\"messageTypes\":[\"POST_TELEMETRY\",\"POST_ATTRIBUTES\",\"GET_ATTRIBUTES\"]}}]"));
            doPost("/api/rule", rule).andExpect(status().isBadRequest());
            rule.setAction(mapper.readTree("{\"clazz\":\"com.hashmapinc.server.extensions.core.action.telemetry.TelemetryPluginAction\", \"name\":\"TelemetryMsgConverterAction\", \"configuration\":{\"timeUnit\":\"DAYS\", \"ttlValue\":1, \"qualityTimeWindow\":60000, \"qualityDepthWindow\":3000}}"));

            RuleMetaData savedRule = doPost("/api/rule", rule, RuleMetaData.class);
            Assert.assertNotNull(savedRule);
            Assert.assertNotNull(savedRule.getId());

            doPost("/api/rule/" + savedRule.getId().getId().toString() + "/activate").andExpect(status().isOk());
        }

        Device device = new Device();
        device.setName("My device");
        device.setType("default");
        savedDevice = doPost("/api/device", device, Device.class);

        Assert.assertNotNull(savedDevice);

        DeviceCredentials deviceCredentials =
                doGet("/api/device/" + savedDevice.getId().getId().toString() + "/credentials", DeviceCredentials.class);

        assertEquals(savedDevice.getId(), deviceCredentials.getDeviceId());
        accessToken = deviceCredentials.getCredentialsId();
        assertNotNull(accessToken);

        setupWebsocketClient();
    }

    @After
    public void afterTest() throws Exception {
        loginSysAdmin();

        doDelete("/api/tenant/" + savedTenant.getId().getId().toString())
                .andExpect(status().isOk());

    }

    private static void setupWebsocketClient() {
        // Websocket client setup
        cec = ClientEndpointConfig.Builder.create().build();
        client = ClientManager.createClient(JdkClientContainer.class.getName());

        workerThreadPoolConfig = org.glassfish.tyrus.client.ThreadPoolConfig.defaultConfig();
        workerThreadPoolConfig.setDaemon(false);
        workerThreadPoolConfig.setMaxPoolSize(4);
        workerThreadPoolConfig.setCorePoolSize(3);

        client.getProperties().put(ClientProperties.SHARED_CONTAINER, false);
        client.getProperties().put(ClientProperties.WORKER_THREAD_POOL_CONFIG, workerThreadPoolConfig);
    }


    @Test
    public void TestTimeZoneAddedToDeviceTelemetry() throws Exception {

        publishTimeZoneAndTimesries();
        messageLatch = new CountDownLatch(1);
        try {
            client.connectToServer(new ClientTestEndpoint(), cec, new URI(URL + "telemetry?token=" + this.token));
        }
        catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        boolean mesageReceivedByClient = messageLatch.await(20, TimeUnit.SECONDS);
        Assert.assertTrue("Time lapsed before message was received by client.", mesageReceivedByClient);
    }

    private class ClientTestEndpoint extends Endpoint {

        @Override
        public void onOpen(Session session, EndpointConfig config) {
            try {
                session.addMessageHandler(new MessageHandler.Whole<String>() {

                    @Override
                    public void onMessage(String message) {
                        JSONParser parser = new JSONParser();
                        try {
                            JSONObject json = (JSONObject) parser.parse(message);
                            JSONObject latestValues =(JSONObject) json.get("latestValues");
                            long ts = (long)latestValues.get("key1");
                            if(ts == (telemetryTs + timeZoneOffset))
                                messageLatch.countDown(); // signal that the message was received by the client
                        } catch (Exception e){

                        }
                    }
                });

                String deviceId = savedDevice.getId().toString();
                session.getBasicRemote().sendText("{\"tsSubCmds\":[{\"entityType\":\"DEVICE\",\"entityId\":\"" + deviceId + "\",\"scope\":\"LATEST_TELEMETRY\",\"unsubscribe\":false}],\"historyCmds\":[],\"attrSubCmds\":[],\"dsSubCmds\":[],\"depthHistoryCmds\":[]}");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void publishTimeZoneAndTimesries() throws MqttException, InterruptedException {
        String clientId = MqttAsyncClient.generateClientId();
        mqttAsyncClient = new MqttAsyncClient(MQTT_URL, clientId);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(accessToken);
        IMqttToken mqttToken = mqttAsyncClient.connect(options);
        mqttToken.waitForCompletion();

        MqttMessage message = new MqttMessage();

        JSONObject attributeJson = new JSONObject();
        attributeJson.put("TimeZone", timeZoneOffset);
        message.setPayload(attributeJson.toString().getBytes());

        mqttAsyncClient.publish("v1/devices/me/attributes", message);

        JSONObject tsJson = new JSONObject();
        JSONObject valueJson = new JSONObject();

        valueJson.put("key1","value1");
        tsJson.put("ts", telemetryTs);
        tsJson.put("values", valueJson);

        message.setPayload(tsJson.toString().getBytes());
        mqttAsyncClient.publish("v1/devices/me/telemetry", message);
    }

}
