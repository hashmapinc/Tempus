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

<<<<<<< 8dc458b5c00763d9907dcce094dd3441f0fcf3b9:dao/src/main/resources/cassandra/upgrade/3.cql
ALTER TABLE tempus.customer ADD data_model_id timeuuid;

=======
ALTER TABLE application ADD state varchar(255) DEFAULT 'SUSPENDED';
>>>>>>> Commit for adding two seprate folders for HSql and postgres:dao/src/main/resources/sql/postgres/upgrade/1.sql
