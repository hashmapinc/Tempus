##################
AWS EC2 Installation
##################

This guide describes how to install `Tempus Cloud <https://www.hashmapinc.com/tempuscloud>`_ on AWS EC2 using community AWS AMIs.

Choose AMI type, instance type and region
=========================================

AMI is based on the microservices version of `Tempus Cloud <https://www.hashmapinc.com/tempuscloud>`_. The microservices are deployed as docker containers using docker-compose on the Amazon Linux 2 OS. This AMI to simplify the deployment and getting started process. We recommend to use the AMI as a trial environment and move to `Tempus Cloud on EKS <https://tempus.hashmapinc.com/login>`_ once you plan a production deployment.
For Tempus Cloud AMI you can choose any instance type with at least 4GB of RAM. The AMI is available only in the N. Virginia zone.

Use the following link to start the installation of AMIs:

* `N. Virginia <https://console.aws.amazon.com/ec2/v2/home?region=us-east-1#LaunchInstanceWizard:ami=ami-0dfd0db3149f3703e>`_

Configure Instance
=========================================

No specific configuration items here. You can choose a t2.medium instance or above.

Add Storage
=========================================

Minimum 20 Gb of Storage is required. We recommend having at least 50 if you plan to upload some data.

Add Tags
=========================================

No specific configuration items here. You can leave this tab without changes or apply a configuration that is specific to your use-case.

Configure Security Group
=========================================

We recommend to create new security group, for example "Tempus". Configure following inbound rules:

+------------------------+------------+------------+------------+
| Type                   | Protocol   | Port Range |   Source   |
+========================+============+============+============+
| HTTP                   | TCP        | 80         |  0.0.0.0/0 |
+------------------------+------------+------------+------------+
| SSH                    | TCP        | 22         |  0.0.0.0/0 |
+------------------------+------------+------------+------------+
| Custom TCP Rule        | TCP        | 1883       | 0.0.0.0/0  |
+------------------------+------------+------------+------------+

Review and launch your instance
=========================================

Once the instance is launched, please wait some time for services to boot up and open Administration UI in the browser using public DNS from instance details.


Accessing Tempus Cloud service
=========================================

Once the instance from AMI is created, please access http://your_public_ip to access the application.

You can use the following credentials to login: 

+------------------------+------------+------------+------------+
| username(email)        | Password   | Role                    |
+========================+============+=========================+
|sysadmin@hashmapinc.com | sysadmin   | System Administrator    |
+------------------------+------------+-------------------------+
|demo@hashmapinc.com     | tenant     | Tenant                  |
+------------------------+------------+-------------------------+
|bob.jones@hashmapinc.com| driller    | User                    |
+------------------------+------------+-------------------------+

**NOTE:** Please allow up to 90 seconds for the Web UI to start

***************
Troubleshooting
***************

Tempus Cloud logs can be veiwed using:

.. code-block:: bash

    docker logs -f $(docker ps -q --filter "ancestor=hashmapinc/tempus:dev")

For identity and discovery service logs:

.. code-block:: bash

    docker logs -f $(docker ps -q --filter "ancestor=hashmapinc/redtail-api-discovery:latest")
    docker logs -f $(docker ps -q --filter "ancestor=hashmapinc/redtail-identity-service:latest")


You can issue the following command in order to check if there are any errors on the backend side:

.. code-block:: bash

    docker logs $(docker ps -q --filter "ancestor=hashmapinc/hashmapinc/tempus:dev") | grep ERROR

***************
Issue Logging 
***************

You can log your issues at:

* `GitHub <https://github.com/hashmapinc/Tempus/issues>`_





