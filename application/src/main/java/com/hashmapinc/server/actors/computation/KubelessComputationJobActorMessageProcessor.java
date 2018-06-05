package com.hashmapinc.server.actors.computation;

import akka.actor.ActorContext;
import akka.actor.ActorRef;
import akka.event.LoggingAdapter;
import com.fasterxml.jackson.annotation.JsonInclude;;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hashmapinc.server.actors.ActorSystemContext;
import com.hashmapinc.server.actors.shared.ComponentMsgProcessor;
import com.hashmapinc.server.common.data.computation.ComputationJob;
import com.hashmapinc.server.common.data.computation.Computations;
import com.hashmapinc.server.common.data.id.ComputationJobId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.plugin.ComponentLifecycleState;
import com.hashmapinc.server.common.msg.cluster.ClusterEventMsg;
import org.springframework.http.HttpHeaders;

public class KubelessComputationJobActorMessageProcessor extends ComponentMsgProcessor<ComputationJobId> {
    private ComputationJob job;
    private final Computations computation;
    private HttpHeaders headers = new HttpHeaders();
    private ObjectMapper mapper = new ObjectMapper();
    private final ActorRef self;
    private final ActorRef parent;

    protected KubelessComputationJobActorMessageProcessor(TenantId tenantId, ComputationJobId id, ActorSystemContext systemContext
            , LoggingAdapter logger, ActorRef parent, ActorRef self, Computations computation) {
        super(systemContext, logger, tenantId, id);
        this.computation = computation;
        this.self = self;
        this.parent = parent;
        mapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
    }

    @Override
    public void start() throws Exception {
        logger.info("[{}] Going to start kubeless computation actor.", entityId);
        job = systemContext.getComputationJobService().findComputationJobById(entityId);
        if (job == null) {
            throw new ComputationInitializationException("kubelesss computation Job not found!");
        }
        if (job.getArgParameters() == null) {
            throw new ComputationInitializationException("kubeless computation Job Arguments is empty!");
        }
        if (job.getState() == ComponentLifecycleState.ACTIVE) {
            logger.info("[{}] kubeless computation Job is active. Going to initialize job.", entityId);
            initComponent();
        } else {
            logger.info("[{}] kubeless computation Job is suspended. Skipping job initialization.", entityId);
        }
    }

    @Override
    public void stop() throws Exception {

    }

    @Override
    public void onCreated(ActorContext context) throws Exception {
        logger.info("[{}] Going to process onCreated kubeless computation job.", entityId);
    }

    @Override
    public void onUpdate(ActorContext context) throws Exception {
        ComputationJob oldJob = job;
        job = systemContext.getComputationJobService().findComputationJobById(entityId);
        logger.info("[{}] Computation configuration was updated from {} to {}.", entityId, oldJob, job);
        if(!oldJob.getArgParameters().equals(job.getArgParameters())){
            onStop(context);
            systemContext.getComputationJobService().activateComputationJobById(job.getId());
            start();
        }
    }

    @Override
    public void onActivate(ActorContext context) throws Exception {
        logger.info("[{}] Going to process onActivate computation job.", entityId);
        start();
    }

    @Override
    public void onSuspend(ActorContext context) throws Exception {
        logger.info("[{}] Going to process onSuspend computation job.", entityId);
    }

    @Override
    public void onStop(ActorContext context) throws Exception {
        logger.info("[{}] Going to process onStop computation job.", entityId);
        scheduleMsgWithDelay(context, new ComputationJobTerminationMsg(entityId), systemContext.getComputationActorTerminationDelay(), parent);
        scheduleMsgWithDelay(context, new ComputationJobTerminationMsg(entityId), systemContext.getComputationActorTerminationDelay(), self);
    }

    @Override
    public void onClusterEventMsg(ClusterEventMsg msg) throws Exception {

    }

    private void initComponent(){
        logger.info("[{}] Going to initialize computation job.", entityId);
        processHeaders();
        postJob();
    }

    private void processHeaders(){
        JsonNode jsonHeaders = job.getArgParameters().get("headers");
        headers.add("Content-Type", "application/json");
        if(jsonHeaders != null && jsonHeaders.isArray()){
            logger.info("Processing headers " + jsonHeaders.asText());
            for(JsonNode e : jsonHeaders){
                this.headers.add(e.get("key").asText(), e.get("value").asText());
            }
        }
    }

    private void postJob(){

    }

    private void suspendJob(){
        ComputationJob savedJob = systemContext.getComputationJobService().findComputationJobById(job.getId());
        if(savedJob != null)
            systemContext.getComputationJobService().suspendComputationJobById(job.getId());
    }


}
