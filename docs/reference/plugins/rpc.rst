##########
RPC Plugin
##########

********
Overview
********

RPC plugin is responsible for:
* providing REST API to send RPC request from server-side applications to devices;
* pushing RPC request to devices via one of available protocols: MQTT, CoAP or HTTP;

By default, this plugin is configured on the system level by a system administrator. You are able to configure your own instance of the plugin on tenant level. Advanced users or platform developers can customize rpc plugin functionality.

*************
Configuration
*************

You can specify default RPC timeout for plugin instance in the plugin configuration.

***************
Server-side API
***************

RPC plugin API description is available in corresponding rpc guides.

*******
Example
*******s

As a system administrator, you are able to review plugin example inside Plugins->System RPC Plugin