/**
 * Copyright © 2016-2018 The Thingsboard Authors
 * Modifications © 2017-2018 Hashmap, Inc
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

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class MetadataSource {
    private MetadataSourceType type;
    private String dbUrl;
    private String username;
    private String password;


    public MetadataSource(MetadataSourceType type, String dbUrl, String username, String password) {
        this.type = type;
        this.dbUrl = dbUrl;
        this.username = username;
        this.password = password;
    }

    public MetadataSource() {
    }

    public MetadataSourceType getType() {
        return type;
    }

    public void setType(MetadataSourceType type) {
        this.type = type;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public void setDbUrl(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
