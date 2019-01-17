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
package com.hashmapinc.server.service.computation;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.kinesis.AmazonKinesisAsync;
import com.amazonaws.services.kinesis.AmazonKinesisAsyncClientBuilder;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.*;
import com.hashmapinc.server.common.data.computation.AWSLambdaComputationMetadata;
import com.hashmapinc.server.common.data.computation.ComputationJob;
import com.hashmapinc.server.common.data.computation.Computations;
import com.hashmapinc.server.common.data.computation.KinesisLambdaTrigger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class AWSLambdaFunctionService implements ServerlessFunctionService {

    @Value("${aws.access_key}")
    private String awsAccesskey;

    @Value("${aws.secret_key}")
    private String awsSecretKey;

    @Value("${aws.lambda.role_arn}")
    private String lambdaRoleArn;

    @Override
    public boolean deployFunction(Computations computations) {
        AWSLambdaComputationMetadata metadata = (AWSLambdaComputationMetadata)computations.getComputationMetadata();

        try {
            AWSLambda awsLambda = getAwsLambdaClient(metadata.getRegion());

            final CreateFunctionResult lambdaFunction = awsLambda.createFunction(new CreateFunctionRequest()
                    .withFunctionName(metadata.getFunctionName())
                    .withDescription(metadata.getDescription())
                    .withHandler(metadata.getFunctionHandler())
                    .withRuntime(metadata.getRuntime())
                    .withRole(lambdaRoleArn)
                    .withMemorySize(metadata.getMemorySize())
                    .withTimeout(metadata.getTimeout())
                    .withPublish(true)
            );
            log.info("Created AWS lambda function {}", lambdaFunction);
        } catch (Exception e) {
           log.error("Error while deploying AWS lambda function {}", e);
           return false;
        }
        return true;
    }

    @Override
    public boolean checkFunction(Computations computations) {
        AWSLambdaComputationMetadata metadata = (AWSLambdaComputationMetadata)computations.getComputationMetadata();
        try {
            AWSLambda awsLambda = getAwsLambdaClient(metadata.getRegion());
            final GetFunctionResult functionResult = awsLambda.getFunction(new GetFunctionRequest().withFunctionName(metadata.getFunctionName()));
            return functionResult != null;
        } catch (Exception e) {
            log.error("Error : AWS lambda function {} may not be present {}", metadata.getFunctionName(), e);
            return false;
        }
    }

    @Override
    public boolean deleteFunction(Computations computations) {
        AWSLambdaComputationMetadata metadata = (AWSLambdaComputationMetadata)computations.getComputationMetadata();
        try {
            AWSLambda awsLambda = getAwsLambdaClient(metadata.getRegion());
            final DeleteFunctionResult deleteFunctionResult = awsLambda.deleteFunction(new DeleteFunctionRequest().withFunctionName(metadata.getFunctionName()));
            return deleteFunctionResult != null;
        } catch (Exception e) {
            log.error("Error while deleting AWS lambda function {}, {}", metadata.getFunctionName(), e);
            return false;
        }
    }

    @Override
    public boolean createTrigger(ComputationJob computationJob) {
        KinesisLambdaTrigger triggerConfig = (KinesisLambdaTrigger)computationJob.getConfiguration();
        try{
            BasicAWSCredentials awsCreds = new BasicAWSCredentials(awsAccesskey, awsSecretKey);
            AWSLambda awsLambda = getAwsLambdaClient(triggerConfig.getRegion());

            final AmazonKinesisAsync amazonKinesisAsync = AmazonKinesisAsyncClientBuilder.standard().withRegion(Regions.fromName(triggerConfig.getRegion()))
                    .withCredentials(new AWSStaticCredentialsProvider(awsCreds)).build();

            final String streamARN = amazonKinesisAsync.describeStream(triggerConfig.getStreamName()).getStreamDescription().getStreamARN();

            final CreateEventSourceMappingResult eventSourceMapping = awsLambda.createEventSourceMapping(new CreateEventSourceMappingRequest()
                    .withFunctionName(triggerConfig.getFunctionName())
                    .withEventSourceArn(streamARN)
                    .withBatchSize(triggerConfig.getBatchSize())
                    .withEnabled(true)
            );
            log.info("Created AWS lambda function kinesis trigger{}", eventSourceMapping);
        }catch (Exception e){
            log.error("Error while deploying AWS kinesis trigger {}", e);
            return false;
        }
        return true;
    }

    @Override
    public boolean checkTrigger(ComputationJob computationJob) {

        KinesisLambdaTrigger triggerConfig = (KinesisLambdaTrigger)computationJob.getConfiguration();
        try{
            AWSLambda awsLambda = getAwsLambdaClient(triggerConfig.getRegion());

            final ListEventSourceMappingsResult listEventSourceMappingsResult =
                    awsLambda.listEventSourceMappings(new ListEventSourceMappingsRequest().withFunctionName(triggerConfig.getFunctionName()));

            final List<EventSourceMappingConfiguration> eventSourceMappings = listEventSourceMappingsResult.getEventSourceMappings();
            log.info("AWS Kinesis trigger exists");
            return eventSourceMappings.size() == 1;

        }catch (Exception e){
            log.error("Error while checking AWS Kinesis trigger ", e);
            return false;
        }
    }

    @Override
    public boolean deleteTrigger(ComputationJob computationJob) {
        KinesisLambdaTrigger triggerConfig = (KinesisLambdaTrigger)computationJob.getConfiguration();
        try{
            AWSLambda awsLambda = getAwsLambdaClient(triggerConfig.getRegion());

            final ListEventSourceMappingsResult listEventSourceMappingsResult =
                    awsLambda.listEventSourceMappings(new ListEventSourceMappingsRequest().withFunctionName(triggerConfig.getFunctionName()));

            final List<EventSourceMappingConfiguration> eventSourceMappings = listEventSourceMappingsResult.getEventSourceMappings();
            if(eventSourceMappings.size() != 1){
                return false;
            }
            final EventSourceMappingConfiguration eventSourceMappingConfiguration = eventSourceMappings.get(0);
            final String configurationUUID = eventSourceMappingConfiguration.getUUID();
            awsLambda.deleteEventSourceMapping(new DeleteEventSourceMappingRequest().withUUID(configurationUUID));
            log.info("Deleted AWS lambda function kinesis trigger");

        }catch (Exception e){
            log.error("Error while deleting AWS kinesis trigger {}", e);
            return false;
        }
        return true;
    }

    private AWSLambda getAwsLambdaClient(String regionName) {
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(awsAccesskey, awsSecretKey);
        return AWSLambdaClientBuilder.standard()
                .withRegion(Regions.fromName(regionName))
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds)).build();
    }
}
