package com.hashmapinc.kubeless;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MqttUtility {
    private String mqttUrl;
    private String accessToken;
    final public static String MQTT_TOPIC = "v1/gateway/telemetry";

    public MqttUtility() {
    }

    public MqttUtility(String mqttUrl, String accessToken) {
        this.mqttUrl = mqttUrl;
        this.accessToken = accessToken;
    }

    public String getMqttUrl() {
        return mqttUrl;
    }

    public void setMqttUrl(String mqttUrl) {
        this.mqttUrl = mqttUrl;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void connect(String mqttUrl) throws MqttException{
        MqttAsyncClient client = new MqttAsyncClient(mqttUrl, MqttAsyncClient.generateClientId());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(accessToken);
        client.connect(options);
    }


    public void publish(MqttAsyncClient client, String data, String deviceName) throws MqttException, JsonProcessingException {
        MqttMessage msg = new MqttMessage();
        toDataJson(data, deviceName);
        msg.setPayload(data.getBytes());
        client.publish(MQTT_TOPIC, msg);
    }

    public void publish(MqttAsyncClient client, String data, Long ts, String deviceName) throws MqttException, JsonProcessingException {
        MqttMessage msg = new MqttMessage();
        toDataJson(ts, data, deviceName);
        msg.setPayload(data.getBytes());
        client.publish(MQTT_TOPIC, msg);
    }

    private String toDataJson(Long ts, String json, String deviceName) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode objectNode = mapper.createObjectNode();
        ArrayNode an = objectNode.putArray(deviceName);
        ObjectNode objectNode2 = an.addObject();
        objectNode2.put("ts", ts);
        objectNode2.put("values", json);
        return mapper.writeValueAsString(objectNode2);
    }

    private String toDataJson(String json, String deviceName) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode on1 = mapper.createObjectNode();
        ArrayNode an = on1.putArray(deviceName);
        ObjectNode on2 = an.addObject();
        on2.put("ts", System.currentTimeMillis());
        on2.put("values", json);
        return mapper.writeValueAsString(on2);
    }

    public void disconnect(MqttAsyncClient client) throws MqttException {
        client.disconnect();
    }

}
