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
package org.thingsboard.server.dao;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import lombok.extern.slf4j.Slf4j;
import org.junit.rules.ExternalResource;
import org.thingsboard.server.dao.model.ModelConstants;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;


/**
 * Created by Valerii Sosliuk on 6/24/2017.
 */
@Slf4j
public class CustomSqlUnit extends ExternalResource {

    private final List<String> sqlFiles;
    private final String dropAllTablesSqlFile;
    private final String dbUrl;
    private final String dbUserName;
    private final String dbPassword;
    //private final String upgradePath;

    public CustomSqlUnit(List<String> sqlFiles, String dropAllTablesSqlFile, String configurationFileName//, String upgradePath
    ) {
        this.sqlFiles = sqlFiles;
        this.dropAllTablesSqlFile = dropAllTablesSqlFile;
        //this.upgradePath = upgradePath;
        final Properties properties = new Properties();
        try (final InputStream stream = this.getClass().getClassLoader().getResourceAsStream(configurationFileName)) {
            properties.load(stream);
            this.dbUrl = properties.getProperty("spring.datasource.url");
            this.dbUserName = properties.getProperty("spring.datasource.username");
            this.dbPassword = properties.getProperty("spring.datasource.password");
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void before() {
        cleanUpDb();

        Connection conn = null;
        try {
            conn = DriverManager.getConnection(dbUrl, dbUserName, dbPassword);
            //Path upgradeScriptsDirectory = Paths.get(upgradePath);

            for (String sqlFile : sqlFiles) {
                URL sqlFileUrl = Resources.getResource(sqlFile);
                String sql = Resources.toString(sqlFileUrl, Charsets.UTF_8);
                conn.createStatement().execute(sql);
            }

            log.info("Installing pending upgrades ...");

            List<String> executedUpgrades = new ArrayList<>();
            ResultSet rs = conn.createStatement().executeQuery("select "+ ModelConstants.INSTALLED_SCRIPTS_COLUMN + " from "+ ModelConstants.INSTALLED_SCHEMA_VERSIONS);
            while (rs.next()) {
                executedUpgrades.add(rs.getString(ModelConstants.INSTALLED_SCRIPTS_COLUMN));
            }

           // List<Integer> sortedScriptsIndexes = Files.list(upgradeScriptsDirectory).map(a -> stripExtensionFromName(a.getFileName().toString())).sorted().collect(Collectors.toList());

           /* for(Integer i: sortedScriptsIndexes) {
                String scriptFileName = i.toString()+".sql";
                if(!executedUpgrades.contains(scriptFileName)) {
                    String upgradeQueries = new String(Files.readAllBytes(upgradeScriptsDirectory.resolve(scriptFileName)), Charset.forName("UTF-8"));
                    conn.createStatement().execute(upgradeQueries);
                    conn.createStatement().execute("insert into " + ModelConstants.INSTALLED_SCHEMA_VERSIONS+ " values('"+scriptFileName+"'"+")");
                }
            }*/


        } catch (IOException | SQLException e) {
            throw new RuntimeException("Unable to start embedded hsqldb. Reason: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public void after() {
        cleanUpDb();
    }

    private void cleanUpDb() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(dbUrl, dbUserName, dbPassword);
            URL dropAllTableSqlFileUrl = Resources.getResource(dropAllTablesSqlFile);
            String dropAllTablesSql = Resources.toString(dropAllTableSqlFileUrl, Charsets.UTF_8);
            conn.createStatement().execute(dropAllTablesSql);
        } catch (IOException | SQLException e) {
            throw new RuntimeException("Unable to clean up embedded hsqldb. Reason: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    private Integer stripExtensionFromName(String fileName) {
        return Integer.parseInt(fileName.substring(0, fileName.indexOf(".sql")));
    }
}
