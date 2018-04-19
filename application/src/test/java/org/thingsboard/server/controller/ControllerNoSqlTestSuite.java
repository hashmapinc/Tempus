/**
 * Copyright © 2016-2018 The Thingsboard Authors
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
package org.thingsboard.server.controller;

import org.cassandraunit.dataset.CQLDataSet;
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet;
import org.junit.ClassRule;
import org.junit.extensions.cpsuite.ClasspathSuite;
import org.junit.runner.RunWith;
import org.thingsboard.server.dao.CustomCassandraCQLUnit;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RunWith(ClasspathSuite.class)
@ClasspathSuite.ClassnameFilters({
        "org.thingsboard.server.controller.nosql.*Test"})
public class ControllerNoSqlTestSuite {

    @ClassRule
    public static CustomCassandraCQLUnit cassandraUnit =
            new CustomCassandraCQLUnit(
                    getDataSetLists(),
                    "cassandra-test.yaml", 30000l);

    private static List<CQLDataSet> getDataSetLists(){
        List<CQLDataSet> dataSets = new ArrayList<>();
        dataSets.add(new ClassPathCQLDataSet("cassandra/schema.cql", false, false));
        dataSets.add(new ClassPathCQLDataSet("cassandra/system-data.cql", false, false));
        dataSets.add(new ClassPathCQLDataSet("cassandra/system-test.cql", false, false));
        String upgradePath = "cassandra/upgrade/";
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        URL url = loader.getResource(upgradePath);
        String path = url.getPath();
        Path upgradeScriptsDirectory = Paths.get(path);
        List<Integer> sortedScriptsIndexes = null;
        try {
            sortedScriptsIndexes = Files.list(upgradeScriptsDirectory).map(a -> stripExtensionFromName(a.getFileName().toString())).sorted().collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        for(Integer i: sortedScriptsIndexes) {
            String scriptFileName = upgradePath + i.toString()+".cql";
            dataSets.add(new ClassPathCQLDataSet(scriptFileName, false, false));
        }
        return dataSets;
    }

    private static Integer stripExtensionFromName(String fileName) {
        return Integer.parseInt(fileName.substring(0, fileName.indexOf(".cql")));
    }
}
