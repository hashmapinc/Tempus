/**
 * Copyright © 2016-2017 Hashmap, Inc
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
package org.thingsboard.server.dao.sql.depthSeries;

import org.springframework.data.repository.CrudRepository;
import org.thingsboard.server.common.data.EntityType;
import org.thingsboard.server.dao.model.sql.DsKvLatestCompositeKey;
import org.thingsboard.server.dao.model.sql.DsKvLatestEntity;
import org.thingsboard.server.dao.util.SqlDao;

import java.util.List;

@SqlDao
public interface DsKvLatestRepository extends CrudRepository<DsKvLatestEntity, DsKvLatestCompositeKey> {

    List<DsKvLatestEntity> findAllByEntityTypeAndEntityId(EntityType entityType, String entityId);
    List<DsKvLatestEntity> findAllByEntityTypeAndEntityIdAndKey(EntityType entityType, String entityId, String key);
}
