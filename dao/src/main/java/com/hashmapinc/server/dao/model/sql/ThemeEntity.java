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

package com.hashmapinc.server.dao.model.sql;

import com.hashmapinc.server.common.data.Theme;
import com.hashmapinc.server.common.data.id.ThemeId;
import com.hashmapinc.server.dao.model.BaseEntity;
import com.hashmapinc.server.dao.model.BaseSqlEntity;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.util.mapping.JsonStringType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@TypeDef(name = "json", typeClass = JsonStringType.class)
@Table(name = ModelConstants.THEME_TABLE_NAME)
public class ThemeEntity extends BaseSqlEntity<Theme>implements BaseEntity<Theme> {

    @Column(name = ModelConstants.THEME_NAME)
    private String name;

    @Column(name = ModelConstants.THEME_VALUE)
    private String value;


    @Column(name = ModelConstants.IS_ENABLED)
    private boolean is_enabled;


    public ThemeEntity() {
        super();
    }

    public ThemeEntity(Theme theme) {
        if (theme.getId() != null) {
            this.setId(theme.getId().getId());
        }

        this.name = theme.getThemeName();
        this.value = theme.getThemeValue();
        this.is_enabled = theme.getThemeStatus();
    }

    @Override
    public Theme toData() {
        Theme theme = new Theme(new ThemeId(getId()));
        theme.setThemeName(name);
        theme.setThemeValue(value);
        theme.setThemeStatus(is_enabled);
        return theme;
    }


}
