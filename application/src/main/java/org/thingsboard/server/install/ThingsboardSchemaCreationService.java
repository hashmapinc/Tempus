/**
 * Copyright © 2016-2017 Hashmap, Inc
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
package org.thingsboard.server.install;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.thingsboard.server.service.install.DatabaseSchemaService;

import java.nio.file.Files;
import java.nio.file.Paths;

@Service
@Profile("install")
@Slf4j
public class ThingsboardSchemaCreationService {

    @Value("${install.upgrade:false}")
    private Boolean isUpgrade;

    @Value("${install.upgrade.from_version:1.2.3}")
    private String upgradeFromVersion;

    @Value("${install.data_dir}")
    private String dataDir;

    @Value("${install.load_demo:false}")
    private Boolean loadDemo;

    @Autowired
    private DatabaseSchemaService databaseSchemaService;

    @Autowired
    private ApplicationContext context;

    public void performInstall() {
        try {
        log.info("Starting Tempus Installation...");

        if (this.dataDir == null) {
            throw new RuntimeException("'install.data_dir' property should specified!");
        }
        if (!Files.isDirectory(Paths.get(this.dataDir))) {
            throw new RuntimeException("'install.data_dir' property value is not a valid directory!");
        }

        log.info("Installing DataBase schema...");

            databaseSchemaService.createDatabaseSchema();
        } catch (Exception e) {
            log.error("Unexpected error during Tempus installation!", e);
            throw new ThingsboardInstallException("Unexpected error during Tempus installation!", e);
        } finally {
            SpringApplication.exit(context);
        }
    }
}
