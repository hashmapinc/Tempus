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
package com.hashmapinc.server.service.install;

import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.util.SqlDao;
import com.hashmapinc.server.exception.TempusApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
@Profile("install")
@Slf4j
public class SqlDatabaseSchemaService implements DatabaseSchemaService {

    private static final String SQL_DIR_HSQL = "sql/hsql";
    private static  String SQL_DIR = ""; //NOSONAR
    private static final String SQL_DIR_POSTGRES = "sql/postgres";
    private static final String UPGRADE_DIR = "upgrade";
    protected static final  String SCHEMA_SQL = "schema.sql";

    @Value("${install.data_dir}")
    private String dataDir;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUserName;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Override
    public void createDatabaseSchema() throws TempusApplicationException {

        log.info("Installing SQL DataBase schema...");
        int hsqldbConn = dbUrl.indexOf("hsqldb");
        int postgresConn = dbUrl.indexOf("postgres");



        if(postgresConn != -1) {
            SQL_DIR = SQL_DIR_POSTGRES; //NOSONAR
        }

        if(hsqldbConn != -1) {
            SQL_DIR = SQL_DIR_HSQL; //NOSONAR
        }


        Path schemaFile = Paths.get(this.dataDir, SQL_DIR, SCHEMA_SQL);
        Path upgradeScriptsDirectory = Paths.get(this.dataDir, SQL_DIR, UPGRADE_DIR);


        try {
            try (Connection conn = DriverManager.getConnection(dbUrl, dbUserName, dbPassword);
                 Statement stmt = conn.createStatement()) {

                String sql = new String(Files.readAllBytes(schemaFile), Charset.forName("UTF-8"));

                stmt.execute(sql); //NOSONAR Ignoring as we are reading queries from file


                List<String> executedUpgrades = new ArrayList<>();
                try (ResultSet rs = stmt.executeQuery("select " + ModelConstants.INSTALLED_SCRIPTS_COLUMN + " from " + ModelConstants.INSTALLED_SCHEMA_VERSIONS)) {
                    while (rs.next()) {
                        executedUpgrades.add(rs.getString(ModelConstants.INSTALLED_SCRIPTS_COLUMN));
                    }
                }

                if(!Files.isDirectory(upgradeScriptsDirectory)){
                    log.info("There are no upgrade scripts for SQL");
                    return;
                }

                try (Stream<Path> filesStream = Files.list(upgradeScriptsDirectory)) {
                    List<Integer> sortedScriptsIndexes = filesStream.map(a -> stripExtensionFromName(a.getFileName().toString())).sorted().collect(Collectors.toList());

                    for (Integer i : sortedScriptsIndexes) {
                        String scriptFileName = i.toString() + ".sql";
                        if (!executedUpgrades.contains(scriptFileName)) {
                            String upgradeQueries = new String(Files.readAllBytes(upgradeScriptsDirectory.resolve(scriptFileName)), Charset.forName("UTF-8"));
                            log.info(upgradeQueries);
                            stmt.execute(upgradeQueries); //NOSONAR Ignoring as we are reading queries from file
                            stmt.execute("insert into " + ModelConstants.INSTALLED_SCHEMA_VERSIONS + " values('" + scriptFileName + "'" + ")");
                        }
                    }

                    stmt.execute(sql); //NOSONAR, ignoring because method used to load initial tempus database schema
                }

            }
        } catch (SQLException | IOException e) {
            throw new TempusApplicationException(e);
        }
    }

    private Integer stripExtensionFromName(String fileName) {
        return Integer.parseInt(fileName.substring(0, fileName.indexOf(".sql")));
    }

}
