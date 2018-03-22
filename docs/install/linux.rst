##################
Linux Installation
##################

This guide describes how to install Tempus Cloud on a Linux based server machine. Instructions below are provided for Ubuntu 16.04 and CentOS 7. These instructions can be easily adapted to other similar operating	systems.

*********************
Hardware requirements
*********************

To run Tempus Cloud and third-party components on a single machine you will need at least 1Gb of RAM.

***********************************
Third-party components installation
***********************************

Java
====

Tempus Cloud service is running on Java 8. Although you are able to start the service using OpenJDK, the solution is actively tested on Oracle JDK.

Follow this instructions to install Oracle JDK 8:

* `Ubuntu 16.04 <https://www.digitalocean.com/community/tutorials/how-to-install-java-with-apt-get-on-ubuntu-16-04#installing-the-oracle-jdk>`_
* `CentOS 7 <https://www.digitalocean.com/community/tutorials/how-to-install-java-on-centos-and-fedora#install-oracle-java-8>`_

Please don’t forget to configure your operating system to use Oracle JDK 8 by default. Corresponding instructions are in the same articles listed above.

[Optional] External database installation
=========================================

Tempus Cloud is able to use a SQL or Cassandra database. By default, Tempus Cloud uses embedded HSQLDB instance which is convenient for evaluation or development purposes.
If this is your first experience with Tempus Cloud we recommend to skip this step and use the embedded database. 
Alternatively, you can configure your platform to use either scalable Cassandra DB cluster or various SQL databases. If you prefer to use an SQL database, we recommend PostgreSQL.

SQL Database: PostgreSQL
------------------------

**NOTE**: This is an **optional** step. It is required only for production usage. You can use embedded HSQLDB for platform evaluation or development
Instructions listed below will help you to install PostgreSQL.

.. tabs::

    .. tab:: CentOS

        .. code-block:: bash

            Copy resources/postgresql-ubuntu-installation.sh to clipboard
            # Update your system
            sudo apt-get update
            # Install packages
            sudo apt-get install postgresql postgresql-contrib
            # Initalize PostgreSQL DB
            sudo service postgresql start
            # Optional: Configure PostgreSQL to start on boot
            sudo systemctl enable postgresq

    .. tab:: Ubuntu

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

    psql -U postgres -d postgres -h 127.0.0.1 -W
    CREATE DATABASE Tempus;
    \q

NoSQL Database: Cassandra
-------------------------
**NOTE:** This is an **optional** step. It is required only for production usage. You can use embedded HSQLDB for platform evaluation or development instructions listed below will help you to install Cassandra.

.. tabs::

    .. tab:: CentOS

        .. code-block:: bash

            sudo touch /etc/yum.repos.d/datastax.repo
            echo '[datastax]' | sudo tee --append /etc/yum.repos.d/datastax.repo > /dev/null
            echo 'name = DataStax Repo for Apache Cassandra' | sudo tee --append /etc/yum.repos.d/datastax.repo > /dev/null
            echo 'baseurl = http://rpm.datastax.com/community' | sudo tee --append /etc/yum.repos.d/datastax.repo > /dev/null
            echo 'enabled = 1' | sudo tee --append /etc/yum.repos.d/datastax.repo > /dev/null
            echo 'gpgcheck = 0' | sudo tee --append /etc/yum.repos.d/datastax.repo > /dev/null

            # Cassandra installation
            sudo yum install dsc30
            # Tools installation
            sudo yum install cassandra30-tools
            # Start Cassandra
            sudo service cassandra start
            # Configure the database to start automatically when OS starts.
            sudo chkconfig cassandra on

    .. tab:: Ubuntu

        .. code-block:: bash

            # Add cassandra repository
            echo 'deb http://www.apache.org/dist/cassandra/debian 311x main' | sudo tee --append /etc/apt/sources.list.d/cassandra.list > /dev/null
            curl https://www.apache.org/dist/cassandra/KEYS | sudo apt-key add -
            sudo apt-get update
            ## Cassandra installation
            sudo apt-get install cassandra
            ## Tools installation
            sudo apt-get install cassandra-tools

*********************************
Tempus Cloud Service Installation
*********************************

.. tabs::

    .. tab:: CentOS

        .. code-block:: bash

            sudo rpm -Uvh tempus-1.3.1.rpm

    .. tab:: Ubuntu

        .. code-block:: bash

            sudo dpkg -i tempus-1.3.1.deb

*************************************************************
[Optional] Configure Tempus Cloud to use an external database
*************************************************************

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

********************************************
Memory Update for Slow Machines (1GB of RAM)
********************************************

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

**************************
Start Tempus Cloud service
**************************

Execute the following command to start Tempus Cloud:

.. code-block:: bash

    sudo service tempus start

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

You can issue the following command in order to check if there are any errors on the backend side:

.. code-block:: bash

    cat /var/log/tempus/tempus.log | grep ERROR

