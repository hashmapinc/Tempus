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
package com.hashmapinc.server.dao.sql.settings;

import com.hashmapinc.server.common.data.UUIDConverter;
import com.hashmapinc.server.common.data.UserSettings;
import com.hashmapinc.server.common.data.id.UserId;
import com.hashmapinc.server.dao.DaoUtil;
import com.hashmapinc.server.dao.model.sql.UserSettingsEntity;
import com.hashmapinc.server.dao.settings.UserSettingsDao;
import com.hashmapinc.server.dao.sql.JpaAbstractDao;
import com.hashmapinc.server.dao.util.SqlDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
@SqlDao
public class JpaUserSettingsDao extends JpaAbstractDao<UserSettingsEntity, UserSettings> implements UserSettingsDao {

    @Autowired
    private UserSettingsRepository userSettingsRepository;

    @Override
    protected Class<UserSettingsEntity> getEntityClass() {
        return UserSettingsEntity.class;
    }

    @Override
    protected CrudRepository<UserSettingsEntity, String> getCrudRepository() {
        return userSettingsRepository;
    }

    @Override
    public UserSettings findByKeyAndUserId(String key, UUID userId) {
        return DaoUtil.getData(userSettingsRepository.findByKeyAndUserId(key, UUIDConverter.fromTimeUUID(userId)));
    }
}
