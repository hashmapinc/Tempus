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

import com.hashmapinc.server.common.data.id.UserId;
import com.hashmapinc.server.dao.model.sql.UserSettingsEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * Created by Valerii Sosliuk on 5/6/2017.
 */
public interface UserSettingsRepository extends CrudRepository<UserSettingsEntity, String> {

    @Query("SELECT s FROM UserSettingsEntity s WHERE s.key = :key and s.userId = :userId")
    UserSettingsEntity findByKeyAndUserId(@Param("key") String key, @Param("userId") String userId);
}
