#############################
Raspberry Pi (B) Installation
#############################

This guide describes how to install Tempus Cloud on a Raspberry Pi 3 running Raspbian Jessie.

***********************************
Third-party components installation
***********************************

Java
====

Tempus Cloud service is running on Java 8. Oracle Java 8 is already pre-installed on Raspbian. You can check java version using the following command

.. code-block:: bash

    $ java -version
    java version "1.8.0_65"
    Java(TM) SE Runtime Environment (build 1.8.0_65-b17)
    Java HotSpot(TM) Client VM (build 25.65-b01, mixed mode)

Any Java version higher than or equal to 1.8 is fine

[Optional] External database installation
=========================================

Tempus Cloud is able to use SQL database. By default, Tempus Cloud  uses embedded HSQLDB instance which is very convenient for evaluation or development purposes.If this is your first experience with Tempus Cloud  we recommend to skip this step and use the embedded database. For running in production, we recommend PostgreSQL.

SQL Database: PostgreSQL
------------------------

**NOTE:** This is an **optional** step. It is required only for production usage. You can use embedded HSQLDB for platform evaluation or development. Instructions listed below will help you to install PostgreSQL.

.. code-block:: bash

    sudo apt-get update
    sudo apt-get install postgresql postgresql-contrib
    sudo service postgresql start

Once PostgreSQL is installed you may want to create a new user or set the password for the the main user.
See the following guides for more details: 

* `Using postgresql roles and databases <https://www.digitalocean.com/community/tutorials/how-to-install-and-use-postgresql-on-ubuntu-16-04#using-postgresql-roles-and-databases>`_
* `Changing the postgres user password <https://blog.2ndquadrant.com/how-to-safely-change-the-postgres-user-password-via-psql/>`_

When it’s done, connect to the database and create Tempus Cloud DB:

.. code-block:: bash

    $ psql -U postgres -d postgres -h 127.0.0.1 -W
    CREATE DATABASE thingsboard;
    \q

*********************************
Tempus Cloud service installation
*********************************

.. code-block:: bash

    # Download the package
    $ wget https://github.com/thingsboard/thingsboard/releases/download/v1.3.1/thingsboard-1.3.1.deb
    # Install ThingsBoard as a service
    $ sudo dpkg -i thingsboard-1.3.1.deb
    # Update ThingsBoard memory usage and restrict it to 150MB in /etc/thingsboard/conf/thingsboard.conf
    export JAVA_OPTS="$JAVA_OPTS -Dplatform=rpi -Xms256M -Xmx256M"

**************************************************
[Optional] Configure ThingsBoard to use PostgreSQL
**************************************************

**NOTE:** This is an **optional** step. It is required only for production usage. You can use embedded HSQLDB for platform evaluation or development. Edit Tempus Cloud configuration file:

.. code-block:: bash

    sudo nano /etc/thingsboard/conf/thingsboard.yml

Comment ‘# HSQLDB DAO Configuration’ block.

.. code-block:: yaml

    # HSQLDB DAO Configuration
    #spring:
    #  data:
    #    jpa:
    #      repositories:
    #        enabled: "true"
    #  jpa:
    #    hibernate:
    #      ddl-auto: "validate"
    #    database-platform: "org.hibernate.dialect.HSQLDialect"
    #  datasource:
    #    driverClassName: "${SPRING_DRIVER_CLASS_NAME:org.hsqldb.jdbc.JDBCDriver}"
    #    url: "${SPRING_DATASOURCE_URL:jdbc:hsqldb:file:${SQL_DATA_FOLDER:/tmp}/thingsboardDb;sql.enforce_size=false}"
    #    username: "${SPRING_DATASOURCE_USERNAME:sa}"
    #    password: "${SPRING_DATASOURCE_PASSWORD:}"

Uncomment ‘# PostgreSQL DAO Configuration’ block. Be sure to update the postgres databases username and password in the bottom two lines of the block (here, as shown, they are both “postgres”).

.. code-block:: yaml

    # PostgreSQL DAO Configuration
    spring:
    data:
        jpa:
        repositories:
            enabled: "true"
    jpa:
        hibernate:
        ddl-auto: "validate"
        database-platform: "org.hibernate.dialect.PostgreSQLDialect"
    datasource:
        driverClassName: "${SPRING_DRIVER_CLASS_NAME:org.postgresql.Driver}"
        url: "${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/thingsboard}"
        username: "${SPRING_DATASOURCE_USERNAME:postgres}"
        password: "${SPRING_DATASOURCE_PASSWORD:postgres}"

***********************
Run installation script
***********************

Once Tempus Cloud service is installed, you can execute the following script:

.. code-block:: bash

    # --loadDemo option will load demo data: users, devices, assets, rules, widgets.
    sudo /usr/share/thingsboard/bin/install/install.sh --loadDemo

**************************
Start Tempus Cloud Service
**************************

Execute the following command to start Tempus Cloud:

.. code-block:: bash

    sudo service tempus start

Once started, you will be able to open Web UI using the following link:

.. code-block:: bash

    http://localhost:8080/

**NOTE:** Please allow up to 2 minutes for the Web UI to start

***************
Troubleshooting
***************

Tempus Cloud logs are stored in the following directory:

.. code-block:: bash

    /var/log/tempus

You can issue the following command in order to check if there are any errors on the backend side:

.. code-block:: bash

    cat /var/log/tempus/tempus.log | grep ERROR