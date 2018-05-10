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

import com.datastax.driver.core.utils.UUIDs;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.fasterxml.jackson.databind.JsonNode;
import com.hashmapinc.server.common.data.id.UserId;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import com.hashmapinc.server.common.data.UserSettings;
import com.hashmapinc.server.common.data.id.UserSettingsId;
import com.hashmapinc.server.dao.model.BaseEntity;
import com.hashmapinc.server.dao.model.type.JsonCodec;

import java.util.UUID;

import static com.hashmapinc.server.dao.model.ModelConstants.*;

@Table(name = USER_SETTINGS_COLUMN_FAMILY_NAME)
@EqualsAndHashCode
@ToString
public final class UserSettingsEntity implements BaseEntity<UserSettings> {

    @PartitionKey(value = 0)
    @Column(name = ID_PROPERTY)
    private UUID id;
    
    @Column(name = USER_SETTINGS_KEY_PROPERTY)
    private String key;

    @Column(name = USER_SETTINGS_JSON_VALUE_PROPERTY, codec = JsonCodec.class)
    private JsonNode jsonValue;

    @Column(name = USER_SETTINGS_USER_ID_PROPERTY)
    private UUID userId;

    public UserSettingsEntity() {
        super();
    }

    public UserSettingsEntity(UserSettings userSettings) {
        if (userSettings.getId() != null) {
            this.id = userSettings.getId().getId();
        }
        this.key = userSettings.getKey();
        this.jsonValue = userSettings.getJsonValue();
        this.userId = userSettings.getUserId().getId();
    }
    
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public JsonNode getJsonValue() {
        return jsonValue;
    }

    public void setJsonValue(JsonNode jsonValue) {
        this.jsonValue = jsonValue;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    @Override
    public UserSettings toData() {
        UserSettings userSettings = new UserSettings(new UserSettingsId(id));
        userSettings.setCreatedTime(UUIDs.unixTimestamp(id));
        userSettings.setKey(key);
        userSettings.setJsonValue(jsonValue);
        if (userId != null) {
            userSettings.setUserId(new UserId(userId));
        }
        return userSettings;
    }

}