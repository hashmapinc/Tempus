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
package com.hashmapinc.server.dao.model.sql;

import com.datastax.driver.core.utils.UUIDs;
import com.hashmapinc.server.common.data.Entitlements;
import com.hashmapinc.server.common.data.id.EntitlementsId;
import com.hashmapinc.server.common.data.id.UserId;
import com.hashmapinc.server.common.data.security.EntitledServices;
import com.hashmapinc.server.dao.model.BaseSqlEntity;
import com.hashmapinc.server.dao.model.ModelConstants;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import javax.persistence.*;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = ModelConstants.ENTITLEMENTS_COLUMN_FAMILY_NAME)
public class EntitlementsEntity extends BaseSqlEntity<Entitlements> {

    @Column(name = ModelConstants.USER_ID_PROPERTY)
    private String userId;

    @ElementCollection(targetClass=EntitledServices.class, fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = ModelConstants.USER_ENTITLEMENTS_JOIN_TABLE, joinColumns = @JoinColumn(name = ModelConstants.ENTITLEMENTS_ENTITY_ID_COLUMN))
    @Column(name = ModelConstants.USER_ENTITLEMENT_PROPERTY)
    private Set<EntitledServices> entitledServices;


    public EntitlementsEntity() {
        super();
    }

    public EntitlementsEntity(Entitlements entitlements) {
        if(entitlements.getId() != null) {
            this.setId(entitlements.getId().getId());
        }

        if(entitlements.getUserId() !=null) {
            this.userId = toString(entitlements.getUserId().getId());
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
            entitlements.setUserId(new UserId(toUUID(userId)));
        }
        if(entitledServices != null) {
            entitlements.setEntitledServices(entitledServices);
        }
        return entitlements;
    }
}
