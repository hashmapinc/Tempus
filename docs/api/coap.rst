#########################
CoAP Device API Reference
#########################

***************
Getting started
***************

CoAP basics
===========

CoAP is a light-weight IoT protocol for constrained devices. You can find more information about CoAP here. CoAP protocol is UDP based, but similar to HTTP it uses request-response model. CoAP observes option allows to subscribe to resources and receive notifications on resource change.

Tempus Cloud nodes act as CoAP Servers that support both regular and observe requests.

Client libraries setup
======================

You can find CoAP client libraries for different programming languages on the web. Examples in this article will be based on CoAP cli. In order to setup this tool, you can use instructions in our Hello World guide.

CoAP Authentication and error codes
===================================

We will use access token device credentials in this article and they will be referred to later as $ACCESS_TOKEN. The application needs to include $ACCESS_TOKEN as a path parameter into each CoAP request. Possible error codes and their reasons:

* 4.00 Bad Request - Invalid URL, request parameters or body.
* 4.01 Unauthorized - Invalid $ACCESS_TOKEN.
* 4.04 Not Found - Resource not found.


****************
Key-value format
****************

By default, Tempus Cloud supports key-value content in JSON. Key is always a string, while value can be either string, boolean, double or long. Using custom binary format or some serialization framework is also possible. See protocol customization for more details. For example:

.. code-block:: json

    {"stringKey":"value1", "booleanKey":true, "doubleKey":42.0, "longKey":73}


********************
Telemetry upload API
********************

In order to publish telemetry data to Tempus Cloud server node, send POST request to the following URL:

.. code-block:: bash

    coap://host:port/api/v1/$ACCESS_TOKEN/telemetry

The simplest supported data formats are:

.. code-block:: json
    
    {"key1":"value1", "key2":"value2"}

or

.. code-block:: json

    [{"key1":"value1"}, {"key2":"value2"}]

Please note that in this case, the server-side timestamp will be assigned to uploaded data!

In case your device is able to get the client-side timestamp, you can use following format:

.. code-block:: json

    {"ts":1451649600512, "values":{"key1":"value1", "key2":"value2"}}

In the example above, we assume that “1451649600512” is a unix timestamp with milliseconds precision. For example, the value ‘1451649600512’ corresponds to ‘Fri, 01 Jan 2016 12:00:00.512 GMT’

Example

.. tabs::

    .. tab:: coap-telemetry.sh

        .. code-block:: bash
            
            # Publish data as an object without timestamp (server-side timestamp will be used)
            cat telemetry-data-as-object.json | coap post coap://localhost/api/v1/$ACCESS_TOKEN/telemetry
            # Publish data as an array of objects without timestamp (server-side timestamp will be used)
            cat telemetry-data-as-array.json | coap post coap://localhost/api/v1/$ACCESS_TOKEN/telemetry
            # Publish data as an object with timestamp (server-side timestamp will be used)
            cat telemetry-data-with-ts.json | coap post coap://localhost/api/v1/$ACCESS_TOKEN/telemetry
    
    .. tab:: telemetry-data-as-object.json

        .. code-block:: json
        
            {"key1":"value1", "key2":true, "key3": 3.0, "key4": 4}

    .. tab:: telemetry-data-as-array.json

        .. code-block:: json
        
            [{"key1":"value1"}, {"key2":true}]

    .. tab:: telemetry-data-with-ts.json

        .. code-block:: json

            {"ts":1451649600512, "values":{"key1":"value1", "key2":"value2"}}


**************
Attributes API
**************

Tempus Cloud attributes API allows devices to

* Upload client-side device attributes to the server.
* Request client-side and shared device attributes from the server.
* Subscribe to shared device attributes from the server.


Publish attribute update to the server
======================================

In order to publish client-side device attributes to Tempus Cloud server node, send POST request to the following URL:

.. code-block:: bash

    coap://host:port/api/v1/$ACCESS_TOKEN/attributes


