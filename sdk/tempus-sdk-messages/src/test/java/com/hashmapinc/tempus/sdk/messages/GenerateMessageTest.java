package com.hashmapinc.tempus.sdk.messages;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

public class GenerateMessageTest {

    @Test
    public void testReadGatewayConnectDeviceWithoutType() throws Exception {
        String msg = "{\"device\":\"Device A\"}";
        ObjectMapper objectMapper = new ObjectMapper();
        GatewayConnectDevice device = objectMapper.readValue(msg, GatewayConnectDevice.class);
        Assert.assertNotNull(device);
        String out = objectMapper.writeValueAsString(device);
        Assert.assertNotNull(out);
        Assert.assertEquals(out,msg);
    }

    @Test
    public void testReadGatewayConnectDeviceWithType() throws Exception {
        String msg = "{\"device\":\"Device A\",\"type\":\"Device Type\"}";
        ObjectMapper objectMapper = new ObjectMapper();
        GatewayConnectDevice device = objectMapper.readValue(msg, GatewayConnectDevice.class);
        Assert.assertNotNull(device);
        String out = objectMapper.writeValueAsString(device);
        Assert.assertNotNull(out);
        Assert.assertEquals(out,msg);
    }

}
