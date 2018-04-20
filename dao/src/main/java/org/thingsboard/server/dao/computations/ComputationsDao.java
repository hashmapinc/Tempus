/**
 * Copyright © 2016-2017 Hashmap, Inc
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
package org.thingsboard.server.dao.computations;

import org.thingsboard.server.common.data.computation.Computations;
import org.thingsboard.server.common.data.id.ComputationId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.TextPageLink;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ComputationsDao {
    Computations findByName(String name);

    Optional<Computations> findByTenantIdAndName(TenantId tenantId, String name);

    Computations save(Computations computations);

    void deleteById(ComputationId computationId);

    List<Computations> findByTenantIdAndPageLink(TenantId tenantId, TextPageLink pageLink);

    Computations findById(UUID id);

    List<Computations> findByTenantId(TenantId tenantId);
}
