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

CREATE TABLE IF NOT EXISTS tempus.tag_metadata_cf (
    entity_type text,
    entity_id timeuuid,
    key text,
    unit text,
    avg_frequency double,
    max_frequency double,
    min_frequency double,
    mean_frequency double,
    median_frequency double,
    source text,
    PRIMARY KEY (( entity_type, entity_id ), key)
) WITH compaction = { 'class' :  'LeveledCompactionStrategy'  };

ALTER TABLE tempus.ds_kv_cf ADD ds_diff double;
ALTER TABLE tempus.ts_kv_cf ADD ts_diff bigint;