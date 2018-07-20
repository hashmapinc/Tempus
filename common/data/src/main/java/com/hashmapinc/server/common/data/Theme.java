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

package com.hashmapinc.server.common.data;

import com.hashmapinc.server.common.data.id.ThemeId;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class Theme extends BaseData<ThemeId> {

    private String name;
    private String value;
    private boolean enabled;

    public Theme() {
        super();
    }

    public Theme(ThemeId id) {
        super(id);
    }

    public Theme(Theme theme) {
        super(theme);
        this.name = theme.name;
        this.value = theme.value;
        this.enabled = theme.enabled;
    }


    public String getThemeName() {
        return name;
    }

    public void setThemeName(String name) {
        this.name = name;
    }

    public String getThemeValue() {
        return value;
    }


    public void setThemeValue(String value) {
        this.value = value;
    }

    public boolean getThemeStatus() {
        return enabled;
    }

    public void setThemeStatus(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        return "theme{" +
                "name=" + name +
                ", value=" + value +
                ", is_enabled=" + enabled +
                '}';
    }

}
