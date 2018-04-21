package com.hashmapinc.server.dao.sql.computations;

import com.hashmapinc.server.common.data.computation.ComputationJob;
import com.hashmapinc.server.common.data.id.ComputationId;
import com.hashmapinc.server.common.data.id.ComputationJobId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.page.TextPageLink;
import com.hashmapinc.server.dao.DaoUtil;
import com.hashmapinc.server.dao.computations.ComputationJobDao;
import com.hashmapinc.server.dao.util.SqlDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import com.hashmapinc.server.common.data.UUIDConverter;
import com.hashmapinc.server.dao.model.sql.ComputationJobEntity;
import com.hashmapinc.server.dao.sql.JpaAbstractSearchTextDao;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.hashmapinc.server.dao.model.ModelConstants.NULL_UUID_STR;

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
        log.debug("Search computationJob entity by id [{}]", computationJobId);
        ComputationJob computationJob = super.findById(computationJobId.getId());
        if (log.isTraceEnabled()) {
            log.trace("Search result: [{}] for computationJob entity [{}]", computationJob != null, computationJob);
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
        log.debug("Try to find computationJobs by tenantId [{}] and pageLink [{}]", tenantId, pageLink);
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
