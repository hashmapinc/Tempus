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
package com.hashmapinc.server.dao.rule;

import com.hashmapinc.server.common.data.id.RuleId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.page.TextPageLink;
import com.hashmapinc.server.dao.Dao;
import com.hashmapinc.server.common.data.rule.RuleMetaData;

import java.util.List;
import java.util.UUID;

public interface RuleDao extends Dao<RuleMetaData> {

    RuleMetaData save(RuleMetaData rule);

    RuleMetaData findById(RuleId ruleId);

    List<RuleMetaData> findRulesByPlugin(String pluginToken);

    List<RuleMetaData> findByTenantIdAndPageLink(TenantId tenantId, TextPageLink pageLink);

    /**
     * Find all tenant rules (including system) by tenantId and page link.
     *
     * @param tenantId the tenantId
     * @param pageLink the page link
     * @return the list of rules objects
     */
    List<RuleMetaData> findAllTenantRulesByTenantId(UUID tenantId, TextPageLink pageLink);

    void deleteById(UUID id);

    void deleteById(RuleId ruleId);
}
