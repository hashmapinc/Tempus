package com.hashmapinc.server.actors.computation;

import com.hashmapinc.server.ComputationJobActor;
import com.hashmapinc.server.actors.ActorSystemContext;
import com.hashmapinc.server.actors.computation.AWSLambdaComputationJobActorMessageProcessor;
import com.hashmapinc.server.actors.service.ContextBasedCreator;
import com.hashmapinc.server.actors.shared.ComponentMsgProcessor;
import com.hashmapinc.server.common.data.computation.Computations;
import com.hashmapinc.server.common.data.id.ComputationJobId;
import com.hashmapinc.server.common.data.id.TenantId;

public class AWSLambdaComputationJobActor extends ComputationJobActor<AWSLambdaComputationJobActorMessageProcessor> {
    public AWSLambdaComputationJobActor(ActorSystemContext systemContext, TenantId tenantId,
                                       Computations computation, ComputationJobId computationJobId) {
        super(systemContext, tenantId, computationJobId);
        setProcessor(new AWSLambdaComputationJobActorMessageProcessor(tenantId, computationJobId, systemContext,
                logger, context().parent(), context().self(), computation));
    }

    public static class ActorCreator extends ContextBasedCreator<AWSLambdaComputationJobActor> {
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
        public AWSLambdaComputationJobActor create() throws Exception {
            return new AWSLambdaComputationJobActor(context, tenantId, computation, computationJobId);
        }
    }
}
