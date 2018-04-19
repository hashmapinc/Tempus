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

import com.datastax.driver.core.querybuilder.Select;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
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
        log.info("Delete computationJob entity by id [{}]", id);
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
        Select select = select().from(ModelConstants.COMPUTATION_JOB_COLUMN_FAMILY_NAME).allowFiltering();
        Select.Where query = select.where();
        query.and(eq(ModelConstants.COMPUTATION_JOB_COMPUTATION_ID_PROPERTY, computationId.getId()));
        List<ComputationJobEntity> computationJobEntities = findListByStatement(query);
        log.trace("computationJobEntities returned [{}] ", computationJobEntities);
        return DaoUtil.convertDataList(computationJobEntities);
    }

    @Override
    public List<ComputationJob> findByTenantIdAndComputationIdAndPageLink(TenantId tenantId, ComputationId computationId, TextPageLink pageLink) {

        log.debug("Try to find computationJobs by tenantId [{}], computationId[{}] and pageLink [{}]", tenantId, computationId, pageLink);
        List<ComputationJobEntity> computationJobEntities = findPageWithTextSearch(ModelConstants.COMPUTATION_JOB_BY_TENANT_AND_COMPUTATION,
                Arrays.asList(eq(ModelConstants.COMPUTATION_JOB_TENANT_ID_PROPERTY, tenantId.getId()),
                        eq(ModelConstants.COMPUTATION_JOB_COMPUTATION_ID_PROPERTY, computationId.getId())),
                pageLink);

        log.trace("Found computationJobs [{}] by tenantId [{}], customerId [{}] and pageLink [{}]", computationJobEntities, tenantId, computationId, pageLink);
        return DaoUtil.convertDataList(computationJobEntities);

    }
}
