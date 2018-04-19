##############################
Tempus Installation on PowerPC
##############################

This guide describes how to install Tempus Cloud on a IBM PowerPC based server machine. Instructions below are provided for PowerPC CentOS. These instructions can be easily adapted to other similar operating systems.

*********************
Hardware requirements
*********************

To run Tempus Cloud and third-party components on a single machine you will need at least 1Gb of RAM.

***********************************
Third-party components installation
***********************************

Java
====

Tempus Cloud service is running on Java 8. Although you are able to start the service using OpenJDK.

Follow this instructions to install IBM JDK 8:

* `CentOS 7 <https://developer.ibm.com/javasdk/downloads/sdk8>`_




EPEL
====

Extra Packages for Enterprise Linux (or EPEL) is a Fedora Special Interest Group that creates, maintains, and manages a high quality set of additional packages for Enterprise Linux.
To install Docker, the Epel repositories must be enabled on your system by issuing the following command.

.. tabs::

    .. tab:: Install EPEL

        .. code-block:: bash

            yum install epel-release

Python
======

Python 2.7.5
pip is a package management system used to install and manage software packages written in Python.

.. tabs::

    .. tab:: Install Python

        .. code-block:: bash

            yum install python
            yum install -y python-pip


Docker
======

Install Docker Engine.After, Docker package has been installed, start the daemon, check its status and enable it system wide using.


.. tabs::

    .. tab:: Install Docker

        .. code-block:: bash

            yum install docker
            systemctl start docker
            systemctl enable docker

Docker Compose
==============

Docker Compose is a tool for running multi-container Docker applications. To configure an application’s services with Compose we use a configuration file,
and then, executing a single command, it is possible to create and start all the services specified in the configuration.

.. tabs::

    .. tab:: Install Docker Compose

        .. code-block:: bash

            yum install docker-compose



TempusDevEnvironment repository
===============================

Download following files from the repository.

docker-compose.yml -> main docker-compose file.

.env -> main env file that contains default location of cassandra data folder and cassandra schema.

tb.env -> default tempus environment variables.


External database installation
=========================================

Tempus Cloud is able to use a SQL or Cassandra database. By default, Tempus Cloud uses embedded HSQLDB instance which is convenient for evaluation or development purposes.
Alternatively, you can configure your platform to use either scalable Cassandra DB cluster or various SQL databases.


**************************************************
Configure Tempus Cloud to use an external database
**************************************************

**NOTE:** This is an **optional** step. It is required only for production usage. You can use embedded HSQLDB for platform evaluation or development

Edit Tempus Cloud configuration file

.. code-block:: bash

    sudo nano /etc/Tempus/conf/Tempus.yml

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
    #    url: "${SPRING_DATASOURCE_URL:jdbc:hsqldb:file:${SQL_DATA_FOLDER:/tmp}/TempusDb;sql.enforce_size=false}"
    #    username: "${SPRING_DATASOURCE_USERNAME:sa}"
    #    password: "${SPRING_DATASOURCE_PASSWORD:}"

For *PostgreSQL*:

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
        url: "${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/Tempus}"
        username: "${SPRING_DATASOURCE_USERNAME:postgres}"
        password: "${SPRING_DATASOURCE_PASSWORD:postgres}"

For *Cassandra DB*:
Locate and set database type configuration parameter to ‘cassandra’.

.. code-block:: yaml

    database:
        type: "${DATABASE_TYPE:cassandra}" # cassandra OR sql

NoSQL Database: Cassandra
-------------------------
It is recommended to use Cassandra for production environment.


.. tabs::

    .. tab:: CentOS

        .. code-block:: bash

            docker-compose build cassandra
            docker-compose up -d cassandra


*********************************
Tempus Cloud Service Installation
*********************************

.. tabs::

    .. tab:: CentOS

        .. code-block:: bash

            docker-compose build tb

*********************************
Memory Configuration (1GB of RAM)
*********************************

We recommend to use embedded HSQLDB or PostgreSQL DB in this setup. We don’t recommend to use Cassandra on machines with less then 4GB of RAM.

For Tempus Cloud service:

.. code-block:: bash

    # Update Tempus memory usage and restrict it to 256MB in /etc/Tempus/conf/Tempus.conf
    export JAVA_OPTS="$JAVA_OPTS -Xms256M -Xmx256M"

***********************
Run installation script
***********************

Once Tempus Cloud service is installed, you can execute the following script:

.. code-block:: bash

    # --loadDemo option will load demo data: users, devices, assets, rules, widgets.
    sudo /usr/share/Tempus/bin/install/install.sh --loadDemo


**************
Firewall Setup
**************

Tempus is configured to run on 8080 port. This port should be accessible.

sudo firewall-cmd --zone=public --add-port=8080/tcp --permanent
sudo firewall-cmd --reload

**************************
Start Tempus Cloud service
**************************

Execute the following command to start Tempus Cloud:

.. code-block:: bash

    docker-compose up -d tb

Once started, you will be able to open Web UI using the following link:

.. code-block:: bash

    http://localhost:8080/

**NOTE:** Please allow up to 90 seconds for the Web UI to start


***************
Troubleshooting
***************

Tempus Cloud logs are stored in the following directory:

.. code-block:: bash

    /var/log/tempus

Check for errors in following logs:

.. code-block:: bash

    cat /var/log/tempus/tempus.log | grep ERROR

**************
Stop Cassandra
**************

Execute the following command to stop Tempus Cloud:

.. code-block:: bash

    docker-compose stop cassandra

**************************
Stop Tempus Cloud service
**************************

Execute the following command to stop Tempus Cloud:

.. code-block:: bash

    docker-compose stop tb

