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
package com.hashmapinc.server.dao.entitlements;

import com.hashmapinc.server.common.data.Entitlements;
import com.hashmapinc.server.dao.Dao;

import java.util.Optional;
import java.util.UUID;

public interface EntitlementsDao extends Dao<Entitlements> {

    /**
     * Save or update entitlements object
     *
     * @param entitlements the entitlements object
     * @return saved entitlements object
     */
    Entitlements save(Entitlements entitlements);

    /**
     * Find entitlements by tenantId and page link.
     *
     * @param userId the userId
     * @return the Entitlements
     */
    Optional<Entitlements> findEntitlementsByUserId(UUID userId);
}
