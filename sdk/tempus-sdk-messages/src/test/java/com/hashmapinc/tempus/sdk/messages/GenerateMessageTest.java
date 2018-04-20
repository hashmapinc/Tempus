package com.hashmapinc.tempus.sdk.messages;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.Assert;
import org.junit.Test;

import javax.xml.crypto.Data;

public class GenerateMessageTest {

    @Test
    public void testGatewayConnectDeviceMessage() throws Exception {
        String msg = "{\"device\":\"Device A\"}";
        ObjectMapper objectMapper = new ObjectMapper();
        GatewayConnectDevice device = objectMapper.readValue(msg, GatewayConnectDevice.class);
        Assert.assertNotNull(device);
        String out = objectMapper.writeValueAsString(device);
        Assert.assertNotNull(out);
        Assert.assertEquals(out,msg);
    }

    @Test

    public void testGatewayConnectDeviceWithTypeMessage() throws Exception {
        String msg = "{\"device\":\"Device A\",\"type\":\"Device Type\"}";
        ObjectMapper objectMapper = new ObjectMapper();
        GatewayConnectDevice device = objectMapper.readValue(msg, GatewayConnectDevice.class);
        Assert.assertNotNull(device);
        String out = objectMapper.writeValueAsString(device);
        Assert.assertNotNull(out);
        Assert.assertEquals(out,msg);
    }

    @Test
    public void testGatewayDisconnectDeviceMessage() throws Exception {
        String msg = "{\"device\":\"Device A\"}";
        ObjectMapper objectMapper = new ObjectMapper();
        GatewayDisconnectDevice device = objectMapper.readValue(msg, GatewayDisconnectDevice.class);
        Assert.assertNotNull(device);
        String out = objectMapper.writeValueAsString(device);
        Assert.assertNotNull(out);
        Assert.assertEquals(out,msg);
    }

    @Test
    public void testDeviceTelemetryMessage() throws Exception {
        String msg = "[{\"values\":{\"temp\":42.0,\"humidity\":82.0},\"ts\":\"1524242990171\"}]";
        DeviceValue device = new DeviceValue();

        DataValue values = new DataValue();
        values.addValue("temp",42.0);
        values.addValue("humidity",82.0);
        device.addDataValue(values);
        values.setTimeStamp("1524242990171");

        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(DeviceValue.class, new DeviceValueSerializer());
        objectMapper.registerModule(module);
        String out = objectMapper.writeValueAsString(device);
        Assert.assertNotNull(out);
        Assert.assertEquals(out,msg);
    }

    @Test
    public void testDeviceTelemetryArrayMessage() throws Exception {
        String msg = "[{\"values\":{\"temp\":42.0,\"humidity\":82.0},\"ts\":\"1524242990171\"},{\"values\":{\"temp\":42.0,\"humidity\":82.0},\"ts\":\"1624242990171\"}]";
        DeviceValue device = new DeviceValue();

        DataValue ts1 = new DataValue();
        ts1.addValue("temp",42.0);
        ts1.addValue("humidity",82.0);
        device.addDataValue(ts1);
        ts1.setTimeStamp("1524242990171");

        DataValue ts2 = new DataValue();
        ts2.addValue("temp",42.0);
        ts2.addValue("humidity",82.0);
        device.addDataValue(ts2);
        ts2.setTimeStamp("1624242990171");

        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(DeviceValue.class, new DeviceValueSerializer());
        objectMapper.registerModule(module);
        String out = objectMapper.writeValueAsString(device);
        Assert.assertNotNull(out);
        Assert.assertEquals(out,msg);
    }

    @Test
    public void testDeviceDepthMessage() throws Exception {
        String msg = "[{\"values\":{\"key1\":42.0,\"key2\":82.0},\"ds\":305.1}]";
        DeviceDepthValue device = new DeviceDepthValue();

        DepthDataValue values = new DepthDataValue();
        values.addValue("key1",42.0);
        values.addValue("key2",82.0);
        device.addDepthDataValue(values);
        values.setDepth(305.1);

        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(DeviceDepthValue.class, new DeviceDepthValueSerializer());
        objectMapper.registerModule(module);
        String out = objectMapper.writeValueAsString(device);
        Assert.assertNotNull(out);
        Assert.assertEquals(out,msg);
    }

