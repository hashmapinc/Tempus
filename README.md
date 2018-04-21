<img src="https://github.com/hashmapinc/hashmap.github.io/blob/master/images/tempus/TempusLogoBlack2.png" width="910" height="245" alt="Hashmap, Inc Tempus"/>

[![License](http://img.shields.io/:license-Apache%202-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.txt) 
[![Build Status](https://travis-ci.org/hashmapinc/Tempus.svg?branch=dev)](https://travis-ci.org/hashmapinc/Tempus)
[![Docker pulls](https://img.shields.io/docker/pulls/hashmapinc/tempus.svg)](https://hub.docker.com/r/hashmapinc/tempus/)
[![CLA assistant](https://cla-assistant.io/readme/badge/hashmapinc/Tempus)](https://cla-assistant.io/hashmapinc/Tempus)


Tempus is an open IIoT framework for industrial data ingestion and analysis.


# Depth Data Support 

Thingsboard can now support depthseries data also. Earlier thingsboard supported timeseries data i.e timestamp based telemetry data, but there is a need for the support of depthseries data also i.e depth based telemetry data.

[**Smart metering**](https://thingsboard.io/smart-metering/)
[![Smart metering](https://user-images.githubusercontent.com/8308069/31455788-6888a948-aec1-11e7-9819-410e0ba785e0.gif "Smart metering")](https://thingsboard.io/smart-metering/)

1. There is only one configuration to be made, it is in thingsboard.yml. Under the heading UI Related configuration set depthSeries to true like:
```
#UI Related Configuration
  configurations:
    ui:
      depthSeries: "true"
```

## Build

1. Checkout branch "save_depthdata_to_thingsboardDb".
2. Do : mvn clean install -DskipTests

## Add a rule for depthseries data

1. Create a new rule similar to system telemetry rule with message type filter, just set the message types as "POST_TELEMETRY_DEPTH" for filter. Save and start the rule.

## Publish Depthseries Data to tempus

1. Presently depthseries data can be published to tempus via MQTT only. The MQTT topic for publishing depthseries data is:
   **v1/devices/me/depth/telemetry**

2. The JSON format to publish depthseries data to above topic

* Depth data as JSON array.
```json
   [{"ds":3000.1,"values":{"viscosity":0.5, "humidity":72.0}}, {"ds":3000.2,"values":{"viscosity":0.7, "humidity":69.0}}]
```
* Depth data as JSON object.
```json
   {"ds":5844.23,"values":{"viscosity":0.1, "humidity":22.0}}
```
   **Note:** the json has ds for depth instead of ts which is for timestamp.

3. Now depthseries data can be published to tempus similar to timeseries telemetry data using mqtt publish.

## Visualize depthseries data

### LATEST depthseries data

1. Latest values can be seen on the respective devices on which the depthseries data has been published.
2. It can also be visualized on widgets: analog and digital gauges.
* For that create/use a new/existing dashboard. Use the following link for that
  -[Getting Started](https://thingsboard.io/docs/getting-started-guides/helloworld/)
  Go to the heading "Create new dashboard to visualize the data".

### REALTIME OR HISTORICAL depthseries data

1. Go to the the dashboard created/used in the previous step.
2. Create a new widget of chart type as "new depthseries flot".
3. Add datasources with Entity alias as device name and then select from depthseries keys.
4. Click add. A "new depthseries flot" would get displayed on dashboard for realtime depthseries data for the selected key(s)(It needs realtime depthseries data being published to thingsboard).
5. For historical data go to history tab of the depthwindow panel present at top right of the dashboard(A clock icon).
6. Set the required range of depth and click update.
7. Now the depthseries data for the specified range and the selected key(s) would get plotted on the "new depthseries flot".
**Note:** Keep data aggregation function in depthwindow panel as "None".


## Enabling LDAP Security

The default installation doesn't use LDAP security.  However, It can be changed to use LDAP server for authentication and tempus to authorize the user based on the authentication.

To enable LDAP authentication change the value of flag 'authentication-enabled' under 'ldap' in thingsboard.yml to 'true'. Other settings under ldap also needs to be changed accordingly to point to the right ldap server, dn etc.

The corresponding code can be found in class ThingsboardSecurityConfiguration.java and RestAuthenticationProvider.java. Please refer the official oracle documentation on how LDAP security has been implemented - [LDAP authentication in Java](https://docs.oracle.com/javase/jndi/tutorial/ldap/security/ldap.html)

## SCHEMA MODIFICATION

To upgrade the schema(modify the existing tables), Put the upgrade scripts in upgrade folder under resources in "dao" module. there are different folders "sql" and "cassandra" for corresponding database. Note that the naming format of the upgrade script needs to be - [Name].[Extension] Where name can only be number[anything else other then number in name will cause an ERROR] and upgrades will be applied in the order of names sorted numerically by name and extension can be either sql or cql

example - 1.sql, 5.sql, 4.sql, 3.sql, 2.sql (or extension cql for cassandra)

in the example above the order in which scripts will be applied is - 1.sql, 2.sql, 3.sql, 4.sql, 5.sql


# Spark Annotations

## Introduction

This feature allows to add Plugin actions dynamically for Spark Computation Plugin, once Spark application jar annotated with [annotations](https://github.com/hashmapinc/TempusSparkAnnotations) is placed in configured directory. ComputationDiscoveryService is configured to scan these jars for create/modify/delete events and process them.

### Configurations

For polling 2 configurations need to be made.
1. SPARK_COMPUTATIONS_PATH : Directory to poll for jar files
2. DIRECTORY_POLLING_INTERVAL : Polling interval to scan jars from directory, in seconds.

### Annotations

Spark application jar should contain a class with below annotations to be added as a dynamic action for Plugin
1. **SparkAction** : Main annotation which will be used to build Action class, with below configurable settings.

```java
  @SparkAction(applicationKey = "TEST_APPLICATION", name = "Test Application", actionClass = "TestSparkAction", descriptor = "TestAppActionDescriptor.json")
```

  * *name* : Name to be displayed on UI for action.

  * *actionClass* : Absolute Class name of an action without package e.g. TestSparkComputationAction.

  * *applicationKey* : Application name string to be used as identifier to identitify which spark applications are running.

  * *descriptor* : Option field pointing to React JSON schema file for configuring Spark input parameters. (This json file if customized should be present under resources folder).
 
2. **Configurations** : This annotation will be used to generate Configuration class to hold spark application paramteres eneterd through action descriptor. Once generated class it will extend to *SparkComputationPluginActionConfiguration*

 * *className* : Absolute class name to be generated e.g. TestSparkComputationActionConfiguration.
 
 * *mappings* : Optional array of ConfigurationMappings to map field names to Java Types, see below.
 
3. **ConfigurationMappings** : Hold information of each member variable of Configuration Class.

 * *field* : Name of a member variable to add to Configuration class generated, e.g. zkUrl, kafkaBrokers etc.
 
 * *type* : java.lang.Class type field which will be used as data type for above field, e.g. String.class, Integer.class etc.
 
 ```java
 @Configurations(className = "TestAppConfiguration", mappings = {
        @ConfigurationMapping(field = "zkUrl", type = String.class),
        @ConfigurationMapping(field = "kafkaBrokers", type = String.class),
        @ConfigurationMapping(field = "window", type = Long.class)
})
 ```
 
4. **SparkRequest** : Annotation used to get information to build a request to be posted to start a Spark application from plugin. In will be used to build SparkComputationRequest.

 * *main* : Canonical name of a spark application main class which will be used to trigger Spark application.
 
 * *jar* : Name of jar(without group id) containing this main class e.g. arima-model-0.1.jar
 
 * *args* : Array of strings used by spark job. e.g. 
 ```java
 @SparkRequest(main = "com.hashmap.app.SparkApp", jar = "sample-spark-computation-1.0-SNAPSHOT",
        args = {"--window", "Long.toString(configuration.getWindow())", "--mqttbroker", "configuration.getEndpoint()",
                "--kafka", "configuration.getKafkaBrokers()", "--token", "configuration.getGatewayApiToken()"})
 ```
 
 ***These are all Type annotations, Processed by AnnotationsProcessor. Also package name for action and configuration is defaulted to package name of class having those annotations. e.g. Annotations are placed on class com.hashmap.tempus.app.TestSparkApp then classes generated will have the same package i.e. com.hashmap.tempus.app***
 
### Computations Discovery

#### Motivation

*As a user if i want to add new Spark computation capability in tempus, i don't want to create a new Action for Spark computation plugin and i want a mechanism so that i should be able to register spark computation application without taking tempus down*

#### Implementation

1. **File system monitor** : It polls configured directory for given interval to identify changes like File create, modify or delete. This is started as soon as serve is started.

2. **Create** : When new jar is added with annotations, it's classes are scanned for annotations explained above. Then it goes through series of steps as below

* *Generate source* : Using velocity templates (action.vm and config.vm) and annotation values Java source is generated and written to temporary directory(Prefix spark, directory "java.io.tmpdir" system property) by AnnotationsProcessor, for debugging purposes. This directory is added to ClassPath of Application ClassLoader so that running application can access them runtime.

* *Compile Generated Source* : Using javax.tools.JavaCompiler API from JDK tools.jar API generated sources are compiled at the same location as sources and added to application ClassLoader as directory is on Classpath already.

* *Persist Components* : ComponentDiscoveryService needs to be notified about newly generated Actions to get persisted in DB and associated with Plugin, so that when request comes from UI for actions against Plugin it can return newly added actions as well.

3. **Modify** : It goes through all the steps again as listed in Create section, so if there are any changes made to annotations those will be updated to DB and Plugins as well.

***It's highly discouraged to change the class or package name of Action and Configuration. Because application will not delete old one but add new entries for newly generated classes*** 

4. **Delete**

* *Suspend and Delete Rule actors* : As Spark computation plugin is a tenant plugin, all existing tenant rule actors associated with the actions needs to be suspended and then deleted. Once that's completed same Rules from DB are also deleted along with any attributes stored against them.

* *Delete Component Descriptors* : ComponentDescriptors associated with those actions are deleted. Remember generated action and configuration classes are not deleted, so that in case of error in any of the earlier stage that rule will still be available.

***In case jars are deleted when thingsbord server is stopped, nothing mentioned above will be triggered. You have to be careful while deleting existing spark computations***

## Licenses

This project is released under [Apache 2.0 License](./LICENSE).
