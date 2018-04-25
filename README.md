<!--

    Copyright © 2017-2018 Hashmap, Inc

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

-->

<img src="https://github.com/hashmapinc/hashmap.github.io/blob/master/images/tempus/TempusLogoBlack2.png" width="910" height="245" alt="Hashmap, Inc Tempus"/>

[![License](http://img.shields.io/:license-Apache%202-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.txt) 
[![Build Status](https://travis-ci.org/hashmapinc/Tempus.svg?branch=dev)](https://travis-ci.org/hashmapinc/Tempus)
[![Docker pulls](https://img.shields.io/docker/pulls/hashmapinc/tempus.svg)](https://hub.docker.com/r/hashmapinc/tempus/)
[![CLA assistant](https://cla-assistant.io/readme/badge/hashmapinc/Tempus)](https://cla-assistant.io/hashmapinc/Tempus)
[![Quality Gate](https://sonarcloud.io/api/badges/gate?key=com.hashmapinc%3Atempus)](https://sonarcloud.io/dashboard/index/com.hashmapinc%3Atempus)
[![Slack](https://now-examples-slackin-sdipawcoxa.now.sh/badge.svg)](https://now-examples-slackin-sdipawcoxa.now.sh)


[Tempus](https://www.hashmapinc.com/tempuscloud) is an IIoT framework for industrial data ingestion and analysis.

[![Tempus Video](https://img.youtube.com/vi/BQ8QG5S3-Fc/0.jpg)](https://www.youtube.com/watch?v=BQ8QG5S3-Fc)


### Table of Contents

- [Features](#features)
- [Requirements](#requirements)
- [Getting Started](#getting-started)
- [Getting Help](#getting-help)
- [Documentation](#documentation)
- [License](#license)
- [Export Control](#export-control)

## Features

Tempus was made to simplify ingest/analysis/storage/visualization of your IIoT data. Some of the key features include:

- Device Management
  - Configure and Control your edge devices and acquistion conifigurations
  - This can range from simple data ingestion to complex data analysis and transformation
- Security
  - Data transmission via MQTT over TLS
  - Device authorization via Tokens or X.509 certificates
  - Supports LDAP or built in authentication
- IIoT protocol support
  - Support for:
    - CoAP
    - MQTT
    - HTTP(S)
    - OPC-UA
    - WITSML 1.3.1.1/1.4.1.1 (as a client)
    - Sparkplug B
    - Apache NiFi (via any flow, using the Tempus supplied NAR files to transmit data to the system via MQTT)
- Built in Rules engine for Data flow
  - Using visually-created rules, data can be routed where it needs to go, when it needs to go
  - Data formatting provided by Apache Velocity
- Designed for extensiblity
  - Ability to integrate with any system on the backend. Out of the box support for:
    - Apache Kafka
    - AWS SQS/SNS
    - Kudu
    - HBase
    - MQTT
    - REST API endpoints
- Comprehensive Visualization System
  - Ability to create drill down and specific views that include
    - Charts
    - Maps
    - Gauges
    - Digital I/O
    - and more
- Scalable
  - Scales with a zero-master clustering model (orchestrated by Zookeeper)
- Configurable Storage
  - Built in data store support for:
    - HSQLDB (Demos/Development/Testing)
    - PostgreSQL
    - Cassandra (recommended for production)
  - Caching support via Redis

## Requirements

* JDK 1.8 or newer
* Apache Maven 3.1.0 or newer
* Git Client

## Getting Started

- Read through the [Tempus Developer Quickstart](http://tempus-cloud.s3-website-us-west-2.amazonaws.com/help/developerQS/html/).
  It will include a section on how to build the local development environment via Docker to get you up and running quickly.

To build:
- Execute `mvn clean install` or for parallel build execute `mvn -T 2.0C clean install`. On a
  modest development laptop that is a couple of years old, the latter build takes a bit under fifteen
  minutes. After a large amount of output you should eventually see a success message.

      [INFO] ------------------------------------------------------------------------
      [INFO] Reactor Summary:
      [INFO] 
      [INFO] Tempus ............................................. SUCCESS
      [INFO] Tempus Server Commons .............................. SUCCESS
      [INFO] Tempus Server Common Data .......................... SUCCESS
      [INFO] Tempus Server Common Messages ...................... SUCCESS
      [INFO] Tempus Server Common Transport components .......... SUCCESS
      [INFO] Tempus Server DAO Layer ............................ SUCCESS
      [INFO] Tempus Server Extensions API ....................... SUCCESS
      [INFO] Tempus Server Core Extensions ...................... SUCCESS
      [INFO] Tempus Extensions .................................. SUCCESS
      [INFO] Tempus Server RabbitMQ Extension ................... SUCCESS
      [INFO] Tempus Server REST API Call Extension .............. SUCCESS
      [INFO] Tempus Server Kafka Extension ...................... SUCCESS
      [INFO] Tempus Server MQTT Extension ....................... SUCCESS
      [INFO] Tempus Server Livy Extension ....................... SUCCESS
      [INFO] Tempus Server SQS Extension ........................ SUCCESS
      [INFO] Tempus Server SNS Extension ........................ SUCCESS
      [INFO] Tempus Server Transport Modules .................... SUCCESS
      [INFO] Tempus HTTP Transport .............................. SUCCESS
      [INFO] Tempus COAP Transport .............................. SUCCESS
      [INFO] Tempus MQTT Transport .............................. SUCCESS 
      [INFO] Tempus Server UI ................................... SUCCESS
      [INFO] Tempus Server Tools ................................ SUCCESS 
      [INFO] Tempus Server Application .......................... SUCCESS 
      [INFO] ------------------------------------------------------------------------
      [INFO] BUILD SUCCESS
      [INFO] ------------------------------------------------------------------------
      [INFO] Total time: 26:17 min
      [INFO] Finished at: 2018-04-22T02:01:32Z
      [INFO] Final Memory: 146M/2153M
      [INFO] ------------------------------------------------------------------------

## Getting Help
If you have questions, you can join our slack channel using the link above or [here](https://now-examples-slackin-sdipawcoxa.now.sh).

You can also submit issues or questions via GitHub Issues [here](https://github.com/hashmapinc/Tempus/issues)

## Documentation

See [The Documentation Here](http://tempus-cloud.s3-website-us-west-2.amazonaws.com/help/) for the latest updates.

## License

Except as otherwise noted this software is licensed under the
[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

## Export Control

This distribution includes cryptographic software. The country in which you
currently reside may have restrictions on the import, possession, use, and/or
re-export to another country, of encryption software. BEFORE using any
encryption software, please check your country's laws, regulations and
policies concerning the import, possession, or use, and re-export of encryption
software, to see if this is permitted. See <http://www.wassenaar.org/> for more
information.

The U.S. Government Department of Commerce, Bureau of Industry and Security
(BIS), has classified this software as Export Commodity Control Number (ECCN)
5D002.C.1, which includes information security software using or performing
cryptographic functions with asymmetric algorithms. The form and manner of this
Apache Software Foundation distribution makes it eligible for export under the
License Exception ENC Technology Software Unrestricted (TSU) exception (see the
BIS Export Administration Regulations, Section 740.13) for both object code and
source code.

The following provides more details on the included cryptographic software:

Tempus uses BouncyCastle and the built-in
java cryptography libraries for SSL, SSH. See
http://bouncycastle.org/about.html
http://www.oracle.com/us/products/export/export-regulations-345813.html
for more details on each of these libraries cryptography features.
