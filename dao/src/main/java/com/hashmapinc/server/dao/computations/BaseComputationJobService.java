/**
 * Copyright © 2017-2018 Hashmap, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hashmapinc.server.dao.computations;

import com.hashmapinc.server.common.data.computation.ComputationJob;
import com.hashmapinc.server.common.data.id.ComputationId;
import com.hashmapinc.server.common.data.id.ComputationJobId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.page.TextPageData;
import com.hashmapinc.server.common.data.page.TextPageLink;
import com.hashmapinc.server.common.data.plugin.ComponentLifecycleState;
import com.hashmapinc.server.dao.entity.AbstractEntityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.hashmapinc.server.dao.exception.DatabaseException;
import com.hashmapinc.server.dao.exception.IncorrectParameterException;
import com.hashmapinc.server.dao.service.Validator;

import java.util.List;

import static com.hashmapinc.server.dao.service.Validator.validateId;
import static com.hashmapinc.server.dao.service.Validator.validatePageLink;

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

    @Override
    public List<ComputationJob> findByComputationId(ComputationId computationId) {
        return computationJobDao.findByComputationId(computationId);
    }

    @Override
    public void activateComputationJobById(ComputationJobId computationJobId) {
        Validator.validateId(computationJobId, "Incorrect computation Job id for state change request.");
        ComputationJob computationJob = computationJobDao.findById(computationJobId);
        if (computationJob != null) {
            computationJob.setState(ComponentLifecycleState.ACTIVE);
            computationJobDao.save(computationJob);
        } else {
            throw new DatabaseException("ComputaionJob not found!");
        }
    }

    @Override
    public void suspendComputationJobById(ComputationJobId computationJobId) {
        Validator.validateId(computationJobId, "Incorrect computation Job id for state change request.");
        ComputationJob computationJob = computationJobDao.findById(computationJobId);
        if (computationJob != null) {
            computationJob.setState(ComponentLifecycleState.SUSPENDED);
            computationJob.setJobId(null);
            computationJobDao.save(computationJob);
        } else {
            throw new DatabaseException("ComputaionJob not found!");
        }
    }
}
