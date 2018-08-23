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
package com.hashmapinc.server.service.security.auth.jwt.extractor;

import com.hashmapinc.server.common.data.id.CustomerId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.id.UserId;
import com.hashmapinc.server.common.data.security.Authority;
import com.hashmapinc.server.common.msg.exception.TempusRuntimeException;
import com.hashmapinc.server.service.security.auth.JwtAuthenticationToken;
import com.hashmapinc.server.service.security.model.SecurityUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.provider.token.UserAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class UserInfoTokenConverter implements UserAuthenticationConverter {

    @Override
    public Map<String, ?> convertUserAuthentication(Authentication userAuthentication) {
        Map<String, Object> response = new LinkedHashMap();
        SecurityUser user = (SecurityUser) userAuthentication.getPrincipal();
        response.put("id", user.getId().getId());
        response.put("user_name", user.getEmail());
        response.put("firstName", user.getFirstName());
        response.put("lastName", user.getLastName());
        response.put("tenant_id", user.getTenantId().getId());
        response.put("customer_id", user.getCustomerId().getId());
        response.put("enabled", user.isEnabled());
        if(userAuthentication.getAuthorities() != null && !userAuthentication.getAuthorities().isEmpty()) {
            response.put("authorities", AuthorityUtils.authorityListToSet(userAuthentication.getAuthorities()));
        }
        if(user.getPermissions() != null && !user.getPermissions().isEmpty()){
            response.put("permissions", user.getPermissions());
        }

        return response;
    }

    @Override
    public Authentication extractAuthentication(Map<String, ?> map) {
        Object userName = map.get("user_name");
        Object id = map.get("id");
        Object tenantId = map.get("tenant_id");
        Object enabled = map.get("enabled");
        Object authorities = map.get("authorities");

        if (StringUtils.isEmpty(userName) && StringUtils.isEmpty(id) && StringUtils.isEmpty(tenantId) && authorities == null) {
            return null;
        }

        if(StringUtils.isEmpty(userName) || StringUtils.isEmpty(id) || StringUtils.isEmpty(tenantId)){
            throw new TempusRuntimeException("Invalid details");
        }

        SecurityUser securityUser = new SecurityUser();
        securityUser.setEmail((String)userName);
        securityUser.setId(new UserId(UUID.fromString((String) id)));
        securityUser.setTenantId(new TenantId(UUID.fromString((String)tenantId)));
        Object customerId = map.get("customer_id");
        if(!StringUtils.isEmpty(customerId)){
            securityUser.setCustomerId(new CustomerId(UUID.fromString((String)customerId)));
        }
        securityUser.setEnabled(enabled != null && (Boolean)enabled);
        securityUser.setFirstName(extractNullable("firstName", map));
        securityUser.setLastName(extractNullable("lastName", map));

        if(authorities != null){
            List<String> authority = (List<String>) authorities;
            if(!authority.isEmpty())
                securityUser.setAuthority(Authority.parse(authority.get(0)));
        }
        Object permissions = map.get("permissions");
        if(permissions != null){
            List<String> permissionList = ((List<String>)permissions);
            if(!permissionList.isEmpty())
                securityUser.setPermissions(permissionList);
        }
        return new JwtAuthenticationToken(securityUser);
    }

    private String extractNullable(String key, Map<String, ?> map){
        Object value = map.get(key);
        return StringUtils.isEmpty(value) ? null : (String)value;
    }
}
