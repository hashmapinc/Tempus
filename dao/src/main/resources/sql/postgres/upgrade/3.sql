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

<<<<<<< HEAD
DROP TABLE computations;

CREATE TABLE IF NOT EXISTS computations (
    id varchar(31) NOT NULL CONSTRAINT computations_pkey PRIMARY KEY,
    search_text varchar,
    computation_name varchar,
    type varchar,
    tenant_id varchar(31)
);

CREATE TABLE IF NOT EXISTS spark_computation_meta_data (
    id varchar(31) NOT NULL CONSTRAINT spark_computations_pkey PRIMARY KEY,
    json_descriptor varchar,
    jar_name varchar,
    jar_path varchar,
    main_class varchar,
    args_format varchar,
    args_type varchar
);

CREATE TABLE IF NOT EXISTS kubeless_computation_meta_data (
    id varchar(31) NOT NULL CONSTRAINT kubeless_computations_pkey PRIMARY KEY,
    namespace varchar,
    function varchar,
    handler varchar,
    runtime varchar,
    dependencies varchar,
    func_type varchar,
    timeout varchar,
    checksum varchar
);
=======
ALTER TABLE data_model_object ADD logo_file VARCHAR;
>>>>>>> dev
