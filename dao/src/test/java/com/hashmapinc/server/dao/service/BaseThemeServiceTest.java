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
import com.hashmapinc.server.common.data.Theme;
import com.datastax.driver.core.utils.UUIDs;
import com.hashmapinc.server.common.data.id.ThemeId;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.util.List;
import java.util.Optional;


public class BaseThemeServiceTest extends AbstractServiceTest {

    @Test
    public void saveTheme() throws Exception {

        Theme theme1 = new Theme();
        theme1.setThemeName("Tempus Blue");
        theme1.setThemeValue("themeBlue");
        theme1.setThemeStatus(false);
        themeService.saveTheme(theme1);

        Theme theme2 = new Theme();
        theme2.setThemeName("Tempus Dark");
        theme2.setThemeValue("themeDark");
        theme2.setThemeStatus(false);
        themeService.saveTheme(theme2);

        List<Theme> theme = themeService.findAll();
        Assert.assertEquals(2, theme.size());

        themeService.deleteThemeEntryByvalue("themeDark");
        themeService.deleteThemeEntryByvalue("themeBlue");


    }


    @Test
    public void findEnabledTheme() {

        Theme theme = new Theme();
        theme.setThemeName("Tempus Dark");
        theme.setThemeValue("themeDark");
        theme.setThemeStatus(true);
        themeService.saveTheme(theme);

        Theme themei = themeService.findEnabledTheme();
        Assert.assertEquals(theme.getThemeStatus(), themei.getThemeStatus());
        themeService.deleteThemeEntryByvalue("themeDark");
    }


    @Test
    public void updateThemeStatus() {

        Theme theme1 = new Theme();
        theme1.setThemeName("Tempus Blue");
        theme1.setThemeValue("themeBlue");
        theme1.setThemeStatus(false);
        themeService.saveTheme(theme1);

        Theme theme2 = new Theme();
        theme2.setThemeName("Tempus Dark");
        theme2.setThemeValue("themeDark");
        theme2.setThemeStatus(true);
        themeService.saveTheme(theme2);

        Theme enabledTheme = themeService.updateThemeStatus("themeBlue");
        Assert.assertEquals(true, enabledTheme.getThemeStatus());

        themeService.deleteThemeEntryByvalue("themeDark");
        themeService.deleteThemeEntryByvalue("themeBlue");

    }
}
