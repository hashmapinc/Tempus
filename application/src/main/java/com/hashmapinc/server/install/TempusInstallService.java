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
package com.hashmapinc.server.install;

import com.hashmapinc.server.common.msg.exception.TempusRuntimeException;
import com.hashmapinc.server.service.component.ComponentDiscoveryService;
import com.hashmapinc.server.service.install.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;
import java.util.Optional;

@Service
@Profile("install")
@Slf4j
public class TempusInstallService {

    @Value("${install.upgrade:false}")
    private Boolean isUpgrade;

    @Value("${install.upgrade.from_version:1.2.3}")
    private String upgradeFromVersion;

    @Value("${install.data_dir}")
    private String dataDir;

    @Value("${install.load_demo:false}")
    private Boolean loadDemo;

//    @Autowired
//    private DatabaseSchemaService databaseSchemaService;

    @Autowired
    private SqlDatabaseSchemaService sqlDatabaseSchemaService;

    @Autowired
    private Optional<CassandraDatabaseSchemaService> cassandraDatabaseSchemaService;

//    @Autowired
//    private DatabaseUpgradeService databaseUpgradeService;

    @Autowired
    private SqlDatabaseUpgradeService sqlDatabaseUpgradeService;

    @Autowired
    private Optional<CassandraDatabaseUpgradeService> cassandraDatabaseUpgradeService;

    @Autowired
    private ComponentDiscoveryService componentDiscoveryService;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private SystemDataLoaderService systemDataLoaderService;

    public void performInstall() {
        try {
            if (isUpgrade) {
                log.info("Starting Tempus Upgrade from version {} ...", upgradeFromVersion);

                switch (upgradeFromVersion) {
                    case "1.2.3": //NOSONAR, Need to execute gradual upgrade starting from upgradeFromVersion
                        log.info("Upgrading Tempus from version 1.2.3 to 1.3.0 ...");

                        sqlDatabaseUpgradeService.upgradeDatabase("1.2.3");
                        if(cassandraDatabaseUpgradeService.isPresent()){
                            cassandraDatabaseUpgradeService.get().upgradeDatabase("1.2.3");
                        }

                    case "1.3.0":  //NOSONAR, Need to execute gradual upgrade starting from upgradeFromVersion
                        log.info("Upgrading Tempus from version 1.3.0 to 1.3.1 ...");

                        sqlDatabaseUpgradeService.upgradeDatabase("1.3.0");
                        if(cassandraDatabaseUpgradeService.isPresent()) {
                            cassandraDatabaseUpgradeService.get().upgradeDatabase("1.3.0");
                        }

                    case "1.3.1":
                        log.info("Upgrading Tempus from version 1.3.1 to 1.4.0 ...");

                        sqlDatabaseUpgradeService.upgradeDatabase("1.3.1");
                        if(cassandraDatabaseUpgradeService.isPresent()) {
                            cassandraDatabaseUpgradeService.get().upgradeDatabase("1.3.1");
                        }

                        log.info("Updating system data...");

                        systemDataLoaderService.deleteSystemWidgetBundle("charts");
                        systemDataLoaderService.deleteSystemWidgetBundle("cards");
                        systemDataLoaderService.deleteSystemWidgetBundle("maps");
                        systemDataLoaderService.deleteSystemWidgetBundle("analogue_gauges");
                        systemDataLoaderService.deleteSystemWidgetBundle("digital_gauges");
                        systemDataLoaderService.deleteSystemWidgetBundle("gpio_widgets");
                        systemDataLoaderService.deleteSystemWidgetBundle("alarm_widgets");
                        systemDataLoaderService.deleteSystemWidgetBundle("control_widgets");
                        systemDataLoaderService.deleteSystemWidgetBundle("maps_v2");
                        systemDataLoaderService.deleteSystemWidgetBundle("gateway_widgets");

                        systemDataLoaderService.loadSystemWidgets();

                        break;
                    default:
                        throw new TempusRuntimeException("Unable to upgrade Tempus, unsupported fromVersion: " + upgradeFromVersion);

                }
                log.info("Upgrade finished successfully!");

            } else {

                log.info("Starting Tempus Installation...");

                if (this.dataDir == null) {
                    throw new TempusRuntimeException("'install.data_dir' property should specified!");
                }
                if (!Paths.get(this.dataDir).toFile().isDirectory()) {
                    throw new TempusRuntimeException("'install.data_dir' property value is not a valid directory!");
                }

                log.info("Installing DataBase schema...");

                //databaseSchemaService.createDatabaseSchema();
                sqlDatabaseSchemaService.createDatabaseSchema();
                if(cassandraDatabaseSchemaService.isPresent()) {
                    cassandraDatabaseSchemaService.get().createDatabaseSchema();
                }

                log.info("Loading system data...");

                componentDiscoveryService.discoverComponents();

                systemDataLoaderService.loadSystemThemes();
                systemDataLoaderService.createSysAdminWithGroupAndSettings();
                systemDataLoaderService.loadSystemWidgets();
                systemDataLoaderService.loadSystemPlugins();
                systemDataLoaderService.loadSystemRules();


                if (loadDemo) {
                    log.info("Loading demo data...");
                    systemDataLoaderService.loadDemoData();
                }
                log.info("Installation finished successfully!");
            }


        } catch (Exception e) {
            log.error("Unexpected error during Tempus installation!", e);
            throw new TempusInstallException("Unexpected error during Tempus installation!", e);
        } finally {
            SpringApplication.exit(context);
        }
    }

}
