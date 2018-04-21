package com.hashmapinc.server.dao.computations;

import com.hashmapinc.server.common.data.computation.ComputationJob;
import com.hashmapinc.server.common.data.id.ComputationId;
import com.hashmapinc.server.common.data.id.ComputationJobId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.page.TextPageData;
import com.hashmapinc.server.common.data.page.TextPageLink;

import java.util.List;

public interface ComputationJobService {
    ComputationJob saveComputationJob(ComputationJob computationJob);
    ComputationJob findComputationJobById(ComputationJobId computationJobId);
    void deleteComputationJobById(ComputationJobId computationJobId);
    void activateComputationJobById(ComputationJobId computationJobId);
    void suspendComputationJobById(ComputationJobId computationJobId);
    List<ComputationJob> findByComputationId(ComputationId computationId);
    TextPageData<ComputationJob> findTenantComputationJobs(TenantId tenantId, ComputationId computationId, TextPageLink link);
}
