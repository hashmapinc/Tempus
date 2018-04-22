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
package com.hashmapinc.server.service.install;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.hashmapinc.server.dao.cassandra.CassandraInstallCluster;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.util.NoSqlDao;
import com.hashmapinc.server.service.install.cql.CQLStatementsParser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static com.hashmapinc.server.dao.model.ModelConstants.tempus_KEYSPACE;

@Service
@NoSqlDao
@Profile("install")
@Slf4j
public class CassandraDatabaseSchemaService implements DatabaseSchemaService {

    private static final String CASSANDRA_DIR = "cassandra";
    private static final String SCHEMA_CQL = "schema.cql";
    private static final String UPGRADE_DIR = "upgrade";

    @Value("${install.data_dir}")
    private String dataDir;

    @Autowired
    private CassandraInstallCluster cluster;

    @Override
    public void createDatabaseSchema() throws Exception {
        log.info("Installing Cassandra DataBase schema...");

        Path schemaFile = Paths.get(this.dataDir, CASSANDRA_DIR, SCHEMA_CQL);
        loadCql(schemaFile);

        log.info("Installing pending upgrades ...");

        Path upgradeScriptsDirectory = Paths.get(this.dataDir, CASSANDRA_DIR, UPGRADE_DIR);
        List<String> executedUpgrades = new ArrayList<>();
        ResultSet resultSet = cluster.getSession().execute("select "+ModelConstants.INSTALLED_SCRIPTS_COLUMN+" from " +tempus_KEYSPACE +"." + ModelConstants.INSTALLED_SCHEMA_VERSIONS+";");
        Iterator<Row> rowIterator = resultSet.iterator();

        while(rowIterator.hasNext()) {
            Row row = rowIterator.next();
            executedUpgrades.add(row.getString(ModelConstants.INSTALLED_SCRIPTS_COLUMN));
        }
        List<Integer> sortedScriptsIndexes = Files.list(upgradeScriptsDirectory).map(a -> stripExtensionFromName(a.getFileName().toString())).sorted().collect(Collectors.toList());

        for(Integer i: sortedScriptsIndexes) {
            String scriptFileName = i.toString()+".cql";
            if(!executedUpgrades.contains(scriptFileName)) {
                loadCql(upgradeScriptsDirectory.resolve(scriptFileName));
                cluster.getSession().execute("insert into " +tempus_KEYSPACE +"."+ ModelConstants.INSTALLED_SCHEMA_VERSIONS+ "("+ModelConstants.INSTALLED_SCRIPTS_COLUMN+")" +" values('"+scriptFileName+"'"+")");
            }
        }
    }

    private void loadCql(Path cql) throws Exception {
        List<String> statements = new CQLStatementsParser(cql).getStatements();
        statements.forEach(statement -> cluster.getSession().execute(statement));
    }

    private Integer stripExtensionFromName(String fileName) {
        return Integer.parseInt(fileName.substring(0, fileName.indexOf(".cql")));
    }
}
