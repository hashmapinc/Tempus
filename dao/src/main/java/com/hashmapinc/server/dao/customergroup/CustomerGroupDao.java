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

import com.hashmapinc.server.common.data.CustomerGroup;
import com.hashmapinc.server.common.data.page.TextPageLink;
import com.hashmapinc.server.dao.Dao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * The Interface CustomerGroupDao.
 */
public interface CustomerGroupDao extends Dao<CustomerGroup> {
    /**
     * Save or update customerGroup object
     *
     * @param customerGroup the customerGroup object
     * @return saved customerGroup object
     */
    CustomerGroup save(CustomerGroup customerGroup);

    /**
     * Find customerGroups by tenant id and page link.
     *
     * @param tenantId the tenant id
     * @param pageLink the page link
     * @return the list of customerGroup objects
     */
    List<CustomerGroup> findCustomerGroupsByTenantId(UUID tenantId, TextPageLink pageLink);

    /**
     * Find customerGroups by tenantId, customerId and page link.
     *
     * @param tenantId the tenantId
     * @param customerId the customerId
     * @param pageLink the page link
     * @return the list of customerGroup objects
     */
    List<CustomerGroup> findCustomerGroupsByTenantIdAndCustomerId(UUID tenantId, UUID customerId, TextPageLink pageLink);

    /**
     * Find customerGroups by tenantId and customerGroup title.
     *
     * @param tenantId the tenantId
     * @param title the customerGroup title
     * @return the optional customerGroup object
     */
    Optional<CustomerGroup> findCustomerGroupsByTenantIdAndTitle(UUID tenantId, String title);
}
