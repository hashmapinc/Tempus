package com.hashmapinc.server.service.computation;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.CreateEventSourceMappingRequest;
import com.amazonaws.services.lambda.model.CreateEventSourceMappingResult;
import com.amazonaws.services.lambda.model.CreateFunctionRequest;
import com.amazonaws.services.lambda.model.CreateFunctionResult;
import com.hashmapinc.server.common.data.computation.AWSLambdaComputationMetadata;
import com.hashmapinc.server.common.data.computation.ComputationJob;
import com.hashmapinc.server.common.data.computation.Computations;
import com.hashmapinc.server.common.data.computation.KinesisLambdaTrigger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
public class AWSLambdaFunctionService implements ServerlessFunctionService {

    @Value("${aws.access_key}")
    private String awsAccesskey;

    @Value("${aws.secret_key}")
    private String awsSecretKey;

    @Value("${aws.lambda.role_arn}")
    private String lambdaRoleArn;

    BasicAWSCredentials awsCreds = new BasicAWSCredentials(awsAccesskey, awsSecretKey);

    @Override
    public boolean deployFunction(Computations computations) {
        AWSLambdaComputationMetadata metadata = (AWSLambdaComputationMetadata)computations.getComputationMetadata();

        try {
            AWSLambda awsLambda = AWSLambdaClientBuilder.standard()
                    .withRegion(Regions.fromName(metadata.getRegion()))
                    .withCredentials(new AWSStaticCredentialsProvider(awsCreds)).build();


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
    public boolean checkFunction(Computations computation) {
        return false;
    }

    @Override
    public boolean deleteFunction(Computations computation) {
        return false;
    }

    @Override
    public boolean createTrigger(ComputationJob computationJob) {
        KinesisLambdaTrigger triggerConfig = (KinesisLambdaTrigger)computationJob.getConfiguration();
        try{
            AWSLambda awsLambda = AWSLambdaClientBuilder.standard()
                    .withRegion(Regions.fromName(triggerConfig.getRegion()))
                    .withCredentials(new AWSStaticCredentialsProvider(awsCreds)).build();

            final CreateEventSourceMappingResult eventSourceMapping = awsLambda.createEventSourceMapping(new CreateEventSourceMappingRequest()
                    .withFunctionName(triggerConfig.getFunctionName())
                    .withEventSourceArn(triggerConfig.getFunctionName())
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
        return false;
    }

    @Override
    public boolean deleteTrigger(ComputationJob computationJob) {
        return false;
    }
}
