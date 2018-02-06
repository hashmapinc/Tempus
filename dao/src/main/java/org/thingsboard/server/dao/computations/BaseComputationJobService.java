package org.thingsboard.server.dao.computations;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thingsboard.server.common.data.computation.ComputationJob;
import org.thingsboard.server.common.data.id.ComputationId;
import org.thingsboard.server.common.data.id.ComputationJobId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.TextPageData;
import org.thingsboard.server.common.data.page.TextPageLink;
import org.thingsboard.server.common.data.plugin.ComponentLifecycleState;
import org.thingsboard.server.dao.entity.AbstractEntityService;
import org.thingsboard.server.dao.exception.IncorrectParameterException;

import java.util.List;

import static org.thingsboard.server.dao.service.Validator.validateId;
import static org.thingsboard.server.dao.service.Validator.validatePageLink;

@Slf4j
@Service
public class BaseComputationJobService extends AbstractEntityService implements ComputationJobService {

    @Autowired
    ComputationJobDao computationJobDao;

    @Override
    public ComputationJob saveComputationJob(ComputationJob computationJob) {
        if (computationJob.getId() != null) {
            ComputationJob oldVersion = computationJobDao.findById(computationJob.getId());
            if (computationJob.getState() == null) {
                computationJob.setState(oldVersion.getState());
            } else if (computationJob.getState() != oldVersion.getState()) {
                throw new IncorrectParameterException("Use Activate/Suspend method to control state of the Computation Job!");
            }
        } else {
            if (computationJob.getState() == null) {
                computationJob.setState(ComponentLifecycleState.SUSPENDED);
            } else if (computationJob.getState() != ComponentLifecycleState.SUSPENDED) {
                throw new IncorrectParameterException("Use Activate/Suspend method to control state of the Computation Job!");
            }
        }
        return computationJobDao.save(computationJob);
    }

    @Override
    public ComputationJob findComputationJobById(ComputationJobId computationJobId) {
        return computationJobDao.findById(computationJobId.getId());
    }

    @Override
    public void deleteComputationJobById(ComputationJobId computationJobId) {
        computationJobDao.deleteByComputaionJobId(computationJobId);
    }

    @Override
    public TextPageData<ComputationJob> findTenantComputationJobs(TenantId tenantId, ComputationId computationId, TextPageLink pageLink) {
        validateId(tenantId, "Incorrect tenant id for search computation request.");
        validatePageLink(pageLink, "Incorrect PageLink object for search computation request.");
        List<ComputationJob> computationJobs = computationJobDao.findByTenantIdAndComputationIdAndPageLink(tenantId, computationId, pageLink);
        return new TextPageData<>(computationJobs, pageLink);
    }
}
