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
