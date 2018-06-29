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
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.stream.Collectors;

@Data
@EqualsAndHashCode
@ToString
public abstract class AbstractPermissionMatcher implements PermissionMatcher {

    @Override
    public boolean hasAccessToResource(TempusResource resource, UserPermission permission, User user) {
        return permission.getResources().stream().map(r -> r.name()).collect(Collectors.toList()).contains(resource.getId().getEntityType().name());
    }

    @Override
    public boolean hasPermissionToAct(String action, UserPermission permission) {
        return permission.getUserActions().stream().map(a -> a.name()).collect(Collectors.toList()).contains(action);
    }
}
