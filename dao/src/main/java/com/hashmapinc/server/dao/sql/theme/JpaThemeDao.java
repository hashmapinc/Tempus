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

package com.hashmapinc.server.dao.sql.theme;

import com.hashmapinc.server.common.data.Theme;
import com.hashmapinc.server.dao.DaoUtil;
import com.hashmapinc.server.dao.model.sql.ThemeEntity;
import com.hashmapinc.server.dao.sql.JpaAbstractDao;
import com.hashmapinc.server.dao.theme.ThemeDao;
import com.hashmapinc.server.dao.util.SqlDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;


@Component
@SqlDao
@Slf4j
public class JpaThemeDao extends JpaAbstractDao<ThemeEntity,Theme> implements ThemeDao {

    @Autowired
    private ThemeRepository themeRepository;

    @Override
    protected Class<ThemeEntity> getEntityClass() {
        return ThemeEntity.class;
    }

    @Override
    protected CrudRepository<ThemeEntity, String> getCrudRepository() {
        return themeRepository;
    }

    @Override
    public Theme findEnabledTheme() {
        Theme theme = DaoUtil.getData(themeRepository.findEnabledTheme());
        if (theme != null) {
            return theme;
        } else {
            return null;
        }
    }


    @Override
    public Theme findByValue(String value) {
        Theme theme = DaoUtil.getData(themeRepository.findByValue(value));
        if (theme != null) {
            return theme;
        } else {
            return null;
        }
    }
}
