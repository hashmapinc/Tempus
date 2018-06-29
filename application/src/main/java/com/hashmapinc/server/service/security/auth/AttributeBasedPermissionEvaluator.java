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

import com.hashmapinc.server.common.data.EntityType;
import com.hashmapinc.server.common.data.EnumUtil;
import com.hashmapinc.server.common.data.TempusResource;
import com.hashmapinc.server.common.data.UUIDConverter;
import com.hashmapinc.server.common.data.id.AssetId;
import com.hashmapinc.server.common.data.id.DeviceId;
import com.hashmapinc.server.dao.asset.AssetService;
import com.hashmapinc.server.dao.device.DeviceService;
import com.hashmapinc.server.service.security.auth.permissions.PermissionChecker;
import com.hashmapinc.server.service.security.model.SecurityUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.Serializable;

@Slf4j
@Component
public class AttributeBasedPermissionEvaluator implements PermissionEvaluator{

    @Autowired
    private PermissionChecker permissionChecker;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private AssetService assetService;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object action) {
        log.debug("hasPersmission({}, {}, {})", authentication, targetDomainObject, action);

        if(targetDomainObject == null)
            throw new RuntimeException("Cannot evaluate attribute based permissions for null targetDomainObject");

        if(!(targetDomainObject instanceof TempusResource))
            throw new RuntimeException("Cannot evaluate attribute based permissions for type " + targetDomainObject.getClass().toString());

        if(StringUtils.isEmpty(action))
            throw new RuntimeException("Action cannot be empty");

        String[] actionTokens = ((String) action).split("_");

        if(actionTokens.length != 2)
            throw new RuntimeException("Invalid action value");

        SecurityUser user = (SecurityUser)authentication.getPrincipal();
        TempusResource resource = (TempusResource) targetDomainObject;
        String entityName = actionTokens[0];
        String entityOperation = actionTokens[1];

        if(!resource.getId().getEntityType().name().equalsIgnoreCase(entityName))
            throw new RuntimeException("Incompatible resource and actions");

        return permissionChecker.check(user, resource, entityOperation);
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable entityId, String targetType, Object action) {
        return hasPermission(authentication, fetchTargetDomainObject((String)entityId, targetType), action);
    }

    private Object fetchTargetDomainObject(String entityId, String entityType) {

        switch (new EnumUtil<>(EntityType.class).parse(entityType)) {
            case DEVICE:
                return deviceService.findDeviceById(new DeviceId(UUIDConverter.fromString(entityId)));
            case ASSET:
                return assetService.findAssetById(new AssetId(UUIDConverter.fromString(entityId)));
            default:
                return null;
        }
    }
}
