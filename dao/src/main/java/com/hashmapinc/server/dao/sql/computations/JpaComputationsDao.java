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
package com.hashmapinc.server.dao.sql.computations;

import com.hashmapinc.server.dao.computations.ComputationsDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import com.hashmapinc.server.common.data.computation.Computations;
import com.hashmapinc.server.common.data.UUIDConverter;
import com.hashmapinc.server.common.data.id.ComputationId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.page.TextPageLink;
import com.hashmapinc.server.dao.DaoUtil;
import com.hashmapinc.server.dao.model.sql.ComputationsEntity;
import com.hashmapinc.server.dao.sql.JpaAbstractDaoListeningExecutorService;
import com.hashmapinc.server.dao.util.SqlDao;

import java.util.*;

import static com.hashmapinc.server.dao.model.ModelConstants.NULL_UUID_STR;

@Service
@SqlDao
@Slf4j
public class JpaComputationsDao extends JpaAbstractDaoListeningExecutorService implements ComputationsDao {

    @Autowired
    ComputationsRepository computationsRepository;

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
    public Computations save(Computations computations) {
        ComputationsEntity computationsEntity = new ComputationsEntity(computations);
        computationsRepository.save(computationsEntity);
        return computations;
    }

    @Override
    public void deleteById(ComputationId computationId) {
        computationsRepository.delete(UUIDConverter. fromTimeUUID(computationId.getId()));
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
        Iterable <ComputationsEntity> computationsEntities = computationsRepository.findByTenantId(UUIDConverter.fromTimeUUID(tenantId.getId()));
        List<ComputationsEntity> computationsEntityList = DaoUtil.toList(computationsEntities);
        List<Computations> computationsList = DaoUtil.convertDataList(computationsEntityList);
        return computationsList;
    }

    @Override
    public Optional<Computations> findByTenantIdAndName(TenantId tenantId, String name) {
        ComputationsEntity computationsEntity = computationsRepository.findByTenantIdAndName(UUIDConverter.fromTimeUUID(tenantId.getId()), name);
        return Optional.ofNullable(DaoUtil.getData(computationsEntity));
    }

}
