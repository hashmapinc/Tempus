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

import akka.actor.ActorContext;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.hashmapinc.server.actors.ActorSystemContext;
import com.hashmapinc.server.actors.rule.RuleActor;
import com.hashmapinc.server.actors.rule.SimpleRuleActorChain;
import com.hashmapinc.server.actors.service.ContextAwareActor;
import com.hashmapinc.server.common.data.id.RuleId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.plugin.ComponentLifecycleEvent;
import com.hashmapinc.server.common.data.plugin.ComponentLifecycleState;
import com.hashmapinc.server.common.data.rule.RuleMetaData;
import com.hashmapinc.server.dao.rule.RuleService;
import lombok.extern.slf4j.Slf4j;
import com.hashmapinc.server.actors.rule.RuleActorChain;
import com.hashmapinc.server.actors.rule.RuleActorMetaData;
import com.hashmapinc.server.common.data.page.PageDataIterable;
import com.hashmapinc.server.common.data.page.PageDataIterable.FetchFunction;

import java.util.*;

@Slf4j
public abstract class RuleManager {

    protected final ActorSystemContext systemContext;
    protected final RuleService ruleService;
    protected final Map<RuleId, ActorRef> ruleActors;
    protected final TenantId tenantId;

    private Map<RuleMetaData, RuleActorMetaData> ruleMap;
    private RuleActorChain ruleChain;

    public RuleManager(ActorSystemContext systemContext, TenantId tenantId) {
        this.systemContext = systemContext;
        this.ruleService = systemContext.getRuleService();
        this.ruleActors = new HashMap<>();
        this.tenantId = tenantId;
    }

    public void init(ActorContext context) {
        doInit(context);
    }

    private void doInit(ActorContext context) {
        PageDataIterable<RuleMetaData> ruleIterator = new PageDataIterable<>(getFetchRulesFunction(),
                ContextAwareActor.ENTITY_PACK_LIMIT);
        ruleMap = new HashMap<>();

        for (RuleMetaData rule : ruleIterator) {
            log.debug("[{}] Creating rule actor {}", rule.getId(), rule);
            ActorRef ref = getOrCreateRuleActor(context, rule.getId());
            ruleMap.put(rule, RuleActorMetaData.systemRule(rule.getId(), rule.getWeight(), ref));
            log.debug("[{}] Rule actor created.", rule.getId());
        }

        refreshRuleChain();
    }

    public Optional<ActorRef> update(ActorContext context, RuleId ruleId, ComponentLifecycleEvent event) {
        if (ruleMap == null) {
            doInit(context);
        }
        RuleMetaData rule;
        if (event != ComponentLifecycleEvent.DELETED) {
            rule = systemContext.getRuleService().findRuleById(ruleId);
            if(rule != null){
                RuleActorMetaData actorMd = ruleMap.get(rule);
                if (actorMd == null) {
                    ActorRef ref = getOrCreateRuleActor(context, rule.getId());
                    actorMd = getRuleActorMetadata(rule, ref);
                    ruleMap.put(rule, actorMd);
                }
                refreshRuleChain();
                return Optional.of(actorMd.getActorRef());
            }
        } else {
            rule = ruleMap.keySet().stream()
                    .filter(r -> r.getId().equals(ruleId))
                    .peek(r -> r.setState(ComponentLifecycleState.SUSPENDED))
                    .findFirst()
                    .orElse(null);
            log.debug("Suspended rule {}", rule);
            if (rule != null) {
                RuleActorMetaData ruleActorMetaData = ruleMap.get(rule);
                if(ruleActorMetaData != null){
                    ruleMap.remove(rule);
                    ruleActors.remove(ruleId);
                    refreshRuleChain();
                    return Optional.of(ruleActorMetaData.getActorRef());
                }
            }
        }
        log.warn("[{}] Can't process unknown rule!", ruleId);
        return Optional.empty();
    }

    abstract FetchFunction<RuleMetaData> getFetchRulesFunction();

    abstract String getDispatcherName();

    abstract RuleActorMetaData getRuleActorMetadata(RuleMetaData rule, ActorRef actorRef);

    public ActorRef getOrCreateRuleActor(ActorContext context, RuleId ruleId) {
        return ruleActors.computeIfAbsent(ruleId, rId ->
                context.actorOf(Props.create(RuleActor.class, new RuleActor.ActorCreator(systemContext, tenantId, rId))
                        .withDispatcher(getDispatcherName()), rId.toString()));
    }

    public RuleActorChain getRuleChain(ActorContext context) {
        if (ruleChain == null) {
            doInit(context);
        }
        return ruleChain;
    }

    private void refreshRuleChain() {
        Set<RuleActorMetaData> activeRuleSet = new HashSet<>();
        for (Map.Entry<RuleMetaData, RuleActorMetaData> rule : ruleMap.entrySet()) {
            if (rule.getKey().getState() == ComponentLifecycleState.ACTIVE) {
                activeRuleSet.add(rule.getValue());
            }
        }
        ruleChain = new SimpleRuleActorChain(activeRuleSet);
    }
}
