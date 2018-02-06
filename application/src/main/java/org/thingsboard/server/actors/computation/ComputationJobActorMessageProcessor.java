/**
 * Copyright © 2016-2017 The Thingsboard Authors
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

import akka.actor.ActorContext;
import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.event.LoggingAdapter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.thingsboard.server.actors.ActorSystemContext;
import org.thingsboard.server.actors.shared.ComponentMsgProcessor;
import org.thingsboard.server.common.data.computation.ComputationJob;
import org.thingsboard.server.common.data.computation.Computations;
import org.thingsboard.server.common.data.id.ComputationJobId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.plugin.ComponentLifecycleState;
import org.thingsboard.server.common.msg.cluster.ClusterEventMsg;
import org.thingsboard.server.extensions.spark.computation.action.SparkComputationRequest;
import org.thingsboard.server.extensions.spark.computation.model.Batch;
import org.thingsboard.server.extensions.spark.computation.model.SparkComputationStatus;
import scala.concurrent.duration.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ComputationJobActorMessageProcessor extends ComponentMsgProcessor<ComputationJobId> {

    private static final String BASE_URL_TEMPLATE = "http://%s:%d/";
    private static final String BATCH_STATE_URI = "batches/%s";
    private String baseUrl;
    private HttpHeaders headers = new HttpHeaders();
    private ComputationJob job;
    private Cancellable schedule;
    private final Computations computation;
    private SparkComputationStatus status;
    private ObjectMapper mapper = new ObjectMapper();
    private final ActorRef self;
    private final ActorRef parent;

    protected ComputationJobActorMessageProcessor(TenantId tenantId, ComputationJobId id, ActorSystemContext systemContext
            , LoggingAdapter logger, ActorRef parent, ActorRef self, Computations computation) {
        super(systemContext, logger, tenantId, id);
        this.computation = computation;
        this.self = self;
        this.parent = parent;
        mapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
    }

    @Override
    public void start() throws Exception {
        logger.info("[{}] Going to start plugin actor.", entityId);
        job = systemContext.getComputationJobService().findComputationJobById(entityId);
        if (job == null) {
            throw new ComputationInitializationException("Computation Job not found!");
        }
        if (job.getArgParameters() == null) {
            throw new ComputationInitializationException("Computation Job Arguments is empty!");
        }
        if (job.getState() == ComponentLifecycleState.ACTIVE) {
            logger.info("[{}] Computation Job is active. Going to initialize job.", entityId);
            initComponent();
        } else {
            logger.info("[{}] Computation Job is suspended. Skipping job initialization.", entityId);
        }
    }

    @Override
    public void stop() throws Exception {
        onStop();
    }

    @Override
    public void onCreated(ActorContext context) throws Exception {
        logger.info("[{}] Going to process onCreated computation job.", entityId);
    }

    @Override
    public void onUpdate(ActorContext context) throws Exception {
        ComputationJob oldJob = job;
        job = systemContext.getComputationJobService().findComputationJobById(entityId);
        logger.info("[{}] Computation configuration was updated from {} to {}.", entityId, oldJob, job);
        if(!oldJob.getArgParameters().equals(job.getArgParameters())){
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
        onStop();
    }

    @Override
    public void onStop(ActorContext context) throws Exception {
        logger.info("[{}] Going to process onStop computation job.", entityId);
        onStop();
        scheduleMsgWithDelay(context, new ComputationJobTerminationMsg(entityId), systemContext.getComputationActorTerminationDelay(), parent);
        scheduleMsgWithDelay(context, new ComputationJobTerminationMsg(entityId), systemContext.getComputationActorTerminationDelay(), self);
    }

    @Override
    public void onClusterEventMsg(ClusterEventMsg msg) throws Exception {

    }

    private void initComponent(){
        logger.info("[{}] Going to initialize computation job.", entityId);
        try {
            buildBaseUrl();
            processHeaders();
            checkJobStatus();
            postJob();
        } catch (IOException e) {
            throw new ComputationInitializationException("Error while creating computation job request.", e);
        }catch (Exception e){
            throw new ComputationInitializationException("Error while initializing computation job.", e);
        }
    }

    private void processHeaders(){
        JsonNode jsonHeaders = job.getArgParameters().get("headers");
        if(jsonHeaders != null && jsonHeaders.isArray()){
            for(JsonNode e : jsonHeaders){
                this.headers.add(e.get("key").asText(), e.get("value").asText());
            }
        }
    }

    private void buildBaseUrl(){
        JsonNode configuration = job.getArgParameters();
        this.baseUrl = String.format(
                BASE_URL_TEMPLATE,
                configuration.get("host").asText("localhost"),
                configuration.get("port").asInt(8080));
        logger.info("[{}] Base url for computation is [{}]", entityId, baseUrl);
    }

    private void onStop(){
        stopJobOnServer();
    }

    private void postJob() throws IOException {
        if(status != SparkComputationStatus.RUNNING){
            JsonNode conf = job.getArgParameters();
            ResponseEntity<Batch> response = new RestTemplate().exchange(
                    baseUrl + conf.get("actionPath"),
                    HttpMethod.POST,
                    new HttpEntity<>(buildSparkComputationRequest(), headers),
                    Batch.class);
            if (response.getStatusCode() == HttpStatus.CREATED) {
                Batch res =  response.getBody();
                logger.debug("[{}] Computation Job is created with id [{}]", entityId, res.getId());
                job.setJobId(String.valueOf(res.getId()));
                job.setState(ComponentLifecycleState.ACTIVE);
                systemContext.getComputationJobService().saveComputationJob(job);
                this.status = SparkComputationStatus.RUNNING;
                schedulePeriodicStatusCheckWithDelay();
            }else{
                suspendJob();
            }
        }else{
            schedulePeriodicStatusCheckWithDelay();
        }
    }

    private String buildSparkComputationRequest() throws IOException {
        SparkComputationRequest.SparkComputationRequestBuilder builder = SparkComputationRequest.builder();
        builder.file(systemContext.getComputationLocation() + computation.getJarName());
        builder.className(computation.getMainClass());
        builder.args(args());
        SparkComputationRequest sparkComputationRequest = builder.build();
        return mapper.writeValueAsString(sparkComputationRequest);
    }

    private String[] args() throws IOException {
        JsonNode conf = job.getArgParameters();
        JsonNode node = mapper.readTree(computation.getArgsformat());
        List<String> args = new ArrayList<>();
        if(node != null && node.isArray()){
            for(JsonNode ar : node){
                args.add("--" + ar.asText());
                args.add(conf.get(ar.asText()).asText());
            }
        }
        return (String[])args.toArray();
    }

    private void stopJobOnServer(){
        if(job.getJobId() != null){
            String url = String.format(this.baseUrl + BATCH_STATE_URI, job.getJobId());
            try{
                ResponseEntity<String> response = new RestTemplate().exchange(
                        url, HttpMethod.DELETE, new HttpEntity<>(headers), String.class);
                if (response.getStatusCode() == HttpStatus.OK) {
                    logger.info("[{}] Computation job with id [{}] deleted. Suspending Job from DB", entityId, job.getJobId());
                    suspendJob();
                }else{
                    logger.info("[{}] Computation job with id [{}] deletion returned invalid response [{}]", entityId, response.getStatusCodeValue());
                }
            } catch (RestClientException e){
                logger.warning("[{}] Computation job deletion returned error", entityId);
                suspendJob();
            }
        }
    }

    private void suspendJob(){
        job.setState(ComponentLifecycleState.SUSPENDED);
        job.setJobId(null);
        systemContext.getComputationJobService().saveComputationJob(job);
        if(schedule != null && !schedule.isCancelled()) schedule.cancel();
    }

    private void schedulePeriodicStatusCheckWithDelay(){
        logger.info("[{}] Starting scheduler", entityId);
        schedule = systemContext.getScheduler().schedule(Duration.create(systemContext.getComputationStatusCheckDelay(), TimeUnit.MILLISECONDS),
                Duration.create(systemContext.getComputationStatusCheckFrequency(), TimeUnit.MILLISECONDS), new StatusChecker(), systemContext.getActorSystem().dispatcher());
    }

    private void checkJobStatus(){
        if(job.getJobId() != null){
            String url = String.format(this.baseUrl + BATCH_STATE_URI, job.getJobId());
            try {
                ResponseEntity<Batch> response = new RestTemplate().exchange(
                        url, HttpMethod.GET, new HttpEntity<>(headers), Batch.class);
                if (response.getStatusCode() == HttpStatus.OK) {
                    logger.info("[{}] Computation job status on server is [{}]", entityId, response.getBody().getState());
                    switch (response.getBody().getState()){
                        case "starting" :
                        case "running"  : this.status = SparkComputationStatus.RUNNING;
                                          break;
                        case "dead"     : this.status = SparkComputationStatus.DEAD;
                                          break;
                        default         : this.status = SparkComputationStatus.UNKNOWN;
                                          break;
                    }
                }else {
                    logger.warning("[{}] Computation job status response received is [{}]", entityId, response.getStatusCodeValue());
                    this.status = SparkComputationStatus.NOT_FOUND;
                }
            }catch (RestClientException e){
                logger.warning("[{}] Computation job status response received with error.", entityId);
                this.status = SparkComputationStatus.NOT_FOUND;
            }
        }else{
            this.status = SparkComputationStatus.NOT_FOUND;
        }
        logger.debug("[{}] Computation Job status is [{}]", entityId, status);
    }

    private class StatusChecker implements Runnable{
        @Override
        public void run() {
            SparkComputationStatus oldStatus = status;
            checkJobStatus();
            if(oldStatus != status &&
                    status != SparkComputationStatus.RUNNING){
                onStop();
            }
        }
    }
}
