package org.thingsboard.server.dao.sql.computations;

import org.springframework.data.repository.CrudRepository;
import org.thingsboard.server.dao.model.sql.ComputationJobEntity;
import org.thingsboard.server.dao.util.SqlDao;

@SqlDao
public interface ComputationJobRepository extends CrudRepository<ComputationJobEntity, String> {

}
