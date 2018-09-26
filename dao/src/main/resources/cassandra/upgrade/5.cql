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

DROP MATERIALIZED VIEW tempus.computations_by_tenant_and_search_text;

DROP MATERIALIZED VIEW tempus.computations_by_tenant_and_computation_name;

DROP TABLE tempus.computations;

DROP MATERIALIZED VIEW tempus.cmp_jobs_by_tenant_and_cmp_search_text;

DROP TABLE tempus.computation_job;

CREATE TABLE IF NOT EXISTS tempus.computations (
    id uuid,
    search_text varchar,
    computation_name varchar,
    type varchar,
    tenant_id uuid,
    PRIMARY KEY (id, tenant_id)
);

CREATE TABLE IF NOT EXISTS tempus.spark_computation_meta_data (
    id uuid,
    json_descriptor text,
    jar_name text,
    jar_path text,
    main_class text,
    args_format text,
    args_type text,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS tempus.kubeless_computation_meta_data (
    id uuid,
    namespace text,
    function text,
    handler text,
    runtime text,
    dependencies text,
    func_type text,
    timeout text,
    checksum text,
    PRIMARY KEY (id)
);

CREATE MATERIALIZED VIEW IF NOT EXISTS tempus.computations_by_tenant_and_search_text AS
    SELECT *
    from tempus.computations
    WHERE tenant_id IS NOT NULL AND search_text IS NOT NULL AND id IS NOT NULL
    PRIMARY KEY ( tenant_id, search_text, id )
    WITH CLUSTERING ORDER BY ( search_text ASC, id DESC );

CREATE MATERIALIZED VIEW IF NOT EXISTS tempus.computations_by_tenant_and_computation_name AS
    SELECT *
    from tempus.computations
    WHERE tenant_id IS NOT NULL AND computation_name IS NOT NULL AND id IS NOT NULL
    PRIMARY KEY ( tenant_id, computation_name, id )
    WITH CLUSTERING ORDER BY ( computation_name ASC, id DESC );


CREATE TABLE IF NOT EXISTS tempus.computation_job (
    id uuid,
    job_name text,
    search_text text,
    computation_id uuid,
    job_configuration text,
    state text,
    tenant_id uuid,
    PRIMARY KEY (id, tenant_id, computation_id)
);

CREATE MATERIALIZED VIEW IF NOT EXISTS tempus.cmp_jobs_by_tenant_and_cmp_search_text AS
    SELECT *
    from tempus.computation_job
    WHERE tenant_id IS NOT NULL AND computation_id IS NOT NULL AND search_text IS NOT NULL AND id IS NOT NULL
    PRIMARY KEY (tenant_id, computation_id, search_text, id)
    WITH CLUSTERING ORDER BY ( search_text ASC, id DESC );
