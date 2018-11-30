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
package com.hashmapinc.server.controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hashmapinc.server.common.data.Logo;
import com.hashmapinc.server.common.data.Tenant;
import com.hashmapinc.server.common.data.Theme;
import com.hashmapinc.server.common.data.UserSettings;
import com.hashmapinc.server.common.data.id.UserId;
import com.hashmapinc.server.common.data.security.Authority;
import com.hashmapinc.server.dao.logo.LogoService;
import com.hashmapinc.server.dao.settings.UserSettingsService;
import com.hashmapinc.server.dao.theme.ThemeService;
import com.hashmapinc.server.exception.TempusException;
import com.hashmapinc.server.service.mail.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.hashmapinc.server.dao.tenant.TenantService;
import com.hashmapinc.server.common.data.id.TenantId;


import java.util.List;


@Slf4j
@RestController
@RequestMapping("/api")
public class UserSettingsController extends BaseController {

    @Autowired
    private MailService mailService;
    
    @Autowired
    private UserSettingsService userSettingsService;

    @Autowired
    private ThemeService themeService;

    @Autowired
    private LogoService logoService;

    @Autowired
    private TenantService tenantService;


    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER', 'SYS_ADMIN')")
    @GetMapping(value = "/settings/{key}")
    @ResponseBody
    public UserSettings getUserSettings(@PathVariable("key") String key) throws TempusException {
        try {
            UserId userId = getCurrentUser().getId();
            return userSettingsService.findUserSettingsByKeyAndUserId(key, userId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER', 'SYS_ADMIN')")
    @PostMapping(value = "/settings")
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
    @PostMapping(value = "/settings/testMail")
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

    @PreAuthorize("hasAuthority('SYS_ADMIN')")
    @GetMapping(value = "/settings/themes")
    public List<Theme> getThemes() throws TempusException  {
        try {
            return themeService.findAll();
        } catch (Exception e) {
            throw handleException(e);
        }
    }



    @GetMapping(value = "/theming")
    @ResponseStatus(value = HttpStatus.OK)
    public Theme getEnabledTheme() throws TempusException  {
        try {
            return themeService.findEnabledTheme();
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('SYS_ADMIN')")
    @PostMapping(value = "/settings/theme")
    public Theme updateTheme(@RequestBody String value) throws TempusException {
        try {
            JsonObject request = new JsonParser().parse(value).getAsJsonObject();
            return themeService.updateThemeStatus(request.get("value").getAsString());
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('SYS_ADMIN')")
    @PostMapping(value = "/settings/uploadLogo")
    public Logo uploadLogo(@RequestParam("file") MultipartFile file) throws TempusException {
        try {

            Logo l = new Logo();
            l.setDisplay(true);
            l.setName(file.getOriginalFilename());
            l.setFile(file.getBytes());

            return logoService.saveLogo(l);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @GetMapping(value = "/logo")
    @ResponseStatus(value = HttpStatus.OK)
    public Logo getLogo() throws TempusException  {
        try {

            List <Logo> logo = logoService.find();

            if(!logo.isEmpty()) {

                return logo.get(0);
            }

            return null;

        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER', 'SYS_ADMIN')")
    @GetMapping(value = "/settings/getUserLogo/{tenantId}")
    @ResponseStatus(value = HttpStatus.OK)
    public String getUserLogo(@PathVariable("tenantId") String strTenantId) throws TempusException {
        checkParameter("tenantId", strTenantId);
        try {
            TenantId tenantId = new TenantId(toUUID(strTenantId));
            log.info("tenant in controller{}",tenantId);
            checkTenantId(tenantId);
            return tenantService.findLogoByTenantId(tenantId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

}
