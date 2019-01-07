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
package com.hashmapinc.server.dao.computations;
import com.hashmapinc.server.common.data.computation.Computations;
import com.hashmapinc.server.common.data.id.ComputationId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.page.TextPageData;
import com.hashmapinc.server.common.data.page.TextPageLink;

import java.util.List;
import java.util.Optional;

public interface ComputationsService {

    Computations findByName(String name);

    Optional<Computations> findByTenantIdAndName(TenantId tenantId, String name);

    Computations findById(ComputationId id);

    Computations save(Computations computations);

    void deleteById(ComputationId computationId);

    TextPageData<Computations> findTenantComputations(TenantId tenantId, TextPageLink pageLink);

    List<Computations> findAllTenantComputationsByTenantId(TenantId tenantId);

}
