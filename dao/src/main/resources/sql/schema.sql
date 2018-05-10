--
-- Copyright © 2017-2018 Hashmap, Inc
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

CREATE TABLE IF NOT EXISTS user_settings (
    id varchar(31) NOT NULL CONSTRAINT user_settings_pkey PRIMARY KEY,
    json_value varchar,
    key varchar(255),
    user_id varchar(31)
);

CREATE TABLE IF NOT EXISTS alarm (
    id varchar(31) NOT NULL CONSTRAINT alarm_pkey PRIMARY KEY,
    ack_ts bigint,
    clear_ts bigint,
    additional_info varchar,
    end_ts bigint,
    originator_id varchar(31),
    originator_type integer,
    propagate boolean,
    severity varchar(255),
    start_ts bigint,
    status varchar(255),
    tenant_id varchar(31),
    type varchar(255)
);

CREATE TABLE IF NOT EXISTS asset (
    id varchar(31) NOT NULL CONSTRAINT asset_pkey PRIMARY KEY,
    additional_info varchar,
    customer_id varchar(31),
    name varchar(255),
    search_text varchar(255),
    tenant_id varchar(31),
    type varchar(255)
);

CREATE TABLE IF NOT EXISTS audit_log (
    id varchar(31) NOT NULL CONSTRAINT audit_log_pkey PRIMARY KEY,
    tenant_id varchar(31),
    customer_id varchar(31),
    entity_id varchar(31),
    entity_type varchar(255),
    entity_name varchar(255),
    user_id varchar(31),
    user_name varchar(255),
    action_type varchar(255),
    action_data varchar(1000000),
    action_status varchar(255),
    action_failure_details varchar(1000000)
);

CREATE TABLE IF NOT EXISTS attribute_kv (
  entity_type varchar(255),
  entity_id varchar(31),
  attribute_type varchar(255),
  attribute_key varchar(255),
  bool_v boolean,
  str_v varchar(10000000),
  long_v bigint,
  dbl_v double precision,
  json_v varchar,
  last_update_ts bigint,
  CONSTRAINT attribute_kv_unq_key UNIQUE (entity_type, entity_id, attribute_type, attribute_key)
);

CREATE TABLE IF NOT EXISTS component_descriptor (
    id varchar(31) NOT NULL CONSTRAINT component_descriptor_pkey PRIMARY KEY,
    actions varchar(255),
    clazz varchar,
    configuration_descriptor varchar,
    name varchar(255),
    scope varchar(255),
    search_text varchar(255),
    type varchar(255)
);

CREATE TABLE IF NOT EXISTS customer (
    id varchar(31) NOT NULL CONSTRAINT customer_pkey PRIMARY KEY,
    additional_info varchar,
    address varchar,
    address2 varchar,
    city varchar(255),
    country varchar(255),
    email varchar(255),
    phone varchar(255),
    search_text varchar(255),
    state varchar(255),
    tenant_id varchar(31),
    title varchar(255),
    zip varchar(255)
);

CREATE TABLE IF NOT EXISTS dashboard (
    id varchar(31) NOT NULL CONSTRAINT dashboard_pkey PRIMARY KEY,
    configuration varchar(10000000),
    assigned_customers varchar(1000000),
    search_text varchar(255),
    tenant_id varchar(31),
    title varchar(255)
);

CREATE TABLE IF NOT EXISTS device (
    id varchar(31) NOT NULL CONSTRAINT device_pkey PRIMARY KEY,
    additional_info varchar,
    customer_id varchar(31),
    type varchar(255),
    name varchar(255),
    search_text varchar(255),
    tenant_id varchar(31)
);

CREATE TABLE IF NOT EXISTS device_credentials (
    id varchar(31) NOT NULL CONSTRAINT device_credentials_pkey PRIMARY KEY,
    credentials_id varchar,
    credentials_type varchar(255),
    credentials_value varchar,
    device_id varchar(31)
);

CREATE TABLE IF NOT EXISTS event (
    id varchar(31) NOT NULL CONSTRAINT event_pkey PRIMARY KEY,
    body varchar,
    entity_id varchar(31),
    entity_type varchar(255),
    event_type varchar(255),
    event_uid varchar(255),
    tenant_id varchar(31),
    CONSTRAINT event_unq_key UNIQUE (tenant_id, entity_type, entity_id, event_type, event_uid)
);

CREATE TABLE IF NOT EXISTS plugin (
    id varchar(31) NOT NULL CONSTRAINT plugin_pkey PRIMARY KEY,
    additional_info varchar,
    api_token varchar(255),
    plugin_class varchar(255),
    configuration varchar,
    name varchar(255),
    public_access boolean,
    search_text varchar(255),
    state varchar(255),
    tenant_id varchar(31)
);

CREATE TABLE IF NOT EXISTS relation (
    from_id varchar(31),
    from_type varchar(255),
    to_id varchar(31),
    to_type varchar(255),
    relation_type_group varchar(255),
    relation_type varchar(255),
    additional_info varchar,
    CONSTRAINT relation_unq_key UNIQUE (from_id, from_type, relation_type_group, relation_type, to_id, to_type)
);

CREATE TABLE IF NOT EXISTS rule (
    id varchar(31) NOT NULL CONSTRAINT rule_pkey PRIMARY KEY,
    action varchar,
    additional_info varchar,
    filters varchar,
    name varchar(255),
    plugin_token varchar(255),
    processor varchar,
    search_text varchar(255),
    state varchar(255),
    tenant_id varchar(31),
    weight integer
);

