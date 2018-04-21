/**
 * Copyright © 2016-2018 Hashmap, Inc
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
package org.thingsboard.server.system;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import org.cassandraunit.dataset.CQLDataSet;
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet;
import org.junit.ClassRule;
import org.junit.extensions.cpsuite.ClasspathSuite;
import org.junit.runner.RunWith;
import org.thingsboard.server.dao.CustomCassandraCQLUnit;
import org.thingsboard.server.dao.model.ModelConstants;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static org.thingsboard.server.dao.model.ModelConstants.THINGSBOARD_KEYSPACE;

/**
 * @author Andrew Shvayka
 */
@RunWith(ClasspathSuite.class)
@ClasspathSuite.ClassnameFilters({"org.thingsboard.server.system.*NoSqlTest"})
public class SystemNoSqlTestSuite {

    @ClassRule
    public static CustomCassandraCQLUnit cassandraUnit =
            new CustomCassandraCQLUnit(getDataSetLists(),
                    "cassandra-test.yaml", 30000l);

    private static List<CQLDataSet> getDataSetLists(){
        List<CQLDataSet> dataSets = new ArrayList<>();
        dataSets.add(new ClassPathCQLDataSet("cassandra/schema.cql", false, false));
        dataSets.add(new ClassPathCQLDataSet("cassandra/system-data.cql", false, false));
        dataSets.addAll(Arrays.asList(
                new ClassPathCQLDataSet("cassandra/upgrade/1.cql", false, false)

        ));
        return dataSets;
    }

    private static Integer stripExtensionFromName(String fileName) {
        return Integer.parseInt(fileName.substring(0, fileName.indexOf(".cql")));
    }
}
