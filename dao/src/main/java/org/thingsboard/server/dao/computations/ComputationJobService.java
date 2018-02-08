package org.thingsboard.server.dao.computations;

import org.thingsboard.server.common.data.computation.ComputationJob;
import org.thingsboard.server.common.data.id.ComputationId;
import org.thingsboard.server.common.data.id.ComputationJobId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.TextPageData;
import org.thingsboard.server.common.data.page.TextPageLink;
import org.thingsboard.server.common.data.plugin.PluginMetaData;

import java.util.List;

public interface ComputationJobService {
    ComputationJob saveComputationJob(ComputationJob computationJob);
    ComputationJob findComputationJobById(ComputationJobId computationJobId);
    void deleteComputationJobById(ComputationJobId computationJobId);
    List<ComputationJob> findByComputationId(ComputationId computationId);
    TextPageData<ComputationJob> findTenantComputationJobs(TenantId tenantId, ComputationId computationId, TextPageLink link);
}
