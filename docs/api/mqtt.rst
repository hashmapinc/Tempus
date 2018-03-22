#########################
MQTT Device API Reference
#########################

***************
Getting Started
***************

MQTT basics
===========

MQTT is a lightweight publish-subscribe messaging protocol which probably makes it the most suitable for various IoT devices. You can find more information about MQTT here.
Tempus Cloud server nodes act as an MQTT Broker that supports QoS levels 0 (at most once) and 1 (at least once) and a set of predefined topics.

Client libraries setup
======================

You can find a large number of MQTT client libraries on the web. Examples in this article will be based on Mosquitto and MQTT.js. In order to setup one of those tools, you can use instructions in our Hello World guide.

MQTT Connect
============

We will use access token device credentials in this article and they will be referred to later as $ACCESS_TOKEN. The application needs to send MQTT CONNECT message with username that contains $ACCESS_TOKEN. Possible return codes and their reasons during connect sequence:

* 0x00 Connected - Successfully connected to Tempus Cloud MQTT server.
* 0x04 Connection Refused, bad user name or password - Username is empty.
* 0x05 Connection Refused, not authorized - Username contains invalid $ACCESS_TOKEN.

****************
Key-Value Format
****************

By default, Tempus Cloud supports key-value content in JSON. Key is always a string, while value can be either string, boolean, double or long. Using custom binary format or some serialization framework is also possible. See protocol customization for more details. For example:

.. code-block:: json

    {"stringKey":"value1", "booleanKey":true, "doubleKey":42.0, "longKey":73}

********************
Telemetry upload API
********************

.. code-block:: bash

    v1/devices/me/telemetry

The simplest supported data formats are:

.. code-block:: json

    {"key1":"value1", "key2":"value2"}
    
or

.. code-block:: json

    [{"key1":"value1"}, {"key2":"value2"}]

**Please note** that in this case, the server-side timestamp will be assigned to uploaded data!

In case your device is able to get the client-side timestamp, you can use following format:

.. code-block:: json

    {"ts":1451649600512, "values":{"key1":"value1", "key2":"value2"}}

In the example above, we assume that “1451649600512” is a unix timestamp with milliseconds precision. For example, the value ‘1451649600512’ corresponds to ‘Fri, 01 Jan 2016 12:00:00.512 GMT’

.. tabs::

    .. tab:: Mosquitto

        .. code-block:: bash

            # Publish data as an object without timestamp (server-side timestamp will be used)
            mosquitto_pub -d -h "127.0.0.1" -t "v1/devices/me/telemetry" -u "$ACCESS_TOKEN" -f "telemetry-data-as-object.json"
            # Publish data as an array of objects without timestamp (server-side timestamp will be used)
            mosquitto_pub -d -h "127.0.0.1" -t "v1/devices/me/telemetry" -u "$ACCESS_TOKEN" -f "telemetry-data-as-array.json"
            # Publish data as an object with timestamp (server-side timestamp will be used)
            mosquitto_pub -d -h "127.0.0.1" -t "v1/devices/me/telemetry" -u "$ACCESS_TOKEN" -f "telemetry-data-with-ts.json"

    .. tab:: MQTT.js

        .. code-block:: bash

            # Publish data as an object without timestamp (server-side timestamp will be used)
            cat telemetry-data-as-object.json | mqtt pub -v -h "127.0.0.1" -t "v1/devices/me/telemetry" -u '$ACCESS_TOKEN' -s
            # Publish data as an array of objects without timestamp (server-side timestamp will be used)
            cat telemetry-data-as-array.json | mqtt pub -v -h "127.0.0.1" -t "v1/devices/me/telemetry" -u '$ACCESS_TOKEN' -s
            # Publish data as an object with timestamp (server-side timestamp will be used)
            cat telemetry-data-with-ts.json | mqtt pub -v -h "127.0.0.1" -t "v1/devices/me/telemetry" -u '$ACCESS_TOKEN' -s

    .. tab:: telemetry-data-as-object.json

        .. code-block:: json

            {"key1":"value1", "key2":true, "key3": 3.0, "key4": 4}

    .. tab:: telemetry-data-as-array.json

        .. code-block:: json

            [{"key1":"value1"}, {"key2":true}]

    .. tab:: telemetry-data-with-ts.json

        .. code-block:: json

            {"ts":1451649600512, "values":{"key1":"value1", "key2":"value2"}}

********************
Telemetry upload API
********************

In order to support depth data, the administrator must first configure Tempus to handle depth data. This is done by chaning the configuration in Tempus.yml and uner the heading **UI Related configuration** set depthSeries to true: 

.. code-block:: yaml

    #UI Related Configuration
    configurations:
        ui:
        depthSeries: "true"

The depth topic is as follows:

.. code-block:: bash

    v1/devices/me/depth/telemetry

The supported data format is:

.. code-block:: json

     {"ds":5844.23,"values":{"viscosity":0.1, "humidity":22.0}}

Notice the use of DS. DS stands for depth stamp. This can either be in meters or feet, but must be consistant throughout the publication of data to the device. Mixing of units will cause data integrity issues.

**************
Attributes API
**************

Tempus Cloud attributes API allows devices to

* Upload client-side device attributes to the server.
* Request client-side and shared device attributes from the server.
* Subscribe to shared device attributes from the server.

Publish attribute update to the server
======================================

In order to publish client-side device attributes to Tempus Cloud server node, send PUBLISH message to the following topic:

.. code-block:: bash

    v1/devices/me/attributes

