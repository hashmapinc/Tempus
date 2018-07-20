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

import com.hashmapinc.server.common.msg.exception.TempusRuntimeException;
import com.hashmapinc.server.exception.TempusApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import com.hashmapinc.server.dao.dashboard.DashboardService;
import com.hashmapinc.server.dao.util.SqlDao;
import com.hashmapinc.server.service.install.sql.SqlDbHelper;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static com.hashmapinc.server.service.install.DatabaseHelper.*;
import static com.hashmapinc.server.service.install.DatabaseHelper.CONFIGURATION;

@Service
@Profile("install")
@Slf4j
@SqlDao
public class SqlDatabaseUpgradeService implements DatabaseUpgradeService {

    private static final String SCHEMA_UPDATE_SQL = "schema_update.sql";

    @Value("${install.data_dir}")
    private String dataDir;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUserName;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Autowired
    private DashboardService dashboardService;

    @Override
    public void upgradeDatabase(String fromVersion) throws TempusApplicationException {
        try {
            switch (fromVersion) {
                case "1.3.0":
                    log.info("Updating schema ...");
                    Path schemaUpdateFile = Paths.get(this.dataDir, "upgrade", "1.3.1", SCHEMA_UPDATE_SQL);
                    try (Connection conn = DriverManager.getConnection(dbUrl, dbUserName, dbPassword)) {
                        String sql = new String(Files.readAllBytes(schemaUpdateFile), Charset.forName("UTF-8"));
                        conn.createStatement().execute(sql); //NOSONAR, ignoring because method used to execute tempus database upgrade script
                    }
                    log.info("Schema updated.");
                    break;
                case "1.3.1":
                    try (Connection conn = DriverManager.getConnection(dbUrl, dbUserName, dbPassword)) {

                        log.info("Dumping dashboards ...");
                        Path dashboardsDump = SqlDbHelper.dumpTableIfExists(conn, DASHBOARD,
                                new String[]{ID, TENANT_ID, CUSTOMER_ID, TITLE, SEARCH_TEXT, ASSIGNED_CUSTOMERS, CONFIGURATION},
                                new String[]{"", "", "", "", "", "", ""},
                                "tb-dashboards", true);
                        log.info("Dashboards dumped.");

                        log.info("Updating schema ...");
                        schemaUpdateFile = Paths.get(this.dataDir, "upgrade", "1.4.0", SCHEMA_UPDATE_SQL);
                        String sql = new String(Files.readAllBytes(schemaUpdateFile), Charset.forName("UTF-8"));
                        conn.createStatement().execute(sql); //NOSONAR, ignoring because method used to execute tempus database upgrade script
                        log.info("Schema updated.");

                        log.info("Restoring dashboards ...");
                        if (dashboardsDump != null) {
                            SqlDbHelper.loadTable(conn, DASHBOARD,
                                    new String[]{ID, TENANT_ID, TITLE, SEARCH_TEXT, CONFIGURATION}, dashboardsDump, true);
                            DatabaseHelper.upgradeTo40AssignDashboards(dashboardsDump, dashboardService, true);
                            Files.deleteIfExists(dashboardsDump);
                        }
                        log.info("Dashboards restored.");
                    }
                    break;
                default:
                    throw new TempusRuntimeException("Unable to upgrade SQL database, unsupported fromVersion: " + fromVersion);
            }
        } catch (SQLException | IOException e) {
            throw new TempusApplicationException(e);
        }
    }
}
