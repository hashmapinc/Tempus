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
package org.thingsboard.server.dao.sql.computations;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.thingsboard.server.common.data.computation.Computations;
import org.thingsboard.server.common.data.UUIDConverter;
import org.thingsboard.server.common.data.id.ComputationId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.TextPageLink;
import org.thingsboard.server.dao.DaoUtil;
import org.thingsboard.server.dao.computations.ComputationsDao;
import org.thingsboard.server.dao.model.sql.ComputationsEntity;
import org.thingsboard.server.dao.sql.JpaAbstractDaoListeningExecutorService;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.thingsboard.server.dao.model.ModelConstants.NULL_UUID_STR;

@Service
@Slf4j
public class JpaComputationsDao extends JpaAbstractDaoListeningExecutorService implements ComputationsDao {

    @Autowired
    ComputationsRepository computationsRepository;

    @Override
    public List<Computations> findAll() {
        Iterable <ComputationsEntity> computationsEntities = computationsRepository.findAll();
        List<ComputationsEntity> computationsEntityList = DaoUtil.toList(computationsEntities);
        List<Computations> computationsList = DaoUtil.convertDataList(computationsEntityList);
        return computationsList;
    }

    @Override
    public Computations findByName(String name) {
        ComputationsEntity computationsEntity = null;
        List<ComputationsEntity> computationsEntityList = computationsRepository.findByName(name);
        if( computationsEntityList != null && !computationsEntityList.isEmpty()) {
            computationsEntity = computationsEntityList.get(0);
        }
        return DaoUtil.getData(computationsEntity);
    }

    @Override
    public void save(ComputationsEntity computationsEntity) {
        computationsRepository.save(computationsEntity);
    }

    @Override
    public void deleteById(ComputationId computationId) {
        computationsRepository.deleteById(UUIDConverter. fromTimeUUID(computationId.getId()));
    }

    @Override
    public void deleteByJarName(String name) {
        computationsRepository.deleteByJarName(name);
    }


    @Override
    public List<Computations> findByTenantIdAndPageLink(TenantId tenantId, TextPageLink pageLink) {
        log.debug("Try to find rules by tenantId [{}] and pageLink [{}]", tenantId, pageLink);
        List<ComputationsEntity> entities =
                computationsRepository
                        .findByTenantIdAndPageLink(
                                UUIDConverter.fromTimeUUID(tenantId.getId()),
                                Objects.toString(pageLink.getTextSearch(), ""),
                                pageLink.getIdOffset() == null ? NULL_UUID_STR :  UUIDConverter.fromTimeUUID(pageLink.getIdOffset()),
                                new PageRequest(0, pageLink.getLimit()));
        if (log.isTraceEnabled()) {
            log.trace("Search result: [{}]", Arrays.toString(entities.toArray()));
        } else {
            log.debug("Search result: [{}]", entities.size());
        }
        return DaoUtil.convertDataList(entities);
    }

    @Override
    public Computations findById(UUID id) {
        ComputationsEntity entity = computationsRepository.findOne(UUIDConverter.fromTimeUUID(id));
        return DaoUtil.getData(entity);
    }

    @Override
    public List<Computations> findByTenantId(TenantId tenantId) {
        Iterable <ComputationsEntity> computationsEntities = computationsRepository.findTenantById(UUIDConverter.fromTimeUUID(tenantId.getId()));
        List<ComputationsEntity> computationsEntityList = DaoUtil.toList(computationsEntities);
        List<Computations> computationsList = DaoUtil.convertDataList(computationsEntityList);
        return computationsList;
    }

}
