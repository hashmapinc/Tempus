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
package com.hashmapinc.server.dao.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hashmapinc.server.common.data.User;
import com.hashmapinc.server.common.data.UserSettings;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.hashmapinc.server.dao.exception.DataValidationException;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseUserSettingsServiceTest extends AbstractServiceTest {

    private User admin;

    @Before
    public void before() {
        this.admin = userService.findUserByEmail("sysadmin@hashmapinc.com");
    }

    @Test
    public void testFindAdminSettingsByKey() {
        UserSettings userSettings = userSettingsService.findUserSettingsByKeyAndUserId("general", admin.getId());
        Assert.assertNotNull(userSettings);
        userSettings = userSettingsService.findUserSettingsByKeyAndUserId("mail", admin.getId());
        Assert.assertNotNull(userSettings);
        userSettings = userSettingsService.findUserSettingsByKeyAndUserId("unknown", admin.getId());
        Assert.assertNull(userSettings);
    }
    
    @Test
    public void testFindAdminSettingsById() {
        UserSettings userSettings = userSettingsService.findUserSettingsByKeyAndUserId("general", admin.getId());
        UserSettings foundUserSettings = userSettingsService.findUserSettingsById(userSettings.getId());
        Assert.assertNotNull(foundUserSettings);
        Assert.assertEquals(userSettings, foundUserSettings);
    }
    
    @Test
    public void testSaveAdminSettings() throws Exception {
        UserSettings userSettings = userSettingsService.findUserSettingsByKeyAndUserId("general", admin.getId());
        JsonNode json = userSettings.getJsonValue();
        ((ObjectNode) json).put("baseUrl", "http://myhost.org");
        userSettings.setJsonValue(json);
        userSettingsService.saveUserSettings(userSettings);
        UserSettings savedUserSettings = userSettingsService.findUserSettingsByKeyAndUserId("general", admin.getId());
        Assert.assertNotNull(savedUserSettings);
        Assert.assertEquals(userSettings.getJsonValue(), savedUserSettings.getJsonValue());
    }
    
    @Test(expected = DataValidationException.class)
    public void testSaveAdminSettingsWithEmptyKey() {
        UserSettings userSettings = userSettingsService.findUserSettingsByKeyAndUserId("mail", admin.getId());
        userSettings.setKey(null);
        userSettingsService.saveUserSettings(userSettings);
    }
    
    @Test(expected = DataValidationException.class)
    public void testChangeAdminSettingsKey() {
        UserSettings userSettings = userSettingsService.findUserSettingsByKeyAndUserId("mail", admin.getId());
        userSettings.setKey("newKey");
        userSettingsService.saveUserSettings(userSettings);
    }
    
    @Test(expected = DataValidationException.class)
    public void testSaveAdminSettingsWithNewJsonStructure() throws Exception {
        UserSettings userSettings = userSettingsService.findUserSettingsByKeyAndUserId("mail", admin.getId());
        JsonNode json = userSettings.getJsonValue();
        ((ObjectNode) json).put("newKey", "my new value");
        userSettings.setJsonValue(json);
        userSettingsService.saveUserSettings(userSettings);
    }
    
    @Test(expected = DataValidationException.class)
    public void testSaveAdminSettingsWithNonTextValue() throws Exception {
        UserSettings userSettings = userSettingsService.findUserSettingsByKeyAndUserId("mail", admin.getId());
        JsonNode json = userSettings.getJsonValue();
        ((ObjectNode) json).put("timeout", 10000L);
        userSettings.setJsonValue(json);
        userSettingsService.saveUserSettings(userSettings);
    }
}
