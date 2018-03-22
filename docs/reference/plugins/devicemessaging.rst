#######################
Device Messaging Plugin
#######################

********
Overview
********

This RPC plugin enables communication between various IoT devices through the Tempus cluster. The plugin introduces basic security features: devices are able to exchange messages only if they belong to the same customer. The plugin implementation can be customized to cover more complex security features.
Configuration

You can specify following configuration parameters:

* Maximum amount of devices per customer
* Default request timeout
* Maximum request timeout

**************
Device RPC API
**************

The plugin handles two rpc methods: getDevices and sendMsg. The examples listed below will be based on demo account and MQTT protocol. Please note that you are able to use other protocols - CoAP and HTTP.

Get Device List API
===================

In order to send a message to other devices, you will need to know their identifiers. A device can request a list of other devices that belong to the same customer using getDevices RPC call.

**mqtt-get-device-list.sh**

.. code-block:: bash

    export TOKEN=A1_TEST_TOKEN
    node mqtt-get-device-list.js

**mqtt-get-device-list.js**

.. code-block:: javascript

    var mqtt = require('mqtt');
    var client = mqtt.connect('mqtt://127.0.0.1', {
        username: process.env.TOKEN
    });

    client.on('connect', function () {
        console.log('connected');
        client.subscribe('v1/devices/me/rpc/response/+');
        var requestId = 1;
        var request = {
            "method": "getDevices",
            "params": {}
        };
        client.publish('v1/devices/me/rpc/request/' + requestId, JSON.stringify(request));
    });

    client.on('message', function (topic, message) {
        console.log('response.topic: ' + topic);
        console.log('response.body: ' + message.toString());
    });

**RESPONSE**

.. code-block:: json

    [
        {
            "id": "aa435e80-9fce-11e6-8080-808080808080",
            "name": "Test Device A2"
        },
        {
            "id": "86801880-9fce-11e6-8080-808080808080",
            "name": "Test Device A3"
        }
    ]

Send Message API
================

A device can send a message to other device that belongs to the same customer using sendMsg RPC call.
The example below will attempt to send a message from device “Test Device A1” to device “Test Device A2”.

**mqtt-send-msg.sh**

.. code-block:: bash

    export TOKEN=A1_TEST_TOKEN
    node 'mqtt-send-msg.js'

**mqtt-send-msg.js**

.. code-block:: javascript

        var mqtt = require('mqtt');
        var client = mqtt.connect('mqtt://127.0.0.1', {
            username: process.env.TOKEN
        });

        client.on('connect', function () {
            console.log('connected');
            client.subscribe('v1/devices/me/rpc/response/+');
            var requestId = 1;
            var request = {
                method: "sendMsg",
                params: {
                    deviceId: "aa435e80-9fce-11e6-8080-808080808080",
                    timeout: 2000,
                    oneway: false,
                    body: {
                        param1: "value1"
                    }
                }
            };
            client.publish('v1/devices/me/rpc/request/' + requestId, JSON.stringify(request));
        });

        client.on('message', function (topic, message) {
            console.log('response.topic: ' + topic);
            console.log('response.body: ' + message.toString());
        });

As a result, you should receive the following error:

.. code-block:: json

    {"error":"No active connection to the remote device!"}

Let’s launch emulator of target device and send message again:

**mqtt-recieve-msg.sh**

.. code-block:: bash

    export TOKEN=A2_TEST_TOKEN
    node mqtt-receive-msg.js

**mqtt-recieve-msg.sh**

.. code-block:: javascript

    var mqtt = require('mqtt');
    var client = mqtt.connect('mqtt://127.0.0.1', {
        username: process.env.TOKEN
    });

    client.on('connect', function () {
        console.log('connected');
        client.subscribe('v1/devices/me/rpc/request/+');
    });

    client.on('message', function (topic, message) {
        console.log('response.topic: ' + topic);
        console.log('response.body: ' + message.toString());
        client.publish(topic.replace('request', 'response'), '{"status":"ok"}');
    });

As a result, you should receive following response from device:

.. code-block:: json

    {"status":"ok"}

**Note** that target device id, access tokens, request and response bodies are hardcoded into scripts and correspond to devices that must be created beforehand.