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

CREATE KEYSPACE IF NOT exists tempus
WITH replication = {
	'class' : 'SimpleStrategy',
	'replication_factor' : 1
};

CREATE TABLE IF NOT exists tempus.ts_kv_cf (
    entity_type text, // (DEVICE, CUSTOMER, TENANT)
    entity_id timeuuid,
    key text,
    partition bigint,
    ts bigint,
    bool_v boolean,
    str_v text,
    long_v bigint,
    dbl_v double,
    json_v text,
    ts_diff bigint,
    PRIMARY KEY (( entity_type, entity_id, key, partition ), ts)
);

CREATE TABLE IF NOT exists tempus.ts_kv_partitions_cf (
    entity_type text, // (DEVICE, CUSTOMER, TENANT)
    entity_id timeuuid,
    key text,
    partition bigint,
    PRIMARY KEY (( entity_type, entity_id, key ), partition)
) WITH CLUSTERING ORDER BY ( partition ASC )
  AND compaction = { 'class' :  'LeveledCompactionStrategy'  };

CREATE TABLE IF NOT exists tempus.ts_kv_latest_cf (
    entity_type text, // (DEVICE, CUSTOMER, TENANT)
    entity_id timeuuid,
    key text,
    ts bigint,
    bool_v boolean,
    str_v text,
    long_v bigint,
    dbl_v double,
    json_v text,
    PRIMARY KEY (( entity_type, entity_id ), key)
) WITH compaction = { 'class' :  'LeveledCompactionStrategy'  };

CREATE TABLE IF NOT exists tempus.ds_kv_cf (
    entity_type text, // (DEVICE, CUSTOMER, TENANT)
    entity_id timeuuid,
    key text,
    partition double,
    ds double,
    bool_v boolean,
    str_v text,
    long_v bigint,
    dbl_v double,
    json_v text,
    ds_diff double,
    PRIMARY KEY (( entity_type, entity_id, key, partition ), ds)
);

CREATE TABLE IF NOT exists tempus.ds_kv_partitions_cf (
    entity_type text, // (DEVICE, CUSTOMER, TENANT)
    entity_id timeuuid,
    key text,
    partition double,
    PRIMARY KEY (( entity_type, entity_id, key ), partition)
) WITH CLUSTERING ORDER BY ( partition ASC )
  AND compaction = { 'class' :  'LeveledCompactionStrategy'  };

CREATE TABLE IF NOT exists tempus.ds_kv_latest_cf (
    entity_type text, // (DEVICE, CUSTOMER, TENANT)
    entity_id timeuuid,
    key text,
    ds double,
    bool_v boolean,
    str_v text,
    long_v bigint,
    dbl_v double,
    json_v text,
    PRIMARY KEY (( entity_type, entity_id ), key)
) WITH compaction = { 'class' :  'LeveledCompactionStrategy'  };

CREATE TABLE IF NOT exists tempus.attributes_kv_cf (
    entity_type text, // (DEVICE, CUSTOMER, TENANT)
    entity_id timeuuid,
    attribute_type text, // (CLIENT_SIDE, SHARED, SERVER_SIDE)
    attribute_key text,
    bool_v boolean,
    str_v text,
    long_v bigint,
    dbl_v double,
    json_v text,
    last_update_ts bigint,
    PRIMARY KEY ((entity_type, entity_id, attribute_type), attribute_key)
) WITH compaction = { 'class' :  'LeveledCompactionStrategy'  };

CREATE TABLE IF NOT exists tempus.installed_schema_versions (
    executed_scripts text,
    PRIMARY KEY(executed_scripts)
);