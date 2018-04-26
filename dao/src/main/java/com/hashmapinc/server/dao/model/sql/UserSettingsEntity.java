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

import com.datastax.driver.core.utils.UUIDs;
import com.fasterxml.jackson.databind.JsonNode;
import com.hashmapinc.server.common.data.UserSettings;
import com.hashmapinc.server.common.data.id.UserId;
import com.hashmapinc.server.common.data.id.UserSettingsId;
import com.hashmapinc.server.dao.model.BaseSqlEntity;
import com.hashmapinc.server.dao.util.mapping.JsonStringType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import com.hashmapinc.server.common.data.UUIDConverter;
import com.hashmapinc.server.dao.model.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import static com.hashmapinc.server.dao.model.ModelConstants.*;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@TypeDef(name = "json", typeClass = JsonStringType.class)
@Table(name = USER_SETTINGS_COLUMN_FAMILY_NAME)
public final class UserSettingsEntity extends BaseSqlEntity<UserSettings> implements BaseEntity<UserSettings> {

    @Column(name = USER_SETTINGS_KEY_PROPERTY)
    private String key;

    @Type(type = "json")
    @Column(name = USER_SETTINGS_JSON_VALUE_PROPERTY)
    private JsonNode jsonValue;

    @Column(name = USER_SETTINGS_USER_ID_PROPERTY)
    protected String userId;

    public UserSettingsEntity() {
        super();
    }

    public UserSettingsEntity(UserSettings userSettings) {
        if (userSettings.getId() != null) {
            this.setId(userSettings.getId().getId());
        }
        this.key = userSettings.getKey();
        this.jsonValue = userSettings.getJsonValue();
        this.userId = toString(userSettings.getUserId().getId());
    }

    @Override
    public UserSettings toData() {
        UserSettings userSettings = new UserSettings(new UserSettingsId(UUIDConverter.fromString(id)));
        userSettings.setCreatedTime(UUIDs.unixTimestamp(UUIDConverter.fromString(id)));
        userSettings.setKey(key);
        userSettings.setJsonValue(jsonValue);
        userSettings.setUserId(new UserId(toUUID(userId)));
        return userSettings;
    }

}