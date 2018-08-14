/**
 * Copyright © 2016-2018 The Thingsboard Authors
 * Modifications © 2017-2018 Hashmap, Inc
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
package com.hashmapinc.server;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.hashmapinc.server.dao.CustomSqlUnit;
import org.junit.ClassRule;
import org.junit.extensions.cpsuite.ClasspathSuite;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.springframework.cloud.contract.wiremock.WireMockSpring;

import java.util.Arrays;

@RunWith(ClasspathSuite.class)
@ClasspathSuite.ClassnameFilters({
        "com.hashmapinc.server.controller.sql.*SqlTest",
        "com.hashmapinc.server.mqtt.rpc.sql.*Test",
        "com.hashmapinc.server.mqtt.telemetry.sql.*Test",
        "com.hashmapinc.server.system.sql.*SqlTest"
})
public class SqlIntegrationTestSuite {

    private static WireMockClassRule wiremock = new WireMockClassRule(
            WireMockSpring.options().port(9002).usingFilesUnderClasspath("."));

    private static CustomSqlUnit sqlUnit = new CustomSqlUnit(
            Arrays.asList("sql/hsql/schema.sql", "sql/system-data.sql"),
            "sql/drop-all-tables.sql",
            "sql-test.properties",
            Arrays.asList("sql/hsql/upgrade/1.sql" , "sql/hsql/upgrade/2.sql", "sql/hsql/upgrade/3.sql"));

    @ClassRule
    public static TestRule ruleChain = RuleChain.outerRule(wiremock)
            .around(sqlUnit);

}
