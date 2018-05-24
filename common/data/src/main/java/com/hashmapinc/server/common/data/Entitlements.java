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

import com.hashmapinc.server.common.data.id.EntitlementsId;
import com.hashmapinc.server.common.data.id.UserId;
import com.hashmapinc.server.common.data.security.EntitledServices;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
public class Entitlements extends BaseData<EntitlementsId>{

    private static final long serialVersionUID = 8659164019026526323L;

    private UserId userId;
    private Set<EntitledServices> entitledServices;

    public Entitlements(){
        super();
    }

    public Entitlements(EntitlementsId id) {
        super(id);
    }

    public Entitlements(Entitlements entitlements){
        super(entitlements);
        this.userId = entitlements.getUserId();
        this.entitledServices = entitlements.getEntitledServices();
    }


    @Override
    public String toString() {
        return "Entitlements{" +
                "userId=" + userId +
                ", entitledServices=" + entitledServices +
                '}';
    }
}
