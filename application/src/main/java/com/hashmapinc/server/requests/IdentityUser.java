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
package com.hashmapinc.server.requests;

import com.hashmapinc.server.common.data.UUIDConverter;
import com.hashmapinc.server.common.data.User;
import com.hashmapinc.server.common.data.id.CustomerGroupId;
import com.hashmapinc.server.common.data.id.CustomerId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.id.UserId;
import com.hashmapinc.server.common.data.security.Authority;
import com.hashmapinc.server.dao.model.ModelConstants;
import lombok.Data;

import java.util.*;

import static com.hashmapinc.server.dao.model.ModelConstants.NULL_UUID;

@Data
public class IdentityUser {
    private UUID id;
    private String userName;
    private UUID tenantId;
    private UUID customerId;
    private String firstName;
    private String lastName;
    private List<String> authorities;
    private Collection<String> permissions;
    private String clientId;
    private Map<String, String> additionalDetails;
    private boolean enabled;

    public IdentityUser(){}

    public IdentityUser(User user){
        this.id = user.getUuidId();
        this.userName = user.getEmail();
        this.authorities = Arrays.asList(user.getAuthority().name());
        this.permissions = user.getPermissions();
        if(user.getTenantId() != null) {
            this.tenantId = user.getTenantId().getId();
        } else {
            this.tenantId = NULL_UUID;
        }

        if(user.getCustomerId() != null) {
            this.customerId = user.getCustomerId().getId();
        } else {
            this.customerId = NULL_UUID;
        }

        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        additionalDetails.put(ModelConstants.USER_CUSTOMER_GROUP_ID_PROPERTY, user.getCustomerGroupId().getId().toString());
    }

    public User toUser(){
        User user = new User();
        user.setId(new UserId(id));
        user.setEmail(userName);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        if(tenantId != null) {
            user.setTenantId(new TenantId(tenantId));
        }
        if(customerId != null) {
            user.setCustomerId(new CustomerId(customerId));
        }
        user.setAuthority(Authority.parse(authorities.get(0)));
        user.setPermissions(permissions);
        String customerGroupId = additionalDetails.get(ModelConstants.USER_CUSTOMER_GROUP_ID_PROPERTY);
        user.setCustomerGroupId(CustomerGroupId.fromString(customerGroupId));
        return user;
    }
}
