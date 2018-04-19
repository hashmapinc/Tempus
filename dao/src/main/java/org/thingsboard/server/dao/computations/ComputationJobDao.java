package org.thingsboard.server.dao.computations;

import org.thingsboard.server.common.data.computation.ComputationJob;
import org.thingsboard.server.common.data.id.ComputationId;
import org.thingsboard.server.common.data.id.ComputationJobId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.TextPageLink;
import org.thingsboard.server.dao.Dao;

import java.util.List;

public interface ComputationJobDao extends Dao<ComputationJob> {

    ComputationJob save(ComputationJob computationJob);

    ComputationJob findById(ComputationJobId computationJobId);

    void deleteByComputaionJobId(ComputationJobId computationJobId);

    List<ComputationJob> findByComputationId(ComputationId computationId);

    List<ComputationJob> findByTenantIdAndComputationIdAndPageLink(TenantId tenantId, ComputationId computationId, TextPageLink pageLink);
}
