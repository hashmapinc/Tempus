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
package com.hashmapinc.server.dao.sql.alarm;

import com.hashmapinc.server.common.data.EntityType;
import com.hashmapinc.server.dao.model.sql.AlarmEntity;
import com.hashmapinc.server.dao.util.SqlDao;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Created by Valerii Sosliuk on 5/21/2017.
 */
@SqlDao
public interface AlarmRepository extends CrudRepository<AlarmEntity, String> {

    @Query("SELECT a FROM AlarmEntity a WHERE a.tenantId = :tenantId AND a.originatorId = :originatorId " +
            "AND a.originatorType = :entityType AND a.type = :alarmType ORDER BY a.type ASC, a.id DESC")
    List<AlarmEntity> findLatestByOriginatorAndType(@Param("tenantId") String tenantId,
                                                    @Param("originatorId") String originatorId,
                                                    @Param("entityType") EntityType entityType,
                                                    @Param("alarmType") String alarmType,
                                                    Pageable pageable);
}