CREATE TABLE IF NOT EXISTS tb_user (
    id varchar(31) NOT NULL CONSTRAINT tb_user_pkey PRIMARY KEY,
    additional_info varchar,
    authority varchar(255),
    customer_id varchar(31),
    email varchar(255) UNIQUE,
    first_name varchar(255),
    last_name varchar(255),
    search_text varchar(255),
    tenant_id varchar(31)
);

CREATE TABLE IF NOT EXISTS tenant (
    id varchar(31) NOT NULL CONSTRAINT tenant_pkey PRIMARY KEY,
    additional_info varchar,
    address varchar,
    address2 varchar,
    city varchar(255),
    country varchar(255),
    email varchar(255),
    phone varchar(255),
    region varchar(255),
    search_text varchar(255),
    state varchar(255),
    title varchar(255),
    zip varchar(255)
);

CREATE TABLE IF NOT EXISTS ts_kv (
    entity_type varchar(255) NOT NULL,
    entity_id varchar(31) NOT NULL,
    key varchar(255) NOT NULL,
    ts bigint NOT NULL,
    bool_v boolean,
    str_v varchar(10000000),
    long_v bigint,
    dbl_v double precision,
    json_v varchar,
    CONSTRAINT ts_kv_unq_key UNIQUE (entity_type, entity_id, key, ts)
);

CREATE TABLE IF NOT EXISTS ts_kv_latest (
    entity_type varchar(255) NOT NULL,
    entity_id varchar(31) NOT NULL,
    key varchar(255) NOT NULL,
    ts bigint NOT NULL,
    bool_v boolean,
    str_v varchar(10000000),
    long_v bigint,
    dbl_v double precision,
    json_v varchar,
    CONSTRAINT ts_kv_latest_unq_key UNIQUE (entity_type, entity_id, key)
);

CREATE TABLE IF NOT EXISTS ds_kv (
    entity_type varchar(255) NOT NULL,
    entity_id varchar(31) NOT NULL,
    key varchar(255) NOT NULL,
    ds double precision NOT NULL,
    bool_v boolean,
    str_v varchar(10000000),
    long_v bigint,
    dbl_v double precision,
    json_v varchar,
    CONSTRAINT ds_kv_unq_key UNIQUE (entity_type, entity_id, key, ds)
);

CREATE TABLE IF NOT EXISTS ds_kv_latest (
    entity_type varchar(255) NOT NULL,
    entity_id varchar(31) NOT NULL,
    key varchar(255) NOT NULL,
    ds double precision NOT NULL,
    bool_v boolean,
    str_v varchar,
    long_v bigint,
    dbl_v double precision,
    json_v varchar,
    CONSTRAINT ds_kv_latest_unq_key UNIQUE (entity_type, entity_id, key)
);

CREATE TABLE IF NOT EXISTS user_credentials (
    id varchar(31) NOT NULL CONSTRAINT user_credentials_pkey PRIMARY KEY,
    activate_token varchar(255) UNIQUE,
    enabled boolean,
    password varchar(255),
    reset_token varchar(255) UNIQUE,
    user_id varchar(31) UNIQUE
);

CREATE TABLE IF NOT EXISTS widget_type (
    id varchar(31) NOT NULL CONSTRAINT widget_type_pkey PRIMARY KEY,
    alias varchar(255),
    bundle_alias varchar(255),
    descriptor varchar(1000000),
    name varchar(255),
    tenant_id varchar(31)
);

CREATE TABLE IF NOT EXISTS widgets_bundle (
    id varchar(31) NOT NULL CONSTRAINT widgets_bundle_pkey PRIMARY KEY,
    alias varchar(255),
    search_text varchar(255),
    tenant_id varchar(31),
    title varchar(255)
);

CREATE TABLE IF NOT EXISTS application (
    id varchar(31) NOT NULL CONSTRAINT application_pkey PRIMARY KEY,
    customer_id varchar(31),
    tenant_id varchar(31),
    mini_dashboard_id varchar(31),
    search_text varchar(255),
    dashboard_id varchar(31),
    is_valid boolean,
    name varchar(255),
    description varchar(255)
);

CREATE TABLE IF NOT EXISTS application_device_types(
    application_id varchar(31),
    device_type varchar(255)
);

CREATE TABLE IF NOT EXISTS application_associated_rules(
    application_id varchar(31),
    application_rule_id varchar(31)
);

CREATE TABLE IF NOT EXISTS application_associated_computation_jobs(
    application_id varchar(31),
    application_computation_job_id varchar(31)
);

CREATE TABLE IF NOT EXISTS computations (
    id varchar(31) NOT NULL CONSTRAINT computations_pkey PRIMARY KEY,
    jar_name varchar,
    jar_path varchar,
    search_text varchar,
    computation_name varchar,
    main_class varchar,
    args_format varchar,
    args_type varchar,
    json_descriptor varchar,
    tenant_id varchar(31)
);

CREATE TABLE IF NOT EXISTS computation_job (
    id varchar(31) NOT NULL CONSTRAINT computation_job_pkey PRIMARY KEY,
    job_name varchar,
    job_id varchar,
    search_text varchar,
    computation_id varchar,
    arg_parameters varchar,
    state varchar(255),
    tenant_id varchar(31)
);


CREATE TABLE IF NOT EXISTS installed_schema_versions(executed_scripts varchar(255) UNIQUE);