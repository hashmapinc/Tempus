/**
 * Copyright © 2016-2018 Hashmap, Inc
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
package org.thingsboard.server.common.data.relation;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.thingsboard.server.common.data.EntityType;
import org.thingsboard.server.common.data.id.EntityId;
import org.thingsboard.server.common.data.id.EntityIdFactory;

import java.util.UUID;

/**
 * Created by ashvayka on 03.05.17.
 */
@Data
@AllArgsConstructor
public class RelationsSearchParameters {

    private UUID rootId;
    private EntityType rootType;
    private EntitySearchDirection direction;
    private int maxLevel = 1;

    public RelationsSearchParameters(EntityId entityId, EntitySearchDirection direction, int maxLevel) {
        this.rootId = entityId.getId();
        this.rootType = entityId.getEntityType();
        this.direction = direction;
        this.maxLevel = maxLevel;
    }

    public EntityId getEntityId() {
        return EntityIdFactory.getByTypeAndUuid(rootType, rootId);
    }
}
