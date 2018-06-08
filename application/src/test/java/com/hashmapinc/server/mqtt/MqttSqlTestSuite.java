/**
 * Copyright © 2017-2018 Hashmap, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hashmapinc.server.mqtt;

import com.hashmapinc.server.dao.CustomSqlUnit;
import org.junit.ClassRule;
import org.junit.extensions.cpsuite.ClasspathSuite;
import org.junit.runner.RunWith;

import java.util.Arrays;

@RunWith(ClasspathSuite.class)
@ClasspathSuite.ClassnameFilters({
        "com.hashmapinc.server.mqtt.rpc.sql.*Test", "com.hashmapinc.server.mqtt.telemetry.sql.*Test"})
public class MqttSqlTestSuite {

    @ClassRule
    public static CustomSqlUnit sqlUnit = new CustomSqlUnit(
            Arrays.asList("sql/schema.sql", "sql/system-data.sql"),
            "sql/drop-all-tables.sql",
            "sql-test.properties",
            Arrays.asList("sql/upgrade/1.sql", "sql/upgrade/2.sql", "sql/upgrade/3.sql"));
}