.. tabs::

    .. tab:: coap-telemetry.sh

        .. code-block:: bash

            # Publish client-side attributes update
            cat new-attributes-values.json | coap post coap://localhost/api/v1/$ACCESS_TOKEN/attributes

    .. tab:: new-attributes-values.json

        .. code-block:: json

            {"attribute1":"value1", "attribute2":true, "attribute3":42.0, "attribute4":73}

Request attribute values from the server
========================================

In order to request client-side or shared device attributes to Tempus Cloud server node, send GET request to the following URL:

.. code-block:: bash

    coap://host:port/api/v1/$ACCESS_TOKEN/attributes?clientKeys=attribute1,attribute2&sharedKeys=shared1,shared2

.. tabs::

    .. tab:: Example

        .. code-block:: bash

            # Send CoAP attributes request
            coap get coap://localhost/api/v1/$ACCESS_TOKEN/attributes?clientKeys=attribute1,attribute2&sharedKeys=shared1,shared2

    .. tab:: Result

        .. code-block:: json  

            {"key1":"value1"}

**Please note**: the intersection of client-side and shared device attribute keys is a bad practice! However, it is still possible to have same keys for client, shared or even server-side attributes.

Subscribe to attribute updates from the server
==============================================

In order to subscribe to shared device attribute changes, send GET request with Observe option to the following URL:

.. code-block:: bash

    coap://host:port/api/v1/$ACCESS_TOKEN/attributes

Once shared attribute will be changed by one of the server-side components (REST API or custom plugins) the client will receive the following update:

.. tabs::

    .. tab:: Example

        .. code-block:: bash

            # Subscribe to attribute updates
            coap get -o coap://localhost/api/v1/$ACCESS_TOKEN/attributes

    .. tab:: Result

        .. code-block:: json  

            {"key1":"value1"}
            
*******
RPC API
*******

Server-side RPC
===============

In order to subscribe to RPC commands from the server, send GET request with observe flag to the following URL:

.. code-block:: bash
    
    coap://host:port/api/v1/$ACCESS_TOKEN/rpc


Once subscribed, a client may receive rpc requests. An example of RPC request body is shown below:

.. code-block:: json
 
    {
        "id": "1",
        "method": "setGpio",
        "params": {
            "pin": "23",
            "value": 1
        }
    }

where

* **id** - request id, integer request identifier
* **method** - RPC method name, string
* **params** - RPC method params, custom json object

and can reply to them using POST request to the following URL:

.. code-block:: bash

    coap://host:port/api/v1/$ACCESS_TOKEN/rpc/{$id}

where **$id** is an integer request identifier.

.. tabs::

    .. tab:: Example Subscribe

        .. code-block:: bash

            # Subscribe to RPC requests
            coap get -o coap://localhost/api/v1/$ACCESS_TOKEN/rpc

    .. tab:: Example Reply

        .. code-block:: bash  

            # Publish response to RPC request
            cat rpc-response.json | coap post coap://localhost/api/v1/$ACCESS_TOKEN/rpc/1

    .. tab:: Reply Body

        .. code-block:: json 

            {"result":"ok"}


Client-side RPC
===============

In order to send RPC commands to the server, send POST request to the following URL:

.. code-block:: bash

    coap://host:port/api/v1/$ACCESS_TOKEN/rpc

Both request and response body should be valid JSON documents. The content of the documents is specific to the plugin that will handle your request.

.. tabs::

    .. tab:: Example Request

        .. code-block:: bash

            # Post client-side rpc request
            cat rpc-client-request.json | coap post coap://localhost/api/v1/$ACCESS_TOKEN/rpc

    .. tab:: Request Body

        .. code-block:: json  

            {"method": "getTime", "params":{}}

    .. tab:: Response Body

        .. code-block:: json 

            {"time":"2016 11 21 12:54:44.287"}


Protocol customization
======================

CoAP transport can be fully customized for specific use-case by changing the corresponding module.