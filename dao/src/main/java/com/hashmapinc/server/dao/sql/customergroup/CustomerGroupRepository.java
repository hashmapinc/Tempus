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
package com.hashmapinc.server.dao.sql.customergroup;

import org.springframework.data.domain.Pageable;
import com.hashmapinc.server.dao.model.nosql.CustomerGroupEntity;
import com.hashmapinc.server.dao.util.SqlDao;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

@SqlDao
public interface CustomerGroupRepository  extends CrudRepository<CustomerGroupEntity, String> {
    @Query("SELECT c FROM CustomerGroupEntity c WHERE c.tenantId = :tenantId " +
            "AND c.customerId = :customerId " +
            "AND LOWER(c.searchText) LIKE LOWER(CONCAT(:textSearch, '%')) " +
            "AND c.id > :idOffset ORDER BY c.id")
    List<CustomerGroupEntity> findByTenantIdAndCustomerId(@Param("tenantId") String tenantId,
                                                          @Param("customerId") String customerId,
                                                          @Param("textSearch") String textSearch,
                                                          @Param("idOffset") String idOffset,
                                                          Pageable pageable);

    CustomerGroupEntity findByTenantIdAndCustomerIdAndTitle(String tenantId, String customerId, String title);
}
