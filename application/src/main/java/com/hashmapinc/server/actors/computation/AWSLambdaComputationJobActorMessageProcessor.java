package com.hashmapinc.server.actors.computation;

import akka.actor.ActorContext;
import akka.actor.ActorRef;
import akka.event.LoggingAdapter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hashmapinc.server.actors.ActorSystemContext;
import com.hashmapinc.server.actors.shared.ComponentMsgProcessor;
import com.hashmapinc.server.common.data.computation.ComputationJob;
import com.hashmapinc.server.common.data.computation.Computations;
import com.hashmapinc.server.common.data.computation.CronKubelessTrigger;
import com.hashmapinc.server.common.data.computation.KafkaKubelessTrigger;
import com.hashmapinc.server.common.data.id.ComputationJobId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.plugin.ComponentLifecycleState;
import com.hashmapinc.server.common.msg.cluster.ClusterEventMsg;
import com.hashmapinc.server.exception.TempusApplicationException;

public class AWSLambdaComputationJobActorMessageProcessor extends ComponentMsgProcessor<ComputationJobId> {
    private ComputationJob job;
    private final Computations computation;
    private ObjectMapper objectMapper = new ObjectMapper();
    private final ActorRef self;
    private final ActorRef parent;

    protected AWSLambdaComputationJobActorMessageProcessor(TenantId tenantId, ComputationJobId id, ActorSystemContext systemContext
            , LoggingAdapter logger, ActorRef parent, ActorRef self, Computations computation) {
        super(systemContext, logger, tenantId, id);
        this.computation = computation;
        this.self = self;
        this.parent = parent;
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
    }

    @Override
    public void start() throws TempusApplicationException {
        logger.info("[{}] Going to start AWS lambda computation job actor.", entityId);
        job = systemContext.getComputationJobService().findComputationJobById(entityId);
        if (job == null) {
            throw new ComputationInitializationException("AWS lambda computation Job not found!");
        }
        if(!isValidJobConfiguration()){
            throw new ComputationInitializationException("AWS lambda Computation Job configurations are invalid!");
        }
        if (job.getState() == ComponentLifecycleState.ACTIVE) {
            logger.info("[{}] AWS lambda computation Job is active. Going to initialize job.", entityId);
            initComponent();
        } else {
            logger.info("[{}] AWS lambda is suspended. Skipping job initialization.", entityId);
        }
    }

    @Override
    public void stop() throws TempusApplicationException {
        logger.info("stop is not implemented");
    }

    @Override
    public void onCreated(ActorContext context) throws TempusApplicationException {
        logger.info("[{}] Going to process onCreated AWS lambda computation job.", entityId);
    }

    @Override
    public void onUpdate(ActorContext context) throws TempusApplicationException {
        ComputationJob oldJob = job;
        job = systemContext.getComputationJobService().findComputationJobById(entityId);
        logger.info("[{}] Computation configuration was updated from {} to {}.", entityId, oldJob, job);
    }

    @Override
    public void onActivate(ActorContext context) throws TempusApplicationException {
        logger.info("[{}] Going to process onActivate computation job.", entityId);
        start();
    }

    @Override
    public void onSuspend(ActorContext context) throws TempusApplicationException {
        logger.info("[{}] Going to process onSuspend computation job.", entityId);
        suspendJob();
    }

    @Override
    public void onStop(ActorContext context) throws TempusApplicationException {
        logger.info("[{}] Going to process onStop computation job.", entityId);
        if(systemContext.getAwsLambdaFunctionService().deleteTrigger(job)) {
            scheduleMsgWithDelay(new ComputationJobTerminationMsg(entityId), systemContext.getComputationActorTerminationDelay(), parent);
            scheduleMsgWithDelay(new ComputationJobTerminationMsg(entityId), systemContext.getComputationActorTerminationDelay(), self);
        }
    }

    @Override
    public void onClusterEventMsg(ClusterEventMsg msg) throws TempusApplicationException {
        logger.info("onClusterEventMsg is not implemented");
    }

    private void initComponent(){
        logger.info("[{}] Going to initialize computation job.", entityId);
        checkAndCreateTrigger();
    }

    private void checkAndCreateTrigger(){
        if(!systemContext.getKubelessFunctionService().checkTrigger(job) &&
                !systemContext.getKubelessFunctionService().createTrigger(job)) {
            systemContext.getComputationJobService().suspendComputationJobById(job.getId());
        }
    }

    private void suspendJob(){
        ComputationJob savedJob = systemContext.getComputationJobService().findComputationJobById(job.getId());
        if (savedJob != null && systemContext.getAwsLambdaFunctionService().checkTrigger(job) &&
                systemContext.getAwsLambdaFunctionService().deleteTrigger(job)) {
            systemContext.getComputationJobService().suspendComputationJobById(job.getId());
        }
    }

    //TODO
    private boolean isValidJobConfiguration(){
        return true;
    }

}
