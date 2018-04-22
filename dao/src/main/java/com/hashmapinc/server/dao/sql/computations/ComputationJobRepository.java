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
package com.hashmapinc.server.dao.sql.computations;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import com.hashmapinc.server.dao.model.sql.ComputationJobEntity;
import com.hashmapinc.server.dao.util.SqlDao;

import java.util.List;

@SqlDao
public interface ComputationJobRepository extends CrudRepository<ComputationJobEntity, String> {

    @Query("SELECT cje FROM ComputationJobEntity cje WHERE cje.tenantId = :tenantId " +
            "AND cje.computationId = :computationId " +
            "AND LOWER(cje.searchText) LIKE LOWER(CONCAT(:textSearch, '%')) " +
            "AND cje.id > :idOffset ORDER BY cje.id")
    List<ComputationJobEntity> findByTenantIdAndComputationIdAndPageLink(@Param("tenantId") String tenantId,
                                                                         @Param("computationId") String computationId,
                                                                         @Param("textSearch") String textSearch,
                                                                         @Param("idOffset") String idOffset,
                                                                         Pageable pageable);

    @Query("SELECT cje FROM ComputationJobEntity cje WHERE cje.computationId = :computationId ")
    List<ComputationJobEntity> findByComputationId(@Param("computationId") String computationId);
}
