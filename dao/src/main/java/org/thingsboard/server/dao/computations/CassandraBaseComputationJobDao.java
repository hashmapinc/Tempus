/**
 * Copyright © 2016-2017 The Thingsboard Authors
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
package org.thingsboard.server.dao.computations;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.thingsboard.server.common.data.computation.ComputationJob;
import org.thingsboard.server.common.data.id.ComputationId;
import org.thingsboard.server.common.data.id.ComputationJobId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.TextPageLink;
import org.thingsboard.server.dao.DaoUtil;
import org.thingsboard.server.dao.model.ModelConstants;
import org.thingsboard.server.dao.model.nosql.ComputationJobEntity;
import org.thingsboard.server.dao.nosql.CassandraAbstractSearchTextDao;
import org.thingsboard.server.dao.util.NoSqlDao;
import static com.datastax.driver.core.querybuilder.QueryBuilder.*;
import static org.thingsboard.server.dao.model.ModelConstants.NULL_UUID;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
@NoSqlDao
public class CassandraBaseComputationJobDao extends CassandraAbstractSearchTextDao<ComputationJobEntity, ComputationJob> implements ComputationJobDao {

    @Override
    protected Class<ComputationJobEntity> getColumnFamilyClass() {
        return ComputationJobEntity.class;
    }

    @Override
    protected String getColumnFamilyName() {
        return ModelConstants.COMPUTATION_JOB_COLUMN_FAMILY_NAME;
    }

    public void deleteById(UUID id) {
        log.info("Delete computations entity by id [{}]", id);
        boolean result = removeById(id);
        log.info("Delete result: [{}]", result);
    }

    @Override
    public ComputationJob findById(ComputationJobId computationJobId) {
        return super.findById(computationJobId.getId());
    }

    @Override
    public void deleteByComputaionJobId(ComputationJobId computationJobId) {
        deleteById(computationJobId.getId());
    }

    @Override
    public List<ComputationJob> findByComputationId(ComputationId computationId) {
        TextPageLink pageLink = new TextPageLink(300);
        log.info("Try to find all tenant computationJobs by tenantId [{}] and pageLink [{}]", computationId, pageLink);
        List<ComputationJobEntity> computationJobEntities = findPageWithTextSearch(ModelConstants.COMPUTATION_JOB_BY_COMPUTATION,
                Arrays.asList(in(ModelConstants.COMPUTATION_JOB_COMPUTAION_ID, Arrays.asList(NULL_UUID, computationId))),
                pageLink);
        if (log.isTraceEnabled()) {
            log.trace("Search result: [{}]", Arrays.toString(computationJobEntities.toArray()));
        } else {
            log.info("Search result: [{}]", computationJobEntities.size());
        }
        return DaoUtil.convertDataList(computationJobEntities);
    }

    @Override
    public List<ComputationJob> findByTenantIdAndComputationIdAndPageLink(TenantId tenantId, ComputationId computationId, TextPageLink pageLink) {
        return null;
    }
}
