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
package com.hashmapinc.server.common.data;

import com.hashmapinc.server.common.data.security.Authority;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Data
@EqualsAndHashCode
@ToString
@Slf4j
public class UserPermission {

    private String permissionExpr;
    private Authority subject;      //NOSONAR
    private Collection<EntityType> resources;   //NOSONAR
    private Collection<UserAction> userActions; //NOSONAR
    private Map<ResourceAttribute, String> resourceAttributes;

    public UserPermission(UserPermission permission){
        this(permission.permissionExpr);
    }

    public UserPermission(String permissionExpression){
        this.permissionExpr = permissionExpression;
        this.parseExpression();
    }

    private void parseExpression() {
        if(this.permissionExpr == null)
            throw new IllegalArgumentException("Invalid user permission expression, it cannot be null !!");

        String[] expressionTokens = this.permissionExpr.split(":");

        if(expressionTokens.length != 3)
            throw new IllegalArgumentException("Invalid user permission expression !!");

        this.subject = new EnumUtil<>(Authority.class).parse(expressionTokens[0]);


        String[] resourcesExpression = expressionTokens[1].split("\\?", 2);

        if(resourcesExpression[0].trim().equals("*"))
            this.resources = Arrays.asList(EntityType.values());
        else {
            this.resources = Collections.singletonList(new EnumUtil<>(EntityType.class).parse(resourcesExpression[0]));
            this.resourceAttributes = (resourcesExpression.length == 2) ? getQueryAttributes(resourcesExpression[1]) : Collections.emptyMap();
        }

        if(expressionTokens[2].trim().equals("*"))
            this.userActions = Arrays.asList(UserAction.values());
        else {
            this.userActions = Collections.singletonList(new EnumUtil<>(UserAction.class).parse(expressionTokens[2]));
        }
    }

    private static Map<ResourceAttribute, String> getQueryAttributes(String query)
    {
        String[] params = query.split("&");
        Map<ResourceAttribute, String> map = new HashMap<>();
        for (String param : params)
        {
            String[] keyAndVal = param.split("=");
            map.put(ResourceAttribute.fromString(keyAndVal[0].trim()), keyAndVal[1].trim());
        }
        return map;
    }

    public enum ResourceAttribute {
        DATA_MODEL_ID("dataModelId"), ID("id");
        private final String value;
        ResourceAttribute(String value){
            this.value = value;
        }
        public String toString() {
            return this.value;
        }

        public static ResourceAttribute fromString(String str) {
            for (ResourceAttribute resourceAttribute : ResourceAttribute.values()) {
                if (resourceAttribute.value.equals(str)) {
                    return resourceAttribute;
                }
            }
            return null;
        }
    }

}