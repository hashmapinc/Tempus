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
package com.hashmapinc.server.service.security.auth;

import com.hashmapinc.server.service.security.auth.rules.RulesChecker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Slf4j
@Component
public class AttributeBasedPermissionEvaluator implements PermissionEvaluator{

    @Autowired
    private RulesChecker rulesChecker;

    @Override
    public boolean hasPermission(Authentication authentication , Object targetDomainObject, Object permission) {
        Object user = authentication.getPrincipal();
        log.debug("hasPersmission({}, {}, {})", user, targetDomainObject, permission);
        return rulesChecker.check(user, targetDomainObject, permission);
    }

    //TODO: Implement this for Delete use case where we will fetch an object from targetId and check if user is Authorized
    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        return false;
    }

    public void setRulesChecker(RulesChecker rulesChecker) {
        this.rulesChecker = rulesChecker;
    }
}
