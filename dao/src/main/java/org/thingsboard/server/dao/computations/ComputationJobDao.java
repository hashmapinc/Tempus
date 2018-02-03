package org.thingsboard.server.dao.computations;

import org.thingsboard.server.common.data.computation.ComputationJob;
import org.thingsboard.server.common.data.id.ComputationJobId;
import org.thingsboard.server.dao.Dao;

public interface ComputationJobDao extends Dao<ComputationJob> {

    ComputationJob save(ComputationJob computationJob);

    ComputationJob findById(ComputationJobId computationJobId);

    void deleteByComputaionJobId(ComputationJobId computationJobId);
}
