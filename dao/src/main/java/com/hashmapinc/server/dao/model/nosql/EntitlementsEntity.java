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
package com.hashmapinc.server.dao.model.nosql;

import com.datastax.driver.core.utils.UUIDs;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.hashmapinc.server.common.data.Entitlements;
import com.hashmapinc.server.common.data.id.EntitlementsId;
import com.hashmapinc.server.common.data.id.UserId;
import com.hashmapinc.server.common.data.security.EntitledServices;
import com.hashmapinc.server.dao.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Set;
import java.util.UUID;

import static com.hashmapinc.server.dao.model.ModelConstants.*;

@Table(name = ENTITLEMENTS_COLUMN_FAMILY_NAME)
@EqualsAndHashCode
@ToString
@Data
public class EntitlementsEntity implements BaseEntity<Entitlements> {

    @PartitionKey(value = 0)
    @Column(name = ID_PROPERTY)
    private UUID id;

    @PartitionKey(value = 1)
    @Column(name = USER_ID_PROPERTY)
    private UUID userId;

    @Column(name = USER_ENTITLEMENT_PROPERTY)
    private Set<EntitledServices> entitledServices;

    public EntitlementsEntity() {
        super();
    }

    public EntitlementsEntity(Entitlements entitlements) {
        if(entitlements.getId() != null) {
            this.setId(entitlements.getId().getId());
        }

        if(entitlements.getUserId() !=null) {
            this.userId = entitlements.getUserId().getId();
        }

        if(entitlements.getEntitledServices() != null){
            this.entitledServices = entitlements.getEntitledServices();
        }
    }

    @Override
    public Entitlements toData() {
        Entitlements entitlements = new Entitlements(new EntitlementsId(getId()));
        entitlements.setCreatedTime(UUIDs.unixTimestamp(getId()));
        if (userId != null) {
            entitlements.setUserId(new UserId(userId));
        }
        if(entitledServices != null) {
            entitlements.setEntitledServices(entitledServices);
        }
        return entitlements;
    }

}
