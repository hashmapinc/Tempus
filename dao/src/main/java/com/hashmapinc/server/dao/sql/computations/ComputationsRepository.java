package com.hashmapinc.server.dao.sql.computations;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import com.hashmapinc.server.dao.model.sql.ComputationsEntity;
import com.hashmapinc.server.dao.util.SqlDao;

import java.util.List;

@SqlDao
public interface ComputationsRepository extends CrudRepository<ComputationsEntity, String> {
    List <ComputationsEntity> findByName(String name);

    ComputationsEntity findByTenantIdAndName(String tenantId, String name);

    @Query("SELECT ce FROM ComputationsEntity ce WHERE ce.tenantId = :tenantId " +
            "AND LOWER(ce.searchText) LIKE LOWER(CONCAT(:textSearch, '%')) " +
            "AND ce.id > :idOffset ORDER BY ce.id")
    List<ComputationsEntity> findByTenantIdAndPageLink(@Param("tenantId") String tenantId,
                                                       @Param("textSearch") String textSearch,
                                                       @Param("idOffset") String idOffset,
                                                       Pageable pageable);

    List<ComputationsEntity> findByTenantId(@Param("tenantId") String tenantId);
}