    @Test
    public void testDeviceDepthMessageArray() throws Exception {
        String msg = "[{\"values\":{\"key1\":42.0,\"key2\":82.0},\"ds\":305.1},{\"values\":{\"key1\":42.0,\"key2\":82.0},\"ds\":315.1}]";
        DeviceDepthValue device = new DeviceDepthValue();

        DepthDataValue depth1 = new DepthDataValue();
        depth1.addValue("key1",42.0);
        depth1.addValue("key2",82.0);
        device.addDepthDataValue(depth1);
        depth1.setDepth(305.1);

        DepthDataValue depth2 = new DepthDataValue();
        depth2.addValue("key1",42.0);
        depth2.addValue("key2",82.0);
        device.addDepthDataValue(depth2);
        depth2.setDepth(315.1);

        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(DeviceDepthValue.class, new DeviceDepthValueSerializer());
        objectMapper.registerModule(module);
        String out = objectMapper.writeValueAsString(device);
        Assert.assertNotNull(out);
        Assert.assertEquals(out,msg);
    }

    @Test
    public void testGatewayTelemetryMessage() throws Exception {
        String msg = "{\"Device A\":[{\"values\":{\"temp\":42.0,\"humidity\":82.0},\"ts\":\"1524242990171\"}]}";
        GatewayValue gateway = new GatewayValue();
        gateway.setDeviceName("Device A");
        DataValue values = new DataValue();
        values.addValue("temp",42.0);
        values.addValue("humidity",82.0);
        gateway.addDataValue(values);
        values.setTimeStamp("1524242990171");

        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(GatewayValue.class, new GatewayValueSerializer());
        objectMapper.registerModule(module);
        String out = objectMapper.writeValueAsString(gateway);
        Assert.assertNotNull(out);
        Assert.assertEquals(out,msg);
    }


    @Test
    public void testGatewayTelemetryMessageArray() throws Exception {
        String msg = "{\"Device A\":[{\"values\":{\"temp\":42.0,\"humidity\":82.0},\"ts\":\"1524242990171\"},{\"values\":{\"temp\":42.0,\"humidity\":82.0},\"ts\":\"1624242990171\"}]}";
        GatewayValue gateway = new GatewayValue();
        gateway.setDeviceName("Device A");

        DataValue ts1 = new DataValue();
        ts1.addValue("temp",42.0);
        ts1.addValue("humidity",82.0);
        ts1.setTimeStamp("1524242990171");
        gateway.addDataValue(ts1);

        DataValue ts2 = new DataValue();
        ts2.addValue("temp",42.0);
        ts2.addValue("humidity",82.0);
        ts2.setTimeStamp("1624242990171");
        gateway.addDataValue(ts2);

        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(GatewayValue.class, new GatewayValueSerializer());
        objectMapper.registerModule(module);
        String out = objectMapper.writeValueAsString(gateway);
        Assert.assertNotNull(out);
        Assert.assertEquals(out,msg);
    }

    @Test
    public void testGatewayDepthMessage() throws Exception {
        String msg = "{\"Device A\":[{\"values\":{\"key1\":42.0,\"key2\":82.0},\"ds\":305.1}]}";
        GatewayDepthValue gateway = new GatewayDepthValue();
        gateway.setDeviceName("Device A");
        DepthDataValue values = new DepthDataValue();
        values.addValue("key1",42.0);
        values.addValue("key2",82.0);
        gateway.addDepthDataValue(values);
        values.setDepth(305.1);

        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(GatewayDepthValue.class, new GatewayDepthValueSerializer());
        objectMapper.registerModule(module);
        String out = objectMapper.writeValueAsString(gateway);
        Assert.assertNotNull(out);
        Assert.assertEquals(out,msg);
    }

    @Test
    public void testGatewayDepthMessageArray() throws Exception {
        String msg = "{\"Device A\":[{\"values\":{\"key1\":42.0,\"key2\":82.0},\"ds\":305.1},{\"values\":{\"key1\":42.0,\"key2\":82.0},\"ds\":315.1}]}";
        GatewayDepthValue gateway = new GatewayDepthValue();
        gateway.setDeviceName("Device A");

        DepthDataValue depth1 = new DepthDataValue();
        depth1.addValue("key1",42.0);
        depth1.addValue("key2",82.0);
        gateway.addDepthDataValue(depth1);
        depth1.setDepth(305.1);

        DepthDataValue depth2 = new DepthDataValue();
        depth2.addValue("key1",42.0);
        depth2.addValue("key2",82.0);
        gateway.addDepthDataValue(depth2);
        depth2.setDepth(315.1);

        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(GatewayDepthValue.class, new GatewayDepthValueSerializer());
        objectMapper.registerModule(module);
        String out = objectMapper.writeValueAsString(gateway);
        Assert.assertNotNull(out);
        Assert.assertEquals(out,msg);
    }
}
