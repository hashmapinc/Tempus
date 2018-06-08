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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hashmapinc.server.common.data.Logo;
import com.hashmapinc.server.common.data.Theme;
import com.hashmapinc.server.common.data.User;
import com.hashmapinc.server.common.data.UserSettings;
import com.hashmapinc.server.common.data.id.CustomerId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.id.UserId;
import com.hashmapinc.server.common.data.page.TextPageLink;
import com.hashmapinc.server.common.data.security.Authority;
import com.hashmapinc.server.dao.logo.LogoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.hashmapinc.server.dao.settings.UserSettingsService;
import com.hashmapinc.server.dao.theme.ThemeService;
import com.hashmapinc.server.exception.TempusException;
import com.hashmapinc.server.service.mail.MailService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;


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

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER', 'SYS_ADMIN')")
    @RequestMapping(value = "/settings/{key}", method = RequestMethod.GET)
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

    @PreAuthorize("hasAuthority('SYS_ADMIN')")
    @RequestMapping(value = "/settings/themes", method = RequestMethod.GET)
    public List<Theme> getThemes() throws TempusException  {
        try {
            return themeService.findAll();
        } catch (Exception e) {
            throw handleException(e);
        }
    }



    @RequestMapping(value = "/theming", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public Theme getEnabledTheme() throws TempusException  {
        try {
            return themeService.findEnabledTheme();
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('SYS_ADMIN')")
    @RequestMapping(value = "/settings/theme", method = RequestMethod.POST)
    public Theme updateTheme(@RequestBody String value) throws TempusException {
        try {
            JsonObject request = new JsonParser().parse(value).getAsJsonObject();
            return themeService.updateThemeStatus(request.get("value").getAsString());
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('SYS_ADMIN')")
    @RequestMapping(value = "/settings/uploadLogo", method = RequestMethod.POST)
    public Logo uploadLogo(@RequestParam("file") MultipartFile file) throws TempusException {
        try {

            Logo l = new Logo();
            l.setDisplay(true);
            l.setName(file.getOriginalFilename());
            l.setFile(file.getBytes());

           return logoService.saveLogo(l);
           // return null;
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @RequestMapping(value = "/logo", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public Logo getLogo() throws TempusException  {
        try {

            List <Logo> logo = logoService.find();

            if(logo.size() > 0) {

                return logo.get(0);
            }

            return null;

        } catch (Exception e) {
            throw handleException(e);
        }
    }



}
