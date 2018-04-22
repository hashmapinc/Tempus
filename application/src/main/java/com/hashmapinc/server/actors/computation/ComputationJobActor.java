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
package com.hashmapinc.server.actors.computation;

import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.hashmapinc.server.actors.ActorSystemContext;
import com.hashmapinc.server.actors.service.ContextBasedCreator;
import com.hashmapinc.server.common.data.computation.Computations;
import com.hashmapinc.server.common.data.id.ComputationJobId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.actors.service.ComponentActor;
import com.hashmapinc.server.common.msg.plugin.ComponentLifecycleMsg;

public class ComputationJobActor extends ComponentActor<ComputationJobId, ComputationJobActorMessageProcessor> {

    private final LoggingAdapter logger = Logging.getLogger(getContext().system(), this);

    public ComputationJobActor(ActorSystemContext systemContext, TenantId tenantId,
                               Computations computation, ComputationJobId computationJobId) {
        super(systemContext, tenantId, computationJobId);
        setProcessor(new ComputationJobActorMessageProcessor(tenantId, computationJobId, systemContext,
                logger, context().parent(), context().self(), computation));
    }

    @Override
    public void onReceive(Object msg) throws Exception {
        logger.debug("[{}] Received message: {}", tenantId, msg);
        if(msg instanceof ComponentLifecycleMsg){
            onComponentLifecycleMsg((ComponentLifecycleMsg)msg);
        }else if(msg instanceof ComputationJobTerminationMsg) {
            context().stop(self());
        }else {
            logger.warning("[{}] Unknown message: {}!", tenantId, msg);
        }
    }

    @Override
    protected long getErrorPersistFrequency() {
        return systemContext.getComputationErrorPersistFrequency();
    }


    public static class ActorCreator extends ContextBasedCreator<ComputationJobActor> {
        private static final long serialVersionUID = 1L;

        private final TenantId tenantId;
        private final Computations computation;
        private final ComputationJobId computationJobId;

        public ActorCreator(ActorSystemContext context, TenantId tenantId, Computations computation, ComputationJobId computationJobId) {
            super(context);
            this.tenantId = tenantId;
            this.computation = computation;
            this.computationJobId = computationJobId;
        }

        @Override
        public ComputationJobActor create() throws Exception {
            return new ComputationJobActor(context, tenantId, computation, computationJobId);
        }
    }
}
