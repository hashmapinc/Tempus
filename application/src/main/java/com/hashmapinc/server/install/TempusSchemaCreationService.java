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
import com.hashmapinc.server.service.install.DatabaseSchemaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;

@Service
@Profile("install")
@Slf4j
public class TempusSchemaCreationService {

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
            throw new TempusRuntimeException("'install.data_dir' property should specified!");
        }
        if (!Paths.get(this.dataDir).toFile().isDirectory()) {
            throw new TempusRuntimeException("'install.data_dir' property value is not a valid directory!");
        }

        log.info("Installing DataBase schema...");

            databaseSchemaService.createDatabaseSchema();
        } catch (Exception e) {
            log.error("Unexpected error during Tempus installation!", e);
            throw new TempusInstallException("Unexpected error during Tempus installation!", e);
        } finally {
            SpringApplication.exit(context);
        }
    }
}
