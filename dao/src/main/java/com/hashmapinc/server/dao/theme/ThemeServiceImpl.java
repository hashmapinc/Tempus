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
package com.hashmapinc.server.dao.theme;

import com.hashmapinc.server.dao.theme.ThemeDao;
import com.hashmapinc.server.common.data.Theme;
import com.hashmapinc.server.dao.entity.AbstractEntityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@Slf4j
public class ThemeServiceImpl extends AbstractEntityService implements ThemeService {

    @Autowired
    private ThemeDao themeDao;

    @Override
    public List<Theme> findAll() {
        return themeDao.find();
    }

    @Override
    public Theme findEnabledTheme() {
        return themeDao.findEnabledTheme();
    }

    @Override
    public Theme updateThemeStatus(String value) {
        Theme theme = findEnabledTheme();
        if(theme != null) {
            theme.setThemeStatus(false);
            themeDao.save(theme);
            Theme themeNew = themeDao.findByValue(value);
            if(themeNew != null) {
                themeNew.setThemeStatus(true);
                return themeDao.save(themeNew);
            }
        }

        return null;
    }

    @Override
    public Theme saveTheme(Theme theme) {
        Theme themeNew =  themeDao.save(theme);
        if(themeNew != null) {
            return themeNew;
        }
        return null;
    }


    @Override
    public void deleteThemeEntryByvalue (String value) {

        Theme theme = themeDao.findByValue(value);

        if (theme != null) {

            themeDao.removeById(theme.getUuidId());
        }

    }




}
