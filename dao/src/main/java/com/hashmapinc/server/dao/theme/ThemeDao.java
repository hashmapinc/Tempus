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
import com.hashmapinc.server.common.data.Theme;
import com.hashmapinc.server.dao.Dao;

import java.util.List;

public interface ThemeDao extends Dao<Theme> {

    /**
     * Find theme for listing
     * @return the list of theme objects
     */
    List<Theme> find();

    /**
     * Find theme by status
     * @return the list of theme object
     */
    Theme findEnabledTheme();

    Theme save(Theme theme);

    /**
     * Find theme by value
     * @return the theme object
     */
    Theme findByValue(String value);
}
