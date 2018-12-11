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

import com.hashmapinc.server.common.data.id.CustomerId;
import com.hashmapinc.server.common.data.id.DataModelObjectId;
import com.hashmapinc.server.common.data.id.EntityId;
import com.hashmapinc.server.common.data.id.TenantId;
import lombok.Data;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Data
public class TempusResourceCriteriaSpec {
    private EntityType entityType;
    private TenantId tenantId;
    private Optional<DataModelObjectId> dataModelObjectId = Optional.empty();
    private Optional<CustomerId> customerId = Optional.empty();
    private Optional<String> type = Optional.empty();
    private Optional<String> searchText = Optional.empty();
    private Set<? extends EntityId> accessibleIdsForGivenDataModelObject = new HashSet<>();
    private Set<? extends EntityId> dataModelObjectIds = new HashSet<>();
    public TempusResourceCriteriaSpec(EntityType entityType, TenantId tenantId) {
        this.entityType = entityType;
        this.tenantId = tenantId;
    }
}
