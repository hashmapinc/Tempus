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
package com.hashmapinc.server.dao.tenant;

import com.hashmapinc.server.common.data.page.TextPageLink;
import com.hashmapinc.server.common.data.Tenant;
import com.hashmapinc.server.dao.Dao;

import java.util.List;
import java.util.UUID;

public interface TenantDao extends Dao<Tenant> {

    /**
     * Save or update tenant object
     *
     * @param tenant the tenant object
     * @return saved tenant object
     */
    Tenant save(Tenant tenant);
    
    /**
     * Find tenants by region and page link.
     * 
     * @param region the region
     * @param pageLink the page link
     * @return the list of tenant objects
     */
    List<Tenant> findTenantsByRegion(String region, TextPageLink pageLink);

    void saveUnitSystem(String unitSystem, UUID tenantId);

    String findUnitSystemByTenantId(UUID tenantId);

    void deleteUnitSystemByTenantId(UUID tenantId);

    void updateUnitSystem(String unitSystem, UUID tenantId);

}