.. tabs::

    .. tab:: Mosquitto

        .. code-block:: bash

            # Publish client-side attributes update
            mosquitto_pub -d -h "127.0.0.1" -t "v1/devices/me/attributes" -u "$ACCESS_TOKEN" -f "new-attributes-values.json"
          
    .. tab:: MQTT.js

        .. code-block:: bash

            # Publish client-side attributes update
            cat new-attributes-values.json | mqtt pub -d -h "127.0.0.1" -t "v1/devices/me/attributes" -u '$ACCESS_TOKEN' -s

    .. tab:: new-attributes-values.json

        .. code-block:: json

            {"attribute1":"value1", "attribute2":true, "attribute3":42.0, "attribute4":73}
            
Request attribute values from the server
========================================

In order to request client-side or shared device attributes to Tempus Cloud server node, send PUBLISH message to the following topic:

.. code-block:: bash

    v1/devices/me/attributes/request/$request_id

where **$request_id** is your integer request identifier. Before sending PUBLISH message with the request, client need to subscribe to:

.. code-block:: bash

    v1/devices/me/attributes/response/+

The following example is written in javascript and is based on mqtt.js. Pure command-line examples are not available because subscribe and publish need to happen in the same mqtt session.

.. tabs::

    .. tab:: MQTT.js

        .. code-block:: javascript

            export TOKEN=$ACCESS_TOKEN
            node mqtt-js-attributes-request.js
          
    .. tab:: mqtt-js-attributes-request.js

        .. code-block:: javascript

            var mqtt = require('mqtt')
            var client  = mqtt.connect('mqtt://127.0.0.1',{
                username: process.env.TOKEN
            })

            client.on('connect', function () {
                console.log('connected')
                client.subscribe('v1/devices/me/attributes/response/+')
                client.publish('v1/devices/me/attributes/request/1', '{"clientKeys":"attribute1,attribute2", "sharedKeys":"shared1,shared2"}')
            })

            client.on('message', function (topic, message) {
                console.log('response.topic: ' + topic)
                console.log('response.body: ' + message.toString())
                client.end()
            })
    
    .. tab:: Result

        .. code-block:: json

            {"key1":"value1"}
            
**Note:** The intersection of client-side and shared device attribute keys is a bad practice! However, it is still possible to have same keys for client, shared or even server-side attributes.

Subscribe to Attribute Updates from the Server
==============================================

In order to subscribe to shared device attribute changes, send SUBSCRIBE message to the following topic:

.. code-block:: bash

    v1/devices/me/attributes

Once shared attribute will be changed by one of the server-side components (REST API or custom plugins) the client will receive the following update:

.. code-block:: json

   {"key1":"value1"}

.. tabs::

    .. tab:: Mosquitto

        .. code-block:: bash

            # Subscribes to attribute updates
            mosquitto_sub -d -h "127.0.0.1" -t "v1/devices/me/attributes" -u "$ACCESS_TOKEN"
          
    .. tab:: MQTT.js

        .. code-block:: bash

            # Subscribes to attribute updates
            mqtt sub -v "127.0.0.1" -t "v1/devices/me/attributes" -u '$ACCESS_TOKEN'

*******
RPC API
*******

Server-side RPC
===============

In order to subscribe to RPC commands from the server, send SUBSCRIBE message to the following topic:

.. code-block:: bash

    v1/devices/me/rpc/request/+

Once subscribed, the client will receive individual commands as a PUBLISH message to the corresponding topic:

.. code-block:: bash

    v1/devices/me/rpc/request/$request_id

where $request_id is an integer request identifier.
The client should publish the response to the following topic:

.. code-block:: bash

    v1/devices/me/rpc/response/$request_id

The following example is written in javascript and is based on mqtt.js. Pure command-line examples are not available because subscribe and publish need to happen in the same mqtt session.

.. tabs::

    .. tab:: MQTT.js

        .. code-block:: bash

            export TOKEN=$ACCESS_TOKEN
            node mqtt-js-rpc-from-server.js
          
    .. tab:: mqtt-js-rpc-from-server.js

        .. code-block:: javascript

            var mqtt = require('mqtt');
            var client  = mqtt.connect('mqtt://127.0.0.1',{
                username: process.env.TOKEN
            });

            client.on('connect', function () {
                console.log('connected');
                client.subscribe('v1/devices/me/rpc/request/+')
            });

            client.on('message', function (topic, message) {
                console.log('request.topic: ' + topic);
                console.log('request.body: ' + message.toString());
                var requestId = topic.slice('v1/devices/me/rpc/request/'.length);
                //client acts as an echo service
                client.publish('v1/devices/me/rpc/response/' + requestId, message);
            });

Client-side RPC
===============

In order to send RPC commands to server, send PUBLISH message to the following topic:

.. code-block:: bash

    v1/devices/me/rpc/request/$request_id

where $request_id is an integer request identifier. The response from server will be published to the following topic:

.. code-block:: bash

    v1/devices/me/rpc/response/$request_id

The following example is written in javascript and is based on mqtt.js. Pure command-line examples are not available because subscribe and publish need to happen in the same mqtt session.

.. tabs::

    .. tab:: MQTT.js

        .. code-block:: bash

            export TOKEN=$ACCESS_TOKEN
            node mqtt-js-rpc-from-client.js
          
    .. tab:: mqtt-js-rpc-from-client.js

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
                    "method": "getTime",
                    "params": {}
                };
                client.publish('v1/devices/me/rpc/request/' + requestId, JSON.stringify(request));
            });

            client.on('message', function (topic, message) {
                console.log('response.topic: ' + topic);
                console.log('response.body: ' + message.toString());
            });

**********************
Protocol Customization
**********************

MQTT transport can be fully customized for specific use-case by changing the corresponding module.