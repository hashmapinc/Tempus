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

import com.google.common.util.concurrent.ListenableFuture;
import com.hashmapinc.server.common.data.page.TextPageLink;
import com.hashmapinc.server.common.data.id.RuleId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.page.TextPageData;
import com.hashmapinc.server.common.data.rule.RuleMetaData;

import java.util.List;

public interface RuleService {

    RuleMetaData saveRule(RuleMetaData device);

    RuleMetaData findRuleById(RuleId ruleId);

    ListenableFuture<RuleMetaData> findRuleByIdAsync(RuleId ruleId);

    List<RuleMetaData> findPluginRules(String pluginToken);

    TextPageData<RuleMetaData> findSystemRules(TextPageLink pageLink);

    TextPageData<RuleMetaData> findTenantRules(TenantId tenantId, TextPageLink pageLink);

    List<RuleMetaData> findSystemRules();

    TextPageData<RuleMetaData> findAllTenantRulesByTenantIdAndPageLink(TenantId tenantId, TextPageLink pageLink);

    List<RuleMetaData> findAllTenantRulesByTenantId(TenantId tenantId);

    void activateRuleById(RuleId ruleId);

    void suspendRuleById(RuleId ruleId);

    void deleteRuleById(RuleId ruleId);

    void deleteRulesByTenantId(TenantId tenantId);

}
