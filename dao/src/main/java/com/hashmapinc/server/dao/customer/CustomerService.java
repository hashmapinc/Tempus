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
package com.hashmapinc.server.dao.customer;

import com.google.common.util.concurrent.ListenableFuture;
import com.hashmapinc.server.common.data.Customer;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.page.TextPageData;
import com.hashmapinc.server.common.data.page.TextPageLink;
import com.hashmapinc.server.common.data.id.CustomerId;

import java.util.Optional;

public interface CustomerService {

    Customer findCustomerById(CustomerId customerId);

    Optional<Customer> findCustomerByTenantIdAndTitle(TenantId tenantId, String title);

    ListenableFuture<Customer> findCustomerByIdAsync(CustomerId customerId);

    Customer saveCustomer(Customer customer);

    void deleteCustomer(CustomerId customerId);

    Customer findOrCreatePublicCustomer(TenantId tenantId);

    TextPageData<Customer> findCustomersByTenantId(TenantId tenantId, TextPageLink pageLink);

    void deleteCustomersByTenantId(TenantId tenantId);

}
