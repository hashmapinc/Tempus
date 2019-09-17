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

import akka.actor.ActorContext;
import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.event.LoggingAdapter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hashmap.tempus.models.ArgType;
import com.hashmapinc.server.actors.ActorSystemContext;
import com.hashmapinc.server.actors.shared.ComponentMsgProcessor;
import com.hashmapinc.server.common.data.computation.ComputationJob;
import com.hashmapinc.server.common.data.computation.Computations;
import com.hashmapinc.server.common.data.computation.SparkComputationJob;
import com.hashmapinc.server.common.data.computation.SparkComputationMetadata;
import com.hashmapinc.server.common.data.id.ComputationJobId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.plugin.ComponentLifecycleState;
import com.hashmapinc.server.common.msg.cluster.ClusterEventMsg;
import com.hashmapinc.server.extensions.spark.computation.action.SparkComputationRequest;
import com.hashmapinc.server.extensions.spark.computation.model.Batch;
import com.hashmapinc.server.extensions.spark.computation.model.SparkComputationStatus;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import scala.concurrent.duration.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SparkComputationJobActorMessageProcessor extends ComponentMsgProcessor<ComputationJobId> {

    private static final String BASE_URL_TEMPLATE = "http://%s:%d/";
    private static final String BATCH_STATE_URI = "batches/%s";
    private String baseUrl;
    private HttpHeaders headers = new HttpHeaders();
    private ComputationJob job;
    private Cancellable schedule;
    private final Computations computation;
    private SparkComputationStatus status;
    private ObjectMapper objectMapper = new ObjectMapper();
    private final ActorRef self;
    private final ActorRef parent;
    private final String mainClass = "com.hashmapinc.tempus.Computation";

    protected SparkComputationJobActorMessageProcessor(TenantId tenantId, ComputationJobId id, ActorSystemContext systemContext
            , LoggingAdapter logger, ActorRef parent, ActorRef self, Computations computation) {
        super(systemContext, logger, tenantId, id);
        this.computation = computation;
        this.self = self;
        this.parent = parent;
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
    }

    @Override
    public void start() {
        logger.info("[{}] Going to start plugin actor.", entityId);
        job = systemContext.getComputationJobService().findComputationJobById(entityId);
        if (job == null) {
            throw new ComputationInitializationException("Computation Job not found!");
        }
        if(!(job.getConfiguration() instanceof SparkComputationJob)){
            throw new ComputationInitializationException("Computation Job configurations are invalid!");
        }
        if (((SparkComputationJob)job.getConfiguration()).getArgParameters() == null) {
            throw new ComputationInitializationException("Computation Job Arguments is empty!");
        }
        //We need to build these parameters irrespective of Job is Active or Suspended
        buildBaseUrl();
        processHeaders();
        if (job.getState() == ComponentLifecycleState.ACTIVE) {
            logger.info("[{}] Computation Job is active. Going to initialize job.", entityId);
            initComponent();
        } else {
            logger.info("[{}] Computation Job is suspended. Skipping job initialization.", entityId);
        }
    }

    @Override
    public void stop() {
        onStop();
    }

    @Override
    public void onCreated(ActorContext context) {
        logger.info("[{}] Going to process onCreated computation job.", entityId);
    }

    @Override
    public void onUpdate(ActorContext context) {
        ComputationJob oldJob = job;
        job = systemContext.getComputationJobService().findComputationJobById(entityId);
        logger.info("[{}] Computation configuration was updated from {} to {}.", entityId, oldJob, job);
        if(!oldJob.getConfiguration().equals(job.getConfiguration())){
            onStop();
            systemContext.getComputationJobService().activateComputationJobById(job.getId());
            start();
        }
    }

    @Override
    public void onActivate(ActorContext context) {
        logger.info("[{}] Going to process onActivate computation job.", entityId);
        start();
    }

    @Override
    public void onSuspend(ActorContext context) {
        logger.info("[{}] Going to process onSuspend computation job.", entityId);
        onStop();
    }

    @Override
    public void onStop(ActorContext context) {
        logger.info("[{}] Going to process onStop computation job.", entityId);
        onStop();
        scheduleMsgWithDelay(new ComputationJobTerminationMsg(entityId), systemContext.getComputationActorTerminationDelay(), parent);
        scheduleMsgWithDelay(new ComputationJobTerminationMsg(entityId), systemContext.getComputationActorTerminationDelay(), self);
    }

    @Override
    public void onClusterEventMsg(ClusterEventMsg msg) {
        logger.info("onClusterEventMsg"); // No implementation
    }

    private void initComponent(){
        logger.info("[{}] Going to initialize computation job.", entityId);
        try {
            checkJobStatus();
            postJob();
        } catch (IOException e) {
            suspendJob();
            throw new ComputationInitializationException("Error while creating computation job request.", e);
        }catch (Exception e){
            suspendJob();
            throw new ComputationInitializationException("Error while initializing computation job.", e);
        }
    }

    private void processHeaders(){
        JsonNode jsonHeaders = ((SparkComputationJob)job.getConfiguration()).getArgParameters().get("headers");
        headers.add("Content-Type", "application/json");
        if(jsonHeaders != null && jsonHeaders.isArray()){
            logger.info("Processing headers " + jsonHeaders.asText());
            for(JsonNode e : jsonHeaders){
                this.headers.add(e.get("key").asText(), e.get("value").asText());
            }
        }
    }

    private void buildBaseUrl(){
        JsonNode configuration = ((SparkComputationJob)job.getConfiguration()).getArgParameters();
        logger.info("Configuration is : [{}] ", configuration.toString());
        String host = (configuration.get("host") != null)?configuration.get("host").asText():systemContext.getLivyHost();
        int port = (configuration.get("port") != null)?configuration.get("port").asInt():systemContext.getLivyPort();
        this.baseUrl = String.format(
                BASE_URL_TEMPLATE,
                host, port);
        logger.info("[{}] Base url for computation is [{}]", entityId, baseUrl);
    }

    private void onStop(){
        stopJobOnServer();
    }

    private void postJob() throws IOException {
        SparkComputationJob configuration = (SparkComputationJob) job.getConfiguration();
        logger.info("Status is " + status);
        if(status != SparkComputationStatus.RUNNING){
            JsonNode conf = configuration.getArgParameters();
            logger.info("Payload is " + buildSparkComputationRequest());
            ResponseEntity<Batch> response = new RestTemplate().exchange(
                    baseUrl + conf.get("actionPath").asText("batches"),
                    HttpMethod.POST,
                    new HttpEntity<>(buildSparkComputationRequest(), headers),
                    Batch.class);
            if (response.getStatusCode() == HttpStatus.CREATED) {
                Batch res =  response.getBody();
                logger.info("[{}] Computation Job is created with id [{}]", entityId, res.getId());
                configuration.setJobId(String.valueOf(res.getId()));
                job.setState(ComponentLifecycleState.ACTIVE);
                job.setConfiguration(configuration);
                systemContext.getComputationJobService().saveComputationJob(job);
                this.status = SparkComputationStatus.RUNNING;
                schedulePeriodicStatusCheckWithDelay();
            }else{
                logger.info("status in post job " + response.getStatusCode());
                suspendJob();
            }
        }else{
            logger.info("Job already running");
            schedulePeriodicStatusCheckWithDelay();
        }
    }

    private String buildSparkComputationRequest() throws IOException {
        SparkComputationMetadata md = (SparkComputationMetadata) computation.getComputationMetadata();
        logger.info("Jar name is {}, main class is {}, arg parameters are {}, location is {}", md.getJarName(), md.getMainClass(), md.getArgsformat(), systemContext.getComputationLocation());
        SparkComputationRequest.SparkComputationRequestBuilder builder = SparkComputationRequest.builder();
        builder.file(systemContext.getComputationLocation() + md.getJarName());
        builder.className(mainClass);
        builder.args(args());
        SparkComputationRequest sparkComputationRequest = builder.build();
        return objectMapper.writeValueAsString(sparkComputationRequest);
    }

    private String[] args() {
        JsonNode conf = ((SparkComputationJob)job.getConfiguration()).getArgParameters();
        SparkComputationMetadata md = (SparkComputationMetadata) computation.getComputationMetadata();
        String argsFormat = md.getArgsformat();
        List<String> args = new ArrayList<>();
        if(StringUtils.isNotEmpty(argsFormat)){
            String[] argsList = argsFormat.substring(1, argsFormat.length() - 1).split(",");
            for(String arg : argsList){
                if(conf.get(arg.trim()) != null) {
                    if (md.getArgsType().equals(ArgType.NAMED)) {
                        args.add("--" + arg.trim());
                    }
                    args.add(conf.get(arg.trim()).asText());
                }
            }
        }
        if(md.getArgsType().equals(ArgType.NAMED)){
            args.add("--computation-class");
            args.add(md.getMainClass());
            args.add("--source");
            String source = conf.get("source").asText();
            args.add(source);
            if("kinesis".equalsIgnoreCase(source)){
                args.addAll(kinesisParameters(conf));
            }else{
                args.addAll(kafkaParameters(conf));
            }
        }
        logger.info("Argument array list to spark job " + args);
        return args.toArray(new String[args.size()]);
    }

    private List<String> kinesisParameters(JsonNode conf){
        List<String> kinesisArgs = new ArrayList<String>();
        kinesisArgs.add("--kinesisStreamName");
        kinesisArgs.add(conf.get("kinesisStreamName").asText());
        kinesisArgs.add("--kinesisRegion");
        kinesisArgs.add(conf.get("kinesisRegion").asText());
        return kinesisArgs;
    }

    private List<String> kafkaParameters(JsonNode conf){
        List<String> kafkaArgs = new ArrayList<String>();
        kafkaArgs.add("--kafkaTopic");
        kafkaArgs.add(conf.get("kafkaTopic").asText());
        kafkaArgs.add("--kafkaUrl");
        kafkaArgs.add(conf.get("kafkaUrl").asText());
        return kafkaArgs;
    }

    private void stopJobOnServer(){
        SparkComputationJob configuration = (SparkComputationJob) job.getConfiguration();
        if(configuration.getJobId() != null){
            final String format = this.baseUrl + BATCH_STATE_URI;
            String url = String.format(format, configuration.getJobId());
            try{
                ResponseEntity<String> response = new RestTemplate().exchange(
                        url, HttpMethod.DELETE, new HttpEntity<>(headers), String.class);
                if (response.getStatusCode() == HttpStatus.OK) {
                    logger.info("[{}] Computation job with id [{}] deleted. Suspending Job from DB", entityId, configuration.getJobId());
                    suspendJob();
                }else{
                    logger.info("[{}] Computation job with id [{}] deletion returned invalid response [{}]", entityId, response.getStatusCodeValue());
                }
            } catch (RestClientException e){
                logger.info("[{}] Computation job deletion returned error", entityId);
                suspendJob();
            }
        }
    }

    private void suspendJob(){
        ComputationJob savedJob = systemContext.getComputationJobService().findComputationJobById(job.getId());
        if(savedJob != null)
            systemContext.getComputationJobService().suspendComputationJobById(job.getId());
        if(schedule != null && !schedule.isCancelled()) schedule.cancel();
    }

    private void schedulePeriodicStatusCheckWithDelay(){
        logger.info("[{}] Starting scheduler", entityId);
        schedule = systemContext.getScheduler().schedule(Duration.create(systemContext.getComputationStatusCheckDelay(), TimeUnit.MILLISECONDS),
                Duration.create(systemContext.getComputationStatusCheckFrequency(), TimeUnit.MILLISECONDS), new StatusChecker(), systemContext.getActorSystem().dispatcher());
    }

    private void checkJobStatus(){
        SparkComputationJob configuration = (SparkComputationJob) job.getConfiguration();
        if(configuration.getJobId() != null){
            final String format = this.baseUrl + BATCH_STATE_URI;
            String url = String.format(format, configuration.getJobId());
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
                    logger.info("[{}] Computation job status response received is [{}]", entityId, response.getStatusCodeValue());
                    this.status = SparkComputationStatus.NOT_FOUND;
                }
            }catch (RestClientException e){
                logger.info("[{}] Computation job status response received with error.", entityId);
                this.status = SparkComputationStatus.NOT_FOUND;
            }
        }else{
            this.status = SparkComputationStatus.NOT_FOUND;
        }
        logger.info("[{}] Computation Job status is [{}]", entityId, status);
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
