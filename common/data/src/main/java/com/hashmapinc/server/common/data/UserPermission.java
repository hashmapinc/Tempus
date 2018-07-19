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
package com.hashmapinc.server.common.data;

import com.hashmapinc.server.common.data.security.Authority;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Arrays;
import java.util.Collection;

@Data
@EqualsAndHashCode
@ToString
public class UserPermission {

    private String permissionExpr;
    private Authority subject;      //NOSONAR
    private Collection<EntityType> resources;   //NOSONAR
    private Collection<UserAction> userActions; //NOSONAR

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

        if(expressionTokens[1].trim().equals("*"))
            this.resources = Arrays.asList(EntityType.values());
        else {
            this.resources = Arrays.asList(new EnumUtil<>(EntityType.class).parse(expressionTokens[1]));
        }

        if(expressionTokens[2].trim().equals("*"))
            this.userActions = Arrays.asList(UserAction.values());
        else {
            this.userActions = Arrays.asList(new EnumUtil<>(UserAction.class).parse(expressionTokens[2]));
        }
    }

}