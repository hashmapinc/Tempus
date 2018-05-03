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
package com.hashmapinc.tempus.sdk.messages;

import org.junit.Assert;
import org.junit.Test;

/**
 * The type Generate message test.
 */
public class GenerateMessageTest {

    /**
     * Test gateway connect device message.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGatewayConnectDeviceMessage() throws Exception {
        String msg = "{\"device\":\"Device A\"}";
        GatewayConnectDevice device = new GatewayConnectDevice("Device A");
        Assert.assertNotNull(device);
        String out = device.writeValueAsString(device);
        Assert.assertNotNull(out);
        Assert.assertEquals(out,msg);
    }

    /**
     * Test gateway connect device with type message.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGatewayConnectDeviceWithTypeMessage() throws Exception {
        String msg = "{\"device\":\"Device A\",\"type\":\"Device Type\"}";
        GatewayConnectDevice device = new GatewayConnectDevice("Device A", "Device Type");
        Assert.assertNotNull(device);
        String out = device.writeValueAsString(device);
        Assert.assertNotNull(out);
        Assert.assertEquals(out,msg);
    }

    /**
     * Test gateway disconnect device message.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGatewayDisconnectDeviceMessage() throws Exception {
        String msg = "{\"device\":\"Device A\"}";
        GatewayDisconnectDevice device = new GatewayDisconnectDevice("Device A");
        Assert.assertNotNull(device);
        String out = device.writeValueAsString(device);
        Assert.assertNotNull(out);
        Assert.assertEquals(out,msg);
    }

    /**
     * Test device telemetry message.
     *
     * @throws Exception the exception
     */
    @Test
    public void testDeviceTelemetryMessage() throws Exception {
        String msg = "[{\"values\":{\"temp\":42.0,\"humidity\":82.0},\"ts\":\"1524242990171\"}]";
        DeviceTelemetryValue device = new DeviceTelemetryValue();

        TelemetryDataValue values = new TelemetryDataValue();
        values.addValue("temp",42.0);
        values.addValue("humidity",82.0);
        device.addDataValue(values);
        values.setTimeStamp("1524242990171");

        String out = device.writeValueAsString(device);
        Assert.assertNotNull(out);
        Assert.assertEquals(out,msg);
    }

    /**
     * Test device telemetry array message.
     *
     * @throws Exception the exception
     */
    @Test
    public void testDeviceTelemetryArrayMessage() throws Exception {
        String msg = "[{\"values\":{\"temp\":42.0,\"humidity\":82.0},\"ts\":\"1524242990171\"},{\"values\":{\"temp\":42.0,\"humidity\":82.0},\"ts\":\"1624242990171\"}]";
        DeviceTelemetryValue device = new DeviceTelemetryValue();

        TelemetryDataValue ts1 = new TelemetryDataValue();
        ts1.addValue("temp",42.0);
        ts1.addValue("humidity",82.0);
        device.addDataValue(ts1);
        ts1.setTimeStamp("1524242990171");

        TelemetryDataValue ts2 = new TelemetryDataValue();
        ts2.addValue("temp",42.0);
        ts2.addValue("humidity",82.0);
        device.addDataValue(ts2);
        ts2.setTimeStamp("1624242990171");

        String out = device.writeValueAsString(device);
        Assert.assertNotNull(out);
        Assert.assertEquals(out,msg);
    }

    /**
     * Test device depth message.
     *
     * @throws Exception the exception
     */
    @Test
    public void testDeviceDepthMessage() throws Exception {
        String msg = "[{\"values\":{\"key1\":42.0,\"key2\":82.0},\"ds\":305.1}]";
        DeviceDepthValue device = new DeviceDepthValue();

        DepthDataValue values = new DepthDataValue();
        values.addValue("key1",42.0);
        values.addValue("key2",82.0);
        device.addDepthDataValue(values);
        values.setDepth(305.1);

        String out = device.writeValueAsString(device);
        Assert.assertNotNull(out);
        Assert.assertEquals(out,msg);
    }

