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
import com.hashmapinc.server.common.data.id.CustomerGroupId;
import com.hashmapinc.server.common.data.id.UserId;
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
     * @param customerId the customerId
     * @param title the customerGroup title
     * @return the list of customerGroup objects
     */
    Optional<CustomerGroup> findCustomerGroupsByTenantIdAndCustomerIdAndTitle(UUID tenantId, UUID customerId, String title);

    /**
     * Find User IDs by customer group id.
     *
     * @param customerGroupId the customer group id
     * @return the list of UserId objects
     */
    List<UserId> findUserIdsByCustomerGroupId(UUID customerGroupId);

    /**
     * Delete User IDs by customer group id.
     *
     * @param customerGroupId the customer group id
     *
     */
    void deleteUserIdsForCustomerGroupId(UUID customerGroupId);


    /**
     * Find Customer groups by user id.
     *
     * @param userId the user id
     * @param textPageLink the page link
     * @return the list of customer group objects
     */
    List<CustomerGroup> findByUserId(UUID userId, TextPageLink textPageLink);

    /**
     * Assign Users to groups by id.
     *
     * @param customerGroupId the customerGroupId
     * @param userIds list of userId objects
     */
    void assignUsers(CustomerGroupId customerGroupId, List<UserId> userIds);
}
