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

DROP MATERIALIZED VIEW tempus.cmp_jobs_by_tenant_and_cmp_search_text;

DROP TABLE tempus.computation_job;

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