    /**
     * Test device depth message array.
     *
     * @throws Exception the exception
     */
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

        String out = device.writeValueAsString(device);
        Assert.assertNotNull(out);
        Assert.assertEquals(out,msg);
    }

    /**
     * Test gateway telemetry message.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGatewayTelemetryMessage() throws Exception {
        String msg = "{\"Device A\":[{\"values\":{\"temp\":42.0,\"humidity\":82.0},\"ts\":\"1524242990171\"}]}";
        GatewayTelemetryValue gateway = new GatewayTelemetryValue();
        gateway.setDeviceName("Device A");
        TelemetryDataValue values = new TelemetryDataValue();
        values.addValue("temp",42.0);
        values.addValue("humidity",82.0);
        gateway.addDataValue(values);
        values.setTimeStamp("1524242990171");

        String out = gateway.writeValueAsString(gateway);
        Assert.assertNotNull(out);
        Assert.assertEquals(out,msg);

    }

    /**
     * Test gateway telemetry message array.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGatewayTelemetryMessageArray() throws Exception {
        String msg = "{\"Device A\":[{\"values\":{\"temp\":42.0,\"humidity\":82.0},\"ts\":\"1524242990171\"},{\"values\":{\"temp\":42.0,\"humidity\":82.0},\"ts\":\"1624242990171\"}]}";
        GatewayTelemetryValue gateway = new GatewayTelemetryValue();
        gateway.setDeviceName("Device A");

        TelemetryDataValue ts1 = new TelemetryDataValue();
        ts1.addValue("temp",42.0);
        ts1.addValue("humidity",82.0);
        ts1.setTimeStamp("1524242990171");
        gateway.addDataValue(ts1);

        TelemetryDataValue ts2 = new TelemetryDataValue();
        ts2.addValue("temp",42.0);
        ts2.addValue("humidity",82.0);
        ts2.setTimeStamp("1624242990171");
        gateway.addDataValue(ts2);

        String out = gateway.writeValueAsString(gateway);
        Assert.assertNotNull(out);
        Assert.assertEquals(out,msg);
    }

     /**
     * Test gateway depth message.
     *
     * @throws Exception the exception
     */
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

        String out = gateway.writeValueAsString(gateway);
        Assert.assertNotNull(out);
        Assert.assertEquals(out,msg);
    }

    /**
     * Test gateway depth message array.
     *
     * @throws Exception the exception
     */
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

        String out = gateway.writeValueAsString(gateway);
        Assert.assertNotNull(out);
        Assert.assertEquals(out,msg);
    }

    @Test
    public void testDeviceAttributeMessage() throws  Exception {
        String msg = "{\"attribute1\":\"value1\",\"attribute2\":82.0}";
        DeviceAttributeMessage attribMsg = new DeviceAttributeMessage();
        AttributeDataValue attribValues = new AttributeDataValue();
        attribValues.addValue("attribute1","value1");
        attribValues.addValue("attribute2",82.0);

        attribMsg.addAttributeValue(attribValues);

        String out = attribMsg.writeValueAsString(attribMsg);
        Assert.assertNotNull(out);
        Assert.assertEquals(msg,out);
    }

    @Test
    public void testGatewayDeviceAttributeMessage() throws  Exception {
        String msg = "{\"Device A\":{\"attribute1\":\"value1\",\"attribute2\":82.0}}";
        GatewayDeviceAttributeMessage attribMsg = new GatewayDeviceAttributeMessage();
        attribMsg.setDeviceName("Device A");
        AttributeDataValue attribValues = new AttributeDataValue();
        attribValues.addValue("attribute1","value1");
        attribValues.addValue("attribute2",82.0);

        attribMsg.addAttributeValue(attribValues);

        String out = attribMsg.writeValueAsString(attribMsg);
        Assert.assertNotNull(out);
        Assert.assertEquals(msg,out);
    }
}
