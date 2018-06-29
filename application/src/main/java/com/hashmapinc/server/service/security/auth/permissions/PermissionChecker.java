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
package com.hashmapinc.server.service.security.auth.permissions;

import com.hashmapinc.server.common.data.TempusResource;
import com.hashmapinc.server.common.data.User;
import com.hashmapinc.server.common.data.UserPermission;
import com.hashmapinc.server.common.data.security.Authority;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PermissionChecker {

    @Autowired
    private SystemAdminPermissionMatcher systemAdminPermissionMatcher;

    @Autowired
    private TenantAdminPermissionMatcher tenantAdminPermissionMatcher;

    @Autowired
    private CustomerUserPermissionMatcher customerUserPermissionMatcher;

    private PermissionMatcher getPermissionMatcher(Authority subject) {
        switch (subject) {
            case SYS_ADMIN:
                return systemAdminPermissionMatcher;
            case TENANT_ADMIN:
                return tenantAdminPermissionMatcher;
            case CUSTOMER_USER:
                return customerUserPermissionMatcher;
            default:
                return null;
        }
    }

    public boolean check(User user, TempusResource resource, String action) {

        for (UserPermission permission : user.getPermissions()) {
            PermissionMatcher matcher = getPermissionMatcher(permission.getSubject());
            boolean hasAccessToResource = matcher.hasAccessToResource(resource, permission, user);
            boolean hasPermissionToAct = matcher.hasPermissionToAct(action, permission);
            if (matcher != null && hasAccessToResource && hasPermissionToAct)
                return true;
        }
        return false;
    }
}
