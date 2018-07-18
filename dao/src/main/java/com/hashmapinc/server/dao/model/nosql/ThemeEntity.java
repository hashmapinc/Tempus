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


package com.hashmapinc.server.dao.model.nosql;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.hashmapinc.server.common.data.Theme;
import com.hashmapinc.server.common.data.id.ThemeId;
import com.hashmapinc.server.dao.model.BaseEntity;
import com.hashmapinc.server.dao.model.ModelConstants;

import com.datastax.driver.mapping.annotations.Table;
import com.datastax.driver.mapping.annotations.Column;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.UUID;

import static com.hashmapinc.server.dao.model.ModelConstants.ID_PROPERTY;

@Table(name = ModelConstants.THEME_COLUMN_FAMILY_NAME)
@EqualsAndHashCode
@ToString
public class ThemeEntity implements BaseEntity<Theme> {

    @PartitionKey
    @Column(name = ID_PROPERTY)
    private UUID id;

    @Column(name = ModelConstants.THEME_NAME_PROPERTY)
    private String name;

    @Column(name = ModelConstants.THEME_VALUE_PROPERTY)
    private String value;

    @Column(name = ModelConstants.THEME_IS_ENABLED_PROPERTY)
    private boolean isEnabled;

    public ThemeEntity() {
        super();
    }

    public ThemeEntity(Theme theme) {
        if (theme.getId() != null) {
            this.setId(theme.getId().getId());
        }

        this.name = theme.getThemeName();
        this.value = theme.getThemeValue();
        this.isEnabled = theme.getThemeStatus();
    }


    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean getIsEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(boolean enabled) {
        this.isEnabled = enabled;
    }


    @Override
    public Theme toData() {
        Theme theme  = new Theme(new ThemeId(getId()));
        theme.setThemeName(name);
        theme.setThemeValue(value);
        theme.setThemeStatus(isEnabled);
        return theme;
    }


}
