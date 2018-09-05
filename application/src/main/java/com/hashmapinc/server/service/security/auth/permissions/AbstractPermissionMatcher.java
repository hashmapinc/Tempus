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
package com.hashmapinc.server.service.security.auth.permissions;

import com.hashmapinc.server.common.data.*;
import com.hashmapinc.server.common.data.asset.Asset;
import com.hashmapinc.server.common.data.id.DataModelObjectId;
import com.hashmapinc.server.dao.datamodel.DataModelObjectService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode
@ToString
@Slf4j
public abstract class AbstractPermissionMatcher implements PermissionMatcher {

    @Autowired
    DataModelObjectService dataModelObjectService;

    @Override
    public boolean hasAccessToResource(TempusResource resource, String resourceType, UserPermission permission, User user) {
        return hasAccessToResourceType(resourceType, permission) && checkAttributeBasedPermission(resource, permission.getResourceAttributes());
    }

    @Override
    public boolean hasPermissionToAct(TempusResource resource, String action, UserPermission permission) {
        if (new EnumUtil<>(UserAction.class).parse(action).equals(UserAction.UPDATE) && resource.getId() == null)
            return false;

        return permission.getUserActions().stream().map(Enum::name).collect(Collectors.toList()).contains(action);
    }

    private boolean hasAccessToResourceType(String resourceType, UserPermission permission) {
        return permission.getResources().stream().map(Enum::name).collect(Collectors.toList()).contains(resourceType);
    }

    private boolean checkAttributeBasedPermission(TempusResource resource, Map<UserPermission.ResourceAttribute, String> resourceAttributes){
        if(resourceAttributes == null || resourceAttributes.isEmpty()){
            return true;
        }
        if(resource instanceof Asset){
            Asset assetResource = (Asset) resource;
            return resourceAttributes.entrySet().stream().allMatch(entry -> {
                switch (entry.getKey()) {
                    case DATA_MODEL_ID:
                        return assetResource.getDataModelObjectId() != null
                                && (entry.getValue().equals(assetResource.getDataModelObjectId().getId().toString())
                                || getParentsOf(assetResource.getDataModelObjectId()).contains(entry.getValue()));
                    case ID:
                        return entry.getValue().equals(resource.getId().getId().toString());
                }
                return true;
            });
        }else {
            return true;
        }
    }

    private Set<String> getParentsOf(DataModelObjectId dataModelObjectId){
        Set<DataModelObjectId> allParentDataModelIds = dataModelObjectService.getAllParentDataModelIdsOf(dataModelObjectId);
        return allParentDataModelIds.stream().map(parentDataModelId -> parentDataModelId.getId().toString()).collect(Collectors.toSet());
    }
}
