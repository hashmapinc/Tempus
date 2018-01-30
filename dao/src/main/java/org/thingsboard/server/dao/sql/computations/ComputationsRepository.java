package org.thingsboard.server.dao.sql.computations;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.thingsboard.server.dao.model.sql.ComputationsEntity;
import org.thingsboard.server.dao.util.SqlDao;

import java.util.List;

@SqlDao
public interface ComputationsRepository extends CrudRepository<ComputationsEntity, String> {
    ComputationsEntity findByName(String name);
    @Modifying
    @Transactional
    @Query("delete from ComputationsEntity c where c.name = ?1")
    void deleteByName(String name);

    @Query("SELECT ce FROM ComputationsEntity ce WHERE ce.tenantId = :tenantId " +
            "AND LOWER(ce.searchText) LIKE LOWER(CONCAT(:textSearch, '%')) " +
            "AND ce.id > :idOffset ORDER BY ce.id")
    List<ComputationsEntity> findByTenantIdAndPageLink(@Param("tenantId") String tenantId,
                                                       @Param("textSearch") String textSearch,
                                                       @Param("idOffset") String idOffset,
                                                       Pageable pageable);
}
