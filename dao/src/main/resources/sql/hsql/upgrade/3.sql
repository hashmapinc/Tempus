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

CREATE TABLE IF NOT EXISTS data_model_object (
    id varchar(31) NOT NULL CONSTRAINT data_model_object_pkey PRIMARY KEY,
    name varchar(250),
    description varchar,
    data_model_id varchar(31),
    parent_id varchar(31),
    tenant_id varchar(31),
    customer_id varchar(31),
    search_text varchar(255)
);
