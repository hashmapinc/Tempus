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
package com.hashmapinc.server.actors.shared.computation;

import akka.actor.ActorContext;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.hashmapinc.server.actors.ActorSystemContext;
import com.hashmapinc.server.common.data.id.ComputationId;
import lombok.extern.slf4j.Slf4j;
import com.hashmapinc.server.actors.computation.ComputationActor;
import com.hashmapinc.server.actors.service.ContextAwareActor;
import com.hashmapinc.server.actors.service.DefaultActorService;
import com.hashmapinc.server.common.data.computation.Computations;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.page.PageDataIterable;
import com.hashmapinc.server.dao.computations.ComputationsService;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class TenantComputationManager {

    protected final ActorSystemContext systemContext;
    protected final TenantId tenantId;
    protected final Map<ComputationId, ActorRef> computationActors;
    protected final ComputationsService computationsService;

    public TenantComputationManager(ActorSystemContext systemContext, TenantId tenantId) {
        this.systemContext = systemContext;
        this.tenantId = tenantId;
        this.computationActors = new HashMap<>();
        this.computationsService = systemContext.getComputationsService();
    }

    public void init(ActorContext context) {
        doInit(context);
    }

    private void doInit(ActorContext context) {
        PageDataIterable<Computations> computationsIterator = new PageDataIterable<>(getFetchComputationsFunction(),
                ContextAwareActor.ENTITY_PACK_LIMIT);
        for(Computations c : computationsIterator){
            log.debug("[{}] Creating computation actor {}", c.getId(), c);
            ActorRef ref = getOrCreateComputationActor(context, c.getId());
            computationActors.put(c.getId(), ref);
            log.debug("[{}] computation actor created.", c.getId());
        }
    }

    protected PageDataIterable.FetchFunction<Computations> getFetchComputationsFunction() {
        return link -> computationsService.findTenantComputations(tenantId, link);
    }

    public ActorRef getOrCreateComputationActor(ActorContext context, ComputationId computationId) {
        return computationActors.computeIfAbsent(computationId, cId ->
                context.actorOf(Props.create(new ComputationActor.ActorCreator(systemContext, tenantId, cId))
                        .withDispatcher(getDispatcherName()), cId.toString()));
    }

    protected String getDispatcherName() {
        return DefaultActorService.CORE_DISPATCHER_NAME;
    }
}
