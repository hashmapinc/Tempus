package com.hashmapinc.server.actors.computation;

import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.hashmapinc.server.actors.ActorSystemContext;
import com.hashmapinc.server.actors.service.ComponentActor;
import com.hashmapinc.server.actors.service.ContextBasedCreator;
import com.hashmapinc.server.common.data.computation.Computations;
import com.hashmapinc.server.common.data.id.ComputationJobId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.msg.plugin.ComponentLifecycleMsg;

public class KubelessComputationJobActor extends ComponentActor<ComputationJobId, KubelessComputationJobActorMessageProcessor> {
    private final LoggingAdapter logger = Logging.getLogger(getContext().system(), this);

    public KubelessComputationJobActor(ActorSystemContext systemContext, TenantId tenantId,
                                    Computations computation, ComputationJobId computationJobId) {
        super(systemContext, tenantId, computationJobId);
        setProcessor(new KubelessComputationJobActorMessageProcessor(tenantId, computationJobId, systemContext,
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


    public static class ActorCreator extends ContextBasedCreator<KubelessComputationJobActor> {
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
        public KubelessComputationJobActor create() throws Exception {
            return new KubelessComputationJobActor(context, tenantId, computation, computationJobId);
        }
    }
}
