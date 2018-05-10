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
package com.hashmapinc.server.dao.settings;

import com.datastax.driver.core.querybuilder.Select.Where;
import com.hashmapinc.server.common.data.UserSettings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.hashmapinc.server.dao.DaoUtil;
import com.hashmapinc.server.dao.model.nosql.UserSettingsEntity;
import com.hashmapinc.server.dao.nosql.CassandraAbstractModelDao;
import com.hashmapinc.server.dao.util.NoSqlDao;

import java.util.UUID;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
import static com.hashmapinc.server.dao.model.ModelConstants.*;

@Component
@Slf4j
@NoSqlDao
public class CassandraUserSettingsDao extends CassandraAbstractModelDao<UserSettingsEntity, UserSettings> implements UserSettingsDao {

    @Override
    protected Class<UserSettingsEntity> getColumnFamilyClass() {
        return UserSettingsEntity.class;
    }

    @Override
    protected String getColumnFamilyName() {
        return USER_SETTINGS_COLUMN_FAMILY_NAME;
    }

    @Override
    public UserSettings findByKeyAndUserId(String key, UUID userId) {
        log.debug("Try to find user settings by key [{}], userId [{}] ", key, userId);
        Where query = select().from(USER_SETTINGS_BY_KEY_COLUMN_FAMILY_NAME)
                .where(eq(USER_SETTINGS_KEY_PROPERTY, key))
                .and(eq(USER_SETTINGS_USER_ID_PROPERTY, userId));
        log.trace("Execute query {}", query);
        UserSettingsEntity userSettingsEntity = findOneByStatement(query);
        log.trace("Found user settings [{}] by key [{}] and userId [{}]", userSettingsEntity, key, userId);
        return DaoUtil.getData(userSettingsEntity);
    }

}
