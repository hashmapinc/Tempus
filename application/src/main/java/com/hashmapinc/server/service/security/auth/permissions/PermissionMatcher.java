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

public interface PermissionMatcher {
    boolean hasAccessToResource(TempusResource resource, String resourceType, UserPermission permission, User user);
    boolean hasPermissionToAct(TempusResource resource, String action, UserPermission permission);
}
