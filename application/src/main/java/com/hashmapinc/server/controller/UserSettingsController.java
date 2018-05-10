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
package com.hashmapinc.server.controller;

import com.hashmapinc.server.common.data.UserSettings;
import com.hashmapinc.server.common.data.id.UserId;
import com.hashmapinc.server.common.data.security.Authority;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.hashmapinc.server.dao.settings.UserSettingsService;
import com.hashmapinc.server.exception.TempusException;
import com.hashmapinc.server.service.mail.MailService;

@RestController
@RequestMapping("/api")
public class UserSettingsController extends BaseController {

    @Autowired
    private MailService mailService;
    
    @Autowired
    private UserSettingsService userSettingsService;

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER', 'SYS_ADMIN')")
    @RequestMapping(value = "/settings/{key}", method = RequestMethod.GET)
    @ResponseBody
    public UserSettings getUserSettings(@PathVariable("key") String key) throws TempusException {
        try {
            UserId userId = getCurrentUser().getId();
            return checkNotNull(userSettingsService.findUserSettingsByKeyAndUserId(key, userId));
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER', 'SYS_ADMIN')")
    @RequestMapping(value = "/settings", method = RequestMethod.POST)
    @ResponseBody 
    public UserSettings saveUserSettings(@RequestBody UserSettings userSettings) throws TempusException {
        try {
            UserId userId = getCurrentUser().getId();
            userSettings.setUserId(userId);
            userSettings = checkNotNull(userSettingsService.saveUserSettings(userSettings));
            if (userSettings.getKey().equals("mail") && (getCurrentUser().getAuthority() == Authority.CUSTOMER_USER)) {
                mailService.updateMailConfiguration();
            }
            return userSettings;
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('SYS_ADMIN')")
    @RequestMapping(value = "/settings/testMail", method = RequestMethod.POST)
    public void sendTestMail(@RequestBody UserSettings userSettings) throws TempusException {
        try {
            userSettings = checkNotNull(userSettings);
            if (userSettings.getKey().equals("mail")) {
               String email = getCurrentUser().getEmail();
               mailService.sendTestMail(userSettings.getJsonValue(), email);
            }
        } catch (Exception e) {
            throw handleException(e);
        }
    }

}
