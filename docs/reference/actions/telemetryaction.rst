#######################
Telemetry Plugin Action
#######################

********
Overview
********

This component allows forwarding incoming attributes and timeseries requests to telemetry plugin.

*************
Configuration
*************

There are two additional fields added in configuration i.e

* **Tag Quality Time Window** : It is the time period over which the quality parameters like avg, mean, median etc will be calulated for a tag(i.e. key) present in telemetry data.
* **Tag Quality Depth Window** : It is the depth period over which the quality parameters like avg, mean, median etc will be calulated for a tag(i.e. key) present in depth telemetry data.

*******
Example
*******

As a system administrator, you are able to review action example inside Rules->System Telemetry Rule->Actions->Telemetry Plugin Action.