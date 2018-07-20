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
package com.hashmapinc.server;

import com.hashmapinc.server.dao.CustomCassandraCQLUnit;
import org.cassandraunit.dataset.CQLDataSet;
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet;
import org.junit.ClassRule;
import org.junit.extensions.cpsuite.ClasspathSuite;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

@RunWith(ClasspathSuite.class)
@ClasspathSuite.ClassnameFilters({
        "com.hashmapinc.server.dao.service.*ServiceNoSqlTest"
})
public class NoSqlIntegrationTestSuite {

    @ClassRule
    public static CustomCassandraCQLUnit cassandraUnit =
            new CustomCassandraCQLUnit(getDataSets(),
                    getUpgradeDataSets(),
                    "cassandra-test.yaml", 30000l);

    private static List<CQLDataSet> getDataSets(){
        List<CQLDataSet> dataSets = new ArrayList<>();
        dataSets.add(new ClassPathCQLDataSet("cassandra/schema.cql", false, false));
        dataSets.add(new ClassPathCQLDataSet("cassandra/system-data.cql", false, false));
        return dataSets;
    }

    private static List<CustomCassandraCQLUnit.NamedDataset> getUpgradeDataSets(){
        List<CustomCassandraCQLUnit.NamedDataset> dataSets = new ArrayList<>();
        dataSets.add(new CustomCassandraCQLUnit.NamedDataset("1.cql", new ClassPathCQLDataSet("cassandra/upgrade/1.cql" , false, false)));
        dataSets.add(new CustomCassandraCQLUnit.NamedDataset("2.cql", new ClassPathCQLDataSet("cassandra/upgrade/2.cql" , false, false)));
        return dataSets;
    }
}
