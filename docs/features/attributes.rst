####################
Attribute Management
####################

***************
Attribute types
***************

Attributes are separated into three main groups:

* **server-side** - attributes are reported and managed by the server-side application. Not visible to the device application. Some secret data that may be used by tempus cloud rules, but should not be available to the device. Any Tempus Cloud entity supports server-side attributes: Device, Asset, Customer, Tenant, Rules, etc.

image

* **client-side** - see device specific attributes
* **shared** - see device specific attributes


Device specific Attribute types
===============================

All attributes may be used in Rule Engine components: filters, processors, and actions. This guide provides the overview of the features listed above and some useful links to get more details.

Device specific attributes are separated into two main groups:

* **client-side** - attributes are reported and managed by the device application. For example current software/firmware version, hardware specification, etc.

image

* **shared** - attributes are reported and managed by the server-side application. Visible to the device application. For example customer subscription plan, target software/firmware version.

image

*********************
Device attributes API
*********************

Tempus Cloud provides following API to device applications:

* upload client-side attributes to the server
* request client-side and shared attributes from the server.
* subscribe to updates of shared attributes.

Attributes API is specific for each supported network protocol. You can review API and examples in corresponding reference page:

.. toctree::
    :maxdepth: 1

    ../api/mqtt
    ../api/coap
    ../api/http

****************
Telemetry plugin
****************

Tempus Cloud consists of core services and pluggable modules called plugins. Telemetry plugin is responsible for persisting attributes data to internal data storage; provides server-side API to query and subscribe for attribute updates. Since Telemetry plugin functionality is critical for data visualization purposes in dashboards, it is configured on the system level by a system administrator. Advanced users or platform developers can customize telemetry plugin functionality.

Internal data storage
=====================

Tempus Cloud uses either Cassandra NoSQL database or SQL database to store all data.

Although you can query the database directly, Tempus Cloud provides a set of RESTful and Websocket API that simplify this process and apply certain security policies:

Tenant Administrator user is able to manage attributes for all entities that belong to the corresponding tenant.
Customer user is able to manage attributes only for entities that are assigned to the corresponding customer.

Data Query API
==============

Telemetry plugin provides following API to fetch device attributes:

Attribute keys API
------------------

You can fetch list of all attribute keys for particular entity type and entity id using GET request to the following URL

.. code-block:: bash
    
    http(s)://host:port/api/plugins/telemetry/{entityType}/{entityId}/keys/attributes

.. tabs::

    .. tab:: get-attributes-keys.sh

        .. code-block:: bash

            curl -v -X GET http://localhost:8080/api/plugins/telemetry/DEVICE/ac8e6020-ae99-11e6-b9bd-2b15845ada4e/keys/attributes \
            --header "Content-Type:application/json" \
            --header "X-Authorization: $JWT_TOKEN"
          
    .. tab:: get-attributes-keys-result.json
        .. code-block:: json

            ["model","softwareVersion"]

Supported entity types are: TENANT, CUSTOMER, USER, RULE, PLUGIN, DASHBOARD, ASSET, DEVICE, ALARM

Attribute values API
--------------------

You can fetch list of latest values for particular entity type and entity id using GET request to the following URL

.. code-block:: bash
    
    http(s)://host:port/api/plugins/telemetry/{entityType}/{entityId}/values/attributes?keys=key1,key2,key3

.. tabs::

    .. tab:: get-attributes-values.sh

        .. code-block:: bash

            curl -v -X GET http://localhost:8080/api/plugins/telemetry/DEVICE/ac8e6020-ae99-11e6-b9bd-2b15845ada4e/values/attributes?keys=model,softwareVersion \
            --header "Content-Type:application/json" \
            --header "X-Authorization: $JWT_TOKEN"
          
    .. tab:: get-attributes-values-result.json
    
        .. code-block:: json

            [
                {
                    "lastUpdateTs": 1479735871836,
                    "key": "model",
                    "value": "Model 42"
                },
                {
                    "lastUpdateTs": 1479735871836,
                    "key": "softwareVersion",
                    "value": "1.0.0"
                }
            ]

Supported entity types are: TENANT, CUSTOMER, USER, RULE, PLUGIN, DASHBOARD, ASSET, DEVICE, ALARM