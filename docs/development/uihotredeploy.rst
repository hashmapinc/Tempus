####################
UI Development Guide
####################

*****************************************
Running UI container in hot redeploy mode
*****************************************

By default, the Tempus Cloud UI is served on port 8080. However, when developing the UI, developing in hot redeploy mode is significantly more efficent.

To start the UI container in hot redeploy mode you will need to install node.js first. Once node.js is installed you can start container by executing next command:

.. code-block:: bash

    cd ${TEMPUS_WORK_DIR}/ui
    mvn clean install -P npm-start

This will launch a server that will listen on 3000 port. All REST API and websocket requests will be forwarded to 8080 port.

*****************************
Running server-side container
*****************************

To start server-side container there are 2 options:

* Run the main method of com.hashmapinc.server.TempusServerApplication class that is located in the application module from the IDE.

* Start the server from command line as a regular Spring boot application:

    .. code-block:: bash

        cd ${TB_WORK_DIR}
        java -jar application/target/tempus-${VERSION}-boot.jar

* Run the TempusDevEnvironment as described in the Developer Quick Start

*******
Dry run
*******

Navigate to http://localhost:3000/ or http://localhost:8080/ and login into Tempus Cloud using demo data credentials:

**login** demo@hashmapinc.com
**password** tenant

Make sure that you are able to login and everything has started correctly.