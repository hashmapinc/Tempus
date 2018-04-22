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
package com.hashmapinc.server.dao.sql.rule;

import com.hashmapinc.server.common.data.id.RuleId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.page.TextPageLink;
import com.hashmapinc.server.common.data.rule.RuleMetaData;
import com.hashmapinc.server.dao.DaoUtil;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.rule.RuleDao;
import com.hashmapinc.server.dao.util.SqlDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import com.hashmapinc.server.common.data.UUIDConverter;
import com.hashmapinc.server.dao.model.sql.RuleMetaDataEntity;
import com.hashmapinc.server.dao.sql.JpaAbstractSearchTextDao;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Created by Valerii Sosliuk on 4/30/2017.
 */
@Slf4j
@Component
@SqlDao
public class JpaBaseRuleDao extends JpaAbstractSearchTextDao<RuleMetaDataEntity, RuleMetaData> implements RuleDao {

    public static final String SEARCH_RESULT = "Search result: [{}]";
    @Autowired
    private RuleMetaDataRepository ruleMetaDataRepository;

    @Override
    protected Class<RuleMetaDataEntity> getEntityClass() {
        return RuleMetaDataEntity.class;
    }

    @Override
    protected CrudRepository<RuleMetaDataEntity, String> getCrudRepository() {
        return ruleMetaDataRepository;
    }

    @Override
    public RuleMetaData findById(RuleId ruleId) {
        return findById(ruleId.getId());
    }

    @Override
    public List<RuleMetaData> findRulesByPlugin(String pluginToken) {
        log.debug("Search rules by api token [{}]", pluginToken);
        return DaoUtil.convertDataList(ruleMetaDataRepository.findByPluginToken(pluginToken));
    }

    @Override
    public List<RuleMetaData> findByTenantIdAndPageLink(TenantId tenantId, TextPageLink pageLink) {
        log.debug("Try to find rules by tenantId [{}] and pageLink [{}]", tenantId, pageLink);
        List<RuleMetaDataEntity> entities =
                ruleMetaDataRepository
                        .findByTenantIdAndPageLink(
                                UUIDConverter.fromTimeUUID(tenantId.getId()),
                                Objects.toString(pageLink.getTextSearch(), ""),
                                pageLink.getIdOffset() == null ? ModelConstants.NULL_UUID_STR :  UUIDConverter.fromTimeUUID(pageLink.getIdOffset()),
                                new PageRequest(0, pageLink.getLimit()));
        if (log.isTraceEnabled()) {
            log.trace(SEARCH_RESULT, Arrays.toString(entities.toArray()));
        } else {
            log.debug(SEARCH_RESULT, entities.size());
        }
        return DaoUtil.convertDataList(entities);
    }

    @Override
    public List<RuleMetaData> findAllTenantRulesByTenantId(UUID tenantId, TextPageLink pageLink) {
        log.debug("Try to find all tenant rules by tenantId [{}] and pageLink [{}]", tenantId, pageLink);
        List<RuleMetaDataEntity> entities =
                ruleMetaDataRepository
                        .findAllTenantRulesByTenantId(
                                UUIDConverter.fromTimeUUID(tenantId),
                                ModelConstants.NULL_UUID_STR,
                                Objects.toString(pageLink.getTextSearch(), ""),
                                pageLink.getIdOffset() == null ? ModelConstants.NULL_UUID_STR :  UUIDConverter.fromTimeUUID(pageLink.getIdOffset()),
                                new PageRequest(0, pageLink.getLimit()));

        if (log.isTraceEnabled()) {
            log.trace(SEARCH_RESULT, Arrays.toString(entities.toArray()));
        } else {
            log.debug(SEARCH_RESULT, entities.size());
        }
        return DaoUtil.convertDataList(entities);
    }

    @Override
    public void deleteById(UUID id) {
        log.debug("Delete rule meta-data entity by id [{}]", id);
        ruleMetaDataRepository.delete(UUIDConverter.fromTimeUUID(id));
    }

    @Override
    public void deleteById(RuleId ruleId) {
        deleteById(ruleId.getId());
    }
}
