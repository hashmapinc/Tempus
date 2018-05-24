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

CREATE TABLE IF NOT EXISTS tag_metadata (
    entity_type varchar(255) NOT NULL,
    entity_id varchar(31) NOT NULL,
    key varchar(255) NOT NULL,
    unit varchar(255),
    avg_frequency double precision,
    max_frequency double precision,
    min_frequency double precision,
    mean_frequency double precision,
    median_frequency double precision,
    source varchar,
    CONSTRAINT tag_metadata_unq_key UNIQUE (entity_type, entity_id, key)
);

ALTER TABLE ts_kv ADD ts_diff bigint;
ALTER TABLE ds_kv ADD ds_diff double precision;