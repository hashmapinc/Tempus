.. Tempus Documentation documentation master file, created by
   sphinx-quickstart on Wed Feb 28 08:59:08 2018.
   You can adapt this file completely to your liking, but it should at least
   contain the root `toctree` directive.

#########################################
Welcome to the Tempus Cloud Documentation
#########################################

*****
About
*****

Tempus Cloud  is an open IIoT/IoT framework (Field/Edge to Cloud/DC) and rapid analytics application creator that provides a spectrum of outcome-based experiences on an unmodified data stream and does not force customers into a proprietary approach but focuses on a use-case driven approach for top-line/bottom-line benefits. 

********
Features
********

* **Business Unit Management**:
    Tempus cloud allows for authorization by business unit to allow for segmentation of users, while still allowing all the data to be viewed in context if requred.

* **Asset Management**:
    Tempus Cloud has a flexible asset management model that is based on a graph representation. This allows for different users targeting different types of analysis to arrange the data as they wish. For example, if User A would like to look at their data geographically, they might choose to create relations on a Country/Region/City basis, while User B has the freedom to organize assets by workflows and facilities.

* **Device Management**:
    Devices can be managed and controlled via Tempus Cloud. All of the communcation is facilitated via standard MQTT. This can be secured via X.509 certificates or simple access token for authentication and authorization, and TLS for encryption of the data in motion.

    Additionally, there is a comprehensive attribute management system in place, that allows for attributes to be provided by the client, the server, or shared between the two.

* **Visualization**:
    Tempus includes a flexible Visualization and dashboarding framework. These visualizations can be created and modified on the fly allowing users to see what they need to see, when they need to see it.

* **Rules Engine**:
    There is a flexible rules engine that allows for the invocation of plugins to allow for multiple outcomes based on a combination of rule sets. These outcomes can be (but not limited to):
        
        Alarming
        Data Routing
        Emailing / Notifications
        RPC communication to the Edge
        Modification of the data flow from the edge

Rapid Computation Deployment:
    Tempus Cloud also has the capability to orchestrate and manage Spark computataions on the data. This allows for a deeper analysis of the streaming data, in which the results can be visualized in real time, or even sent back to the edge.

********
Contents
********

.. toctree::
   :maxdepth: 2
   
   quickstart/index
   features/index
   install/index
   api/index
   security/index
   admin/index
   reference/index

Indices and tables
==================

* :ref:`genindex`
* :ref:`modindex`
* :ref:`search`


