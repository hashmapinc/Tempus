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
import org.thingsboard.server.common.data.computation.Computations;
import org.thingsboard.server.common.data.id.ComputationId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.TextPageLink;
import org.thingsboard.server.dao.DaoUtil;
import org.thingsboard.server.dao.model.ModelConstants;
import org.thingsboard.server.dao.model.nosql.ComputationsEntity;
import org.thingsboard.server.dao.nosql.CassandraAbstractSearchTextDao;
import org.thingsboard.server.dao.util.NoSqlDao;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.datastax.driver.core.querybuilder.QueryBuilder.*;
import static org.thingsboard.server.dao.model.ModelConstants.NULL_UUID;

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

    /*@Override
    public Computations findById(ComputationId computationId) {
        Computations computations = super.findById(computationId.getId());
        if (log.isTraceEnabled()) {
            log.trace("Search result: [{}] for plugin entity [{}]", computations != null, computations);
        } else {
            log.info("Search result: [{}]", computations != null);
        }
        return computations;
    }*/

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
        return null;
    }

    @Override
    public List<Computations> findAll() {
        return null;
    }

    @Override
    public Computations findByName(String name) {
        return null;
    }

    @Override
    public void deleteByJarName(String name) {

    }

    @Override
    public List<Computations> findByTenantId(TenantId tenantId) {
        TextPageLink pageLink = new TextPageLink(300);
        log.info("Try to find all tenant computations by tenantId [{}] and pageLink [{}]", tenantId, pageLink);
        List<ComputationsEntity> computationsEntities = findPageWithTextSearch(ModelConstants.COMPUTATIONS_BY_TENANT,
                Arrays.asList(in(ModelConstants.COMPUTATIONS_TENANT_ID, Arrays.asList(NULL_UUID, tenantId))),
                pageLink);
        if (log.isTraceEnabled()) {
            log.trace("Search result: [{}]", Arrays.toString(computationsEntities.toArray()));
        } else {
            log.info("Search result: [{}]", computationsEntities.size());
        }
        return DaoUtil.convertDataList(computationsEntities);
    }

}
