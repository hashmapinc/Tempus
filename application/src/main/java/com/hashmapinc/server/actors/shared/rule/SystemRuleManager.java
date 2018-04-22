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
package com.hashmapinc.server.actors.shared.rule;

import akka.actor.ActorRef;
import com.hashmapinc.server.actors.ActorSystemContext;
import com.hashmapinc.server.actors.rule.RuleActorMetaData;
import com.hashmapinc.server.actors.service.DefaultActorService;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.page.PageDataIterable.FetchFunction;
import com.hashmapinc.server.common.data.rule.RuleMetaData;
import com.hashmapinc.server.dao.model.ModelConstants;

public class SystemRuleManager extends RuleManager {

    public SystemRuleManager(ActorSystemContext systemContext) {
        super(systemContext, new TenantId(ModelConstants.NULL_UUID));
    }

    @Override
    FetchFunction<RuleMetaData> getFetchRulesFunction() {
        return ruleService::findSystemRules;
    }

    @Override
    String getDispatcherName() {
        return DefaultActorService.SYSTEM_RULE_DISPATCHER_NAME;
    }

    @Override
    RuleActorMetaData getRuleActorMetadata(RuleMetaData rule, ActorRef actorRef) {
        return RuleActorMetaData.systemRule(rule.getId(), rule.getWeight(), actorRef);
    }
}
