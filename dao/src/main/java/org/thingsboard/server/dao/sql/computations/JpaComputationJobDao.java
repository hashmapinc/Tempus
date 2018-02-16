package org.thingsboard.server.dao.sql.computations;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.thingsboard.server.common.data.UUIDConverter;
import org.thingsboard.server.common.data.computation.ComputationJob;
import org.thingsboard.server.common.data.id.ComputationId;
import org.thingsboard.server.common.data.id.ComputationJobId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.TextPageLink;
import org.thingsboard.server.dao.DaoUtil;
import org.thingsboard.server.dao.computations.ComputationJobDao;
import org.thingsboard.server.dao.model.sql.ComputationJobEntity;
import org.thingsboard.server.dao.sql.JpaAbstractSearchTextDao;
import org.thingsboard.server.dao.util.SqlDao;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.thingsboard.server.dao.model.ModelConstants.NULL_UUID_STR;

@Slf4j
@Component
@SqlDao
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

    @Override
    public List<ComputationJob> findByTenantIdAndComputationIdAndPageLink(TenantId tenantId, ComputationId computationId, TextPageLink pageLink) {
        log.debug("Try to find rules by tenantId [{}] and pageLink [{}]", tenantId, pageLink);
        List<ComputationJobEntity> entities =
                computationJobRepository
                        .findByTenantIdAndComputationIdAndPageLink(
                                UUIDConverter.fromTimeUUID(tenantId.getId()),
                                UUIDConverter.fromTimeUUID(computationId.getId()),
                                Objects.toString(pageLink.getTextSearch(), ""),
                                pageLink.getIdOffset() == null ? NULL_UUID_STR :  UUIDConverter.fromTimeUUID(pageLink.getIdOffset()),
                                new PageRequest(0, pageLink.getLimit()));
        if (log.isTraceEnabled()) {
            log.trace("Search result: [{}]", Arrays.toString(entities.toArray()));
        } else {
            log.debug("Search result: [{}]", entities.size());
        }
        return DaoUtil.convertDataList(entities);
    }

    @Override
    public List<ComputationJob> findByComputationId(ComputationId computationId) {
        return DaoUtil.convertDataList(computationJobRepository.findByComputationId(UUIDConverter.fromTimeUUID(computationId.getId())));
    }
}
