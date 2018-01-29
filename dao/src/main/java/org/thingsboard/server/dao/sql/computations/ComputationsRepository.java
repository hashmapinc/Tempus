package org.thingsboard.server.dao.sql.computations;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import org.thingsboard.server.dao.model.sql.ComputationsEntity;
import org.thingsboard.server.dao.util.SqlDao;

@SqlDao
public interface ComputationsRepository extends CrudRepository<ComputationsEntity, String> {
    ComputationsEntity findByName(String name);
    @Modifying
    @Transactional
    @Query("delete from ComputationsEntity c where c.name = ?1")
    void deleteByName(String name);
}
