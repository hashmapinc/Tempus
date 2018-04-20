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
package org.thingsboard.server.service.install;

import com.datastax.driver.core.KeyspaceMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.thingsboard.server.dao.cassandra.CassandraCluster;
import org.thingsboard.server.dao.cassandra.CassandraInstallCluster;
import org.thingsboard.server.dao.dashboard.DashboardService;
import org.thingsboard.server.dao.util.NoSqlDao;
import org.thingsboard.server.service.install.cql.CQLStatementsParser;
import org.thingsboard.server.service.install.cql.CassandraDbHelper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.thingsboard.server.service.install.DatabaseHelper.*;

@Service
@NoSqlDao
@Profile("install")
@Slf4j
public class CassandraDatabaseUpgradeService implements DatabaseUpgradeService {

    private static final String SCHEMA_UPDATE_CQL = "schema_update.cql";

    @Value("${install.data_dir}")
    private String dataDir;

    @Autowired
    private CassandraCluster cluster;

    @Autowired
    private CassandraInstallCluster installCluster;

    @Autowired
    private DashboardService dashboardService;

    @Override
    public void upgradeDatabase(String fromVersion) throws Exception {

        switch (fromVersion) {
            case "1.2.3":

                log.info("Upgrading Cassandara DataBase from version {} to 1.3.0 ...", fromVersion);

                //Dump devices, assets and relations

                cluster.getSession();

                KeyspaceMetadata ks = cluster.getCluster().getMetadata().getKeyspace(cluster.getKeyspaceName());

                log.info("Dumping devices ...");
                Path devicesDump = CassandraDbHelper.dumpCfIfExists(ks, cluster.getSession(), DEVICE,
                        new String[]{"id", TENANT_ID, CUSTOMER_ID, "name", SEARCH_TEXT, ADDITIONAL_INFO, "type"},
                        new String[]{"", "", "", "", "", "", "default"},
                        "tb-devices");
                log.info("Devices dumped.");

                log.info("Dumping assets ...");
                Path assetsDump = CassandraDbHelper.dumpCfIfExists(ks, cluster.getSession(), ASSET,
                        new String[]{"id", TENANT_ID, CUSTOMER_ID, "name", SEARCH_TEXT, ADDITIONAL_INFO, "type"},
                        new String[]{"", "", "", "", "", "", "default"},
                        "tb-assets");
                log.info("Assets dumped.");

                log.info("Dumping relations ...");
                Path relationsDump = CassandraDbHelper.dumpCfIfExists(ks, cluster.getSession(), "relation",
                        new String[]{"from_id", "from_type", "to_id", "to_type", "relation_type", ADDITIONAL_INFO, "relation_type_group"},
                        new String[]{"", "", "", "", "", "", "COMMON"},
                        "tb-relations");
                log.info("Relations dumped.");

                log.info("Updating schema ...");
                Path schemaUpdateFile = Paths.get(this.dataDir, "upgrade", "1.3.0", SCHEMA_UPDATE_CQL);
                loadCql(schemaUpdateFile);
                log.info("Schema updated.");

                //Restore devices, assets and relations

                log.info("Restoring devices ...");
                if (devicesDump != null) {
                    CassandraDbHelper.loadCf(ks, cluster.getSession(), DEVICE,
                            new String[]{"id", TENANT_ID, CUSTOMER_ID, "name", SEARCH_TEXT, ADDITIONAL_INFO, "type"}, devicesDump);
                    Files.deleteIfExists(devicesDump);
                }
                log.info("Devices restored.");

                log.info("Dumping device types ...");
                Path deviceTypesDump = CassandraDbHelper.dumpCfIfExists(ks, cluster.getSession(), DEVICE,
                        new String[]{TENANT_ID, "type"},
                        new String[]{"", ""},
                        "tb-device-types");
                if (deviceTypesDump != null) {
                    CassandraDbHelper.appendToEndOfLine(deviceTypesDump, "DEVICE");
                }
                log.info("Device types dumped.");
                log.info("Loading device types ...");
                if (deviceTypesDump != null) {
                    CassandraDbHelper.loadCf(ks, cluster.getSession(), "entity_subtype",
                            new String[]{TENANT_ID, "type", "entity_type"}, deviceTypesDump);
                    Files.deleteIfExists(deviceTypesDump);
                }
                log.info("Device types loaded.");

                log.info("Restoring assets ...");
                if (assetsDump != null) {
                    CassandraDbHelper.loadCf(ks, cluster.getSession(), ASSET,
                            new String[]{"id", TENANT_ID, CUSTOMER_ID, "name", SEARCH_TEXT, ADDITIONAL_INFO, "type"}, assetsDump);
                    Files.deleteIfExists(assetsDump);
                }
                log.info("Assets restored.");

                log.info("Dumping asset types ...");
                Path assetTypesDump = CassandraDbHelper.dumpCfIfExists(ks, cluster.getSession(), ASSET,
                        new String[]{TENANT_ID, "type"},
                        new String[]{"", ""},
                        "tb-asset-types");
                if (assetTypesDump != null) {
                    CassandraDbHelper.appendToEndOfLine(assetTypesDump, "ASSET");
                }
                log.info("Asset types dumped.");
                log.info("Loading asset types ...");
                if (assetTypesDump != null) {
                    CassandraDbHelper.loadCf(ks, cluster.getSession(), "entity_subtype",
                            new String[]{TENANT_ID, "type", "entity_type"}, assetTypesDump);
                    Files.deleteIfExists(assetTypesDump);
                }
                log.info("Asset types loaded.");

                log.info("Restoring relations ...");
                if (relationsDump != null) {
                    CassandraDbHelper.loadCf(ks, cluster.getSession(), "relation",
                            new String[]{"from_id", "from_type", "to_id", "to_type", "relation_type", ADDITIONAL_INFO, "relation_type_group"}, relationsDump);
                    Files.deleteIfExists(relationsDump);
                }
                log.info("Relations restored.");

                break;
            case "1.3.0":
                break;
            case "1.3.1":

                cluster.getSession();

                ks = cluster.getCluster().getMetadata().getKeyspace(cluster.getKeyspaceName());

                log.info("Dumping dashboards ...");
                Path dashboardsDump = CassandraDbHelper.dumpCfIfExists(ks, cluster.getSession(), DASHBOARD,
                        new String[]{ID, TENANT_ID, CUSTOMER_ID, TITLE, SEARCH_TEXT, ASSIGNED_CUSTOMERS, CONFIGURATION},
                        new String[]{"", "", "", "", "", "", ""},
                        "tb-dashboards", true);
                log.info("Dashboards dumped.");


                log.info("Updating schema ...");
                schemaUpdateFile = Paths.get(this.dataDir, "upgrade", "1.4.0", SCHEMA_UPDATE_CQL);
                loadCql(schemaUpdateFile);
                log.info("Schema updated.");

                log.info("Restoring dashboards ...");
                if (dashboardsDump != null) {
                    CassandraDbHelper.loadCf(ks, cluster.getSession(), DASHBOARD,
                            new String[]{ID, TENANT_ID, TITLE, SEARCH_TEXT, CONFIGURATION}, dashboardsDump, true);
                    DatabaseHelper.upgradeTo40_assignDashboards(dashboardsDump, dashboardService, false);
                    Files.deleteIfExists(dashboardsDump);
                }
                log.info("Dashboards restored.");
                break;
            default:
                throw new RuntimeException("Unable to upgrade Cassandra database, unsupported fromVersion: " + fromVersion);
        }

    }

    private void loadCql(Path cql) throws Exception {
        List<String> statements = new CQLStatementsParser(cql).getStatements();
        statements.forEach(statement -> installCluster.getSession().execute(statement));
    }

}
