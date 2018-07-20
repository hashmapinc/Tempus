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
package com.hashmapinc.server.dao.sql.device;

import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.hashmapinc.server.common.data.security.DeviceCredentials;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.hashmapinc.server.dao.AbstractJpaDaoTest;
import com.hashmapinc.server.dao.device.DeviceCredentialsDao;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by Valerii Sosliuk on 5/6/2017.
 */
public class JpaDeviceCredentialsDaoTest extends AbstractJpaDaoTest {

    @Autowired
    DeviceCredentialsDao deviceCredentialsDao;

    @Test
    @DatabaseSetup("classpath:dbunit/device_credentials.xml")
    public void testFindByDeviceId() {
        UUID deviceId = UUID.fromString("958e3a30-3215-11e7-93ae-92361f002671");
        DeviceCredentials deviceCredentials = deviceCredentialsDao.findByDeviceId(deviceId);
        assertNotNull(deviceCredentials);
        assertEquals("958e3314-3215-11e7-93ae-92361f002671", deviceCredentials.getId().getId().toString());
        assertEquals("ID_1", deviceCredentials.getCredentialsId());
    }

    @Test
    @DatabaseSetup("classpath:dbunit/device_credentials.xml")
    public void findByCredentialsId() {
        String credentialsId = "ID_2";
        DeviceCredentials deviceCredentials = deviceCredentialsDao.findByCredentialsId(credentialsId);
        assertNotNull(deviceCredentials);
        assertEquals("958e3c74-3215-11e7-93ae-92361f002671", deviceCredentials.getId().getId().toString());
    }
}
