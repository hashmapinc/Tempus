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

import com.hashmapinc.server.common.data.id.CustomerGroupId;
import com.hashmapinc.server.common.data.id.CustomerId;
import com.hashmapinc.server.common.data.id.TenantId;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString
public class CustomerGroup extends SearchTextBasedWithAdditionalInfo<CustomerGroupId> implements HasName {
    private static final long serialVersionUID = -5520737431477399572L;
    private String title;
    private TenantId tenantId;
    private CustomerId customerId;
    private List<String> policies;

    public CustomerGroup(CustomerGroupId id) {
        super(id);
    }

    public  CustomerGroup(CustomerGroup customerGroup){
        super(customerGroup);
        this.title = customerGroup.title;
        this.tenantId = customerGroup.tenantId;
        this.customerId = customerGroup.customerId;
        this.policies = customerGroup.policies;
    }

    @Override
    public String getName() {
        return getTitle();
    }

    @Override
    public String getSearchText() {
        return getTitle();
    }
}
