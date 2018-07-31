/**
 * Copyright © 2016-2018 The Thingsboard Authors
 * Modifications © 2017-2018 Hashmap, Inc
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
package com.hashmapinc.server.actors.computation;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.hashmapinc.server.actors.ActorSystemContext;
import com.hashmapinc.server.actors.service.ContextAwareActor;
import com.hashmapinc.server.actors.service.ContextBasedCreator;
import com.hashmapinc.server.actors.service.DefaultActorService;
import com.hashmapinc.server.common.data.computation.ComputationJob;
import com.hashmapinc.server.common.data.computation.ComputationType;
import com.hashmapinc.server.common.data.computation.Computations;
import com.hashmapinc.server.common.data.id.ComputationId;
import com.hashmapinc.server.common.data.id.ComputationJobId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.page.PageDataIterable;
import com.hashmapinc.server.common.data.plugin.ComponentLifecycleEvent;
import com.hashmapinc.server.common.data.plugin.ComponentLifecycleState;
import com.hashmapinc.server.common.msg.plugin.ComponentLifecycleMsg;

import java.util.HashMap;
import java.util.Map;

public class ComputationActor extends ContextAwareActor {
    protected final LoggingAdapter logger = Logging.getLogger(getContext().system(), this);

    private final TenantId tenantId;
    private final ComputationId computationId;
    private Computations computation;
    private ComputationType type;
    private final Map<ComputationJobId, ActorRef> computationJobActors;


    public ComputationActor(ActorSystemContext systemContext, TenantId tenantId, ComputationId computationId, ComputationType type) {
        super(systemContext);
        this.tenantId = tenantId;
        this.computationId = computationId;
        this.type = type;
        this.computationJobActors = new HashMap<>();
    }

    @Override
    public void preStart(){
        start();
    }

    private void start(){
        logger.info("[{}][{}] Starting computation actor.", computationId, tenantId);
        computation = systemContext.getComputationsService().findById(computationId);
        if(computation == null){
            throw new ComputationInitializationException("Computation not found!");
        }
        init();
        logger.info("[{}][{}] Started computation actor.", computation, tenantId);
    }

    private void init(){
        PageDataIterable<ComputationJob> computationsIterator = new PageDataIterable<>(getFetchComputationJobsFunction(),
                ContextAwareActor.ENTITY_PACK_LIMIT);
        for(ComputationJob c : computationsIterator){
            if(c.getState() == ComponentLifecycleState.ACTIVE) {
                logger.debug("[{}] Creating computation job actor {}", c.getId(), c);
                ActorRef ref = getOrCreateComputationJobActor(c.getId());
                computationJobActors.put(c.getId(), ref);
                logger.debug("[{}] computation job actor created.", c.getId());
            }
        }
    }

    protected PageDataIterable.FetchFunction<ComputationJob> getFetchComputationJobsFunction() {
        return link -> systemContext.getComputationJobService().findTenantComputationJobs(tenantId, computationId, link);
    }

    @Override
    public void onReceive(Object msg) throws Exception {
        logger.debug("[{}] Received message: {}", tenantId, msg);
        if(msg instanceof ComponentLifecycleMsg){
            onComponentLifecycleMsg((ComponentLifecycleMsg)msg);
        }else if(msg instanceof ComputationJobTerminationMsg) {
            ComputationJobTerminationMsg terminationMsg = (ComputationJobTerminationMsg) msg;
            computationJobActors.remove(terminationMsg.getId());
        }else {
            logger.warning("[{}] Unknown message: {}!", tenantId, msg);
        }
    }

    private void onComponentLifecycleMsg(ComponentLifecycleMsg msg) {
        if(msg.getComputationJobId().isPresent()){
            msg.getComputationJobId().ifPresent(computationJobId ->
                    handleComponentLifecycleMsgForExistingComputation(msg, computationJobId));
        }else {
            handleComponentLifecycleMsgForNonExistingComputation(msg);
        }
    }

    private void handleComponentLifecycleMsgForNonExistingComputation(ComponentLifecycleMsg msg) {
        if(msg.getEvent() != ComponentLifecycleEvent.DELETED){
            if(computationJobActors.isEmpty()){
                init();
            }
            computationJobActors.forEach((k, v) -> v.tell(msg, ActorRef.noSender()));
        }else{
            computationJobActors.forEach((k, v) -> v.tell(msg, ActorRef.noSender()));
            context().stop(self());
        }
    }

    private void handleComponentLifecycleMsgForExistingComputation(ComponentLifecycleMsg msg, ComputationJobId jobId) {
        ActorRef target;
        if(msg.getEvent() == ComponentLifecycleEvent.DELETED){
            target = computationJobActors.get(jobId);
            if(target != null){
                computationJobActors.remove(jobId);
                target.tell(msg, ActorRef.noSender());
                logger.debug("Suspended computation job {}", jobId);
            }
        }else{
            ComputationJob job = systemContext.getComputationJobService().findComputationJobById(jobId);
            if(job != null){
                computationJobActors.putIfAbsent(jobId, getOrCreateComputationJobActor(jobId));
                target = computationJobActors.get(jobId);
                target.tell(msg, ActorRef.noSender());
            }
        }
    }

    private ActorRef getOrCreateComputationJobActor(ComputationJobId computationJobId) {
        ActorRef actorRef = null;
        if(this.type == ComputationType.SPARK) {
            actorRef = computationJobActors.computeIfAbsent(computationJobId, k ->
                    context().actorOf(Props.create(new SparkComputationJobActor.ActorCreator(systemContext, tenantId, computation, computationJobId))
                            .withDispatcher(DefaultActorService.CORE_DISPATCHER_NAME), computationJobId.toString()));
        }
        else if(this.type == ComputationType.KUBELESS){
            actorRef = computationJobActors.computeIfAbsent(computationJobId, k ->
                    context().actorOf(Props.create(new KubelessComputationJobActor.ActorCreator(systemContext, tenantId, computation, computationJobId))
                            .withDispatcher(DefaultActorService.CORE_DISPATCHER_NAME), computationJobId.toString()));

        }
        return actorRef;
    }

    public static class ActorCreator extends ContextBasedCreator<ComputationActor> {
        private static final long serialVersionUID = 1L;

        private final TenantId tenantId;
        private final ComputationId computationId;
        private final ComputationType type;

        public ActorCreator(ActorSystemContext context, TenantId tenantId, ComputationId computationId, ComputationType type) {
            super(context);
            this.tenantId = tenantId;
            this.computationId = computationId;
            this.type = type;
        }

        @Override
        public ComputationActor create() throws Exception {
            return new ComputationActor(context, tenantId, computationId, type);
        }
    }
}
