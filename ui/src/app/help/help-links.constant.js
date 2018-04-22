/*
 * Copyright © 2017-2018 Hashmap, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
var pluginClazzHelpLinkMap = {
    'com.hashmapinc.server.extensions.core.plugin.messaging.DeviceMessagingPlugin': 'pluginDeviceMessaging',
    'com.hashmapinc.server.extensions.core.plugin.telemetry.TelemetryStoragePlugin': 'pluginTelemetryStorage',
    'com.hashmapinc.server.extensions.core.plugin.rpc.RpcPlugin': 'pluginRpcPlugin',
    'com.hashmapinc.server.extensions.core.plugin.mail.MailPlugin': 'pluginMailPlugin',
    'com.hashmapinc.server.extensions.rest.plugin.RestApiCallPlugin': 'pluginRestApiCallPlugin',
    'com.hashmapinc.server.extensions.core.plugin.time.TimePlugin': 'pluginTimePlugin',
    'com.hashmapinc.server.extensions.kafka.plugin.KafkaPlugin': 'pluginKafkaPlugin',
    'com.hashmapinc.server.extensions.rabbitmq.plugin.RabbitMqPlugin': 'pluginRabbitMqPlugin'

};

var filterClazzHelpLinkMap = {
    'com.hashmapinc.server.extensions.core.filter.MsgTypeFilter': 'filterMsgType',
    'com.hashmapinc.server.extensions.core.filter.DeviceTelemetryFilter': 'filterDeviceTelemetry',
    'com.hashmapinc.server.extensions.core.filter.MethodNameFilter': 'filterMethodName',
    'com.hashmapinc.server.extensions.core.filter.DeviceAttributesFilter': 'filterDeviceAttributes'
};

var processorClazzHelpLinkMap = {
    'com.hashmapinc.server.extensions.core.processor.AlarmDeduplicationProcessor': 'processorAlarmDeduplication'
};

var pluginActionsClazzHelpLinkMap = {
    'com.hashmapinc.server.extensions.core.action.rpc.RpcPluginAction': 'pluginActionRpc',
    'com.hashmapinc.server.extensions.core.action.mail.SendMailAction': 'pluginActionSendMail',
    'com.hashmapinc.server.extensions.core.action.telemetry.TelemetryPluginAction': 'pluginActionTelemetry',
    'com.hashmapinc.server.extensions.kafka.action.KafkaPluginAction': 'pluginActionKafka',
    'com.hashmapinc.server.extensions.rabbitmq.action.RabbitMqPluginAction': 'pluginActionRabbitMq',
    'com.hashmapinc.server.extensions.rest.action.RestApiCallPluginAction': 'pluginActionRestApiCall'
};

var helpBaseUrl = "http://tempus-cloud.s3-website-us-west-2.amazonaws.com/help";

export default angular.module('tempus.help', [])
    .constant('helpLinks',
        {
            linksMap: {
                outgoingMailSettings: helpBaseUrl + "/admin/mail.html",
                plugins: helpBaseUrl + "/features/ruleengine.html#plugins",
                pluginDeviceMessaging: helpBaseUrl + "/reference/plugins/devicemessaging.html",
                pluginTelemetryStorage: helpBaseUrl + "/reference/plugins/telemetry.html",
                pluginRpcPlugin: helpBaseUrl + "/reference/plugins/rpc.html",
                pluginMailPlugin: helpBaseUrl + "/reference/plugins/mail.html",
                pluginRestApiCallPlugin: helpBaseUrl + "/reference/plugins/kafka.html",
                pluginTimePlugin: helpBaseUrl + "/reference/plugins/timerpc.html",
                pluginKafkaPlugin: helpBaseUrl + "/reference/plugins/kafka.html",
                pluginRabbitMqPlugin: helpBaseUrl + "/reference/plugins/kafka.html",
                rules: helpBaseUrl + "/features/ruleengine.html#rules",
                filters: helpBaseUrl + "/features/ruleengine.html#filters",
                filterMsgType: helpBaseUrl + "/reference/filters/messagetypefilter.html",
                filterDeviceTelemetry: helpBaseUrl + "/reference/filters/devicetelemetryfilter.html",
                filterMethodName: helpBaseUrl + "/reference/filters/methodname.html",
                filterDeviceAttributes: helpBaseUrl + "/reference/filters/deviceattributefilter.html",
                processors: helpBaseUrl + "/features/ruleengine.html#processors",
                processorAlarmDeduplication: "/features/ruleengine.html#processors",
                pluginActions: helpBaseUrl + "/reference/actions/index.html",
                pluginActionRpc: helpBaseUrl + "/reference/actions/restaction.html",
                pluginActionSendMail: helpBaseUrl + "/reference/actions/sendmailaction.html",
                pluginActionTelemetry: helpBaseUrl + "help/reference/actions/telemetryaction.html",
                pluginActionKafka: helpBaseUrl + "/reference/actions/kafkaaction.html",
                pluginActionRabbitMq: helpBaseUrl + "/reference/actions/kafkaaction.html",
                pluginActionRestApiCall: helpBaseUrl + "/reference/actions/kafkaaction.html",
                tenants: helpBaseUrl + "/admin/tenants.html",
                customers: helpBaseUrl + "/admin/customers.html",
                assets: helpBaseUrl + "/admin/assets.html",
                devices: helpBaseUrl + "/admin/devices.html",
                dashboards: helpBaseUrl + "admin/dashboards.html",
                users: helpBaseUrl + "/admin/users.html",
                widgetsBundles: helpBaseUrl + "/admin/widgets.html#widgets-library-bundles",
                widgetsConfig:  helpBaseUrl + "/admin/widgets.html?highlight=configuration#widget-types",
                widgetsConfigTimeseries:  helpBaseUrl + "/admin/widgets.html?highlight=configuration#time-series",
                widgetsConfigLatest: helpBaseUrl +  "/admin/widgets.html?highlight=configuration#latest-label",
                widgetsConfigRpc: helpBaseUrl +  "/admin/widgets.html?highlight=configuration#rpc-label",
                widgetsConfigAlarm: helpBaseUrl +  "/admin/widgets.html?highlight=configuration#alarm-label",
                widgetsConfigStatic: helpBaseUrl +  "/admin/widgets.html?highlight=configuration#static-label",
            },
            getPluginLink: function(plugin) {
                var link = 'plugins';
                if (plugin && plugin.clazz) {
                    if (pluginClazzHelpLinkMap[plugin.clazz]) {
                        link = pluginClazzHelpLinkMap[plugin.clazz];
                    }
                }
                return link;
            },
            getFilterLink: function(filter) {
                var link = 'filters';
                if (filter && filter.clazz) {
                    if (filterClazzHelpLinkMap[filter.clazz]) {
                        link = filterClazzHelpLinkMap[filter.clazz];
                    }
                }
                return link;
            },
            getProcessorLink: function(processor) {
                var link = 'processors';
                if (processor && processor.clazz) {
                    if (processorClazzHelpLinkMap[processor.clazz]) {
                        link = processorClazzHelpLinkMap[processor.clazz];
                    }
                }
                return link;
            },
            getPluginActionLink: function(pluginAction) {
                var link = 'pluginActions';
                if (pluginAction && pluginAction.clazz) {
                    if (pluginActionsClazzHelpLinkMap[pluginAction.clazz]) {
                        link = pluginActionsClazzHelpLinkMap[pluginAction.clazz];
                    }
                }
                return link;
            }
        }
    ).name;
