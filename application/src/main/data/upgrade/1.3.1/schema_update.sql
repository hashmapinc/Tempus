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

<<<<<<< 2f9c48500538303951cd3804cbf5e090fa68c550:application/src/main/data/upgrade/1.3.1/schema_update.sql
ALTER TABLE ts_kv_latest ALTER COLUMN str_v SET DATA TYPE varchar(10000000);
=======
ALTER TABLE dashboard ADD type varchar DEFAULT 'DEFAULT';
ALTER TABLE asset ADD data_model_object_id varchar(31) DEFAULT '1b21dd2138140008080808080808080';
>>>>>>> Tempus-844 Reverting back tag meta data aggregation feature.:dao/src/main/resources/sql/postgres/upgrade/3.sql
