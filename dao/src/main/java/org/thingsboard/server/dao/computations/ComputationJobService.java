package org.thingsboard.server.dao.computations;

import org.thingsboard.server.common.data.computation.ComputationJob;
import org.thingsboard.server.common.data.id.ComputationJobId;
import org.thingsboard.server.common.data.plugin.PluginMetaData;

public interface ComputationJobService {
    ComputationJob saveComputationJob(ComputationJob computationJob);
    ComputationJob findComputationJobById(ComputationJobId computationJobId);
    void deleteComputationJobById(ComputationJobId computationJobId);
}
