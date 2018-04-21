/**
 * Copyright © 2016-2017 Hashmap, Inc
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
package org.thingsboard.server.actors.computation;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import org.thingsboard.server.actors.ActorSystemContext;
import org.thingsboard.server.actors.service.ContextAwareActor;
import org.thingsboard.server.actors.service.ContextBasedCreator;
import org.thingsboard.server.actors.service.DefaultActorService;
import org.thingsboard.server.common.data.computation.ComputationJob;
import org.thingsboard.server.common.data.computation.Computations;
import org.thingsboard.server.common.data.id.ComputationId;
import org.thingsboard.server.common.data.id.ComputationJobId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.PageDataIterable;
import org.thingsboard.server.common.data.plugin.ComponentLifecycleEvent;
import org.thingsboard.server.common.data.plugin.ComponentLifecycleState;
import org.thingsboard.server.common.msg.plugin.ComponentLifecycleMsg;
import java.util.HashMap;
import java.util.Map;

public class ComputationActor extends ContextAwareActor{
    private final LoggingAdapter logger = Logging.getLogger(getContext().system(), this);

    private final TenantId tenantId;
    private final ComputationId computationId;
    private Computations computation;
    private final Map<ComputationJobId, ActorRef> computationJobActors;

    public ComputationActor(ActorSystemContext systemContext, TenantId tenantId, ComputationId computationId) {
        super(systemContext);
        this.tenantId = tenantId;
        this.computationId = computationId;
        this.computationJobActors = new HashMap<>();
    }

    @Override
    public void preStart() throws Exception {
        start();
    }

    private void start() throws Exception{
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
            ActorRef target;
            ComputationJobId jobId = msg.getComputationJobId().get();
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
        }else {
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
    }

    private ActorRef getOrCreateComputationJobActor(ComputationJobId computationJobId) {
        return computationJobActors.computeIfAbsent(computationJobId, k ->
                context().actorOf(Props.create(new ComputationJobActor.ActorCreator(systemContext, tenantId, computation, computationJobId))
                .withDispatcher(DefaultActorService.CORE_DISPATCHER_NAME), computationJobId.toString()));
    }

    public static class ActorCreator extends ContextBasedCreator<ComputationActor> {
        private static final long serialVersionUID = 1L;

        private final TenantId tenantId;
        private final ComputationId computationId;

        public ActorCreator(ActorSystemContext context, TenantId tenantId, ComputationId computationId) {
            super(context);
            this.tenantId = tenantId;
            this.computationId = computationId;
        }

        @Override
        public ComputationActor create() throws Exception {
            return new ComputationActor(context, tenantId, computationId);
        }
    }
}
