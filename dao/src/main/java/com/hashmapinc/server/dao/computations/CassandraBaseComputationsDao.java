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

import com.datastax.driver.core.querybuilder.Select;
import com.hashmapinc.server.common.data.computation.Computations;
import com.hashmapinc.server.common.data.id.ComputationId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.page.TextPageLink;
import com.hashmapinc.server.dao.DaoUtil;
import com.hashmapinc.server.dao.model.nosql.ComputationsEntity;
import com.hashmapinc.server.dao.nosql.CassandraAbstractSearchTextDao;
import com.hashmapinc.server.dao.util.NoSqlDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.hashmapinc.server.dao.model.ModelConstants;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.datastax.driver.core.querybuilder.QueryBuilder.*;
import static com.hashmapinc.server.dao.model.ModelConstants.*;

@Component
@Slf4j
@NoSqlDao
public class CassandraBaseComputationsDao extends CassandraAbstractSearchTextDao<ComputationsEntity, Computations> implements ComputationsDao {

    @Override
    protected Class<ComputationsEntity> getColumnFamilyClass() {
        return ComputationsEntity.class;
    }

    @Override
    protected String getColumnFamilyName() {
        return ModelConstants.COMPUTATIONS_COLUMN_FAMILY_NAME;
    }

    public void deleteById(UUID id) {
        log.info("Delete computations entity by id [{}]", id);
        boolean result = removeById(id);
        log.info("Delete result: [{}]", result);
    }

    @Override
    public void deleteById(ComputationId computationId) {
        deleteById(computationId.getId());
    }

    @Override
    public List<Computations> findByTenantIdAndPageLink(TenantId tenantId, TextPageLink pageLink) {
        log.info("Try to find all tenant computationJobs by tenantId [{}] and pageLink [{}]", tenantId, pageLink);
        List<ComputationsEntity> computationsEntities = findPageWithTextSearch(ModelConstants.COMPUTATIONS_BY_TENANT,
                Arrays.asList(in(ModelConstants.COMPUTATIONS_TENANT_ID_PROPERTY, tenantId.getId())), pageLink);
        if (log.isTraceEnabled()) {
            log.trace("Search result: [{}]", Arrays.toString(computationsEntities.toArray()));
        } else {
            log.info("Search result: [{}]", computationsEntities.size());
        }
        return DaoUtil.convertDataList(computationsEntities);
    }

    @Override
    public Computations findByName(String name) {
        throw new UnsupportedOperationException("To support multi-tenancy this is not supported without tenantId");
    }

    @Override
    public Optional<Computations> findByTenantIdAndName(TenantId tenantId, String name) {
        Select select = select().from(COMPUTATIONS_BY_TENANT_AND_NAME_COLUMN_FAMILY);
        Select.Where query = select.where();
        query.and(eq(COMPUTATIONS_TENANT_ID_PROPERTY, tenantId.getId()));
        query.and(eq(COMPUTATIONS_NAME_PROPERTY, name));
        return Optional.ofNullable(DaoUtil.getData(findOneByStatement(query)));
    }

    @Override
    public List<Computations> findByTenantId(TenantId tenantId) {
        Select select = select().from(ModelConstants.COMPUTATIONS_COLUMN_FAMILY_NAME).allowFiltering();
        Select.Where query = select.where();
        query.and(eq(ModelConstants.COMPUTATIONS_TENANT_ID, tenantId.getId()));
        List<ComputationsEntity> computationsEntities = findListByStatement(query);
        return DaoUtil.convertDataList(computationsEntities);
    }

}
