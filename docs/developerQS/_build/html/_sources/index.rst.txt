.. Tempus Developer Quickstart documentation master file, created by
   sphinx-quickstart on Fri Apr 20 10:45:14 2018.
   You can adapt this file completely to your liking, but it should at least
   contain the root `toctree` directive.

###########################
Tempus Developer Quickstart
###########################

***********
Source Code
***********

The Tempus source code is version controlled using Git version control at `GitHub <https://github.com/hashmapinc/Tempus/>`_

The Tempus documentation source code is available at `GitHub <https://github.com/hashmapinc/Tempus/tree/dev/docs>`_

**************
Issue Tracking
**************

Track issues on `GitHub <https://github.com/hashmapinc/Tempus/issues>`_

********
Building
********

Configure your git client
=========================

We recommend running the following git config commands in order to ensure
that git checks out the repository in a consistent manner. These changes
are particularly important if running on Windows, as the git client has
trouble with long filenames otherwise. Additionally, in Windows, the
default behavior of the git client, when installed, is to set the
core.autocrlf configuration option to true, which can cause some of
the unit tests to fail.

.. code-block:: bash

  git config --global core.longpaths true
  git config --global core.autocrlf false

Checking out from Git
=====================

To check out the code:

.. code-block:: bash
  
  git clone https://github.com/hashmapinc/Tempus.git

Then checkout the 'master' branch

.. code-block:: bash
  
  git checkout master

The master branch currently represents the last release of Tempus. If you would like to work on the active / stable branch you need to check out the 'dev' branch.

.. code-block:: bash
  
  git checkout dev

.. toctree::
   :maxdepth: 2
   :caption: Contents:

************************************
Tempus Development Environment Setup
************************************


Pre-requisites
==============

* Docker
* Docker Compose
* Make
* Maven > 3.1
* JDK 8

Source Code
===========

The Tempus source code is version controlled using Git version control at `GitHub <https://github.com/hashmapinc/TempusDevEnvironment>`_

The Tempus documentation source code is available at `GitHub <https://github.com/hashmapinc/Tempus/tree/dev/docs>`_

Issue Tracking
==============

Track issues on `GitHub <https://github.com/hashmapinc/TempusDevEnvironment/issues>`_


Introduction
============
The TempusDevEnvironment orchestrates the setup of a developer environment suitable for demos or development work. This setup requires 3 repositories to be pulled:

1. TempusDevEnvironment (this one)
2. Tempus (described above)
3. Nifi-simulator-bundle (described below)

Checking out the TempusDevEnvironment from Git
==============================================

To clone the TempusDevEnvironment code:

.. code-block:: bash
  
  git clone https://github.com/hashmapinc/TempusDevEnvironment.git


To clone out the simulator bundle:

.. code-block:: bash
  
  git clone https://github.com/hashmapinc/nifi-simulator-bundle.git

Once you have cloned both repositories edit the Makefile in the root of the TempusDevEnvironment project.

Modify the top 2 lines that specify PROJECT_DIR (this is the root of the Tempus project) and the SIM_PROJECT_DIR (this is the simulator bundle that was cloned with the command above).

There are 6 options in this make file:

1. Install 
    - This will only build Tempus and the Simulator bundle following the steps in the first section

2. Copy
    - This will copy the files to the appropriate place so the docker images can be built

3. Build
    - This will build and start the docker compose 

4. Build-ldap
    - This will build the environment with openLdap and ldapAdmin

To get started run

.. code-block:: bash
    
    make all