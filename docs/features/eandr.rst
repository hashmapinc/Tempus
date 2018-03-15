######################
Entities and Relations
######################

*****************
Entities Overview
*****************

Tempus Cloud provides the user interface and REST APIs to provision and manage multiple entity types and their relations in your IoT application. Supported entities are:

* **Tenants** - you can treat tenant as a separate business-entity: individual or organization who owns or produce devices and assets; Tenant may have multiple tenant administrator users and millions of customers;
* **Customers** - customer is also a separate business-entity: individual or organization who purchase or uses tenant devices and/or assets; Customer may have multiple users and millions of devices and/or assets;
* **Users** - users are able to browse dashboards and manage entities;
* **Devices** - basic IoT entities that may produce telemetry data and handle RPC commands. For example sensors, actuators, switches;
* **Assets** - abstract IoT entities that may be related to other devices and assets. For example factory, field, vehicle;
* **Alarms** - events that identify issues with your assets, devices or other entities;
* **Dashboards** - visualization of your IoT data and ability to control particular devices through user interface;
* **Rules** - processing units for incoming messages, entity lifecycle events, etc;
* **Plugins** - extensions to the platform that process IoT data and help to integrate with other server applications

Each entity supports:

* **Attributes** - static and semi-static key-value pairs associated with entities. For example serial number, model, firmware version;
* **Telemetry data** - time-series data points available for storage, querying and visualization. For example temperature, humidity, battery level;
* **Relations** - directed connections to other entities. For example contains, manages, owns, produces.

Additionally, devices and assets also have a type. This allows distinguising them and process data from them in a different way.
This guide provides the overview of the features listed above, some useful links to get more details and real-life examples of their usage.

*********************
Real-life application
*********************

The easiest way to understand the concepts of Tempus is to implement your first Tempus application. Let’s assume we want to build an application that collects data from soil moisture and temperature sensors, visualize this data on the dashboard, detect issues, raise alarms and control the irrigation.
Let’s also assume we want to support multiple fields with hundreds of sensors. Fields may be also grouped to the Geo regions.
We believe there should be following logical steps to build such an application:

********************************
Provision entities and relations
********************************

We are going to setup following hierarchy of assets and devices:

.. image:: ../_images/eandr_heirarchy.png
    :align: center
    :alt: A sample asset hierarchy

Please review the following screen cast to learn how to provision land lease, field, and pad assets and their relations using Tempus Web UI

Please review the following screen cast to learn how to provision devices and their relations with assets using Tempus Web UI

You can automate this actions using Tempus REST API. You can provision new assets by using POST requests to the following URL

.. code-block:: bash

    http(s)://host:port/api/asset

For example:

.. tabs::

    .. tab:: create-asset.sh

        .. code-block:: bash

            curl -v -X POST -d @create-asset.json http://localhost:8080/api/asset \
            --header "Content-Type:application/json" \
            --header "X-Authorization: $JWT_TOKEN

    .. tab:: create-asset.json

        .. code-block:: json

            {"name":"Field C","type":"field"}

**Note:** in order to execute this request, you will need to substitute **$JWT_TOKEN** with a valid JWT token. This token should belong to a user with **TENANT_ADMIN** role. You can use following guide to get the token.

Also, you can provision new relation using POST request to the following URL

.. code-block:: bash

    http(s)://host:port/api/asset

.. tabs::

    .. tab:: create-relation.sh

        .. code-block:: bash

            curl -v -X POST -d @create-asset.json http://localhost:8080/api/relation \
            --header "Content-Type:application/json" \
            --header "X-Authorization: $JWT_TOKEN"

    .. tab:: create-relation.json

        .. code-block:: json

           {"from":{"id":"$FROM_ASSET_ID","entityType":"ASSET"},"type":"Contains","to":{"entityType":"ASSET","id":"$TO_ASSET_ID"}}
    
    **Note:** Don’t forget to replace $FROM_ASSET_ID and $TO_ASSET_ID with valid asset ids. **Note:** One can relate any entities. For example, assets to devices or assets to users. You can receive them as a result of previous REST API call or use Web UI.