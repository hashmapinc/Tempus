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

ALTER TABLE tenant ALTER COLUMN logo LONGVARCHAR;
ALTER TABLE data_model_object ALTER COLUMN logo_file LONGVARCHAR;

CREATE TABLE IF NOT EXISTS file_meta_data (
    tenant_id varchar(31),
    related_entity varchar(31),
    file_name varchar,
    file_ext varchar,
    last_updated bigint,
    file_size double precision,
    CONSTRAINT file_meta_data_pkey PRIMARY KEY (tenant_id, related_entity, file_name, file_ext)
);
