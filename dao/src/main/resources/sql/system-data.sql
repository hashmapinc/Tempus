--
-- Copyright © 2016-2018 The Thingsboard Authors
-- Modifications © 2017-2018 Hashmap, Inc
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--     http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

/** System settings **/
INSERT INTO user_settings ( id, key, json_value, user_id )
VALUES ( '1e746126a2266e4a91992ebcb67fe33', 'general', '{
	"baseUrl": "http://localhost:8080"
}',  '1e7461259eab8808080808080808080' );

INSERT INTO user_settings ( id, key, json_value, user_id )
VALUES ( '1e746126eaaefa6a91992ebcb67fe33', 'mail', '{
	"mailFrom": "tempus <sysadmin@localhost.localdomain>",
	"smtpProtocol": "smtp",
	"smtpHost": "localhost",
	"smtpPort": "25",
	"timeout": "10000",
	"enableTls": "false",
	"username": "",
	"password": ""
}', '1e7461259eab8808080808080808080' );

/** System plugins and rules **/
INSERT INTO plugin ( id, tenant_id, name, state, search_text, api_token, plugin_class, public_access, configuration )
VALUES ( '1e7461160cb2da2a91992ebcb67fe33', '1b21dd2138140008080808080808080', 'System Telemetry Plugin', 'ACTIVE',
         'system telemetry plugin', 'telemetry',
         'com.hashmapinc.server.extensions.core.plugin.telemetry.TelemetryStoragePlugin', true, '{}' );

INSERT INTO rule ( id, tenant_id, name, plugin_token, state, search_text, weight, filters, processor, action )
VALUES ( '1e7461165abad4ca91992ebcb67fe33', '1b21dd2138140008080808080808080', 'System Telemetry Rule', 'telemetry', 'ACTIVE',
         'system telemetry rule', 0,
         '[{"clazz":"com.hashmapinc.server.extensions.core.filter.MsgTypeFilter", "name":"TelemetryFilter", "configuration": {"messageTypes":["POST_TELEMETRY","POST_ATTRIBUTES","GET_ATTRIBUTES","POST_TELEMETRY_DEPTH"]}}]',
         null,
         '{"clazz":"com.hashmapinc.server.extensions.core.action.telemetry.TelemetryPluginAction", "name":"TelemetryMsgConverterAction", "configuration":{}}'
);

INSERT INTO plugin ( id, tenant_id, name, state, search_text, api_token, plugin_class, public_access, configuration )
VALUES ( '1e746116b3b8994a91992ebcb67fe33', '1b21dd2138140008080808080808080', 'System RPC Plugin', 'ACTIVE',
         'system rpc plugin', 'rpc', 'com.hashmapinc.server.extensions.core.plugin.rpc.RpcPlugin', true, '{
       "defaultTimeout": 20000
     }' );

INSERT INTO CUSTOMER_GROUP (id, title, tenant_id, customer_id, additional_info, search_text)
VALUES ('1e856116b3b8994a91992ebcb67fe33', 'sys_group', '1b21dd2138140008080808080808080', '1b21dd2138140008080808080808080', null , 'sys_group');

INSERT INTO user_groups(user_id, group_id) VALUES ('1e7461259eab8808080808080808080', '1e856116b3b8994a91992ebcb67fe33');

INSERT INTO customer_group_policy (group_id, policy) VALUES ('1e856116b3b8994a91992ebcb67fe33', 'SYS_ADMIN:*:*');