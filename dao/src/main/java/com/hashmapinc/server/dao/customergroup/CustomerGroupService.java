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
package com.hashmapinc.server.dao.customergroup;

import com.google.common.util.concurrent.ListenableFuture;
import com.hashmapinc.server.common.data.CustomerGroup;
import com.hashmapinc.server.common.data.id.CustomerGroupId;
import com.hashmapinc.server.common.data.id.CustomerId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.id.UserId;
import com.hashmapinc.server.common.data.page.TextPageData;
import com.hashmapinc.server.common.data.page.TextPageLink;

import java.util.List;
import java.util.Optional;

public interface CustomerGroupService {

    CustomerGroup findByCustomerGroupId(CustomerGroupId customerGroupId);

    TextPageData<CustomerGroup> findByUserId(UserId userId, TextPageLink pageLink);

    ListenableFuture<CustomerGroup> findCustomerGroupByIdAsync(CustomerGroupId customerGroupId);

    Optional<CustomerGroup> findCustomerByTenantIdAndCustomerIdAndTitle(TenantId tenantId, CustomerId customerId, String title);

    CustomerGroup saveCustomerGroup(CustomerGroup customerGroup);

    void deleteCustomerGroup(CustomerGroupId customerGroupId);

    TextPageData<CustomerGroup> findCustomerGroupsByTenantIdAndCustomerId(TenantId tenantId, CustomerId customerId, TextPageLink pageLink);

    void deleteCustomerGroupsByTenantIdAndCustomerId(TenantId tenantId, CustomerId customerId);

    CustomerGroup assignUsers(CustomerGroupId customerGroupId, List<UserId> userIds);
}
