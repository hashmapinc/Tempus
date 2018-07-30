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
package com.hashmapinc.server.common.data.id;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hashmapinc.server.common.data.EntityType;

import java.util.UUID;

public class CustomerGroupId extends UUIDBased implements EntityId {

    private static final long serialVersionUID = 5646703518988164203L;

    @JsonCreator
    public CustomerGroupId(@JsonProperty("id") UUID id) {
        super(id);
    }

    public static CustomerGroupId fromString(String customerGroupId) {
        return new CustomerGroupId(UUID.fromString(customerGroupId));
    }

    @JsonIgnore
    @Override
    public EntityType getEntityType() {
        return EntityType.CUSTOMER_GROUP;
    }
}
