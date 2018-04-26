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

import com.hashmapinc.server.common.data.id.UserSettingsId;

import com.fasterxml.jackson.databind.JsonNode;
import com.hashmapinc.server.common.data.id.UserId;

import java.util.Objects;

public class UserSettings extends BaseData<UserSettingsId> {

    private static final long serialVersionUID = -7670322981725511892L;
    
    private String key;
    private transient JsonNode jsonValue;
    private UserId userId;
    
    public UserSettings() {
        super();
    }

    public UserSettings(UserSettingsId id) {
        super(id);
    }
    
    public UserSettings(UserSettings userSettings) {
        super(userSettings);
        this.key = userSettings.getKey();
        this.jsonValue = userSettings.getJsonValue();
        this.userId = userSettings.getUserId();
    }

    public UserId getUserId() {
        return userId;
    }

    public void setUserId(UserId userId) {
        this.userId = userId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        UserSettings that = (UserSettings) o;
        return Objects.equals(key, that.key) &&
                Objects.equals(jsonValue, that.jsonValue) &&
                Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), key, jsonValue, userId);
    }

    @Override
    public String toString() {
        return "UserSettings{" +
                "key='" + key + '\'' +
                ", jsonValue=" + jsonValue +
                ", userId=" + userId +
                ", createdTime=" + createdTime +
                ", id=" + id +
                '}';
    }
}
