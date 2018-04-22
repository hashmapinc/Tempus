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

import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.util.SqlDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Profile("install")
@Slf4j
@SqlDao
public class SqlDatabaseSchemaService implements DatabaseSchemaService {

    private static final String SQL_DIR = "sql";
    private static final String UPGRADE_DIR = "upgrade";
    private static final String SCHEMA_SQL = "schema.sql";

    @Value("${install.data_dir}")
    private String dataDir;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUserName;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Override
    public void createDatabaseSchema() throws Exception {

        log.info("Installing SQL DataBase schema...");

        Path schemaFile = Paths.get(this.dataDir, SQL_DIR, SCHEMA_SQL);
        Path upgradeScriptsDirectory = Paths.get(this.dataDir, SQL_DIR, UPGRADE_DIR);

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUserName, dbPassword)) {
            String sql = new String(Files.readAllBytes(schemaFile), Charset.forName("UTF-8"));

            conn.createStatement().execute(sql);

            log.info("Installing pending upgrades ...");

            List<String> executedUpgrades = new ArrayList<>();
            ResultSet rs = conn.createStatement().executeQuery("select "+ ModelConstants.INSTALLED_SCRIPTS_COLUMN + " from "+ ModelConstants.INSTALLED_SCHEMA_VERSIONS);
            while (rs.next()) {
                executedUpgrades.add(rs.getString(ModelConstants.INSTALLED_SCRIPTS_COLUMN));
            }

            List<Integer> sortedScriptsIndexes = Files.list(upgradeScriptsDirectory).map(a -> stripExtensionFromName(a.getFileName().toString())).sorted().collect(Collectors.toList());

            for(Integer i: sortedScriptsIndexes) {
                String scriptFileName = i.toString()+".sql";
                if(!executedUpgrades.contains(scriptFileName)) {
                    String upgradeQueries = new String(Files.readAllBytes(upgradeScriptsDirectory.resolve(scriptFileName)), Charset.forName("UTF-8"));
                    conn.createStatement().execute(upgradeQueries);
                    conn.createStatement().execute("insert into " + ModelConstants.INSTALLED_SCHEMA_VERSIONS+ " values('"+scriptFileName+"'"+")");
                }
            }

            conn.createStatement().execute(sql); //NOSONAR, ignoring because method used to load initial tempus database schema

        }

    }

    private Integer stripExtensionFromName(String fileName) {
        return Integer.parseInt(fileName.substring(0, fileName.indexOf(".sql")));
    }

}
