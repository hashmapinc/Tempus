/**
 * Copyright © 2016-2018 The Thingsboard Authors
 * Modifications © 2017-2018 Hashmap, Inc
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
package com.hashmapinc.server.dao.model;

import com.datastax.driver.core.utils.UUIDs;
import com.hashmapinc.server.common.data.UUIDConverter;
import com.hashmapinc.server.common.data.kv.Aggregation;
import com.hashmapinc.server.common.data.kv.DepthAggregation;
import com.hashmapinc.server.common.msg.exception.TempusRuntimeException;
import org.apache.commons.lang3.ArrayUtils;

import java.util.UUID;

public class ModelConstants {

    private ModelConstants() {
    }

    public static final UUID NULL_UUID = UUIDs.startOf(0);
    public static final String NULL_UUID_STR = UUIDConverter.fromTimeUUID(NULL_UUID);

    /**
     * Generic constants.
     */
    public static final String ID_PROPERTY = "id";
    public static final String USER_ID_PROPERTY = "user_id";
    public static final String TENANT_ID_PROPERTY = "tenant_id";
    public static final String DATA_MODEL_ID_PROPERTY = "data_model_id";
    public static final String CUSTOMER_ID_PROPERTY = "customer_id";
    public static final String DATA_MODEL_OBJECT_ID = "data_model_object_id";
    public static final String DEVICE_ID_PROPERTY = "device_id";
    public static final String TITLE_PROPERTY = "title";
    public static final String ALIAS_PROPERTY = "alias";
    public static final String SEARCH_TEXT_PROPERTY = "search_text";
    public static final String ADDITIONAL_INFO_PROPERTY = "additional_info";
    public static final String ENTITY_TYPE_PROPERTY = "entity_type";

    public static final String ENTITY_TYPE_COLUMN = ENTITY_TYPE_PROPERTY;
    public static final String ENTITY_ID_COLUMN = "entity_id";
    public static final String ATTRIBUTE_TYPE_COLUMN = "attribute_type";
    public static final String ATTRIBUTE_KEY_COLUMN = "attribute_key";
    public static final String LAST_UPDATE_TS_COLUMN = "last_update_ts";


    /**
     * Installation Constants
     */

    public static final String INSTALLED_SCHEMA_VERSIONS = "installed_schema_versions";
    public static final String INSTALLED_SCRIPTS_COLUMN = "executed_scripts";
    public static final String TEMPUS_KEYSPACE = "tempus";


    /**
     * Admin_settings constants.
     */
    public static final String USER_SETTINGS_COLUMN_FAMILY_NAME = "user_settings";
    public static final String USER_SETTINGS_KEY_PROPERTY = "key";
    public static final String USER_SETTINGS_JSON_VALUE_PROPERTY = "json_value";
    public static final String USER_SETTINGS_USER_ID_PROPERTY = "user_id";

    /**
     * Contact constants.
     */
    public static final String COUNTRY_PROPERTY = "country";
    public static final String STATE_PROPERTY = "state";
    public static final String CITY_PROPERTY = "city";
    public static final String ADDRESS_PROPERTY = "address";
    public static final String ADDRESS2_PROPERTY = "address2";
    public static final String ZIP_PROPERTY = "zip";
    public static final String PHONE_PROPERTY = "phone";
    public static final String EMAIL_PROPERTY = "email";

    /**
     * Tenant constants.
     */
    public static final String TENANT_COLUMN_FAMILY_NAME = "tenant";
    public static final String TENANT_TITLE_PROPERTY = TITLE_PROPERTY;
    public static final String TENANT_REGION_PROPERTY = "region";
    public static final String TENANT_ADDITIONAL_INFO_PROPERTY = ADDITIONAL_INFO_PROPERTY;
    public static final String TENANT_LOGO_FILE = "logo";

    /**
     * Customer constants.
     */
    public static final String CUSTOMER_COLUMN_FAMILY_NAME = "customer";
    public static final String CUSTOMER_TENANT_ID_PROPERTY = TENANT_ID_PROPERTY;
    public static final String CUSTOMER_DATA_MODEL_ID_PROPERTY = DATA_MODEL_ID_PROPERTY;
    public static final String CUSTOMER_TITLE_PROPERTY = TITLE_PROPERTY;
    public static final String CUSTOMER_ADDITIONAL_INFO_PROPERTY = ADDITIONAL_INFO_PROPERTY;
    public static final String DATA_MODEL_TABLE_NAME = "data_model";
    public static final String DATA_MODEL_TENANT_ID_PROPERTY = TENANT_ID_PROPERTY;
    public static final String DATA_MODEL_NAME_PROPERTY = "name";
    public static final String DATA_MODEL_ADDITIONAL_INFO_PROPERTY = ADDITIONAL_INFO_PROPERTY;
    public static final String USER_GROUP_TABLE_NAME = "user_groups";

    /**
     * Unit System Constants
     */
    public static final String TENANT_UNIT_SYSTEM_TABLE = "tenant_unit_system";
    public static final String UNIT_SYSTEM_PROPERTY = "unit_system";

    /**
     * Device constants.
     */
    public static final String DEVICE_COLUMN_FAMILY_NAME = "device";
    public static final String DEVICE_TENANT_ID_PROPERTY = TENANT_ID_PROPERTY;
    public static final String DEVICE_CUSTOMER_ID_PROPERTY = CUSTOMER_ID_PROPERTY;
    public static final String DEVICE_NAME_PROPERTY = "name";
    public static final String DEVICE_TYPE_PROPERTY = "type";
    public static final String DEVICE_ADDITIONAL_INFO_PROPERTY = ADDITIONAL_INFO_PROPERTY;
    public static final String DEVICE_DATA_MODEL_OBJECT_ID = DATA_MODEL_OBJECT_ID;

    /**
     * Metadata Ingestion Constants
     */
    public static final String METADATA_ENTRIES_TABLE = "metadata_entries";
    public static final String METADATA_CONFIG_ID = "metadata_config_id";
    public static final String METADATA_INGESTION_KEY_COLUMN = "key";
    public static final String METADATA_DATASOURCE_NAME_COLUMN = "datasource_name";
    public static final String METADATA_INGESTION_VALUE_COLUMN = "value";
    public static final String METADATA_ATTRIBUTE_COLUMN = "attribute";

    /**
     * Computations Constants
     */
    public static final String COMPUTATIONS_TABLE_NAME = "computations";
    public static final String COMPUTATIONS_JAR = "jar_name";
    public static final String COMPUTATIONS_JAR_PATH = "jar_path";
    public static final String COMPUTATIONS_NAME = "computation_name";
    public static final String COMPUTATIONS_MAIN_CLASS = "main_class";
    public static final String COMPUTATIONS_DESCRIPTOR = "json_descriptor";
    public static final String COMPUTATIONS_TENANT_ID = TENANT_ID_PROPERTY;
    public static final String COMPUTATIONS_ARGS_FORMAT = "args_format";
    public static final String COMPUTATIONS_ARGS_TYPE = "args_type";

    public static final String COMPUTATION_JOB_TABLE_NAME = "computation_job";
    public static final String COMPUTATION_JOB_NAME = "job_name";
    public static final String COMPUTATION_JOB_COMPUTAION_ID = "computation_id";
    public static final String COMPUTATION_JOB_CONFIGURATION = "job_configuration";
    public static final String COMPUTATION_JOB_STATE = "state";
    public static final String COMPUTATION_JOB_TENANT_ID = TENANT_ID_PROPERTY;
    public static final String SPARK_COMPUTATIONS_META_DATA = "spark_computation_meta_data";
    public static final String KUBELESS_COMPUTATIONS_META_DATA = "kubeless_computation_meta_data";
    public static final String KUBELESS_COMPUTATION_NAMESPACE = "namespace";
    public static final String KUBELESS_COMPUTATION_FUNCTION = "function";
    public static final String KUBELESS_COMPUTATION_HANDLER = "handler";
    public static final String KUBELESS_COMPUTATION_DEPENDENCIES = "dependencies";
    public static final String KUBELESS_COMPUTATION_FUNC_TYPE = "func_type";
    public static final String KUBELESS_COMPUTATION_TIMEOUT = "timeout";
    public static final String KUBELESS_COMPUTATION_CHECKSUM = "checksum";
    public static final String KUBELESS_COMPUTATION_RUNTIME = "runtime";

    /**
     * Node Metric Constants
     */
    public static final String NODE_METRIC_TALBE_NAME = "node_metric";
    public static final String NODE_METRIC_HOST = "host";
    public static final String NODE_METRIC_PORT = "port";
    public static final String NODE_METRIC_STATUS = "status";
    public static final String NODE_METRIC_RPC_SESSION = "rpc_session_count";
    public static final String NODE_METRIC_DEVICE_SESSION = "device_session_count";

    /*
     * Audit log constants.
     */

    public static final String AUDIT_LOG_COLUMN_FAMILY_NAME = "audit_log";
    public static final String AUDIT_LOG_TENANT_ID_PROPERTY = TENANT_ID_PROPERTY;
    public static final String AUDIT_LOG_CUSTOMER_ID_PROPERTY = CUSTOMER_ID_PROPERTY;
    public static final String AUDIT_LOG_ENTITY_TYPE_PROPERTY = ENTITY_TYPE_PROPERTY;
    public static final String AUDIT_LOG_ENTITY_ID_PROPERTY = ENTITY_ID_COLUMN;
    public static final String AUDIT_LOG_ENTITY_NAME_PROPERTY = "entity_name";
    public static final String AUDIT_LOG_USER_ID_PROPERTY = USER_ID_PROPERTY;
    public static final String AUDIT_LOG_USER_NAME_PROPERTY = "user_name";
    public static final String AUDIT_LOG_ACTION_TYPE_PROPERTY = "action_type";
    public static final String AUDIT_LOG_ACTION_DATA_PROPERTY = "action_data";
    public static final String AUDIT_LOG_ACTION_STATUS_PROPERTY = "action_status";
    public static final String AUDIT_LOG_ACTION_FAILURE_DETAILS_PROPERTY = "action_failure_details";

    /**
     * Asset constants.
     */
    public static final String ASSET_COLUMN_FAMILY_NAME = "asset";
    public static final String ASSET_TENANT_ID_PROPERTY = TENANT_ID_PROPERTY;
    public static final String ASSET_CUSTOMER_ID_PROPERTY = CUSTOMER_ID_PROPERTY;
    public static final String ASSET_DATA_MODEL_OBJECT_ID = DATA_MODEL_OBJECT_ID;
    public static final String ASSET_NAME_PROPERTY = "name";
    public static final String ASSET_TYPE_PROPERTY = "type";
    public static final String ASSET_ADDITIONAL_INFO_PROPERTY = ADDITIONAL_INFO_PROPERTY;

    /**
     * Entity_subtype constants.
     */
    public static final String ENTITY_SUBTYPE_COLUMN_FAMILY_NAME = "entity_subtype";
    public static final String ENTITY_SUBTYPE_TENANT_ID_PROPERTY = TENANT_ID_PROPERTY;
    public static final String ENTITY_SUBTYPE_ENTITY_TYPE_PROPERTY = ENTITY_TYPE_PROPERTY;
    public static final String ENTITY_SUBTYPE_TYPE_PROPERTY = "type";

    /**
     * Alarm constants.
     */
    public static final String ALARM_COLUMN_FAMILY_NAME = "alarm";
    public static final String ALARM_TENANT_ID_PROPERTY = TENANT_ID_PROPERTY;
    public static final String ALARM_TYPE_PROPERTY = "type";
    public static final String ALARM_ORIGINATOR_ID_PROPERTY = "originator_id";
    public static final String ALARM_ORIGINATOR_TYPE_PROPERTY = "originator_type";
    public static final String ALARM_SEVERITY_PROPERTY = "severity";
    public static final String ALARM_STATUS_PROPERTY = "status";
    public static final String ALARM_START_TS_PROPERTY = "start_ts";
    public static final String ALARM_END_TS_PROPERTY = "end_ts";
    public static final String ALARM_ACK_TS_PROPERTY = "ack_ts";
    public static final String ALARM_CLEAR_TS_PROPERTY = "clear_ts";
    public static final String ALARM_PROPAGATE_PROPERTY = "propagate";

    /**
     * Entity relation constants.
     */
    public static final String RELATION_COLUMN_FAMILY_NAME = "relation";
    public static final String RELATION_FROM_ID_PROPERTY = "from_id";
    public static final String RELATION_FROM_TYPE_PROPERTY = "from_type";
    public static final String RELATION_TO_ID_PROPERTY = "to_id";
    public static final String RELATION_TO_TYPE_PROPERTY = "to_type";
    public static final String RELATION_TYPE_PROPERTY = "relation_type";
    public static final String RELATION_TYPE_GROUP_PROPERTY = "relation_type_group";

    /**
     * Device_credentials constants.
     */
    public static final String DEVICE_CREDENTIALS_COLUMN_FAMILY_NAME = "device_credentials";
    public static final String DEVICE_CREDENTIALS_DEVICE_ID_PROPERTY = DEVICE_ID_PROPERTY;
    public static final String DEVICE_CREDENTIALS_CREDENTIALS_TYPE_PROPERTY = "credentials_type";
    public static final String DEVICE_CREDENTIALS_CREDENTIALS_ID_PROPERTY = "credentials_id";
    public static final String DEVICE_CREDENTIALS_CREDENTIALS_VALUE_PROPERTY = "credentials_value";

    /**
     * Widgets_bundle constants.
     */
    public static final String WIDGETS_BUNDLE_COLUMN_FAMILY_NAME = "widgets_bundle";
    public static final String WIDGETS_BUNDLE_TENANT_ID_PROPERTY = TENANT_ID_PROPERTY;
    public static final String WIDGETS_BUNDLE_ALIAS_PROPERTY = ALIAS_PROPERTY;
    public static final String WIDGETS_BUNDLE_TITLE_PROPERTY = TITLE_PROPERTY;

    /**
     * Widget_type constants.
     */
    public static final String WIDGET_TYPE_COLUMN_FAMILY_NAME = "widget_type";
    public static final String WIDGET_TYPE_TENANT_ID_PROPERTY = TENANT_ID_PROPERTY;
    public static final String WIDGET_TYPE_BUNDLE_ALIAS_PROPERTY = "bundle_alias";
    public static final String WIDGET_TYPE_ALIAS_PROPERTY = ALIAS_PROPERTY;
    public static final String WIDGET_TYPE_NAME_PROPERTY = "name";
    public static final String WIDGET_TYPE_DESCRIPTOR_PROPERTY = "descriptor";

    /**
     * Dashboard constants.
     */
    public static final String DASHBOARD_COLUMN_FAMILY_NAME = "dashboard";
    public static final String DASHBOARD_TENANT_ID_PROPERTY = TENANT_ID_PROPERTY;
    public static final String DASHBOARD_TITLE_PROPERTY = TITLE_PROPERTY;
    public static final String DASHBOARD_CONFIGURATION_PROPERTY = "configuration";
    public static final String DASHBOARD_ASSIGNED_CUSTOMERS_PROPERTY = "assigned_customers";
    public static final String DASHBOARD_TYPE_PROPERTY = "type";

    /**
     * Asset landing dashboard constants.
     */
    public static final String ASSET_LANDING_COLUMN_FAMILY_NAME = "asset_landing_info";
    public static final String ASSET_LANDING_DATA_MODEL_ID = "data_model_id";
    public static final String ASSET_LANDING_DATAMODEL_OBJECT_ID = "data_model_object_id";

    /**
     * Plugin metadata constants.
     */
    public static final String PLUGIN_COLUMN_FAMILY_NAME = "plugin";
    public static final String PLUGIN_TENANT_ID_PROPERTY = TENANT_ID_PROPERTY;
    public static final String PLUGIN_NAME_PROPERTY = "name";
    public static final String PLUGIN_API_TOKEN_PROPERTY = "api_token";
    public static final String PLUGIN_CLASS_PROPERTY = "plugin_class";
    public static final String PLUGIN_ACCESS_PROPERTY = "public_access";
    public static final String PLUGIN_STATE_PROPERTY = STATE_PROPERTY;
    public static final String PLUGIN_CONFIGURATION_PROPERTY = "configuration";

    public static final String COMPUTATIONS_TYPE = "type";

    /**
     * AttributeDefinition constants
     */
    public static final String ATTRIBUTE_DEFINITION_COLUMN_FAMILY_NAME = "attribute_definition";
    public static final String ATTRIBUTE_DEFINITION_NAME = "name";
    public static final String ATTRIBUTE_DEFINITION_VALUE = "attr_value";
    public static final String ATTRIBUTE_DEFINITION_VALUE_TYPE = "value_type";
    public static final String ATTRIBUTE_DEFINITION_MODEL_OBJECT_ID = "data_model_object_id";
    public static final String ATTRIBUTE_DEFINITION_SOURCE = "source";
    public static final String ATTRIBUTE_DEFINITION_IS_KEY_ATTRIBUTE = "key_attribute";

    /**
     * Plugin component metadata constants.
     */
    public static final String COMPONENT_DESCRIPTOR_COLUMN_FAMILY_NAME = "component_descriptor";
    public static final String COMPONENT_DESCRIPTOR_TYPE_PROPERTY = "type";
    public static final String COMPONENT_DESCRIPTOR_SCOPE_PROPERTY = "scope";
    public static final String COMPONENT_DESCRIPTOR_NAME_PROPERTY = "name";
    public static final String COMPONENT_DESCRIPTOR_CLASS_PROPERTY = "clazz";
    public static final String COMPONENT_DESCRIPTOR_CONFIGURATION_DESCRIPTOR_PROPERTY = "configuration_descriptor";
    public static final String COMPONENT_DESCRIPTOR_ACTIONS_PROPERTY = "actions";

    /**
     * Rule metadata constants.
     */
    public static final String RULE_COLUMN_FAMILY_NAME = "rule";
    public static final String RULE_TENANT_ID_PROPERTY = TENANT_ID_PROPERTY;
    public static final String RULE_NAME_PROPERTY = "name";
    public static final String RULE_STATE_PROPERTY = STATE_PROPERTY;
    public static final String RULE_WEIGHT_PROPERTY = "weight";
    public static final String RULE_PLUGIN_TOKEN_PROPERTY = "plugin_token";
    public static final String RULE_FILTERS = "filters";
    public static final String RULE_PROCESSOR = "processor";
    public static final String RULE_ACTION = "action";

    /**
     * Template metadata constants.
     */
    public static final String TEMPLATE_COLUMN_FAMILY_NAME = "templates";
    public static final String TEMPLATE_NAME_PROPERTY = "name";
    public static final String TEMPLATE_BODY_PROPERTY = "body";

    /**
     * Event constants.
     */
    public static final String EVENT_COLUMN_FAMILY_NAME = "event";
    public static final String EVENT_TENANT_ID_PROPERTY = TENANT_ID_PROPERTY;
    public static final String EVENT_TYPE_PROPERTY = "event_type";
    public static final String EVENT_UID_PROPERTY = "event_uid";
    public static final String EVENT_ENTITY_TYPE_PROPERTY = ENTITY_TYPE_PROPERTY;
    public static final String EVENT_ENTITY_ID_PROPERTY = "entity_id";
    public static final String EVENT_BODY_PROPERTY = "body";


    /**
     * Cassandra attributes and timeseries constants.
     */
    public static final String ATTRIBUTES_KV_CF = "attributes_kv_cf";
    public static final String TS_KV_CF = "ts_kv_cf";
    public static final String TS_KV_PARTITIONS_CF = "ts_kv_partitions_cf";
    public static final String TS_KV_LATEST_CF = "ts_kv_latest_cf";
    public static final String DS_KV_CF = "ds_kv_cf";
    public static final String DS_KV_PARTITIONS_CF = "ds_kv_partitions_cf";
    public static final String DS_KV_LATEST_CF = "ds_kv_latest_cf";

    public static final String PARTITION_COLUMN = "partition";
    public static final String KEY_COLUMN = "key";
    public static final String TS_COLUMN = "ts";
    public static final String DS_COLUMN = "ds";
    public static final String UNIT_COLUMN = "unit";
    public static final String SOURCE_UNIT_COLUMN = "source_unit";


    /**
     * ModelObject constants.
     */

    public static final String DATA_MODEL_OBJECT_TABLE = "data_model_object";
    public static final String DATA_MODEL_OBJECT_NAME_PROPERTY = "name";
    public static final String DATA_MODEL_OBJECT_DESCRIPTION = "description";
    public static final String DATA_MODEL_OBJECT_TYPE = "type";
    public static final String DATA_MODEL_OBJECT_PARENT_ID = "parent_id";
    public static final String DATA_MODEL_ID = "data_model_id";
    public static final String DATA_MODEL_LOGO_FILE = "logo_file";


    /**
     * Theme constants.
     */
    public static final String THEME_TABLE_NAME = "theme";
    public static final String THEME_NAME = "name";
    public static final String THEME_VALUE = "value";
    public static final String IS_ENABLED = "is_enabled";

    /**
     * Logo constants.
     */
    public static final String LOGO_TABLE_NAME = "logo";
    public static final String LOGO_NAME = "name";
    public static final String LOGO_DISPLAY = "enabled";
    public static final String LOGO_FILE = "file";


    /**
     * Main names of cassandra key-value columns storage.
     */
    public static final String BOOLEAN_VALUE_COLUMN = "bool_v";
    public static final String STRING_VALUE_COLUMN = "str_v";
    public static final String LONG_VALUE_COLUMN = "long_v";
    public static final String DOUBLE_VALUE_COLUMN = "dbl_v";
    public static final String JSON_VALUE_COLUMN = "json_v";

    /**
     * CustomerGroup Constants
     */
    public static final String CUSTOMER_GROUP_TABLE_NAME = "customer_group";
    public static final String CUSTOMER_GROUP_TITLE = TITLE_PROPERTY;
    public static final String CUSTOMER_GROUP_TENANT_ID = TENANT_ID_PROPERTY;
    public static final String CUSTOMER_GROUP_CUSTOMER_ID = CUSTOMER_ID_PROPERTY;
    public static final String CUSTOMER_GROUP_ID_PROPERTY = "group_id";


    /**
     * CustomerGroup Policy Constants
     */
    public static final String CUSTOMER_GROUP_POLICY_TABLE_NAME = "customer_group_policy";
    public static final String CUSTOMER_GROUP_POLICY_ID = "group_id";
    public static final String CUSTOMER_GROUP_POLICY_COLUMN = "policy";

    /**
     * TempusGatewayConfiguration Constants
     */
    public static final String TEMPUS_GATEWAY_CONFIGURATION_TABLE_NAME = "tempus_gateway_configuration";
    public static final String TEMPUS_GATEWAY_CONFIGURATION_TENANT_ID = TENANT_ID_PROPERTY;
    public static final String TEMPUS_GATEWAY_CONFIGURATION_REPLICAS = "replicas";
    public static final String TEMPUS_GATEWAY_CONFIGURATION_GATEWAY_TOKEN = "gateway_token";

    public static final String AWS_LAMBDA_COMPUTATIONS_META_DATA = "lambda_computation_meta_data";
    public static final String AWS_LAMBDA_FILE_PATH = "file_path";
    public static final String AWS_LAMBDA_FUNCTION_HANDLER = "function_handler";
    public static final String AWS_LAMBDA_FUNCTION_NAME = "function_name";
    public static final String AWS_LAMBDA_DESCRIPTION = "description";
    public static final String AWS_LAMBDA_TIMEOUT = "lambda_timeout";
    public static final String AWS_LAMBDA_MEMORY_SIZE = "memory_size";
    public static final String AWS_LAMBDA_RUNTIME = "runtime";
    public static final String AWS_LAMBDA_REGION = "region";

    public static final String LAMBDA_KINESIS_STARTING_OFFSET = "LATEST";
    public static final int LAMBDA_KINESIS_BATCH_SIZE = 200;

    /**
     * File Meta Data constants
     */

    public static final String FILE_META_DATA_TABLE_NAME = "file_meta_data";
    public static final String FILE_META_DATA_RELATED_ENTITY_ID = "related_entity_id";
    public static final String FILE_META_DATA_RELATED_ENTITY_TYPE = "related_entity_type";
    public static final String FILE_META_DATA_FILE_NAME = "file_name";
    public static final String FILE_META_DATA_FILE_EXTENSION = "file_ext";
    public static final String FILE_META_DATA_FILE_LAST_UPDATED = "last_updated";
    public static final String FILE_META_DATA_FILE_SIZE = "file_size";

    public static final String PLUGIN_DESCRIPTOR_TYPE = "type";
    public static final String PLUGIN_DESCRIPTOR_KEY = "key";
    public static final String PLUGIN_DESCRIPTOR_PASSWORD = "password";

    /**
     * AES-ENCRYPTION
     */
    public static final String UTF_8 = "UTF8";
    public static final String ALGORITHM_STR = "AES/ECB/PKCS5Padding";

    protected static final String[] NONE_AGGREGATION_COLUMNS = new String[]{LONG_VALUE_COLUMN, DOUBLE_VALUE_COLUMN, BOOLEAN_VALUE_COLUMN, STRING_VALUE_COLUMN, JSON_VALUE_COLUMN, KEY_COLUMN, TS_COLUMN};
    protected static final String[] NONE_DS_AGGREGATION_COLUMNS = new String[]{LONG_VALUE_COLUMN, DOUBLE_VALUE_COLUMN, BOOLEAN_VALUE_COLUMN, STRING_VALUE_COLUMN, JSON_VALUE_COLUMN, KEY_COLUMN, DS_COLUMN};

    protected static final String[] COUNT_AGGREGATION_COLUMNS = new String[]{count(LONG_VALUE_COLUMN), count(DOUBLE_VALUE_COLUMN), count(BOOLEAN_VALUE_COLUMN), count(STRING_VALUE_COLUMN), count(JSON_VALUE_COLUMN)};

    protected static final String[] MIN_AGGREGATION_COLUMNS = ArrayUtils.addAll(COUNT_AGGREGATION_COLUMNS,
                                                                                min(LONG_VALUE_COLUMN), min(DOUBLE_VALUE_COLUMN), min(BOOLEAN_VALUE_COLUMN), min(STRING_VALUE_COLUMN), min(JSON_VALUE_COLUMN));
    protected static final String[] MAX_AGGREGATION_COLUMNS = ArrayUtils.addAll(COUNT_AGGREGATION_COLUMNS,
                                                                                max(LONG_VALUE_COLUMN), max(DOUBLE_VALUE_COLUMN), max(BOOLEAN_VALUE_COLUMN), max(STRING_VALUE_COLUMN), min(JSON_VALUE_COLUMN));
    protected static final String[] SUM_AGGREGATION_COLUMNS = ArrayUtils.addAll(COUNT_AGGREGATION_COLUMNS,
                                                                                sum(LONG_VALUE_COLUMN), sum(DOUBLE_VALUE_COLUMN));
    protected static final String[] AVG_AGGREGATION_COLUMNS = SUM_AGGREGATION_COLUMNS;

    public static String min(String s) {
        return "min(" + s + ")";
    }

    public static String max(String s) {
        return "max(" + s + ")";
    }

    public static String sum(String s) {
        return "sum(" + s + ")";
    }

    public static String count(String s) {
        return "count(" + s + ")";
    }

    public static String[] getFetchColumnNames(Aggregation aggregation) {
        switch (aggregation) {
            case NONE:
                return NONE_AGGREGATION_COLUMNS;
            case MIN:
                return MIN_AGGREGATION_COLUMNS;
            case MAX:
                return MAX_AGGREGATION_COLUMNS;
            case SUM:
                return SUM_AGGREGATION_COLUMNS;
            case COUNT:
                return COUNT_AGGREGATION_COLUMNS;
            case AVG:
                return AVG_AGGREGATION_COLUMNS;
            default:
                throw new TempusRuntimeException("Aggregation type: " + aggregation + " is not supported!");
        }
    }

    public static String[] getFetchColumnNames(DepthAggregation aggregation) {
        switch (aggregation) {
            case NONE:
                return NONE_DS_AGGREGATION_COLUMNS;
            case MIN:
                return MIN_AGGREGATION_COLUMNS;
            case MAX:
                return MAX_AGGREGATION_COLUMNS;
            case SUM:
                return SUM_AGGREGATION_COLUMNS;
            case COUNT:
                return COUNT_AGGREGATION_COLUMNS;
            case AVG:
                return AVG_AGGREGATION_COLUMNS;
            default:
                throw new TempusRuntimeException("Aggregation type: " + aggregation + " is not supported!");
        }
    }
}
