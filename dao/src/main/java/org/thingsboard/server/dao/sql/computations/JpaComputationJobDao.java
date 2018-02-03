package org.thingsboard.server.dao.sql.computations;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.thingsboard.server.common.data.UUIDConverter;
import org.thingsboard.server.common.data.computation.ComputationJob;
import org.thingsboard.server.common.data.id.ComputationJobId;
import org.thingsboard.server.dao.computations.ComputationJobDao;
import org.thingsboard.server.dao.model.sql.ComputationJobEntity;
import org.thingsboard.server.dao.sql.JpaAbstractSearchTextDao;

@Slf4j
@Component
public class JpaComputationJobDao extends JpaAbstractSearchTextDao<ComputationJobEntity, ComputationJob> implements ComputationJobDao {

    @Autowired
    ComputationJobRepository computationJobRepository;

    @Override
    protected Class<ComputationJobEntity> getEntityClass() {
        return ComputationJobEntity.class;
    }

    @Override
    protected CrudRepository<ComputationJobEntity, String> getCrudRepository() {
        return computationJobRepository;
    }

    @Override
    public ComputationJob findById(ComputationJobId computationJobId) {
        log.debug("Search plugin meta-data entity by id [{}]", computationJobId);
        ComputationJob computationJob = super.findById(computationJobId.getId());
        if (log.isTraceEnabled()) {
            log.trace("Search result: [{}] for plugin entity [{}]", computationJob != null, computationJob);
        } else {
            log.debug("Search result: [{}]", computationJob != null);
        }
        return computationJob;
    }

    @Override
    public void deleteByComputaionJobId(ComputationJobId computationJobId) {
        computationJobRepository.delete(UUIDConverter. fromTimeUUID(computationJobId.getId()));
    }
}
