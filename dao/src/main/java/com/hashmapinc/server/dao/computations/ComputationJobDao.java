package com.hashmapinc.server.dao.computations;

import com.hashmapinc.server.common.data.computation.ComputationJob;
import com.hashmapinc.server.common.data.id.ComputationId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.page.TextPageLink;
import com.hashmapinc.server.common.data.id.ComputationJobId;
import com.hashmapinc.server.dao.Dao;

import java.util.List;

public interface ComputationJobDao extends Dao<ComputationJob> {

    ComputationJob save(ComputationJob computationJob);

    ComputationJob findById(ComputationJobId computationJobId);

    void deleteByComputaionJobId(ComputationJobId computationJobId);

    List<ComputationJob> findByComputationId(ComputationId computationId);

    List<ComputationJob> findByTenantIdAndComputationIdAndPageLink(TenantId tenantId, ComputationId computationId, TextPageLink pageLink);
}
