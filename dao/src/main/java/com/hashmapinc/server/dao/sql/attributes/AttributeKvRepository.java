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
package com.hashmapinc.server.dao.sql.attributes;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import com.hashmapinc.server.common.data.EntityType;
import com.hashmapinc.server.dao.model.sql.AttributeKvCompositeKey;
import com.hashmapinc.server.dao.model.sql.AttributeKvEntity;
import com.hashmapinc.server.dao.util.SqlDao;

import java.util.List;

@SqlDao
public interface AttributeKvRepository extends CrudRepository<AttributeKvEntity, AttributeKvCompositeKey> {

    List<AttributeKvEntity> findAllByEntityTypeAndEntityIdAndAttributeType(EntityType entityType,
                                                                           String entityId,
                                                                           String attributeType);

    @Query("SELECT akv.lastUpdateTs, akv.attributeKey, akv.booleanValue, akv.strValue, akv.longValue, akv.doubleValue, akv.jsonValue FROM AttributeKvEntity akv WHERE akv.entityId = :entityId " +
            "AND akv.entityType = :entityType ORDER BY akv.lastUpdateTs DESC")
    List<Object[]> findAllByEntityTypeAndEntityId(@Param("entityId") String entityId, @Param("entityType") EntityType entityType);
}

